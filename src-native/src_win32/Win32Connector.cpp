/*******************************************************************************
 * Copyright (c) 2006 r-yu/xai
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
 * 
 * Contributors:
 * r-yu/xai - initial implementation
 ******************************************************************************/
#include <tchar.h>
#include <windows.h>
#include <windowsx.h>
#include <winsock.h>
#include <stdio.h>
#include <ctype.h>
#include <time.h>

#include <jni.h>
#include "com_skype_connector_win32_Win32Connector.h"

// Protos
LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
BOOL OnCreate (HWND hWnd, LPCREATESTRUCT lpCreateStruct);
BOOL OnTimer( HWND hWnd, UINT id);
BOOL OnDestroy( HWND hWnd );
BOOL OnPaint(HWND hWnd);
int OnCopyData( HWND hWnd, HWND hWnd2, PCOPYDATASTRUCT piCopyData );

// constant
char szClassName[] = "SkypeBridge";

UINT WM_SKYPECONTROL_DISCOVER = 0;
UINT WM_SKYPECONTROL_ATTACH = 0;

// Main Window handle
HWND hMainWnd = NULL;

// JNI
JNIEnv *envWndProc = NULL;
jobject *objWndProc = NULL;
/*
__declspec( thread ) HWND hMainWnd = NULL;
__declspec( thread ) JNIEnv *envWndProc = NULL;
__declspec( thread ) jobject *objWndProc = NULL;
*/

HANDLE hEvent = NULL;

// Skype Window Handle
HWND hSkypeWnd = NULL;

JNIEXPORT void JNICALL Java_com_skype_connector_win32_Win32Connector_jni_1init
  (JNIEnv *, jobject){

	// Rrgister Message for cooporate Skype
	WM_SKYPECONTROL_DISCOVER = ::RegisterWindowMessage(_T("SkypeControlAPIDiscover"));
	WM_SKYPECONTROL_ATTACH = ::RegisterWindowMessage(_T("SkypeControlAPIAttach"));

	hEvent = CreateEvent( NULL, TRUE, FALSE, NULL );

}

// Main Window Proc
JNIEXPORT void JNICALL Java_com_skype_connector_win32_Win32Connector_jni_1windowProc
  (JNIEnv *env, jobject obj){

	HINSTANCE hInstance = NULL;
	HINSTANCE hPreInst = NULL;
	int nCmdShow = SW_HIDE;
	envWndProc = env;
	objWndProc = &obj;

	MSG msg;
	WNDCLASS myProg;
	if (!hPreInst) {
		myProg.style			= CS_HREDRAW | CS_VREDRAW;
		myProg.lpfnWndProc		= WndProc;
		myProg.cbClsExtra		= 0;
		myProg.cbWndExtra		= 0;
		myProg.hInstance		= hInstance;
		myProg.hIcon			= NULL;
		myProg.hCursor			= LoadCursor(NULL, IDC_ARROW);
		myProg.hbrBackground	= NULL;
		myProg.lpszMenuName		= NULL;
		myProg.lpszClassName	= szClassName;
		if (!RegisterClass(&myProg))
			return;
	}

	hMainWnd = CreateWindow(szClassName, szClassName,
								WS_OVERLAPPEDWINDOW,
								0, 0, 0, 0,
								NULL, NULL,
								hInstance,
								NULL);

	ShowWindow(hMainWnd, nCmdShow);
	UpdateWindow(hMainWnd);

	SetEvent( hEvent );

	// Message Loop
	while (GetMessage(&msg, NULL, 0, 0)) {
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	}

	return;
}

// Window Procedure
LRESULT CALLBACK WndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam)
{

	switch (msg) {

		HANDLE_MSG (hWnd, WM_CREATE,  OnCreate );
		HANDLE_MSG (hWnd, WM_DESTROY, OnDestroy );
		HANDLE_MSG (hWnd, WM_PAINT,   OnPaint );
		case WM_COPYDATA:
			return OnCopyData( hWnd, (HWND)wParam, (PCOPYDATASTRUCT)lParam );

		default:
			if( msg == WM_SKYPECONTROL_ATTACH){
				if(lParam == 0){
					hSkypeWnd = (HWND)wParam;
				}
				jclass clazz = envWndProc->GetObjectClass(*objWndProc);
				if( clazz == NULL ){
					printf("cannot find class\n");
				}

				jmethodID methodid = envWndProc->GetMethodID( clazz,
								"jni_onAttach", "(I)V"); 
				if( methodid == NULL ){
					printf("cannot find methodid\n");
				}

				envWndProc->CallIntMethod(*objWndProc, methodid, (int)lParam ); 
			}
			break;
	}
	return DefWindowProc(hWnd, msg, wParam, lParam);
}

