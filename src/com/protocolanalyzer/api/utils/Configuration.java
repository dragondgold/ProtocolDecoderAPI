package com.protocolanalyzer.api.utils;

import java.util.Properties;

public class Configuration extends Properties{

	public void setProperty (String key, boolean value){
		setProperty(key, value ? "b1" : "b0");
	}
	
	public void setProperty (String key, int value){
		setProperty(key, "i" + value);
	}
	
	public boolean getBoolean (String key, boolean defaultValue) {
		String p = getProperty(key);
		if(p.charAt(0) == 'b'){
			if(p.charAt(1) == '1') return true;
			else return false;
		}
		return defaultValue;
	}
	
	public int getInteger (String key, int defaultValue) {
		String p = getProperty(key);
		if(p.charAt(0) == 'i'){
			return Integer.valueOf(p.substring(1));
		}
		return defaultValue;
	}
	
}
