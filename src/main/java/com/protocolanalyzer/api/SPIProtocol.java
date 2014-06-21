package com.protocolanalyzer.api;

import com.protocolanalyzer.api.utils.PrintDebug;

/**
 * Decodes SPI protocol taking the Clock source and one data source (MISO or MOSI).
 * In case we need to decode a Full-Duplex SPI communication we need to decode it twice, one decode for MISO
 *  line and another decode for MOSI line.
 */
public class SPIProtocol extends Protocol {

    private Clock clockSource;
    private boolean CPOL = false;
    private boolean CPHA = true;

    /**
     * @param freq sample frequency
     */
    public SPIProtocol(long freq) {
        super(freq);
    }

    /**
     * Decode data with SPI protocol
     *
     * <pre>
     * {@code
     * The Strings decoded from the {@link com.protocolanalyzer.api.LogicBitSet} are:
     *  "[N]b [number]"    -> [N] is the number of bits of [number] which is the data
     *
     * The incoming data is decoded considering the bits are received starting from the LSB
     * If this is not the case you can use {@link com.protocolanalyzer.api.LogicHelper#reverseBits(int)}
     * }
     * </pre>
     * @param startTime offset of start time
     */
    @Override
    public void decode(double startTime) {
        if(clockSource == null)
            throw new IllegalArgumentException("Clock source must be defined for SPI protocol");

        LogicBitSet clock = clockSource.getChannelBitsData();
        LogicBitSet dataLine = getChannelBitsData();

        // Time between each sample
        final double sampleTime = 1.0d/sampleFrec;
        double t1 = 0, t2;

        int dataInteger = 0;
        int nBits = 0;
        int index = 0, prevIndex = 0;

        // Calculate clock period between two rising edges
        final int clockDuration = clock.nextRisingEdge(clock.nextRisingEdge(0)) - clock.nextRisingEdge(0);

        // We need at least 3 samples otherwise data may be corrupted (low sample rate)
        if( ((double)clockDuration / sampleTime) < 3){
            PrintDebug.printWarning("Low sample rate! Data decoding may not be correct");
        }

        // Capture the data on every rising/falling edge of the clock according to SPI mode
        while ((index = getNextCaptureDataClockIndex(index)) != -1){
            prevIndex = index;
            if(nBits == 0) t1 = index*sampleTime;
            dataInteger = LogicHelper.bitSet(dataInteger, dataLine.get(index), nBits++);

            // Last bit, add decoded data and start again another byte
            if(nBits == 8){
                PrintDebug.printInfo("SPI Byte: " + dataInteger);
                t2 = index*sampleTime;
                addString(nBits + "b " + Integer.toString(dataInteger), t1, t2, startTime);
                nBits = dataInteger = 0;
            }
        }

        // Last byte with the remaining bits
        if(nBits > 0) {
            PrintDebug.printInfo("SPI Byte: " + dataInteger);
            t2 = prevIndex * sampleTime;
            addString(nBits + "b " + Integer.toString(dataInteger), t1, t2, startTime);
        }
    }


    /**
     * Returns the index of the rising/falling edge of the clock where we are supposed to capture the data.
     * This depends on the CPOL and CHPA settings.
     *
     * @param index from where to start searching
     * @return index of the rising/falling edge of the clock
     */
    private int getNextCaptureDataClockIndex(int index){
        // CPOL = 0
        if(!CPOL) {
            if (!CPHA)
                return clockSource.getChannelBitsData().nextRisingEdge(index);
            else
                return clockSource.getChannelBitsData().nextFallingEdge(index);
        }
        else{
            if (!CPHA)
                return clockSource.getChannelBitsData().nextFallingEdge(index);
            else
                return clockSource.getChannelBitsData().nextRisingEdge(index);
        }
    }

    /**
     * Returns the index of the rising/falling edge of the clock where we are supposed to transmit from
     *  MOSI or MISO.
     * This depends on the CPOL and CHPA settings.
     *
     * @param index from where to start searching
     * @return index of the rising/falling edge of the clock
     */
    private int getNextPropagateDataClockIndex(int index){
        // CPOL = 0
        if(!CPOL) {
            if (!CPHA)
                return clockSource.getChannelBitsData().nextFallingEdge(index);
            else
                return clockSource.getChannelBitsData().nextRisingEdge(index);
        }
        else{
            if (!CPHA)
                return clockSource.getChannelBitsData().nextRisingEdge(index);
            else
                return clockSource.getChannelBitsData().nextFallingEdge(index);
        }
    }

    public boolean isCPOL() {
        return CPOL;
    }

    public void setCPOL(boolean CPOL) {
        this.CPOL = CPOL;
    }

    public boolean isCPHA() {
        return CPHA;
    }

    public void setCPHA(boolean CPHA) {
        this.CPHA = CPHA;
    }

    /**
     * Set CPOL and CPHA base on SPI mode
     * @param mode number from 0 to 3
     */
    public void setSPIMode(int mode){
        switch (mode){
            default:
            case 0:
                CPOL = false;
                CPHA = false;
                break;
            case 1:
                CPOL = false;
                CPHA = true;
                break;
            case 2:
                CPOL = true;
                CPHA = false;
                break;
            case 3:
                CPOL = true;
                CPHA = true;
                break;
        }
    }

    /**
     * Get SPI mode based on CPOL and CPHA state
     * @return SPI mode from 0 to 3
     */
    public int getSPIMode(){
        if(!CPOL && !CPHA){
            return 0;
        }else if(!CPOL && CPHA){
            return 1;
        }else if(CPOL && !CPHA){
            return 2;
        }else{
            return 3;
        }
    }

    @Override
    public ProtocolType getProtocol() {
        return ProtocolType.SPI;
    }

    @Override
    public boolean hasClock() {
        return true;
    }

    public Clock getClockSource() {
        return clockSource;
    }

    public void setClockSource(Clock clockSource) {
        this.clockSource = clockSource;
    }
}