BOOL OnPaint(HWND hWnd) { return TRUE; }
BOOL OnCreate (HWND hWnd, LPCREATESTRUCT lpCreateStruct) {  return TRUE; }
BOOL OnDestroy( HWND hWnd ){ PostQuitMessage(0); return TRUE; }

int OnCopyData( HWND hWnd, HWND hWnd2, PCOPYDATASTRUCT piCopyData ){

	if( hSkypeWnd == hWnd2 ){

		jclass clazz = envWndProc->GetObjectClass(*objWndProc);
		if( clazz == NULL ){
			printf("cannot find class\n");
		}

		jmethodID methodid = envWndProc->GetMethodID( clazz,
						"jni_onSkypeMessage", "(Ljava/lang/String;)V"); 
		if( methodid == NULL ){
			printf("cannot find methodid\n");
		}

		jstring message =  envWndProc->NewStringUTF( (const char *)piCopyData->lpData );
		if( message == NULL ){
			printf("cannot find message\n");
		}

		envWndProc->CallObjectMethod(*objWndProc, methodid, message ); 

		envWndProc->DeleteLocalRef(message);
	}

   	return 1;
}

JNIEXPORT void JNICALL Java_com_skype_connector_win32_Win32Connector_jni_1sendMessage
  (JNIEnv *env, jobject obj, jstring message){

	if( hSkypeWnd == NULL ) return;
	if( message == NULL ) return;

	jboolean isCopy;

	int length = env->GetStringUTFLength(message); 
	const char *utf8 = env->GetStringUTFChars(message, &isCopy);
	char *szUTF8 = (char *)GlobalAlloc( GPTR, length + 1 );
	memset( szUTF8, 0, length + 1 );
	memcpy( szUTF8, utf8, length );
	env->ReleaseStringUTFChars( message, utf8 );

	COPYDATASTRUCT oCopyData;
	// send command to skype
	oCopyData.dwData = 0;
	oCopyData.lpData = (void *)szUTF8;
	oCopyData.cbData = strlen( szUTF8 ) + 1;

	if( SendMessage( hSkypeWnd, WM_COPYDATA, (WPARAM)hMainWnd, (LPARAM)&oCopyData)==FALSE ) {
		// TODO: throw exception
	}

	GlobalFree( szUTF8 );

	return;
}

JNIEXPORT void JNICALL Java_com_skype_connector_win32_Win32Connector_jni_1connect
  (JNIEnv *, jobject){

	WaitForSingleObject(hEvent, INFINITE);
	::PostMessage( HWND_BROADCAST, WM_SKYPECONTROL_DISCOVER, (WPARAM)hMainWnd, 0);		

}

JNIEXPORT jstring JNICALL Java_com_skype_connector_win32_Win32Connector_jni_1getInstalledPath
  (JNIEnv *env, jobject){
  
	HKEY hKey;
	wchar_t path[MAX_PATH];
	DWORD dwBufLen = MAX_PATH -1;

	RegOpenKeyExW( HKEY_LOCAL_MACHINE, L"Software\\Skype\\Phone",  0, KEY_QUERY_VALUE, &hKey );
	RegQueryValueExW( hKey, L"SkypePath", NULL, NULL, (LPBYTE) path, &dwBufLen);
	RegCloseKey( hKey );

	if( dwBufLen == 0 ){
		RegOpenKeyExW( HKEY_CURRENT_USER, L"Software\\Skype\\Phone",  0, KEY_QUERY_VALUE, &hKey );
		RegQueryValueExW( hKey, L"SkypePath", NULL, NULL, (LPBYTE) path, &dwBufLen);
		RegCloseKey( hKey );
	}

	return env->NewString( path, dwBufLen );

}
