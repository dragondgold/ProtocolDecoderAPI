package test.com.protocolanalyzer.api; 

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.protocolanalyzer.api.LogicBitSet;
import com.protocolanalyzer.api.LogicHelper;
import com.protocolanalyzer.api.UARTProtocol;
import com.protocolanalyzer.api.utils.Configuration;
import org.junit.*;

/** 
* UARTProtocol Tester. 
* 
* @author <Authors name> 
* @since <pre>jun 20, 2014</pre> 
* @version 1.0 
*/ 
public class UARTProtocolTest extends AbstractBenchmark {

    private static LogicBitSet data;
    private static UARTProtocol channelUART;
    private static boolean showData = false;

    @BeforeClass
    public static void before() throws Exception {
        System.out.println("******************************************");
        System.out.println("*             UART PROTOCOL              *");
        System.out.println("******************************************");

        Configuration config = new Configuration();
        config.setProperty("BaudRate0", 9600);
        config.setProperty("nineData0", false);
        config.setProperty("dualStop0", false);
        config.setProperty("Parity0", UARTProtocol.Parity.NoParity.ordinal());

        channelUART = new UARTProtocol(200000, config, 0);

        System.out.println("Parsing");

        data = LogicHelper.bitParser("110110101011", 21, 300);

        channelUART.setChannelBitsData(data);
        System.out.println("Parsed! -> " + channelUART.getChannelBitsData().length() + " bits to decode");
    }

    @Ignore
    @AfterClass
    public static void after() throws Exception {
        if(showData) {
            for (int n = 0; n < channelUART.getDecodedData().size(); ++n) {
                System.out.println(channelUART.getDecodedData().get(n).getString() + " -> " + String.format("%.3f", channelUART.getDecodedData().get(n).startTime() * 1000) + " uS");
            }
        }
    }

    @BenchmarkOptions(benchmarkRounds = 200, warmupRounds = 5)
    @Test
    public void testDecode() throws Exception {
        channelUART.decode(0);
    }

} 
