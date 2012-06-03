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
#include <stdarg.h>
#include <time.h>
#include <stdio.h>
#include <malloc.h>
#include <string.h>
#include <stdlib.h>

#include <X11/Xlib.h>
#include <X11/Xatom.h>

#include "com_skype_connector_linux_SkypeFramework.h"
#include "Logging.h"

const char * logLevelNames[3] = {"ERROR","INFO","DEBUG"};

FILE* logfile = NULL;
void logToFile(int level, const char* fmt, ...);
void log4jmessage(LoggerContext * loggerContext, int level, const char * message);

void log4jmessage(LoggerContext * loggerContext, int level, const char * message)
{
	if (loggerContext == NULL) {
		logToFile(level, message);
		return;
	}
	jstring messageString = (*loggerContext->env)->NewStringUTF(loggerContext->env, message);
	jmethodID messageMethod = loggerContext->log4JMethods[level];
	(*loggerContext->env)->CallVoidMethod(loggerContext->env, loggerContext->logger, messageMethod, messageString);
	free(loggerContext);
}

void logInfo(JNIEnv * env, const char * message)
{
	LoggerContext * loggerContext = getLoggerContext(env);
	log4jmessage(loggerContext, LOG_INFO, message);

}

void logError(JNIEnv * env, const char * message)
{
	LoggerContext * loggerContext = getLoggerContext(env);
	log4jmessage(loggerContext, LOG_ERR, message);

}

void logDebug(JNIEnv *env, const char* fmt, ...)
{
	LoggerContext *  loggerContext = getLoggerContext(env);
	
	va_list ap;
	va_start(ap, fmt);

	char buffer[200000];
	vsnprintf(buffer, sizeof(buffer), fmt, ap);

	log4jmessage(loggerContext, LOG_DEBUG, buffer);
}

LoggerContext * getLoggerContext(JNIEnv *env)
{
	return NULL;

	jclass logger = (*env)->FindClass(env, "org/apache/log4j/Logger");
	if ((*env)->ExceptionCheck(env))
		return NULL;

	LoggerContext * loggerContext = (LoggerContext*)malloc(sizeof(LoggerContext));
	loggerContext->env = env;

	loggerContext->log4JClass  = logger;
	
	jmethodID method = (*env)->GetStaticMethodID(env, loggerContext->log4JClass, "getLogger", "(Ljava/lang/String;)Lorg/apache/log4j/Logger;");

	if (method == NULL) {
		free(loggerContext);
		return NULL;
	}

	jstring category= (*env)->NewStringUTF(env, LOGGER_CATEGORY);
	loggerContext->logger = (*env)->CallStaticObjectMethod(env, loggerContext->log4JClass, method, category);

	loggerContext->log4JMethods[LOG_INFO] = (*env)->GetMethodID(env, loggerContext->log4JClass , "info", "(Ljava/lang/Object;)V");
	loggerContext->log4JMethods[LOG_ERR] = (*env)->GetMethodID(env, loggerContext->log4JClass , "error", "(Ljava/lang/Object;)V");
	loggerContext->log4JMethods[LOG_DEBUG] = (*env)->GetMethodID(env, loggerContext->log4JClass , "debug", "(Ljava/lang/Object;)V");

	return loggerContext;
}

void openLogFile(const char* logfilename);
void logTimeAndLevel(int level);
void ensureLogFileIsInitialized(); 

void logToFile(int level, const char* fmt, ...)
{
	ensureLogFileIsInitialized();
	if (logfile == NULL)
		return;

	logTimeAndLevel(level);

	va_list ap;
	va_start(ap, fmt);

	vfprintf(logfile, fmt, ap);

	fprintf(logfile, "\n");
	fflush(logfile);
	va_end(ap);
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

void openLogWithStdout() {
	logfile = stdout;
}

void openLogFile(const char* logfilename)
{
	logfile = fopen(logfilename, "a+");
}

void ensureLogFileIsInitialized() 
{
	if (logfile == NULL) {
		char * debugFileName = getenv("SKYPE_API_NATIVE_DEBUG_FILENAME");
		if (debugFileName == NULL) {
			return;
		}

		if (strcmp(debugFileName, "stdout") == 0) {
			logfile = stdout;
		}
		else {
			openLogFile(debugFileName);
		}
		fprintf(stdout, "Linux Native Lib Debug initialized. Logging to file: %s\n", debugFileName);
		fflush(stdout);
	}
}

