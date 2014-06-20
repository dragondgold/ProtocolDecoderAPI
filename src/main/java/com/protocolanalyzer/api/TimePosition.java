package com.protocolanalyzer.api;

public class TimePosition {

	private double[] time = new double[2];
	private String text;
	
	/**
	 * Constructor
	 * @param text String to add
	 * @param startTime initial time in seconds
	 * @param stopTime final time in seconds
	 */
	TimePosition(String text, double startTime, double stopTime){
		time[0] = startTime;
		time[1] = stopTime;
		this.text = text;
	}

	public String getString(){
		return text;
	}
	
	/**
	 * Gets start time in seconds
	 * @return start time in seconds
	 */
	public double startTime(){
		return time[0];
	}
	
	/**
	 * Gets end time in seconds
	 * @return end time in seconds
	 */
	public double endTime(){
		return time[1];
	}
	
}

