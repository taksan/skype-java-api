NODEBUG=1

!include <Win32.Mak>

TARGET = skype.dll
OBJS = Win32Connector.obj
LIBS =  user32.lib advapi32.lib

ALL: $(TARGET)

$(TARGET): $(OBJS)
	$(link) $(ldebug) $(dlllflags) -out:$*.dll $(OBJS) $(dlllibsmt)  $(LIBS)
	move /y skype.dll ..\res
	del *.obj *.res *.exp *.lib

$(OBJS): 

Win32Connector.obj:	Win32Connector.cpp com_skype_connector_win32_Win32Connector.h

.cpp.obj:
	$(cc) $(cdebug)	\
		-I"$(JAVA_HOME)\include"	\
		-I"$(JAVA_HOME)\include\win32"	\
		$(cflags) $(cvarsmt) /EHsc $*.cpp

HEADER:
	javah -classpath ..\release\skype.jar -jni com.skype.connector.win32.Win32Connector
