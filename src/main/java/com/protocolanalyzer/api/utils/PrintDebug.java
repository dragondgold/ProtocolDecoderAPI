package com.protocolanalyzer.api.utils;

public final class PrintDebug {

    public static void printError(String msg){
        System.out.println("[E]: " + msg);
    }

    public static void printWarning(String msg){
        System.out.println("[W]: " + msg);
    }

    public static void printInfo(String msg){
        System.out.println("[I]: " + msg);
    }
}
