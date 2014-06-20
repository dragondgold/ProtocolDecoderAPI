package com.protocolanalyzer.api;

import java.util.BitSet;

/**
 * Esta clase es una extensión de la clase BitSet. Agrega funcionalidades como b'usqueda de flancos de subida
 * y bajada y la mitad de un bit en alto
 */
public class LogicBitSet extends BitSet{

	private static final long serialVersionUID = 1L;
	private int currentSize;

	@Override
	public void set(int index, boolean state) {
		if(index > currentSize) currentSize = index;
		super.set(index, state);
	}

	@Override
	public void set(int index) {
		if(index > currentSize) currentSize = index;
		super.set(index);
	}
	
	@Override
	public void set(int fromIndex, int toIndex, boolean state) {
		if(toIndex > currentSize) currentSize = toIndex;
		super.set(fromIndex, toIndex, state);
	}

	@Override
	public void set(int fromIndex, int toIndex) {
		if(toIndex > currentSize) currentSize = toIndex;
		super.set(fromIndex, toIndex);
	}

	@Override
	public void clear() {
		currentSize = 0;
		super.clear();
	}

	@Override
	public void clear(int index) {
		if(index > currentSize) currentSize = index;
		super.clear(index);
	}
	
	@Override
	public void clear(int fromIndex, int toIndex) {
		if(toIndex > currentSize) currentSize = toIndex;
		super.clear(fromIndex, toIndex);
	}

	/**
     * Index of the last bit that was set to '1' or '0'
	 */
	@Override
	public int length() {
		return currentSize;
	}

	public LogicBitSet(){
		super();
		currentSize = 0;
	}

	public LogicBitSet(int size){
		super(size);
	}
	
	/**
     * Search for the next falling edge starting in the given index
	 * @param index where to start searching
	 * @return falling edge index where it's already '0', -1 if no falling edge exists
	 */
	public int nextFallingEdge(int index){
		if(index >= 0) {
			int t = super.nextSetBit(index);
			if(t != -1) return super.nextClearBit(t);
		}
		return -1;
	}
	
	/**
     * Search for the next rising edge starting in the given index
     * @param index where to start searching
     * @return rising edge index where it's already '1', -1 if no rising edge exists
	 */
	public int nextRisingEdge(int index){
		if(index >= 0) {
			return super.nextSetBit( super.nextClearBit(index) );
		}
		return -1;
	}
	
	/**
     * Search for the next set bit and returns the index in the middle of it. It is
     *  intended for usage with clock signal so we get into the middle of the clock
     *  pulse.
     *
	 * @param index where to start searching
	 * @return bit index in the middle of it, -1 if it doesn't exist
	 */
	public int nextSetBitToTest(int index) {
		int rising = nextRisingEdge(index);
		int fall = nextFallingEdge(rising);
		
		// Test if valid
		if(rising == -1 || fall == -1) return -1;
				
		return ( rising + ((fall - rising)/2) );
	}

}
