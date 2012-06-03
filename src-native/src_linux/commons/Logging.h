/*******************************************************************************
 * Copyright (c) 2011 Gabriel Takeuchi <g.takeuchi@gmail.com>
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
 * Gabriel Takeuchi - log4j support for native library
 ******************************************************************************/
#ifndef __SKYPE_FRAMEWORK_LOGGER__
#define __SKYPE_FRAMEWORK_LOGGER__

#define LOG_ERR   0
#define LOG_INFO  1
#define LOG_DEBUG 2

#define LOGGER_CATEGORY "skype-framework-native"

typedef struct  {
	JNIEnv *env;
	jclass log4JClass;
	jobject logger;	
	jmethodID log4JMethods[3];

} LoggerContext;

LoggerContext * getLoggerContext(JNIEnv *env);


void openLogWithStdout();
void openLogFile(const char* logfilename);
void logToFile(int level, const char* fmt, ...);

void logInfo(JNIEnv * env, const char * message);
void logError(JNIEnv * env, const char * message);
void logDebug(JNIEnv * env, const char* fmt, ...);

#endif
