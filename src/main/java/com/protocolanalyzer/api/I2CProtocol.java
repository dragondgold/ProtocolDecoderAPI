package com.protocolanalyzer.api;

import com.protocolanalyzer.api.utils.PrintDebug;

public class I2CProtocol extends Protocol{

	private static final boolean DEBUG = false;
	private Clock clockSource = null;

    /**
     * @param freq sample frequency in Hz
     */
	public I2CProtocol(long freq) {
		super(freq);
	}
	
	/**
	 * Decode {@link com.protocolanalyzer.api.LogicBitSet} put using {@link com.protocolanalyzer.api.Protocol#setChannelBitsData(LogicBitSet)}
	 * @param startTime time offset we wish to add to calculations
     *
     * <pre>
     * {@code
	 * The Strings decoded from the {@link com.protocolanalyzer.api.LogicBitSet} are:
	 *  "S"             -> Start condition
	 *  "Sr"            -> Repeated start condition
	 *  "P"             -> Stop condition
	 *  "\R"            -> Read mode
	 *  "\W"            -> Write mode
	 *  "ACK"           -> ACK bit
	 *  "NAK"           -> NAK bir
	 *  [number]        -> 8 bits data
	 *  "A([number])"   -> I2C address
	 *  "E"             -> Bus ERROR
     * }
     * </pre>
	 * @see <a href="http://www.i2c-bus.org/">www.i2c-bus.org</a>
	 */
	@Override
	public void decode(double startTime) {
            
		if(DEBUG) PrintDebug.printInfo("I2C Protocol decode");
		if(clockSource == null) 
			throw new IllegalArgumentException("Clock source must be defined for I2C protocol");
		
		final LogicBitSet clock = clockSource.getChannelBitsData();

        if(DEBUG) {
            PrintDebug.printInfo("Data length:  "   + logicData.length());
            PrintDebug.printInfo("Clock length: "   + clock.length());
            PrintDebug.printInfo("BitSet data:  "   + logicData.toString());
            PrintDebug.printInfo("BitSet clock: "   + clock.toString());
        }
		
		// Possible finite state machine states
		final int startCondition = 0;
		final int readByte = 1;
		final int readAddress = 2;
		final int stopCondition = 3;
		
		// Initial state machine state
		int i2cState = startCondition;
		// Time between each sample
		final double sampleTime = 1.0d/sampleFrec;

		// SCL clock period
		final int clockDuration;
		int index = 0;					// Samples index
		int[] data;						// Data read from I2C
		boolean rwBit = false;
		boolean ackBit = false;

        // Not enough data, get out of here!
		if(clock.nextRisingEdge(0) == -1 || clock.nextRisingEdge(clock.nextRisingEdge(0)) == -1){
            PrintDebug.printError("Not enough data!");
            return;
        }
		
		// Calculate clock period between two rising edges
		clockDuration = clock.nextRisingEdge(clock.nextRisingEdge(0)) - clock.nextRisingEdge(0);

		// We need at least 3 samples otherwise data may be corrupted (low sample rate)
		if( ((double)clockDuration / sampleTime) < 3){
            PrintDebug.printWarning("Low sample rate! Data decoding may not be correct");
        }
		
		// Decode while we have data
		while(index < logicData.length()) {
			
			// State machine
			switch(i2cState) {
			
			// Start condition
			case startCondition:					
				// Check start condition, if it doesn't exists, get out of here
				int[] start = nextStartCondition(index);
				if(start != null){
					if(DEBUG) PrintDebug.printInfo("Start Condition - index: " + index);
					addString("S", (start[0]*sampleTime), start[1]*sampleTime, startTime);
					index = start[1];
					i2cState = readAddress;
				}
				else return;
				break;
				
			// Stop condition
			case stopCondition:
				data = getStopCondition(index, clockDuration);
				addString("P", data[0]*sampleTime, data[1]*sampleTime, startTime);
				index = data[1];
				i2cState = startCondition;
				break;
				
			// Address
			case readAddress:
				// Read the 7 bits address
				data = readBits(index, 7);
				if(data == null) return;
				try {
					if(DEBUG) PrintDebug.printInfo("I2CAddress: " + Integer.toBinaryString(data[2] & 0xFF) + " -> " + data[2]);
					// Address
					addString("A("+data[2]+")", data[0]*sampleTime,data[1]*sampleTime, startTime);	
		
					// Get RW bit (8th bit)
					index = clock.nextSetBitToTest(data[1]);
					rwBit = logicData.get(index);
						
					if(rwBit) addString("\\R", data[1]*sampleTime, index*sampleTime, startTime);	
					else addString("\\W", data[1]*sampleTime, index*sampleTime, startTime);
						
					// ACK bit
					index = clock.nextSetBitToTest(index);	
					ackBit = logicData.get(index);	
						
					if(!ackBit) addString("ACK", index*sampleTime, (index+clockDuration)*sampleTime, startTime);
					else addString("NAK", index*sampleTime, (index+clockDuration)*sampleTime, startTime);
					
					// If we have a stop condition exit, nothing more to read
					if (existsStopCondition(index, clockDuration)){
						i2cState = stopCondition;
						break;
					}

                    // Check ACK bit, if we got an ACK bit read the incoming data byte, otherwise it's an error
                    //  because we have a NAK but we don't have the stop condition!
					if(!ackBit) i2cState = readByte;
					else {
						i2cState = startCondition;
						addString("E", index*sampleTime, (index+clockDuration)*sampleTime, startTime);
					}
				} catch (IndexOutOfBoundsException e) {
					if(DEBUG) PrintDebug.printError("IndexOutOfBoundsException - Returning");
					return;
				}
				break;
			
			// Leo un byte de dato
			case readByte:		
				if(DEBUG) PrintDebug.printInfo("Read Byte - index: " + index);
				i2cState = startCondition;
				
				data = readBits(index, 8);
				if(data == null) return;
				try{
					if(DEBUG) PrintDebug.printInfo("I2CData: " + Integer.toBinaryString(data[2] & 0xFF) + " -> " + data[2]);
					
					// Byte read
					addString(""+data[2], data[0]*sampleTime, data[1]*sampleTime, startTime);
					
					// ACK bit
					index = clock.nextSetBitToTest(data[1]);
					ackBit = logicData.get(index);
					
					if(!ackBit) addString("ACK", data[1]*sampleTime, index*sampleTime, startTime);
					else addString("NAK", data[1]*sampleTime, index*sampleTime, startTime);

                    // If we have a stop condition exit, nothing more to read
					if (existsStopCondition(index, clockDuration)){
						i2cState = stopCondition;
						break;
					}

                    // Check ACK bit, if we got an ACK bit read another incoming data byte, otherwise it's an error
                    //  because we have a NAK but we don't have the stop condition!
					if(!ackBit) i2cState = readByte;
					else {
						i2cState = startCondition;
						addString("E", index*sampleTime, (index+clockDuration)*sampleTime, startTime);
					}
				} catch (IndexOutOfBoundsException e) {
					if(DEBUG) PrintDebug.printError("IndexOutOfBoundsException - Returning");
					return;
				}
				break;
			}
		}
	}
	
