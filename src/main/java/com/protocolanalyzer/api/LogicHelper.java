package com.protocolanalyzer.api;

import com.protocolanalyzer.api.utils.ByteArrayBuffer;

public class LogicHelper {
    
	private static final boolean DEBUG = true;
                
	/**
	 * Test bit inside a byte
	 * @param a byte to test
	 * @param bit bit number to test from 0 to 7
	 * @see <a href="http://en.wikipedia.org/wiki/Mask_(computing)">Wikipedia bit masking</a>
	 */
	public static boolean bitTest (byte a, int bit) {
		return (a & (1 << bit)) != 0;
	}
	
	/**
	 * Test bit inside 32 bit integer
	 * @param a 32 bit integer to test
	 * @param bit bit number to test from 0 to 31
     * @see <a href="http://en.wikipedia.org/wiki/Mask_(computing)">Wikipedia bit masking</a>
	 */
	public static boolean bitTest (int a, int bit) {
		return (a & (1 << bit)) != 0;
	}
	
	/**
	 * Set bit inside a byte to the given state
	 * @param a byte to modify
	 * @param state true or false
	 * @param bit bit number to modify from 0 to 7
	 * @return modified byte
     * @see <a href="http://en.wikipedia.org/wiki/Mask_(computing)">Wikipedia bit masking</a>
	 */
	public static byte bitSet (byte a, boolean state, int bit){
		if(bit < 0) return a;
		if(state) return (byte)(a | (1 << bit));
		else return (byte)(a & ~(1 << bit));
	}
	
	/**
	 * Set bit inside a 32 bit integer to the given state
	 * @param a 32 bit integer to modify
	 * @param state true or false
	 * @param bit bit number to modify from 0 to 31
	 * @return modified 32 bit integer
     * @see <a href="http://en.wikipedia.org/wiki/Mask_(computing)">Wikipedia bit masking</a>
	 */
	public static int bitSet (int a, boolean state, int bit){
		if(bit < 0) return a;
		if(state == true) return (a | (1 << bit));
		else return (a & ~(1 << bit));
	}
	
	/**
     * Bit parser which generates '1' and '0' according to the passed String
	 * @param data String with '1' and '0' characters, other characters are ignored
	 * @param samplesPerBit number of bits for each '1' or '0'
	 * @param times how many times to repeat the given sequence. This used to create larger sequences
	 * @return {@link com.protocolanalyzer.api.LogicBitSet} containing the bits
	 */
	public static LogicBitSet bitParser (final String data, final int samplesPerBit, final int times){
		LogicBitSet bitSet = new LogicBitSet();
		
		if(DEBUG) System.out.println("BitParse - samplesPerBit: " + samplesPerBit);
		if(DEBUG) System.out.println("BitParse - String: " + data);
		if(DEBUG) System.out.println("BitParse - String Length: " + data.length());
		
		int bitPosition = 0;
		
		for(int l = 0; l < times; ++l){
			for(int n = 0; n < data.length(); ++n){
				if(data.charAt(n) == '1'){
					for(int t = 0; t < samplesPerBit; ++t){
						bitSet.set(bitPosition++);
					}
				}
				else if(data.charAt(n) == '0'){
					for(int t = 0; t < samplesPerBit; ++t){
						bitSet.clear(bitPosition++);
					}
				}
			}
		}
		if(DEBUG) System.out.println("BitParse - BitSet length: " + bitSet.length());
		return bitSet;
	}
	
	/**
	 * Converts two bytes to a 16 bit integer
	 * @param LSB LSB byte
	 * @param MSB MSB byte
	 * @return integer
	 */
	public static int byteToInt (final byte LSB, final byte MSB){
		int temp = 0;
	    temp = temp | (MSB & 0xFF);		// Coloco el MSB
	    temp <<= 8;    					// Desplazo el byte
	    temp = temp | (LSB & 0xFF);  	// Coloco el LSB
	    return temp;
	}
	
	/**
	 * Decodes the Run Length Algorithm
	 * @param data {@link com.protocolanalyzer.api.utils.ByteArrayBuffer} with the compressed data
	 * @return byte[] array containing the decompressed data
	 */
	public static byte[] runLengthDecode(final ByteArrayBuffer data){
		
		int length = data.length();
		int repeat;
		ByteArrayBuffer returnData = new ByteArrayBuffer(data.length());
		
		for(int n = 0; n < length; n += 3){
			repeat = LogicHelper.byteToInt((byte)data.byteAt(n), (byte)data.byteAt(n+1));
			for(int k = 0; k < repeat; ++k){
				returnData.append(data.byteAt(n+2));
			}
		}
		
		return returnData.toByteArray();
	}
	
	/**
     * Copy a byte buffer to each {@link com.protocolanalyzer.api.Protocol}
	 * @param data byte[] array containing the data from each channel being bit 0 the data from channel 0
     *             to bit 7 the data from channel 7
	 */
	 public static void bufferToChannel (final byte[] data, Protocol[] list) {
		
		if(DEBUG) System.out.println("LogicHelper - Lenght data array: " + data.length);

         for (Protocol aList : list) aList.getChannelBitsData().clear();
		
		for(int n=0; n < data.length; ++n){						// Go through received bytes
			for(int bit=0; bit < list.length; ++bit){			// Go through each channel
				if(LogicHelper.bitTest(data[n], bit)){			// Bit is 1
					list[bit].getChannelBitsData().set(n);		// bit is the channel number
				}
				else{											// Bit is 0
					list[bit].getChannelBitsData().clear(n);
				}
			}
		}
	}
	 
	/**
	 * Adds a byte buffer to each {@link com.protocolanalyzer.api.Protocol}
	 * @param data byte[] array containing the data from each channel being bit 0 the data from channel 0
     *             to bit 7 the data from channel 7
	 */
	 public static void addBufferToChannel (final byte[] data, Protocol[] list) {
		
		if(DEBUG) System.out.println("LogicHelper - Lenght data array: " + data.length);
		if(DEBUG) System.out.println("LogicHelper - Lenght BitSet: " + list[0].getChannelBitsData().length());
		int initialLength = list[0].getBitsNumber()+1;

		for(int n=initialLength; n < (initialLength+data.length); ++n){		// Go through received bytes
			for(int bit=0; bit < list.length; ++bit){			            // Go through each channel
				if(LogicHelper.bitTest(data[n], bit)){			            // Bit is 1
					list[bit].getChannelBitsData().set(n);		            // bit is the channel number
				}
				else{											            // Bit is 0
					list[bit].getChannelBitsData().clear(n);
				}
			}
		}
	}

    /**
     * Reverse the bits in the given 32 bit integer but only considering nBits as the
     *  variable length. For examples, turns 1101 to 1011
     * @param data data to be reversed
     * @param nBits number of bits of the data
     * @return integer with reversed bits
     */
     public static int reverseBits(int data, int nBits){
         int reversed = 0;

         while (data != 0 && nBits > 0){
             reversed <<= 1;
             reversed |=  data & 1;
             data >>= 1;
             --nBits;
         }

         return reversed;
     }
}
