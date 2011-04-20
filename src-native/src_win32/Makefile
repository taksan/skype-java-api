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
# * Gabriel Takeuchi - Makes both 32 bit and 64 bit dlls automatically
# ******************************************************************************/
# use NMAKE
all: 
	devenv skype.sln /build "Release|Win32"
	devenv skype.sln /build "Release|x64"
	move /y x86\skype_x86.dll ..\..\src\main\resources
	move /y amd64\skype_amd64.dll ..\..\src\main\resources

clean:
	del amd64 x86 skype.suo 
