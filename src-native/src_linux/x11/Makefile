#*******************************************************************************
# * Copyright (c) 2006-2007 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
# * Copyright (c) 2006-2007 UBION Inc. <http://www.ubion.co.jp/>
# * 
# * Copyright (c) 2006-2007 Skype Technologies S.A. <http://www.skype.com/>
# * 
# * Skype4Java is licensed under either the Apache License, Version 2.0 or
# * the Eclipse Public License v1.0.
# * You may use it freely in commercial and non-commercial products.
# * You may obtain a copy of the licenses at
# *
# *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
# *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
# *
# * If it is possible to cooperate with the publicity of Skype4Java, please add
# * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
# * in your web site or documents.
# * 
# * Contributors:
# * Koji Hisano - initial API and implementation
# * Gabriel Takeuchi - added support for 64 bit arch
# ******************************************************************************/
JDK_DIR = /usr/lib/jvm/java-6-openjdk/

CC = gcc
CFLAGS = -pipe -g -fPIC -I../commons 
#-Wall -W 
INCPATH = -I. -I$(JDK_DIR)/include -I$(JDK_DIR)/include/linux
LOGGING=../commons/Logging.c
SOURCES = SkypeFramework.c Logging.c
OBJECTS = $(SOURCES:.c=.o)
OBJECTS_X64 = $(SOURCES:.c=_x64.o)

.SECONDEXPANSION:

LINK = g++
LD_FLAGS_i386 = -shared -L/usr/lib/i386-linux-gnu
LD_FLAGS = -shared -L/usr/lib/x86_64-linux-gnu/
LIBS = -lX11

PROJECT_ROOT=../../..
TARGET_CLASSES=$(PROJECT_ROOT)/target/classes
TARGET=$(PROJECT_ROOT)/src/main/resources/

all: libskype_x86.so libskype_x64.so

libskype_x86.so: SkypeFramework Logging
	$(LINK) $(LD_FLAGS_i386) -shared  -m32 -o $(TARGET)/libskype_x86.so $(OBJECTS) $(LIBS) 
libskype_x64.so: SkypeFramework Logging
	$(LINK) $(LD_FLAGS) -m64 -o $(TARGET)/libskype_x64.so $(OBJECTS_X64) $(LIBS)

skypetest: SkypeFramework.o skypetest.cpp
	$(LINK) -o skypetest  $(OBJECTS_X64) skypetest.cpp -L/usr/lib/x86_64-linux-gnu/ -lX11 -lpthread

com_skype_connector_linux_SkypeFramework.h:
	javah -classpath $(TARGET_CLASSES) com.skype.connector.linux.SkypeFramework

SkypeFramework: com_skype_connector_linux_SkypeFramework.h SkypeFramework.c
	$(CC) -c $(CFLAGS) $(INCPATH) -m32 -o $@.o $@.c
	$(CC) -c $(CFLAGS) $(INCPATH) -m64 -o $@_x64.o $@.c

Logging: com_skype_connector_linux_SkypeFramework.h $(LOGGING)
	$(CC) -c $(CFLAGS) $(INCPATH) -m32 -o $@.o $(LOGGING)
	$(CC) -c $(CFLAGS) $(INCPATH) -m64 -o $@_x64.o $(LOGGING)

clean:
	rm -f com_skype_connector_linux_SkypeFramework.h *.o *.so skypetest
