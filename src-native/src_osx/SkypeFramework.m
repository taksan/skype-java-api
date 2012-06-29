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
 * Gabriel Takeuchi - small fix under mac for CHATMESSAGE reply
 ******************************************************************************/

#import "com_skype_connector_osx_SkypeFramework.h"
#import <Skype/Skype.h>
#import <Carbon/Carbon.h>
#import <Foundation/Foundation.h>

#define SKYPE_FRAMEWORK_CLASS "com/skype/connector/osx/SkypeFramework"

#define SKYPE_STRING_MAX 0x10000

static JavaVM *_vm;

static unichar *_notificationStringBuffer;
static unsigned int _notificationStringBufferLength = 0;

static unichar *_resultStringBuffer;
static unsigned int _resultStringBufferLength = 0;

static struct SkypeDelegate _delegate;

static JNIEnv *getEnv() {
	JNIEnv *env;
	if ((*_vm)->AttachCurrentThreadAsDaemon(_vm, (void **)&env, NULL) < 0) {
		jclass clazz = (*env)->FindClass(env, "java/lang/InternalError");
		(*env)->ThrowNew(env, clazz, "Attaching to current thread failed.");
		return NULL;
	} else {
		return env;
	}
}

static void fireStatusChanged(const char *methodName) {
	JNIEnv *env = getEnv();
	if (env == NULL) {
		return;
	}

	jclass clazz  = (*env)->FindClass(env, SKYPE_FRAMEWORK_CLASS);
	jmethodID method = (*env)->GetStaticMethodID(env, clazz, methodName, "()V");
	(*env)->CallStaticVoidMethod(env, clazz, method);
}

static void SkypeBecameAvailable(CFPropertyListRef aNotification) {
	fireStatusChanged("fireBecameAvailable");
}

static void SkypeBecameUnavailable(CFPropertyListRef aNotification){
	fireStatusChanged("fireBecameUnavailable");
} 

static void SkypeAttachResponse(unsigned int aAttachResponseCode){
	JNIEnv *env = getEnv();
	if (env == NULL) {
		return;
	}

	jclass clazz  = (*env)->FindClass(env, SKYPE_FRAMEWORK_CLASS);
	jmethodID method = (*env)->GetStaticMethodID(env, clazz, "fireAttachResponse", "(I)V");
	(*env)->CallStaticVoidMethod(env, clazz, method, (jint)aAttachResponseCode);
}

static bool monitorEnter(JNIEnv *env, jobject mutex) {
	if ((*env)->MonitorEnter(env, mutex) == 0) {
		return true;
	} else {
		jclass clazz = (*env)->FindClass(env, "java/lang/InternalError");
		(*env)->ThrowNew(env, clazz, "Entering to monitor failed.");
		return false;
	}
}

static bool monitorExit(JNIEnv *env, jobject mutex) {
	if ((*env)->MonitorExit(env, mutex) == 0) {
		return true;
	} else {
		jclass clazz = (*env)->FindClass(env, "java/lang/InternalError");
		(*env)->ThrowNew(env, clazz, "Exiting from monitor failed.");
		return false;
	}
}

static bool checkNull(JNIEnv *env, void *value) {
	if (value == NULL) {
		jclass clazz = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
		(*env)->ThrowNew(env, clazz, NULL);
		return true;
	} else {
		return false;
	}
}

