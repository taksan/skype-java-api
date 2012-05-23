#include <stdlib.h>
#include <stdio.h>
#include <malloc.h>
#include <string.h>
#include <pthread.h>
#include <unistd.h>

#include <X11/Xlib.h>
#include <X11/Xatom.h>

#define FALSE 0
#define TRUE  1


extern "C" {
	int aisRunning();
	void * runLoop(void * arg);
	void setup();
};

void startEventLoopInSeparateThread(){
	pthread_t thread;
        pthread_create(&thread, NULL,&runLoop, NULL );
        sleep(1);
}

int main(){
	setup();
	startEventLoopInSeparateThread();
	printf("%d\n", aisRunning());
//	skype_connect();
}
