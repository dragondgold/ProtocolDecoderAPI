package com.protocolanalyzer.api.utils;

import java.util.Properties;

public class Configuration extends Properties{

	public void setProperty (String key, boolean value){
		setProperty(key, value ? "b1" : "b0");
	}
	
	public void setProperty (String key, int value){
		setProperty(key, "i" + value);
	}
	
	public boolean getBoolean (String key) throws NoSuchFieldException{
		String p = getProperty(key);
		if(p.charAt(0) == 'b'){
			if(p.charAt(1) == '1') return true;
			else return false;
		}
		throw new NoSuchFieldException("Key: " + key + " no es boolean");
	}
	
	public int getInteger (String key) throws NoSuchFieldException{
		String p = getProperty(key);
		if(p.charAt(0) == 'i'){
			return Integer.valueOf(p.substring(1));
		}
		throw new NoSuchFieldException("Key: " + key + " no es entero (int)");
	}
	
}
