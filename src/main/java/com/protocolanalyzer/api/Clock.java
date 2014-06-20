package com.protocolanalyzer.api;

import com.protocolanalyzer.api.utils.Configuration;

public class Clock extends Protocol{

	public Clock(long freq, Configuration prop, int id) {
		super(freq, prop, id);
	}

	@Override
	public void decode(double startTime) {
	}

	@Override
	public ProtocolType getProtocol() {
		return ProtocolType.CLOCK;
	}

	@Override
	public void setProperties (Configuration prop){
		mProperties = prop;
		invalidateProperties();
	}
	
	@Override
	public boolean invalidateProperties (){
		return false;
	}
	
	/**
	 * Gets clock frequency base on the sample rate
	 * @return clock frequency, -1 if was not possible to calculate it
	 */
	public int getCalculatedFrequency (){
		int firstEdge, secondEdge;
		
		firstEdge = logicData.nextRisingEdge(0);
		if(firstEdge != -1) secondEdge = logicData.nextRisingEdge(firstEdge);
		else return -1;
		
		if(secondEdge != -1){
			return (int)(1/((secondEdge - firstEdge) * 1.0d/sampleFrec));
		}
		else return -1;
	}
	
	/**
	 * Get calculated frequency tolerance due to sample rate time
	 * @return deviation from the real frequency in +-x Hz
	 */
	public float getFrequencyTolerance (){
		int firstEdge, secondEdge;
		double firstEdgeTime, secondEdgeTime;
		
		firstEdge = logicData.nextRisingEdge(0);
		if(firstEdge != -1) secondEdge = logicData.nextRisingEdge(firstEdge);
		else return -1;
		
		if(secondEdge != -1){
            // Convert indexes to time and add one sample time (tolerance)
			firstEdgeTime = firstEdge * 1.0d/sampleFrec;
			secondEdgeTime = secondEdge * 1.0d/sampleFrec;
			secondEdgeTime += 1.0d/sampleFrec;

            // Get the difference between calculated frequency and the one where we added a sample time
            //  to obtain the tolerance
			return (float)(getCalculatedFrequency() - (1.0d/(secondEdgeTime - firstEdgeTime)));
		}
		else return -1;
	}

	@Override
	public boolean hasClock() {
		return false;
	}

}
