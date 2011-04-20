#/*******************************************************************************
# * Copyright (c) 2011 Gabriel Takeuchi <g.takeuchi@gmail.com>
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
# * Gabriel Takeuchi - Removed need for project builder and xcode, builds now just using "make"
# ******************************************************************************/

ARCH=-arch i386 -arch ppc 
CWD=.
JNILib=$(CWD)/build/skype.build/Release/JNILib.build
OBJS_NORMAL=$(JNILib)/Objects-normal/
DERIVED_SRCS=$(JNILib)/DerivedSources
RELEASE_DIR=$(CWD)/build/Release

OBJ=$(OBJS_NORMAL)/SkypeFramework.o
CC=/Developer/usr/bin/gcc-4.2 
LINK_FILELIST=$(OBJS_NORMAL)/skype.LinkFileList
CC_FLAGS=-fmessage-length=0 -pipe -Wno-trigraphs -fpascal-strings -fasm-blocks -Os -Wunused-variable \
	-Wmost -Wno-four-char-constants -Wno-unknown-pragmas

INCLUDES=-I$(JNILib)/skype.hmap -I/System/Library/Frameworks/JavaVM.framework/Headers -I$(DERIVED_SRCS)

F_DIR=-F$(RELEASE_DIR) -F$(CWD)/

FRAMEWORKS=-framework JavaVM -framework Skype -framework Carbon -framework Foundation
LD_FLAGS=-dynamiclib -single_module -compatibility_version 1 -current_version 1
INSTALL_FLAG=-install_name /usr/local/lib/libskype.jnilib
TARGET=$(RELEASE_DIR)/libskype.jnilib
JAVA_RESOURCES=../../src/main/resources/
SKYPE_FRAMEWORK_SHARED=Skype.framework/Versions/A/Skype

all: $(TARGET)
	cp $(TARGET) $(JAVA_RESOURCES)
	cp $(SKYPE_FRAMEWORK_SHARED) $(JAVA_RESOURCES)/Skype.Framework

$(TARGET): mkdirs $(OBJ)
	$(CC) $(ARCH) -L$(RELEASE_DIR) $(F_DIR) -filelist $(LINK_FILELIST) $(FRAMEWORKS) $(INSTALL_FLAG) $(LD_FLAGS) -o $(TARGET)

mkdirs: 
	mkdir -p $(DERIVED_SRCS)
	mkdir -p $(OBJS_NORMAL)
	mkdir -p $(RELEASE_DIR)

$(OBJ): com_skype_connector_osx_SkypeFramework.h
	$(CC) -x objective-c $(ARCH) $(CC_FLAGS) $(F_DIR) $(INCLUDES) -c $(CWD)/SkypeFramework.m -o $(OBJ)
	echo $(OBJ) > $(LINK_FILELIST)

com_skype_connector_osx_SkypeFramework.h:
	ant

clean:
	rm -rf build com_skype_connector_osx_SkypeFramework.h