	/**
     * Search for the next start condition after the specified index. High to low transition
     * of SDA line while SCL remains high is considered a Start condition.
     *
	 * @param index from where start searching
	 * @return int[] array where [0] is falling edge in the SDA line and [1] the end of
     * the start condition, otherwise returns null
	 */
	private int[] nextStartCondition (int index){
		if (index < 0) return null;
		// Search until we found SDA and SCL in high state
		for(int n = index; n < logicData.length(); ++n){
			if(logicData.get(n) && clockSource.getChannelBitsData().get(n)){
				index = n;
				break;
			}
		}
		
		int fallIndex = logicData.nextFallingEdge(index);

        // Start condition: falling edge in SDA while SCL is high
		while(fallIndex != -1 && !clockSource.getChannelBitsData().get(fallIndex)){
			fallIndex = logicData.nextFallingEdge(fallIndex);
		}
		if(clockSource.getChannelBitsData().nextFallingEdge(fallIndex) == -1) return null;
		
		return new int[] {fallIndex, clockSource.getChannelBitsData().nextFallingEdge(fallIndex)};
	}
	
	/**
	 * Read nBits from SDA line starting from the MSB
	 * @param index index from where to start reading
	 * @param nBits how many bits to read
	 * @return int[] array where [0] is the starting index, [1] the finishing index and [2] read data
	 */
	private int[] readBits (int index, int nBits){
		int[] i2cData = new int[3];

		// Starting index
		if(clockSource.getChannelBitsData().nextRisingEdge(index) != -1) 
			i2cData[0] = clockSource.getChannelBitsData().nextRisingEdge(index);
		else return null;
		
		// Read nBits starting from the MSB one
		for(int bit = nBits; bit > 0; --bit){			
			index = clockSource.getChannelBitsData().nextSetBitToTest(index);
			if(index == -1) return null;
            // Check SDA bit in the middle of a SCL clock
			i2cData[2] = LogicHelper.bitSet(i2cData[2], logicData.get(index), bit-1);
		}

		// Final index
		i2cData[1] = index;
		return i2cData;
	}
	
	/**
	 * Check if Stop conditions exists. Low to high transition of SDA line while SCL remains
     * high is considered a Stop condition.
     *
	 * @param index from where start searching
	 * @param clockDuration clock period duration in samples
	 * @return true if Stop condition exits, false otherwise
	 */
	private boolean existsStopCondition (int index, int clockDuration){
		int dataRisingEdge = -1;
		
		int clockRisingEdge = clockSource.getChannelBitsData().nextRisingEdge(index);
		if(clockRisingEdge != -1) dataRisingEdge = logicData.nextRisingEdge(clockRisingEdge);
		
		return dataRisingEdge != -1 &&
               clockSource.getChannelBitsData().get(dataRisingEdge+(clockDuration/2)+1) &&
               logicData.get(dataRisingEdge+(clockDuration/2)+1);
	}

    /**
     * Gets the indexes of the Stop condition, null if it doesn't exits.
     *
     * @param index from where start searching
     * @param clockDuration clock period duration in samples
     * @return the indexes of the Stop condition being [0] SCL rising edge and [1] SDA rising edge.
     * Null if Stop condition doesn't exits.
     */
	private int[] getStopCondition (int index, int clockDuration) {
		int dataRisingEdge = -1;
		
		int clockRisingEdge = clockSource.getChannelBitsData().nextRisingEdge(index);
		if(clockRisingEdge != -1) dataRisingEdge = logicData.nextRisingEdge(clockRisingEdge);
		
		if(dataRisingEdge != -1 && clockSource.getChannelBitsData().get(dataRisingEdge+(clockDuration/2)+1)
				&& logicData.get(dataRisingEdge+(clockDuration/2)+1)) 
			return new int[] {clockRisingEdge, dataRisingEdge};
		
		else return null;
	}
	
	@Override
	public ProtocolType getProtocol() {
		return ProtocolType.I2C;
	}
	
	/**
	 * Set I2C clock source
	 * @param channel {@link com.protocolanalyzer.api.Clock}
	 */
	public void setClockSource (Clock channel){
		clockSource = channel;
	}
	
	public Clock getClockSource() {
		return clockSource;
	}

	@Override
	public boolean hasClock() {
		return true;
	}

}
