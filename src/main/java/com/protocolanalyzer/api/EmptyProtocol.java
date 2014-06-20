package com.protocolanalyzer.api;

import com.protocolanalyzer.api.utils.Configuration;

public class EmptyProtocol extends Protocol{

	public EmptyProtocol(long freq, Configuration prop, int id) {
		super(freq, prop, id);
	}

	@Override
	public void setProperties (Configuration prop){
		mProperties = prop;
		invalidateProperties();
	}

	@Override
	public void decode(double startTime) {
		
	}

	@Override
	public ProtocolType getProtocol() {
		return ProtocolType.NONE;
	}
	
	@Override
	public boolean invalidateProperties (){
		return false;
	}

	@Override
	public boolean hasClock() {
		return false;
	}

}
