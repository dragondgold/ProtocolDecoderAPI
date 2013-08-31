package com.protocolanalyzer.api;

public class EmptyProtocol extends Protocol{

	public EmptyProtocol(long freq) {
		super(freq);
	}

	@Override
	public void decode(double startTime) {
		
	}

	@Override
	public ProtocolType getProtocol() {
		return ProtocolType.NONE;
	}

}
