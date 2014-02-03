/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.test.api;

import java.util.Scanner;

import com.protocolanalyzer.api.Clock;
import com.protocolanalyzer.api.I2CProtocol;
import com.protocolanalyzer.api.LogicBitSet;
import com.protocolanalyzer.api.LogicHelper;
import com.protocolanalyzer.api.Protocol.ProtocolType;
import com.protocolanalyzer.api.UARTProtocol.Parity;
import com.protocolanalyzer.api.UARTProtocol;
import com.protocolanalyzer.api.utils.Configuration;

public class PruebaParser {
    
    public static void main(String[] args){
    	ProtocolType mType = ProtocolType.NONE;
        Scanner inputScanner = new Scanner(System.in);
        long start, end;
        
        // Listado de protocolos para elejir
        System.out.println("Ingrese el tipo de protocolo: ");
        for(ProtocolType mProtocolType : ProtocolType.values()){
        	if(mProtocolType != ProtocolType.NONE && mProtocolType != ProtocolType.CLOCK)
        		System.out.println(" - " + mProtocolType.toString());
        }
        
        // Leo el protocolo
        while(mType == ProtocolType.NONE){
        	try {
        		mType = ProtocolType.valueOf(inputScanner.next().toUpperCase());
			} catch (IllegalArgumentException e) {
				System.out.println("No existe el protocolo vuelva a intentar");
			}
        }

        if(mType == ProtocolType.UART){
        	System.out.println("******************************************");
        	System.out.println("*             UART PROTOCOL              *");
        	System.out.println("******************************************");
        	
            LogicBitSet data;
            Configuration config = new Configuration();
            config.setProperty("BaudRate0", 9600);
            config.setProperty("nineData0", false);
            config.setProperty("dualStop0", false);
            config.setProperty("Parity0", Parity.NoParity.ordinal());
            
            UARTProtocol channelUART = new UARTProtocol(200000, config, 0);

            System.out.println("Parser - Parsing");
            data = LogicHelper.bitParser("110110101011", 21, 1);
            channelUART.setChannelBitsData(data);	// Bits
            
            /*
            channelUART.setBaudRate(9600);			// 9600 Baudios
            channelUART.set9BitsMode(false);
            channelUART.setTwoStopBits(true);
            channelUART.setParity(UARTProtocol.Parity.NoParity);*/

            start = System.currentTimeMillis();
            System.out.println("Parser - Decoding");
            channelUART.decode(0);
            System.out.println("Parser - Decoded");	
            end = System.currentTimeMillis();

            System.out.println(channelUART.getDecodedData().size() + " decoded data");
            for(int n = 0; n < channelUART.getDecodedData().size(); ++n){
                System.out.println("Parser - String " + n + ": " + channelUART.getDecodedData().get(n).getString());
                System.out.println("Parser - String " + n + " position: " + channelUART.getDecodedData().get(n).startTime());
            }
            System.out.println("******************************************");
        }
        
        else if(mType == ProtocolType.I2C){
        	System.out.println("******************************************");
        	System.out.println("*              I2C PROTOCOL              *");
        	System.out.println("******************************************");
        	
            LogicBitSet dataI2C, clkI2C;

            I2CProtocol channelI2C = new I2CProtocol(400000, null, 0);
            Clock clockI2C = new Clock(400000, null, 0);

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
            System.out.println("******************************************");
        }
        inputScanner.close();
    }
}
