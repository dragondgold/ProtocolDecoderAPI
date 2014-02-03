package com.protocolanalyzer.api;

import com.protocolanalyzer.api.utils.Configuration;

public class Clock extends Protocol{

	public Clock(long freq, Configuration prop, int id) {
		super(freq, prop, id);
	}

	@Override
	public void decode(double startTime) {
	}

	@Override
	public ProtocolType getProtocol() {
		return ProtocolType.CLOCK;
	}
	
	/**
	 * Define las propiedades del canal
	 * @param prop
	 */
	@Override
	public void setProperties (Configuration prop){
		mProperties = prop;
		invalidateProperties();
	}
	
	@Override
	public boolean invalidateProperties (){
		return false;
	}
	
	/**
	 * Obtiene la frecuencia de clock en Hz calculada en base al tiempo de muestreo.
	 * @return frecuencia del clock; -1 si no se pudo calcular.
	 */
	public int getCalculatedFrequency (){
		// Resto entre dos flancos de subida (que me asegure antes que hubiera)
		// para calcular la frecuencia del clock
		int firstEdge, secondEdge;
		
		firstEdge = logicData.nextRisingEdge(0);
		if(firstEdge != -1) secondEdge = logicData.nextRisingEdge(firstEdge);
		else return -1;
		
		if(secondEdge != -1){
			return (int)(1/((secondEdge - firstEdge) * 1.0d/sampleFrec));
		}
		else return -1;
	}
	
	/**
	 * Obtiene la tolerancia que tiene la frecuencia calculada debido a la frecuencia de muestreo
	 * @return
	 */
	public float getFrequencyTolerance (){
		// Resto entre dos flancos de subida (que me asegure antes que hubiera)
		// para calcular la frecuencia del clock
		int firstEdge, secondEdge;
		double firstEdgeTime, secondEdgeTime;
		
		firstEdge = logicData.nextRisingEdge(0);
		if(firstEdge != -1) secondEdge = logicData.nextRisingEdge(firstEdge);
		else return -1;
		
		if(secondEdge != -1){
			// Convierto los edges en tiempo y le sumo a uno un tiempo de muestreo (tolerancia)
			firstEdgeTime = firstEdge * 1.0d/sampleFrec;
			secondEdgeTime = secondEdge * 1.0d/sampleFrec;
			secondEdgeTime += 1.0d/sampleFrec;
			
			// Resto entre la frecuencia calculada y a la que le sume el tiempo para obtener la tolerancia
			return (float)(getCalculatedFrequency() - (1.0d/(secondEdgeTime - firstEdgeTime)));
		}
		else return -1;
	}

	@Override
	public boolean hasClock() {
		return false;
	}

}
