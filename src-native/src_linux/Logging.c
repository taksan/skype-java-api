#include <stdarg.h>
#include <time.h>
#include <stdio.h>
#include <malloc.h>
#include <string.h>

#include <X11/Xlib.h>
#include <X11/Xatom.h>

#include "com_skype_connector_linux_SkypeFramework.h"

const char * logLevelNames[3] = {"ERROR","INFO","DEBUG"};

static FILE* logfile;
void openLogFile(const char* logfilename)
{
	logfile = fopen(logfilename, "a+");
}

void logToFile(int level, char* fmt, ...)
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

static void log4jInfo(JNIEnv *env, const char * message)
{
	printf("about to get logger\n");
	jclass clazz  = (*env)->FindClass(env, "org/apache/log4j/Logger");

	printf("logger caught. %p\n", clazz);
	jmethodID method = (*env)->GetStaticMethodID(env, clazz, "getLogger", "(Ljava/lang/String;)Lorg/apache/log4j/Logger");

	if (method == NULL) {
		printf("Could not acquire getLogger method!\n");
		return;
	}
	printf("Logger.getLogger method acquired %p\n", method);

	jstring category= (*env)->NewStringUTF(env, "skype-framework-native");


	printf("category string built\n");

	jobject logger = (*env)->CallStaticObjectMethod(env, clazz, method, category);


	printf("Logger.getLogger invoked\n");

	jmethodID infoMethod = (*env)->GetMethodID(env, clazz , "info", "(Ljava/lang/Object;)V");

	printf("info method retrieved\n");

	jstring messageString = (*env)->NewStringUTF(env, message);


	printf("message string built\n");

	(*env)->CallVoidMethod(env, logger, infoMethod, messageString);

	printf("info method invoked\n");
}

