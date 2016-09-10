# Important: Desktop API is crippled now

As time passes, this project becomes less and less relevant due to Desktop API discontinuation. The API discontinuation
was a hard blow and it is the reason I stopped maintaining the project. Here's the relevant blog post quote:

> Previously we shared that we would retire the Desktop API later this year. However, I’m happy to share that we will 
be extending support for two of the most widely used features – call recording and compatibility with hardware devices – 
until we determine alternative options or retire the current solution. Although chat via third party applications, 
will cease to work as previously communicated

Source: https://blogs.skype.com/2013/11/06/feature-evolution-and-support-for-the-skype-desktop-api/

Another relevant info:

> Important: As communicated in this blog post, due to technology improvements we are making to the Skype experience, some
features of the API will stop working with Skype for desktop. For example, delivery of chat messages using the API will 
cease to work. However, we will be extending support for two of the most widely used features – call recording and compatibility 
with hardware devices – until we determine alternative options or retire the current solution.

According with issue #81, the chat support already stopped working. 

So, this is it. I already stopped supporting the api unofficially back when I learned about the discontinuation,
but now I'm officially dropping all support. 

Thanks everyone for using the API.

# Old Documentation follows


This project is a mavenization and release of Skype4Java with several bug fixes.

## 1.4 RELEASED

Several bugfixes and a few features have been added to this version. 

- Windows : 
	- now the native library can be compiled using mingw, both under windows and linux. It is still possible to 
compile using visual studio, but mingw compilation is preferred.
	- a small bug in getInstaledPath function has been fixed.
	- fixed native library extraction and loading

- Mac OS : 
	- fixed problems with getProfile and other functions that depended on deprecated MESSAGE object have been fixed.
	- framework extraction fixed to work both from jar and eclipse

- Linux : a major rewrite has been done. There are now two native implementations, the old one using X11 and a new one
	using DBUS. The X11 version was very unstable and could even connect to a skype instance running on another user.
	The default implementation is now DBUS, which prove much more stable on my tests. It is possible to use the old
	implementation setting a system variable: "skype.api.impl" to x11

Some new features have been added:

- Chat.setGuidelines : can be used to change a set guidelines
- Chat.addListener : allows registering a listener that will be triggered for users entering and leaving a chat
- Skype.addChatEditListener : allows adding a listener that will be triggered when messages are edited

Besides, the samples have been moved to its own project and a lot of refactorings have been to done to organize
the code.

The samples can be found on the following project:

https://github.com/taksan/skype-api-samples

To use the API you can add the following dependency to your maven project:

<dependency>
	  <groupId>com.github.taksan</groupId>
	  <artifactId>skype-java-api</artifactId>
	  <version>1.4</version>
</dependency>

There is also a shaded jar, if you prefer:

https://www.dropbox.com/s/2j4h8rbjz7modmm/skype-java-api-1.6.jar?dl=0
