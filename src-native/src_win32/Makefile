# use NMAKE
all: 
	devenv skype.sln /build "Release|Win32"
	devenv skype.sln /build "Release|x64"
	move /y x86\skype_x86.dll ..\..\src\main\resources
	move /y amd64\skype_amd64.dll ..\..\src\main\resources

clean:
	del amd64 x86 skype.suo 
