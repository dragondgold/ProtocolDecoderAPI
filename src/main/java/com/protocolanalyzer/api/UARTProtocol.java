package com.protocolanalyzer.api;

import com.protocolanalyzer.api.utils.Configuration;
import com.protocolanalyzer.api.utils.PrintDebug;

public class UARTProtocol extends Protocol{

	private static final boolean DEBUG = false;
	public static enum Parity{
		Even, Odd, NoParity
	}
	
	private int baudRate = 9600;
	private boolean is9Bits = false;
	private boolean twoStopBits = false;
	private Parity mParity = Parity.NoParity;
	
	/**
	 * Properties that must exists in the {@link com.protocolanalyzer.api.utils.Configuration} object passed.
     *
     * <pre>
     * {@code
	 * "BaudRate" + id  -> Bauds (int)
	 * "nineData" + id  -> Is 9 bits data? (boolean)
	 * "dualStop" + id  -> Is two stop bits? (boolean)
	 * "Parity"   + id  -> Parity as defined in Enum (int) ( ie: Parity.Even.ordinal() )
     * }
     * </pre>
	 * @param freq sample frequency in Hz
	 * @param prop {@link com.protocolanalyzer.api.utils.Configuration} object containing channel settings
	 * @param id channel id
	 */
	public UARTProtocol(long freq, Configuration prop, int id) {
		super(freq, prop, id);
		loadFromProperties();
	}

	@Override
	public void setProperties (Configuration prop){
		mProperties = prop;
		invalidateProperties();
	}
	
	@Override
	public boolean invalidateProperties (){
		return loadFromProperties();
	}
	
	private boolean loadFromProperties(){
		if(mProperties == null) return false;
		
		baudRate = mProperties.getInteger("BaudRate" + mID, baudRate);
		is9Bits = mProperties.getBoolean("nineData" + mID, is9Bits);
		twoStopBits = mProperties.getBoolean("dualStop" + mID, twoStopBits);
		
		int parity = mProperties.getInteger("Parity" + mID, Parity.NoParity.ordinal());
		setParity(Parity.values()[parity]);
		return true;
	}

	/**
	 * Decode data with UART protocol.
     * The Strings decoded from the {@link com.protocolanalyzer.api.LogicBitSet} are:
     *      [S]         -> Start bit
     * 		[number]    -> 8 bits data
     * 		[SP]        -> Stop data
     * 		[SP1]       -> Stop bit 1
     * 		[SP2]       -> Stop bit 2
     * 		[P]	        -> Parity bit, parity correct
     *		[P*]        -> Parity bit, parity incorrect
	 * @param startTime, time offset to be added in seconds
	 */
	@Override
	public void decode(double startTime) {
		loadFromProperties();

        if(DEBUG) {
            PrintDebug.printInfo("UART Protocol decode");
            PrintDebug.printInfo("Data length: "    + logicData.length());
            PrintDebug.printInfo("Data: "           + logicData.toString());
        }
		
		int n = 0;
		int tempIndex;
		boolean parityBit = false;
		final int dataBits;
		final double sampleTime = 1.0d/sampleFrec;					// Time between each sample
		final int samplesPerBit = (int)Math.ceil((1.0d/baudRate) / sampleTime);
		final int halfBit = (int)Math.ceil(samplesPerBit/2.0);		// Time to the middle of the bit
		
		if(is9Bits) dataBits = 9;
		else dataBits = 8;
		
		if(DEBUG) PrintDebug.printInfo("samplesPerBit: "    + samplesPerBit);
		if(DEBUG) PrintDebug.printInfo("halfBit: "          + halfBit);
		
		// Test if we have at least 3 samples per bit
		if( ((1.0d/baudRate) / sampleTime) < 3.0d){
            PrintDebug.printWarning("Low sample rate! Data decoding may not be correct");
        }

        // We need at least 10 bits for a complete UART transmission (samplesPerBit*10). Keep going while we have this
        //  10 bits available
		while(n <= (logicData.length()-(samplesPerBit*10))){
			n = logicData.nextFallingEdge(n); if(n == -1) break;		// Search for falling edge (Start)
			
			if(DEBUG) PrintDebug.printInfo("Falling edge index: " + n);
			
			// Go to middle of the start bit to test it
			n += halfBit;
			if(DEBUG) PrintDebug.printInfo("Start index: " + n);

            // If the next bit is 0 then it's the start bit
			if(!logicData.get(n)){
                // Start bit starting time
				tempIndex = n - halfBit;
				if(DEBUG) PrintDebug.printInfo("Start index of byte: " + n);
				int dataByte = 0;
				
				// Start reading from the LSB
				for(int bit = 0; bit < dataBits; ++bit){
					n += samplesPerBit;
					dataByte = LogicHelper.bitSet(dataByte, logicData.get(n), (dataBits-1) - bit);
				}
				
				// Parity bit
				if(mParity != Parity.NoParity){
					n += samplesPerBit;
					parityBit = logicData.get(n);
				}
				
				if(DEBUG) PrintDebug.printInfo("dataByte: " + Integer.toBinaryString(dataByte) + " - dataByte: " + dataByte);
				n += samplesPerBit;

                // Check if the next bit is the stop bit, otherwise this all is an error, skip it!
				if(logicData.get(n)){
					// Si tengo dos bits de stop compruebo que a continuación este el segundo bit
					if(twoStopBits){
						if(!logicData.get(n + samplesPerBit)) continue;
					}
					if(DEBUG) PrintDebug.printInfo("stopBit index: " + n);
					// Start bit
					addString("[S]", tempIndex*sampleTime, (tempIndex+samplesPerBit)*sampleTime, startTime);
					
					// Data
					addString(""+dataByte, (tempIndex+samplesPerBit)*sampleTime,
							(tempIndex+samplesPerBit+(samplesPerBit*dataBits)*sampleTime), startTime);
					
					// Parity bit
					if(mParity != Parity.NoParity){
						if(checkParity(dataByte, parityBit))
							addString("[P]", (n-halfBit)*sampleTime, (n+halfBit)*sampleTime, startTime);
						else
							addString("[P*]", (n-halfBit)*sampleTime, (n+halfBit)*sampleTime, startTime);
					}
					
					// Stop bit(s)
					if(!twoStopBits) addString("[SP]", (n-halfBit)*sampleTime, (n+halfBit)*sampleTime, startTime);
					else{
						// Bits de stop
						addString("[SP1]", ((n-halfBit)*sampleTime), ((n-halfBit)+samplesPerBit)*sampleTime, startTime);	
						n += samplesPerBit;
						addString("[SP2]", ((n-halfBit)*sampleTime), ((n-halfBit)+samplesPerBit)*sampleTime, startTime);
						continue;
					}
				}
				n -= halfBit;
			}
			if(DEBUG) PrintDebug.printInfo("n before while: " + n);
		}
		if(DEBUG) PrintDebug.printInfo("Exit while()");
		if(DEBUG) PrintDebug.printInfo("Decoded data size: " + mDecodedData.size());
	}

