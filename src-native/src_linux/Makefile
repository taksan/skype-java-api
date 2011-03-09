JDK_DIR = /usr/lib/jvm/java-6-sun

CC = gcc
CFLAGS = -pipe -g -fPIC 
#-Wall -W 
INCPATH = -I. -I$(JDK_DIR)/include -I$(JDK_DIR)/include/linux
SKYPEFW_SOURCE = SkypeFramework.c Logging.c
OBJECTS = $(SOURCES:.c=.o)
OBJECTS_X64 = $(SOURCES:.c=_x64.o)

.SECONDEXPANSION:

LINK = g++
LFLAGS = -shared -L/usr/X11R6/lib
LIBS = -lX11

TARGET=../../src/main/resources/

all: libskype.so libskype_x64.so

libskype.so: SkypeFramework Logging
	$(LINK) $(LFLAGS) -m32 -o $(TARGET)/libskype.so $(OBJECTS) $(LIBS)

libskype_x64.so: SkypeFramework Logging
	$(LINK) $(LFLAGS) -m64 -o $(TARGET)/libskype_x64.so $(OBJECTS_X64) $(LIBS)

skypetest: SkypeFramework.o skypetest.cpp
	$(LINK) -o skypetest -L/usr/X11R6/lib -lX11 $(OBJECTS_X64) skypetest.cpp

com_skype_connector_linux_SkypeFramework.h:
	javah -classpath ../../target/classes com.skype.connector.linux.SkypeFramework

SkypeFramework: com_skype_connector_linux_SkypeFramework.h SkypeFramework.c
	$(CC) -c $(CFLAGS) $(INCPATH) -m32 -o $@.o $@.c
	$(CC) -c $(CFLAGS) $(INCPATH) -m64 -o $@.o $@.c

Logging: com_skype_connector_linux_SkypeFramework.h Logging.c
	$(CC) -c $(CFLAGS) $(INCPATH) -m32 -o $@.o $@.c
	$(CC) -c $(CFLAGS) $(INCPATH) -m64 -o $@.o $@.c

clean:
	rm -f *.h *.o *.so skypetest
