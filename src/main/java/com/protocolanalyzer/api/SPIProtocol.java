package com.protocolanalyzer.api;

public class SPIProtocol extends Protocol {

    private Clock clockSource;

    /**
     * @param freq sample frequency
     */
    public SPIProtocol(long freq) {
        super(freq);
    }

    @Override
    public void decode(double startTime) {

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
