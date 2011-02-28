package com.skype.connector.osx;

import org.junit.Ignore;

@Ignore
public class SkypeFrameworkTest {
    public static void main(String[] args) throws Exception {
        SkypeFramework.init("Skype4Java");

        System.out.println("isRunning: " + SkypeFramework.isRunning());
        System.out.println("isAvabileable: " + SkypeFramework.isAvailable());
    }
}
