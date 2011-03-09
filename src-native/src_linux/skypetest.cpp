#include <stdlib.h>
#include <stdio.h>
#include <malloc.h>
#include <string.h>

#include <X11/Xlib.h>
#include <X11/Xatom.h>

#define FALSE 0
#define TRUE  1


extern "C" {
	int aisRunning();
	void setup();
};


int main(){
	setup();
	printf("%d\n", aisRunning());
//	skype_connect();
}
