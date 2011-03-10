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
	jmethodID log4JInfoMethod;
	jmethodID log4JErrorMethod;
	jmethodID log4JDebugMethod;

} LoggerContext;

LoggerContext getLoggerContext(JNIEnv *env);


void openLogFile(const char* logfilename);
void logToFile(int level, const char* fmt, ...);

void logInfo(LoggerContext loggerContext, const char * message);
void logError(LoggerContext loggerContext, const char * message);
void logDebug(LoggerContext loggerContext, const char* fmt, ...);

#endif
