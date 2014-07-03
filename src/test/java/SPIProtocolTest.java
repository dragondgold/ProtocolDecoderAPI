import com.protocolanalyzer.api.Clock;
import com.protocolanalyzer.api.LogicBitSet;
import com.protocolanalyzer.api.LogicHelper;
import com.protocolanalyzer.api.SPIProtocol;
import org.junit.*;

/** 
* SPIProtocol Tester. 
* 
* @author <Authors name> 
* @since <pre>jun 21, 2014</pre> 
* @version 1.0 
*/ 
public class SPIProtocolTest {

    private static LogicBitSet dataSPI, clkSPI;
    private static SPIProtocol channelSPI;
    private static Clock clockSPI;
    private static boolean showData = true;

    @BeforeClass
    public static void before() throws Exception {
        System.out.println("******************************************");
        System.out.println("*              SPI PROTOCOL              *");
        System.out.println("******************************************");

        channelSPI = new SPIProtocol(400000);
        clockSPI = new Clock(400000);
        channelSPI.setSPIMode(0);

        System.out.println("Parsing...");
        //                                          73              215                 14          3
        dataSPI = LogicHelper.bitParser("100  1100001100001100 1111110011001111 0011111100000000 111100111", 5, 1);
        clkSPI = LogicHelper.bitParser( "000  0101010101010101 0101010101010101 0101010101010101 010101000", 5, 1);

        channelSPI.setChannelBitsData(dataSPI);
        channelSPI.setClockSource(clockSPI);
        clockSPI.setChannelBitsData(clkSPI);

        System.out.println("Parsed! -> " + channelSPI.getChannelBitsData().length() + " bits to decode");
        System.out.println("Clock is " + clockSPI.getCalculatedFrequency() + "Hz +- " + clockSPI.getFrequencyTolerance() + "Hz");
    }

    @AfterClass
    public static void after() throws Exception {
        if(showData) {
            for (int n = 0; n < channelSPI.getDecodedData().size(); ++n) {
                String times = String.format("%.3f - %.3f", channelSPI.getDecodedData().get(n).startTime() * 1000, channelSPI.getDecodedData().get(n).endTime() * 1000);
                System.out.println(channelSPI.getDecodedData().get(n).getString() + " -> " + times + " uS");
            }
        }
    }

    @Test
    public void testDecode() throws Exception {
        channelSPI.decode(0);
    }

} 
