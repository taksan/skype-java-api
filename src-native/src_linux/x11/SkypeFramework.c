/*******************************************************************************
 * Copyright (c) 2006-2007 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006-2007 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006-2007 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 * Gabriel Takeuchi - reliability improvements and bug fixes
 ******************************************************************************/

#include <stdarg.h>
#include <time.h>
#include <stdio.h>
#include <malloc.h>
#include <string.h>

#include <X11/Xlib.h>
#include <X11/Xatom.h>

#include "com_skype_connector_linux_SkypeFramework.h"
#include "Logging.h"

#define SKYPE_FRAMEWORK_CLASS "com/skype/connector/linux/SkypeFramework"

#define SKYPE_STRING_MAX 0x10000

static Window getSkypeWindow();

static JNIEnv *currentEnv;
static Display *_display = NULL;
static int _screen = -1;
static Window _desktop;
static Window _dummyWindow;

static Atom _windowNameAtom;
static Atom _skypeInstanceAtom;
static Atom _skypeControlApiMessageBeginAtom;
static Atom _skypeControlApiMessageAtom;
static Atom _stopEventLoopAtom;

static Bool _dispatching;
static Window _skypeWindow = 0x0;

static void throwInternalError(JNIEnv *env, char *message) {
	if (env == NULL) {
		fprintf(stderr, "%s\n", message);
		return;
	}
	jclass clazz = (*env)->FindClass(env, "java/lang/InternalError");
	(*env)->ThrowNew(env, clazz, message);
}

static Bool checkNull(JNIEnv *env, void *value) {
	if (value == NULL) {
		jclass clazz = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
		(*env)->ThrowNew(env, clazz, NULL);
		return True;
	} else {
		return False;
	}
}

void setupSkypeFrameWork(JNIEnv *env)
{
	logDebug(env, "X11 Native Lib setup");

	currentEnv = env;

	if (XInitThreads() == 0) {
		throwInternalError(env, "Xlib don't support multi-threads.");
		return;
	}

	_display = XOpenDisplay(NULL);
	if (_display == NULL) {
		throwInternalError(env, "Opening the diplay failed.");
		return;
	}
	
	_screen = DefaultScreen(_display);
	_desktop = XRootWindow(_display, _screen);
	_dummyWindow = XCreateSimpleWindow(_display, _desktop, 0, 0, 1, 1, 0, BlackPixel(_display, _screen), BlackPixel(_display, _screen));
	
	_skypeInstanceAtom = XInternAtom(_display, "_SKYPE_INSTANCE", False);
	_skypeControlApiMessageBeginAtom = XInternAtom(_display, "SKYPECONTROLAPI_MESSAGE_BEGIN", False);
	_skypeControlApiMessageAtom = XInternAtom(_display, "SKYPECONTROLAPI_MESSAGE", False);
	_stopEventLoopAtom = XInternAtom(_display, "_STOP_EVENT_LOOP", False);
	_windowNameAtom = XInternAtom(_display, "WM_NAME", True);
	
	_dispatching = True;
	getSkypeWindow();
}

JNIEXPORT void JNICALL Java_com_skype_connector_linux_SkypeFramework_setup0(JNIEnv *env, jclass this) {
	setupSkypeFrameWork(env);
}

void setup() {
	setupSkypeFrameWork(NULL);
}

static void fireNotificationReceived(JNIEnv *env, char *notificationChars) {
	if (notificationChars != NULL) {
		logDebug(env, "Received skype notification: %s\0", notificationChars);
	}
	else {
		logDebug(env, "Received a NULL skype notification");
	}
	jstring notificationString = (*env)->NewStringUTF(env, notificationChars);
	if (checkNull(env, (void *)notificationString)) {
		return;
	}

	jclass clazz  = (*env)->FindClass(env, SKYPE_FRAMEWORK_CLASS);
	jmethodID method = (*env)->GetStaticMethodID(env, clazz, "fireNotificationReceived", "(Ljava/lang/String;)V");
	(*env)->CallStaticVoidMethod(env, clazz, method, notificationString);
}


JNIEXPORT void JNICALL Java_com_skype_connector_linux_SkypeFramework_runEventLoop0(JNIEnv *env, jclass this) {
	XEvent event;
	char buffer[21];
	char *notificationChars = (char *)malloc(sizeof(char) * SKYPE_STRING_MAX);
	notificationChars[0] = '\0';

	while(True) {
		XNextEvent(_display, &event);
		if (event.type == ClientMessage) {
			if (event.xclient.message_type == _stopEventLoopAtom) {
				break;
			}
			
			if (event.xclient.format != 8)
				continue;

			if (event.xclient.message_type == _skypeControlApiMessageBeginAtom) {
				notificationChars[0] = '\0';
			}

			int i;
			for (i = 0; i < 20 && event.xclient.data.b[i] != '\0'; i++) {
				buffer[i] = event.xclient.data.b[i];
			}
			buffer[i] ='\0';
			strcat(notificationChars, buffer);
			if (i < 20) {
				fireNotificationReceived(env, notificationChars);
				notificationChars[0] = '\0';
			}
		}
		usleep(10);
	}
}

