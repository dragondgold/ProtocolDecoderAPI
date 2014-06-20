package test.com.protocolanalyzer.api; 

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.protocolanalyzer.api.Clock;
import com.protocolanalyzer.api.I2CProtocol;
import com.protocolanalyzer.api.LogicBitSet;
import com.protocolanalyzer.api.LogicHelper;
import org.junit.*;

/** 
* I2CProtocol Tester. 
* 
* @author <Authors name> 
* @since <pre>jun 20, 2014</pre> 
* @version 1.0 
*/ 
public class I2CProtocolTest extends AbstractBenchmark {

    private static LogicBitSet dataI2C, clkI2C;
    private static I2CProtocol channelI2C;
    private static Clock clockI2C;
    private static boolean showData = false;

    @BeforeClass
    public static void before() throws Exception {
        System.out.println("******************************************");
        System.out.println("*              I2C PROTOCOL              *");
        System.out.println("******************************************");

        channelI2C = new I2CProtocol(400000);
        clockI2C = new Clock(400000);

        System.out.println("Parsing...");
        //								  S		  Address        A 		  Byte		  A  	   Byte       A   ST
        dataI2C = LogicHelper.bitParser("100  11010010011100101  0  11010011110000111 0 11010011110000111 1  0011", 5, 230);
        clkI2C = LogicHelper.bitParser( "110  01010101010101010  1  01010101010101010 1 01010101010101010 1  0111", 5, 230);

        channelI2C.setChannelBitsData(dataI2C);
        channelI2C.setClockSource(clockI2C);
        clockI2C.setChannelBitsData(clkI2C);

        System.out.println("Parsed! -> " + channelI2C.getChannelBitsData().length() + " bits to decode");
    }

    @AfterClass
    public static void after() throws Exception {
        if(showData) {
            for (int n = 0; n < channelI2C.getDecodedData().size(); ++n) {
                System.out.println(channelI2C.getDecodedData().get(n).getString() + " -> " + String.format("%.3f", channelI2C.getDecodedData().get(n).startTime() * 1000) + " uS");
            }
        }
    }

    @BenchmarkOptions(benchmarkRounds = 200, warmupRounds = 5)
    @Test
    public void testDecode() throws Exception {
        channelI2C.decode(0);
    }

} 
