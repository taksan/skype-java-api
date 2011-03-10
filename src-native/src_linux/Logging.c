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

LoggerContext getLoggerContext(JNIEnv *env)
{
	LoggerContext loggerContext;
	loggerContext.env = env;

	loggerContext.log4JClass  = (*env)->FindClass(env, "org/apache/log4j/Logger");
	
	jmethodID method = (*env)->GetStaticMethodID(env, loggerContext.log4JClass, "getLogger", "(Ljava/lang/String;)Lorg/apache/log4j/Logger;");

	if (method == NULL) {
		fprintf(stderr, "Could not acquire getLogger method!\n");
		return;
	}

	jstring category= (*env)->NewStringUTF(env, LOGGER_CATEGORY);
	loggerContext.logger = (*env)->CallStaticObjectMethod(env, loggerContext.log4JClass, method, category);

	loggerContext.log4JInfoMethod  = (*env)->GetMethodID(env, loggerContext.log4JClass , "info", "(Ljava/lang/Object;)V");
	loggerContext.log4JErrorMethod = (*env)->GetMethodID(env, loggerContext.log4JClass , "error", "(Ljava/lang/Object;)V");
	loggerContext.log4JDebugMethod = (*env)->GetMethodID(env, loggerContext.log4JClass , "debug", "(Ljava/lang/Object;)V");

	return loggerContext;
}

void log4jmessage(LoggerContext loggerContext, jmethodID messageMethod, const char * message)
{
/*
	va_list ap;
	va_start(ap, fmt);
	char messageBuffer[3000];

	printf("will do a printf\n")
	vprintf(fmt,ap);

	printf("will print into a string \n")

	vsprintf(messageBuffer, fmt, ap);
//	logToFile(LOG_DEBUG, messageBuffer);
	printf("will invoke log4j\n")
	*/

	jstring messageString = (*loggerContext.env)->NewStringUTF(loggerContext.env, message);
	(*loggerContext.env)->CallVoidMethod(loggerContext.env, loggerContext.logger, messageMethod, messageString);
}

void logInfo(LoggerContext loggerContext, const char * message)
{
	log4jmessage(loggerContext, loggerContext.log4JInfoMethod, message);

}

void logError(LoggerContext loggerContext, const char * message)
{
	log4jmessage(loggerContext, loggerContext.log4JErrorMethod, message);

}

void logDebug(LoggerContext loggerContext, const char* fmt, ...)
{
	va_list ap;
	va_start(ap, fmt);

	char buffer[200000];
	vsnprintf(buffer, sizeof(buffer), fmt, ap);

	log4jmessage(loggerContext, loggerContext.log4JDebugMethod, buffer);
}

