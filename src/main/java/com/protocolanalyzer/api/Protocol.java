package com.protocolanalyzer.api;

import java.util.ArrayList;
import java.util.List;

public abstract class Protocol {

    /**
     * Protocol types
     */
	public enum ProtocolType {
		I2C(1), UART(2), CLOCK(3), SPI(4), NONE(-1);
		
		private final int value;
		private ProtocolType(int value){
			this.value = value;
		}
		
		public int getValue(){
			return value;
		}
	}

    /** Decoded data containing a list of {@link com.protocolanalyzer.api.TimePosition} containing a String with an start
     *   and end time */
	protected List<TimePosition> mDecodedData = new ArrayList<TimePosition>();
	/** Bits to be decoded */
	protected LogicBitSet logicData = new LogicBitSet();
	/** Sample rate */
	protected long sampleFrec = 0;
	
	/**
	 * Decode data in {@link com.protocolanalyzer.api.Protocol#logicData}
	 * @param startTime offset of start time
	 */
	public abstract void decode(double startTime);

	/**
	 * Protocol type
	 * @return {@link com.protocolanalyzer.api.Protocol.ProtocolType} enum
	 */
	public abstract ProtocolType getProtocol();
	
	/**
	 * Whether or not protocol needs a clock source
	 * @return true if it needs clock source, false otherwise
	 */
	public abstract boolean hasClock();

    /**
     * @param freq sample frequency
     */
	public Protocol (long freq){
		sampleFrec = freq;
	}
	
	/**
	 * Get {@link com.protocolanalyzer.api.LogicBitSet} containing the bits to be decoded
	 * @return {@link com.protocolanalyzer.api.LogicBitSet} containing the bits to be decoded
	 */
	public LogicBitSet getChannelBitsData (){
		return logicData;
	}

	public void setChannelBitsData (LogicBitSet data){
		logicData = data;
	}

	public List<TimePosition> getDecodedData() {
		return mDecodedData;
	}

	public void setSampleFrequency (long freq){
		sampleFrec = freq;
	}

	public long getSampleFrequency(){
		return sampleFrec;
	}

    /**
     * Gets the number of bits to be decoded
     * @return number of bits to be decoded
     */
	public int getBitsNumber(){
		return logicData.length();
	}
	
	/**
	 * Clear decoded data and bits to be decoded
	 */
	public void reset(){
		mDecodedData.clear();
		logicData.clear();
	}
	
	/**
     * Adds a String in the given position adding init time as offset in seconds
	 * @param text {@link java.lang.String} to add
	 * @param startTime start time in seconds
	 * @param stopTime end time in seconds
	 * @param initTime time offset in seconds
	 */
	public void addString (String text, double startTime, double stopTime, double initTime){
		if(stopTime >= startTime){
			mDecodedData.add(new TimePosition(text, startTime+initTime, stopTime+initTime));
		}else{
			mDecodedData.add(new TimePosition(text, startTime+initTime, startTime+initTime));
		}
	}
}
