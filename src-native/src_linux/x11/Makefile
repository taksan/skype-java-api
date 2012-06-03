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

SOURCES = SkypeFramework.c Logging.c

.SECONDEXPANSION:

LIBS = -lX11

all: $(X86) $(X64)

$(X86): SkypeFramework Logging
	$(LINK) $(LD_FLAGS) -m32 -o $(TARGET)/$(X86) $(OBJECTS) $(LIBS) 
$(X64): SkypeFramework Logging
	$(LINK) $(LD_FLAGS) -m64 -o $(TARGET)/$(X64) $(OBJECTS_X64) $(LIBS)

skypetest: SkypeFramework.o skypetest.cpp
	$(LINK) -o skypetest  $(OBJECTS_X64) skypetest.cpp  -lX11 -lpthread


