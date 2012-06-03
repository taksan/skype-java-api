/*******************************************************************************
 * Copyright (c) 2006-2007 Skype Technologies S.A. <http://www.skype.com/>
 * Copyright (c) 2011-2012 Gabriel Takeuchi <https://github.com/taksan/>
 * 
 * Skype Java Api is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Gabriel Takeuchi - DBUS native implementation
 ******************************************************************************/

#include <unistd.h>
#include <glib.h>
#include <stdarg.h>
#include <time.h>
#include <stdio.h>
#include <malloc.h>
#include <string.h>
#include <dbus/dbus.h>
#include <dbus/dbus-glib.h>

#include "skype-service.h"
#include "skype-service-object-glue.h"

#include "com_skype_connector_linux_SkypeFramework.h"
#include "Logging.h"

#define SKYPE_FRAMEWORK_CLASS "com/skype/connector/linux/SkypeFramework"

#define SKYPE_STRING_MAX 0x10000
static JNIEnv *currentEnv;

static DBusGConnection *dbus_conn = NULL;
static DBusGProxy *proxy_send = NULL;
static SkypeService *service_object = NULL;

static void throwInternalError(JNIEnv *env, char *message);
gboolean startsWith(const char * subject, const char * prefix);
gboolean endsWith(const char * subject, const char * trail);

typedef enum {
	IssuerNone,
	IssuerCallback,
	IssuerCommandReply
}IssuerType;


DBusGConnection *getSkypeDBusConnection() {
    GError *error = NULL;

    if (!dbus_conn) {
        dbus_conn = dbus_g_bus_get(DBUS_BUS_SESSION, &error);

        if (!dbus_conn) {
			logToFile(LOG_DEBUG, "getSkypeDBusConnection: Cannot connect to DBus: %s\n", error ? error->message : "");

            if (error)
                g_error_free(error);

            return NULL;
        }   
    }   
    return dbus_conn;
}

void setupSkypeFrameWork(JNIEnv *env)
{
	if (dbus_conn != NULL) 
		return;
    logDebug(env, "DBUS Native Lib setup");

    logToFile(LOG_DEBUG, "setupSkypeFrameWork");
	g_type_init();
	dbus_g_thread_init();

    logToFile(LOG_DEBUG, "initializations done");

	currentEnv = env;
	dbus_conn = getSkypeDBusConnection();
    logToFile(LOG_DEBUG, "connected to dbus");

	proxy_send = dbus_g_proxy_new_for_name(dbus_conn, "com.Skype.API", "/com/Skype", "com.Skype.API");
	if (!proxy_send) {
		throwInternalError(env, "Failed to connect to skype");
	}

    service_object = skype_service_new();
    dbus_g_object_type_install_info(G_TYPE_FROM_INSTANCE(service_object), &dbus_glib_server_object_object_info);
    dbus_g_connection_register_g_object(dbus_conn, SKYPE_SERVICE_PATH/*/com/Skype/Client*/, (GObject*)service_object);

    logToFile(LOG_DEBUG, "DBUS Skype connection done.");
}

void initDebugging(JNIEnv *env, jclass this) {
	jfieldID fid = (*env)->GetStaticFieldID(env, this, "isDebugging", "Z");
	if (fid == NULL) return; 

	jboolean isDebugging = (*env)->GetStaticBooleanField(env, this, fid);
	if (isDebugging) {
		openLogWithStdout();
	}
}

JNIEXPORT void JNICALL Java_com_skype_connector_linux_SkypeFramework_setup0(JNIEnv *env, jclass this) {
	initDebugging(env, this);
	setupSkypeFrameWork(env);
}

gboolean stringIsNullOnlyOnOutOfMemoryError(JNIEnv *env, void *value); 
gboolean isDuplicateChatSentNotification(JNIEnv* env, gchar * skypeNotification, IssuerType type);
static void fireNotificationReceived(JNIEnv *env, gchar *skypeNotification, IssuerType type) {
	if (isDuplicateChatSentNotification(env, skypeNotification, type))
		return;

	logDebug(env, "Received skype notification: %s\0", skypeNotification);

	jstring notificationString = (*env)->NewStringUTF(env, skypeNotification);
	if (stringIsNullOnlyOnOutOfMemoryError(env, (void *)notificationString)) {
		return;
	}

	jclass clazz  = (*env)->FindClass(env, SKYPE_FRAMEWORK_CLASS);
	jmethodID method = (*env)->GetStaticMethodID(env, clazz, "fireNotificationReceived", "(Ljava/lang/String;)V");
	(*env)->CallStaticVoidMethod(env, clazz, method, notificationString);
}