Window searchSkypeWindow(Window w)
{
	Atom           type;
	int            format;
	unsigned long  nItems;
	unsigned long  bytesAfter;
	unsigned char *className = NULL;

	// Recurse into child windows.
	Window    wRoot;
	Window    wParent;
	Window   *wChild;
	unsigned  nChildren;

	if(Success == XGetWindowProperty(_display, w, _windowNameAtom, 0, 1024, False, XA_STRING,
				&type, &format, &nItems, &bytesAfter, &className))
	{
		if(className != NULL)
		{
			// doesnt have: WM_WINDOW_ROLE, _NET_WM_STATE
			// If the PID matches, add this window to the result set.
			if(strcasecmp("skype", className) == 0) {
				if (0 != XQueryTree(_display, w, &wRoot, &wParent, &wChild, &nChildren)) {
					int nProp;
					Atom * propList = XListProperties(_display, wChild[0], &nProp);
					if (propList == NULL) {
//						logToFile(LOG_ERR, "Found Skype API Window: 0x%x", (unsigned int)wChild[0]);
						return wChild[0];
					}
					XFree(propList);
				}
			}

			XFree(className);
		}
	}

	if(0 != XQueryTree(_display, w, &wRoot, &wParent, &wChild, &nChildren))
	{
		unsigned i;
		for(i = 0; i < nChildren; i++) {
			Window foundWindow = searchSkypeWindow(wChild[i]);
			if (foundWindow != None) {
				return foundWindow;
			}
		}
	}

	return None;
}

static Window getSkypeWindow() {
	Atom actualType;
	int actualFormat;
	unsigned long numberOfItems;
	unsigned long bytesAfter;
	unsigned char *data;
	int status;

	if (_skypeWindow != 0) {
		return _skypeWindow;
	}

	status = XGetWindowProperty(_display, _desktop, _skypeInstanceAtom, 0, 1, False, XA_WINDOW, &actualType, &actualFormat, &numberOfItems, &bytesAfter, &data);

	if (status != Success || &actualType == None || actualFormat != 32 || numberOfItems != 1) {
		// let's try a brute force approach to find the skype window
		_skypeWindow = searchSkypeWindow(_desktop);
		return _skypeWindow;
	} else {
		_skypeWindow = *(Window *)data;
		return _skypeWindow;
	}
}

static Bool isRunning() {
	return getSkypeWindow() != None;
}

JNIEXPORT jboolean JNICALL Java_com_skype_connector_linux_SkypeFramework_isRunning0(JNIEnv *env, jclass this) {
	return isRunning()? JNI_TRUE: JNI_FALSE;
}

static void sendCommand(JNIEnv *env, const char *commandChars) {
	if (!isRunning()) {
		return;
	}
	logDebug(env, "Sending command to skype: %s\0", commandChars);

	unsigned int position = 0;
	unsigned int length = strlen(commandChars);

	XEvent event;
	memset(&event, 0, sizeof(XEvent));
	event.xclient.type = ClientMessage;
	event.xclient.message_type = _skypeControlApiMessageBeginAtom;
	event.xclient.display = _display;
	event.xclient.window = _dummyWindow;
	event.xclient.format = 8;

	do {
		int i;
		for (i = 0; i < 20 && i + position <= length; i++) {
			event.xclient.data.b[i] = commandChars[i + position];
		}
		XSendEvent(_display, _skypeWindow, False, 0, &event);

		event.xclient.message_type = _skypeControlApiMessageAtom;
		position += i;
	} while (position <= length);
	XFlush(_display);
}

JNIEXPORT void JNICALL Java_com_skype_connector_linux_SkypeFramework_sendCommand0(JNIEnv *env, jclass this, jstring command) {
	jboolean isCopy;
	const char *commandChars = (*env)->GetStringUTFChars(env, command, &isCopy);
	sendCommand(env, commandChars);
	if (isCopy == JNI_TRUE) {
		(*env)->ReleaseStringUTFChars(env, command, commandChars);
	}
}

JNIEXPORT void JNICALL Java_com_skype_connector_linux_SkypeFramework_stopEventLoop0(JNIEnv *env, jclass this) {
	XEvent event;
	memset(&event, 0, sizeof(XEvent));
	event.xclient.type = ClientMessage;
	event.xclient.message_type = _stopEventLoopAtom;
	event.xclient.display = _display;
	event.xclient.window = _dummyWindow;
	event.xclient.format = 8;

	XSendEvent(_display, _dummyWindow, False, 0, &event);
	XFlush(_display);
}

JNIEXPORT void JNICALL Java_com_skype_connector_linux_SkypeFramework_closeDisplay0(JNIEnv *env, jclass this) {
	if (_display != NULL) {
		XCloseDisplay(_display);
		_display = NULL;
	}
}

void * runLoop(void * args){
	Java_com_skype_connector_linux_SkypeFramework_runEventLoop0(NULL, NULL);
}

int aisRunning(){
	return isRunning();
}

