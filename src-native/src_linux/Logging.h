#ifndef __SKYPE_FRAMEWORK_LOGGER__
#define __SKYPE_FRAMEWORK_LOGGER__

#define LOG_ERR   0
#define LOG_INFO  1
#define LOG_DEBUG 2


void openLogFile(const char* logfilename);
void logToFile(int level, char* fmt, ...);
void logToFile(int level, char* fmt, ...);
void log4jInfo(JNIEnv *env, const char * message);

#endif