static void SkypeNotificationReceived(CFStringRef aNotificationString){
	JNIEnv *env = getEnv();
	if (env == NULL) {
		return;
	}
/*
	if (CFStringHasPrefix(aNotificationString, CFSTR("MESSAGE "))) {
		CFMutableStringRef mutable = CFStringCreateMutable(NULL, 0);
		CFStringAppend(mutable, CFSTR("CHAT"));
		CFStringAppend(mutable, aNotificationString);
		aNotificationString = mutable;
	}
*/
	jclass clazz  = (*env)->FindClass(env, SKYPE_FRAMEWORK_CLASS);
	jfieldID field = (*env)->GetStaticFieldID(env, clazz, "notificationReceivedMutex", "Ljava/lang/Object;");
	jobject mutex = (*env)->GetStaticObjectField(env, clazz, field);
	
	if (!monitorEnter(env, mutex)) {
		return;
	}

	CFRange range;
	range.location = 0;
	range.length = CFStringGetLength(aNotificationString);
	
	if (_notificationStringBufferLength < range.length) {
		free(_notificationStringBuffer);
		
		_notificationStringBuffer = (unichar *)malloc(sizeof(unichar) * range.length);
		if (checkNull(env, (void *)_notificationStringBuffer)) {
			monitorExit(env, mutex);
			return;
		}
		_notificationStringBufferLength = range.length;
	}
	
	CFStringGetCharacters(aNotificationString, range, _notificationStringBuffer);

	jstring notificationString = (*env)->NewString(env, (jchar *)_notificationStringBuffer, (jsize)range.length);
	if (checkNull(env, (void *)notificationString)) {
		monitorExit(env, mutex);
		return;
	}

	jmethodID method = (*env)->GetStaticMethodID(env, clazz, "fireNotificationReceived", "(Ljava/lang/String;)V");
	(*env)->CallStaticVoidMethod(env, clazz, method, notificationString);
	
	if (!monitorExit(env, mutex)) {
		return;
	}
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	_vm = vm;
	return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL Java_com_skype_connector_osx_SkypeFramework_setup0(JNIEnv *env, jclass this, jstring applicationName) {
	_notificationStringBuffer = (unichar *)malloc(sizeof(unichar) * SKYPE_STRING_MAX);
	if (checkNull(env, (void *)_notificationStringBuffer)) {
		return;
	}
	_notificationStringBufferLength = SKYPE_STRING_MAX;

	_resultStringBuffer = (unichar *)malloc(sizeof(unichar) * SKYPE_STRING_MAX);
	if (checkNull(env, (void *)_resultStringBuffer)) {
		return;
	}
	_resultStringBufferLength = SKYPE_STRING_MAX;

	jboolean isCopy;
	const char *ccApplicationName  = (*env)->GetStringUTFChars(env, applicationName, &isCopy);
	if (checkNull(env, (void *)ccApplicationName)) {
		return;
	}
	CFStringRef cfApplicationName = CFStringCreateWithCString(kCFAllocatorDefault, ccApplicationName, kCFStringEncodingUTF8);
	if (checkNull(env, (void *)cfApplicationName)) {
		return;
	}

	_delegate.SkypeBecameAvailable = SkypeBecameAvailable;
	_delegate.SkypeBecameUnavailable = SkypeBecameUnavailable;
	_delegate.SkypeAttachResponse = SkypeAttachResponse;
	_delegate.SkypeNotificationReceived = SkypeNotificationReceived;
	_delegate.clientApplicationName = cfApplicationName;
	SetSkypeDelegate(&_delegate);

	if (isCopy == JNI_TRUE) {
		(*env)->ReleaseStringUTFChars(env, applicationName, ccApplicationName);
	}
}

JNIEXPORT jboolean JNICALL Java_com_skype_connector_osx_SkypeFramework_isRunning0(JNIEnv *env, jclass this) {
	return IsSkypeRunning()? JNI_TRUE: JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_skype_connector_osx_SkypeFramework_isAvailable0(JNIEnv *env, jclass this) {
	return IsSkypeAvailable()? JNI_TRUE: JNI_FALSE;
}

JNIEXPORT void JNICALL Java_com_skype_connector_osx_SkypeFramework_connect0(JNIEnv *env, jclass this) {
	ConnectToSkype();
}

JNIEXPORT jstring JNICALL Java_com_skype_connector_osx_SkypeFramework_sendCommand0(JNIEnv *env, jclass this, jstring commandString) {
	jboolean isCopy;
	const char *ccCommandString  = (*env)->GetStringUTFChars(env, commandString, &isCopy);
	if (checkNull(env, (void *)ccCommandString)) {
		return NULL;
	}

	CFStringRef cfCommandString = CFStringCreateWithCString(kCFAllocatorDefault, ccCommandString, kCFStringEncodingUTF8);
	if (checkNull(env, (void *)cfCommandString)) {
		if (isCopy == JNI_TRUE) {
			(*env)->ReleaseStringUTFChars(env, commandString, ccCommandString);
		}
		return NULL;
	}

	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	CFStringRef cfResultString = SendSkypeCommand(cfCommandString);
	
	if (cfResultString == NULL) {
		[pool release];

		CFRelease(cfCommandString);
		if (isCopy == JNI_TRUE) {
			(*env)->ReleaseStringUTFChars(env, commandString, ccCommandString);
		}

		return NULL;
	}
	if (CFStringHasPrefix(cfResultString, CFSTR("MESSAGE "))) {
		CFMutableStringRef mutable = CFStringCreateMutable(NULL, 0);
		CFStringAppend(mutable, CFSTR("CHAT"));
		CFStringAppend(mutable, cfResultString);
		cfResultString = mutable;
	}
	
	CFRange range;
	range.location = 0;
	range.length = CFStringGetLength(cfResultString);


	if (_resultStringBufferLength < range.length) {
		free(_resultStringBuffer);
		
		_resultStringBuffer = (unichar *)malloc(sizeof(unichar) * range.length);
		if (checkNull(env, (void *)_resultStringBuffer)) {
			[pool release];

			CFRelease(cfCommandString);
			if (isCopy == JNI_TRUE) {
				(*env)->ReleaseStringUTFChars(env, commandString, ccCommandString);
			}

			return NULL;
		}
		_resultStringBufferLength = range.length;
	}
	
	CFStringGetCharacters(cfResultString, range, _resultStringBuffer);
	jstring resultString = (*env)->NewString(env, (jchar *)_resultStringBuffer, (jsize)range.length);	

	[pool release];

	CFRelease(cfCommandString);
	if (isCopy == JNI_TRUE) {
		(*env)->ReleaseStringUTFChars(env, commandString, ccCommandString);
	}

	
	return resultString;
}

JNIEXPORT void JNICALL Java_com_skype_connector_osx_SkypeFramework_dispose0(JNIEnv *env, jclass this) {
	DisconnectFromSkype();
	RemoveSkypeDelegate();

	free(_notificationStringBuffer);
	_notificationStringBufferLength = 0;
}

JNIEXPORT jint JNICALL Java_com_skype_connector_osx_SkypeFramework_runCurrentEventLoop0(JNIEnv *env, jclass this, jdouble inTimeout) {
	return RunCurrentEventLoop(inTimeout);
}

JNIEXPORT void JNICALL Java_com_skype_connector_osx_SkypeFramework_runApplicationEventLoop0(JNIEnv *env, jclass this) {
	RunApplicationEventLoop();
}

JNIEXPORT void JNICALL Java_com_skype_connector_osx_SkypeFramework_quitApplicationEventLoop0(JNIEnv *env, jclass this) {
	QuitApplicationEventLoop();
}
