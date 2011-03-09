#include <stdarg.h>
#include <time.h>
#include <stdio.h>
#include <malloc.h>
#include <string.h>

#include <X11/Xlib.h>
#include <X11/Xatom.h>

#include "com_skype_connector_linux_SkypeFramework.h"
#include "Logging.h"

const char * logLevelNames[3] = {"ERROR","INFO","DEBUG"};

static FILE* logfile;
void openLogFile(const char* logfilename)
{
	logfile = fopen(logfilename, "a+");
}

void logToFile(int level, const char* fmt, ...)
{
	va_list ap;
	va_start(ap, fmt);


	vfprintf(logfile, fmt, ap);
	fprintf(logfile, "\n");
	fflush(logfile);
}

void logTimeAndLevel(int level) {
	// time
	time_t now;
	struct tm * timeinfo;
	char buffer [80];

	now = time ( NULL );
	timeinfo = localtime ( &now );

	if (level > 2)
		level = 2;

	strftime (buffer,80,"%Y/%m/%d %H:%M:%S",timeinfo);

	fprintf(logfile, "[%s] [%s]", buffer, logLevelNames[level]);
}


static jclass log4JClass;
static jobject LOGGER = NULL;

static jmethodID log4JInfoMethod  = NULL;
static jmethodID log4JErrorMethod = NULL;
static jmethodID log4JDebugMethod = NULL;

jobject getLogger(JNIEnv *env)
{
	if (LOGGER != NULL) {
		return LOGGER;
	}
	log4JClass  = (*env)->FindClass(env, "org/apache/log4j/Logger");
	
	printf("about to get logger\n");

	printf("logger caught. %p\n", log4JClass);
	jmethodID method = (*env)->GetStaticMethodID(env, log4JClass, "getLogger", "(Ljava/lang/String;)Lorg/apache/log4j/Logger;");

	if (method == NULL) {
		printf("Could not acquire getLogger method!\n");
		return;
	}
	printf("Logger.getLogger method acquired %p\n", method);

	jstring category= (*env)->NewStringUTF(env, "skype-framework-native");

	LOGGER = (*env)->CallStaticObjectMethod(env, log4JClass, method, category);

	printf("Logger.getLogger acquired\n");


	log4JInfoMethod  = (*env)->GetMethodID(env, log4JClass , "info", "(Ljava/lang/Object;)V");
	log4JErrorMethod = (*env)->GetMethodID(env, log4JClass , "error", "(Ljava/lang/Object;)V");
	log4JDebugMethod = (*env)->GetMethodID(env, log4JClass , "debug", "(Ljava/lang/Object;)V");
}

void log4jmessage(JNIEnv *env,jmethodID messageMethod, const char * message)
{
	jobject logger = getLogger(env);
	jstring messageString = (*env)->NewStringUTF(env, message);

	(*env)->CallVoidMethod(env, logger, messageMethod, messageString);
	(*env)->ReleaseStringUTFChars(env, messageString, message);
}

void log4JInfo(JNIEnv *env, const char * message)
{
	logToFile(LOG_INFO, message);
//	log4jmessage(env, log4JInfoMethod, message);
}

void log4jError(JNIEnv *env, const char * message)
{
	logToFile(LOG_ERR, message);
//	log4jmessage(env, log4JErrorMethod, message);
}

void log4jDebug(JNIEnv *env, const char* fmt, ...)
{
	va_list ap;
	va_start(ap, fmt);
	char messageBuffer[3000];

	vsprintf(messageBuffer, fmt, ap);
	logToFile(LOG_DEBUG, messageBuffer);
	//log4jmessage(env, log4JDebugMethod, messageBuffer);
}