gboolean stringIsNullOnlyOnOutOfMemoryError(JNIEnv *env, void *value) 
{
	if (value == NULL) {
		jclass clazz = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
		(*env)->ThrowNew(env, clazz, NULL);
		return TRUE;
	} else {
		return FALSE;
	}
}

gboolean isDuplicateChatSentNotification(JNIEnv* env, gchar * skypeNotification, IssuerType type)
{
	static char lastReceivedMessage[SKYPE_STRING_MAX]="";
	if (type != IssuerCallback)
		return FALSE;

	if (strcmp(skypeNotification, lastReceivedMessage) == 0) {
		if (startsWith(skypeNotification, "CHATMESSAGE") && endsWith(skypeNotification,"STATUS SENT")) {
			logDebug(env, "Ignoring duplicate notification: %s\0", skypeNotification);
			return TRUE;
		}
	}
	
	strcpy(lastReceivedMessage, skypeNotification);
	return FALSE;
}

JNIEnv *eventLoopEnv = NULL;
gboolean skype_service_notify_callback(SkypeService *object, gchar *message, GError **error) {
    logToFile(LOG_DEBUG, "skype_service_notify_callback, message:<%s>", message);
	fireNotificationReceived(eventLoopEnv, message, IssuerCallback);

    return TRUE;
}

JNIEXPORT void JNICALL Java_com_skype_connector_linux_SkypeFramework_runEventLoop0(JNIEnv *env, jclass this) {

	logDebug(env, "runEventLoop started - will keep going");

	eventLoopEnv = env;
    GMainLoop *loop;
    loop = g_main_loop_new ( NULL , FALSE );
    g_main_loop_run (loop);
    g_main_loop_unref(loop);

	logDebug(env, "runEventLoop ended");
}

JNIEXPORT void JNICALL Java_com_skype_connector_linux_SkypeFramework_stopEventLoop0(JNIEnv *env, jclass this) {
	if (service_object)
		g_object_unref(service_object);

	service_object = NULL;
}

static gboolean isRunning() {
	return proxy_send != NULL;
}

JNIEXPORT jboolean JNICALL Java_com_skype_connector_linux_SkypeFramework_isRunning0(JNIEnv *env, jclass this) {
	return isRunning()? JNI_TRUE: JNI_FALSE;
}


static void sendCommand(JNIEnv *env, const char *commandChars); 
JNIEXPORT void JNICALL Java_com_skype_connector_linux_SkypeFramework_sendCommand0(JNIEnv *env, jclass this, jstring command) {
	jboolean isCopy;
	const char *commandChars = (*env)->GetStringUTFChars(env, command, &isCopy);
	sendCommand(env, commandChars);
	if (isCopy == JNI_TRUE) {
		(*env)->ReleaseStringUTFChars(env, command, commandChars);
	}
}

JNIEXPORT void JNICALL Java_com_skype_connector_linux_SkypeFramework_closeDisplay0(JNIEnv *env, jclass this) {
	// NOT NEEDED FOR DBUS IMPLEMENTATION
}

static void sendCommand(JNIEnv *env, const char *command) {
	if (!isRunning()) {
		return;
	}
	logDebug(env, "Sending command to skype: %s\0", command);

	gchar *str = NULL;
	GError *error = NULL;

	gboolean success = dbus_g_proxy_call(proxy_send, "Invoke", &error,
                G_TYPE_STRING, command,
                G_TYPE_INVALID,
                G_TYPE_STRING, &str,
                G_TYPE_INVALID);

	if (success) {
		if (str != NULL) {
			logDebug(env, "Proxy call returned string: %s\0", str);
			fireNotificationReceived(env, str, IssuerCommandReply);
			g_free(str);
		}
		return;
	}

	if (error) {
		logDebug(env, "Failed to make DBus call: %s.\n", error->message);
		g_error_free(error);
	}
	else {
		logDebug(env, "Failed to make DBus call, but returned no error message");
	}
}



static void throwInternalError(JNIEnv *env, char *message) {
	if (env == NULL) {
		fprintf(stderr, "%s\n", message);
		fflush(stderr);
		return;
	}
	jclass clazz = (*env)->FindClass(env, "java/lang/InternalError");
	(*env)->ThrowNew(env, clazz, message);
}

gboolean startsWith(const char * subject, const char * prefix)
{
 	return strncmp(subject, prefix, strlen(prefix)) == 0;
}

gboolean endsWith(const char * subject, const char * trail)
{
	int startMatchIndex = strlen(subject) - strlen(trail);
	const char * from = subject+startMatchIndex;
	return strcmp(from, trail) == 0;
}

// the following functions are just for testing
void setup() {
	setupSkypeFrameWork(NULL);
}

void * runLoop(void * args){
	Java_com_skype_connector_linux_SkypeFramework_runEventLoop0(NULL, NULL);
	return NULL;
}

int aisRunning(){
	return isRunning();
}