	@Override
	public ProtocolType getProtocol() {
		return ProtocolType.UART;
	}
	
	/**
     * Test if parity matches '1' quantity
     *
	 * @param data data to check
	 * @param parityBit parity bit
	 * @return true if parity matches, false otherwise
	 */
	private boolean checkParity (int data, boolean parityBit){
		int counter = 0;
		for(int n = 0; n < 9; ++n){
			if(LogicHelper.bitTest(data, n)) ++counter;
		}
		
		// Numero IMPAR de '1', paridad par y el bit de paridad es 1
		if(counter % 2 != 0 && mParity == Parity.Even && parityBit) return true;
		// Numero PAR de '1', paridad par y el bit de paridad es 0
		if(counter % 2 == 0 && mParity == Parity.Even && !parityBit) return true;
		
		// Numero IMPAR de '1', paridad impar y el bit de paridad es 0 
		if(counter % 2 != 0 && mParity == Parity.Odd && !parityBit) return true;
		// Numero PAR de '1', paridad impar y el bit de paridad es 1 
		if(counter % 2 == 0 && mParity == Parity.Odd && parityBit) return true;
		
		return false;
	}
	
	/**
	 * Sets UART baud rate
	 * @param baud
	 */
	public void setBaudRate (int baud){
		baudRate = baud;
	}
	
	/**
	 * Get UART baud rate
	 * @return UART baud rate. Default 9600.
	 */
	public int getBaudRate() {
		return baudRate;
	}
	
	/**
	 * Sets 9 bit transmission mode
	 */
	public void set9BitsMode (boolean state){
		is9Bits = state;
		if(state) mParity = Parity.NoParity;
	}
	
	public boolean is9BitsMode (){
		return is9Bits;
	}
	
	/**
	 * Set UART parity
	 * @param mParity {@link com.protocolanalyzer.api.UARTProtocol.Parity} enum
	 */
	public void setParity (Parity mParity){
		this.mParity = mParity;
	}
	
	public Parity getParity (){
		return mParity;
	}
	
	/**
	 * Set whether to use two stop bits instead of one
	 */
	public void setTwoStopBits (boolean twoStopBits){
		this.twoStopBits = twoStopBits;
	}
	
	public boolean isTwoStopBits (){
		return twoStopBits;
	}

	@Override
	public boolean hasClock() {
		return false;
	}
}
