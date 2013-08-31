/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.test.api;

import com.protocolanalyzer.api.Clock;
import com.protocolanalyzer.api.I2CProtocol;
import com.protocolanalyzer.api.LogicBitSet;
import com.protocolanalyzer.api.LogicHelper;
import com.protocolanalyzer.api.Protocol;
import com.protocolanalyzer.api.UARTProtocol;

/**
 *
 * @author andres
 */
public class PruebaParser {
    
    public static void main(String[] args){
        final Protocol.ProtocolType mType = Protocol.ProtocolType.I2C;
        long start, end;

        if(mType == Protocol.ProtocolType.UART){
            LogicBitSet data;
            UARTProtocol channelUART = new UARTProtocol(200000);

            System.out.println("Parser - Parsing");
            data = LogicHelper.bitParser("1101101010011", 21, 1);

            channelUART.setBaudRate(9600);			// 9600 Baudios
            channelUART.setChannelBitsData(data);	// Bits
            channelUART.set9BitsMode(false);

            start = System.currentTimeMillis();
            System.out.println("Parser - Decoding");
            channelUART.decode(0);
            System.out.println("Parser - Decoded");	
            end = System.currentTimeMillis();

            System.out.println("Parser - Data decoded in: " + (end-start) + " mS");
            for(int n = 0; n < channelUART.getDecodedData().size(); ++n){
                System.out.println("Parser - String " + n + ": " + channelUART.getDecodedData().get(n).getString());
                System.out.println("Parser - String " + n + " position: " + channelUART.getDecodedData().get(n).startTime());
            }
        }
        else if(mType == Protocol.ProtocolType.I2C){
            LogicBitSet dataI2C, clkI2C;

            I2CProtocol channelI2C = new I2CProtocol(400000);
            Clock clockI2C = new Clock(400000);

            System.out.println("Parser - Parsing");
            //								  S		  Address        A 		  Byte		  A  	   Byte       A   ST
            dataI2C = LogicHelper.bitParser("100  11010010011100101  0  11010011110000111 0 11010011110000111 1  0011", 5, 2);
            clkI2C = LogicHelper.bitParser( "110  01010101010101010  1  01010101010101010 1 01010101010101010 1  0111", 5, 2);

            System.out.println("Parser - Data: " + dataI2C.toString());
            System.out.println("Parser - Clock: " + clkI2C.toString());

            channelI2C.setChannelBitsData(dataI2C);
            channelI2C.setClockSource(clockI2C);
            clockI2C.setChannelBitsData(clkI2C);

            System.out.println("Parser - Parsed");	

            start = System.currentTimeMillis();
            System.out.println("Parser - Decoding");
            channelI2C.decode(0);
            System.out.println("Parser - Decoded");	
            end = System.currentTimeMillis();

            for(int n = 0; n < channelI2C.getDecodedData().size(); ++n){
                System.out.println("Parser - String " + n + ": " + channelI2C.getDecodedData().get(n).getString());
                System.out.println("Parser - String " + n + " position: " + String.format("%.3f", channelI2C.getDecodedData().get(n).startTime()*1000) + " uS");
            }
            System.out.println("Parser - Data Decoded in " + (end-start) + " mS");
        }
    }
}
