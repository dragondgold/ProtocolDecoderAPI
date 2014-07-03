package com.protocolanalyzer.api;

import com.protocolanalyzer.api.utils.PrintDebug;

public class OneWireProtocol extends Protocol{

    private final double TIME_SLOT = 60E-6;

    private final double RESET_PULSE = 480E-6;
    private final double PRESENCE_PULSE = 60E-6;

    private final double BIT_1_LOW_TIME_MIN = 1E-6;
    private final double BIT_1_LOW_TIME_MAX = 15E-6;
    private final double BIT_0_LOW_TIME = 60E-6;

    private final double MASTER_READ_REQUEST_MIN = 1E-6;
    private final double MASTER_READ_REQUEST_MAX = 15E-6;

    /**
     * @param freq sample frequency
     */
    public OneWireProtocol(long freq) {
        super(freq);
    }

    /**
     * Decode 1-Wire protocol
     *
     * <pre>
     * {@code
     * The Strings decoded from the {@link com.protocolanalyzer.api.LogicBitSet} are:
     *      M([number])     -> M indicates that [number] is being transmitted by the master
     *      S([number])     -> S indicates that [number] is being transmitted by a slave device
     * }
     * </pre>
     *
     * @see <a href="http://en.wikipedia.org/wiki/1-Wire">Wikipedia 1-Wire</a>
     * @param startTime offset of start time
     */
    @Override
    public void decode(double startTime) {

        // TODO

    }

    /**
     * Reads a byte from 1-Wire. We should ensure start and presence pulses already happened
     * @param index from where to start searching
     * @return int[] array being [0] the read byte, [1] the start of the event and [2] the end
     * of the event. null if byte can't be completed or doesn't exist.
     */
    private int[] readByte(int index){
        final LogicBitSet line = getChannelBitsData();
        final double sampleTime = 1.0d/getSampleFrequency();

        byte data = 0;
        int firstEdge = 0, lastEdge = 0;

        try {
            // Read 8 bits starting from the LSB
            for(int n = 0; n < 8; ++n){
                int fallingEdge = line.nextFallingEdge(index);
                int risingEdge = line.nextRisingEdge(fallingEdge);

                if(fallingEdge < 0 || risingEdge < 0) return null;

                if(n == 0) firstEdge = fallingEdge;
                else if(n == 7) lastEdge = line.nextFallingEdge(risingEdge);

                double lowTime = (risingEdge-fallingEdge)*sampleTime;

                // Low pulse from 1us to 15uS should be '1' bit
                if(lowTime >= BIT_1_LOW_TIME_MIN && lowTime <= BIT_1_LOW_TIME_MAX){
                    int secondFallingEdge = line.nextFallingEdge(risingEdge);
                    double highTime = (secondFallingEdge-risingEdge)*sampleTime;

                    // The time slot must be at least 60uS
                    if(highTime+lowTime >= TIME_SLOT){
                        data = LogicHelper.bitSet(data, true, n);
                    }else{
                        // TODO: do something when timing is not met
                    }
                }
                // Low pulse for at least 60uS is '0' bit
                else if(lowTime >= BIT_0_LOW_TIME){
                    data = LogicHelper.bitSet(data, false, n);
                }else {
                    // TODO: do something when timing is not met
                }
            }
        }catch (IndexOutOfBoundsException e){
            PrintDebug.printError("IndexOutOfBoundsException - Returning");
            return null;
        }

        return new int[]{data, firstEdge, lastEdge};
    }

    /**
     * Searches for a presence pulse
     * @param index from where to start searching
     * @return int[] array being [0] the start of the event and [1] the end of it. null if presence
     * pulse doesn't exist
     */
    private int[] getPresencePulse(int index){
        final LogicBitSet line = getChannelBitsData();
        final double sampleTime = 1.0d/getSampleFrequency();

        int fallingEdge = line.nextFallingEdge(index);
        int risingEdge = line.nextRisingEdge(fallingEdge);

        // Low pulse for at least 60uS is a reset pulse
        if((risingEdge-fallingEdge)*sampleTime >= PRESENCE_PULSE){
            return new int[]{ fallingEdge, risingEdge };
        }

        return null;
    }

    /**
     * Searches for a reset condition
     * @param index from where to start searching
     * @return int[] array being [0] the start of the event and [1] the end of it. null if reset
     * condition doesn't exist
     */
    private int[] getResetCondition(int index){
        final LogicBitSet line = getChannelBitsData();
        final double sampleTime = 1.0d/getSampleFrequency();

        int fallingEdge = line.nextFallingEdge(index);
        int risingEdge = line.nextRisingEdge(fallingEdge);

        // Low pulse for at least 480uS is a reset pulse
        if((risingEdge-fallingEdge)*sampleTime >= RESET_PULSE){
            return new int[]{ fallingEdge, risingEdge };
        }

        return null;
    }

    @Override
    public ProtocolType getProtocol() {
        return ProtocolType.ONEWIRE;
    }

    @Override
    public boolean hasClock() {
        return false;
    }
}
