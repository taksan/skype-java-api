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

include ../Makefile.common

CFLAGS := $(CFLAGS) $(shell pkg-config --cflags dbus-1 dbus-glib-1 )
SOURCES = SkypeFramework.c SkypeService.c Logging.c

.SECONDEXPANSION:

LIBS = $(shell pkg-config --libs dbus-1 dbus-glib-1)
LIBS_32= -L/usr/lib32/i386-linux-gnu $(shell pkg-config --libs dbus-1 dbus-glib-1)

all: $(X64) $(X86)

$(X86): SkypeFramework Logging SkypeService
	$(LINK) $(LD_FLAGS) -m32 -o $(TARGET)/$(X86) $(OBJECTS) $(LIBS_32) 

$(X64): SkypeFramework Logging SkypeService
	$(LINK) $(LD_FLAGS) -m64 -o $(TARGET)/$(X64) $(OBJECTS_X64) $(LIBS)

SkypeService: skype-service.h skype-service-object-glue.h SkypeService.c
	$(CC) -c $(CFLAGS) $(INCPATH) -m32 -o $@.o $@.c
	$(CC) -c $(CFLAGS) $(INCPATH) -m64 -o $@_x64.o $@.c


