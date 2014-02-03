package com.protocolanalyzer.api;

import java.util.ArrayList;
import java.util.List;

import com.protocolanalyzer.api.utils.Configuration;

public abstract class Protocol {
	
	/**
	 * Enumeracion con los tipos de protocolo
	 * @author andres
	 */
	public enum ProtocolType {
		I2C(1), UART(2), CLOCK(3), NONE(-1);
		
		private final int value;
		private ProtocolType(int value){
			this.value = value;
		}
		
		public int getValue(){
			return value;
		}
	}
	
	/** Contiene un String con las posiciones iniciales y finales del mismo en el tiempo */
	protected List<TimePosition> mDecodedData = new ArrayList<TimePosition>();
	/** Bits para ser decodificados */
	protected LogicBitSet logicData = new LogicBitSet();
	/** Velocidad de muestreo con la que se tomo el canal */
	protected long sampleFrec = 0;
	/** Propiedades del canal */
	protected Configuration mProperties;
	/** Identificacion del canal usada para la carga de las propiedades */
	protected final int mID;
	
	/**
	 * Implementacion independiente de la decodificación del protocolo
	 * @param startTime
	 */
	public abstract void decode(double startTime);

	/**
	 * Debe retornar el tipo de protocolo que se crea
	 * @return
	 */
	public abstract ProtocolType getProtocol();
	
	/**
	 * Indica si el protocolo requiere o no de una fuente de clock
	 * @return
	 */
	public abstract boolean hasClock();
	
	/**
	 * Define las propiedades del canal
	 * @param prop
	 */
	public abstract void setProperties (Configuration prop);
	
	/**
	 * Fuerza la actualizacion de las propiedades
	 * @return true si se actualizan las propiedades, false si hay un error o no existe el objeto
	 */
	public abstract boolean invalidateProperties();
	
	public Configuration getProperties (){
		return mProperties;
	}
	
	/**
	 * @param freq, frecuencia de muestreo
	 */
	public Protocol (long freq, Configuration prop, int id){
		sampleFrec = freq;
		mProperties = prop;
		mID = id;
	}
	
	/**
	 * Obtiene el LogicBitSet que contiene los bits del canal
	 * @return
	 */
	public LogicBitSet getChannelBitsData (){
		return logicData;
	}
	
	/**
	 * Reemplaza los bits existentes del canal con los pasados
	 * @param data
	 */
	public void setChannelBitsData (LogicBitSet data){
		logicData = data;
	}
	
	/**
	 * Obtiene la lista con los Strings del protocolo decodificado con sus correspondientes
	 * posiciones en el tiempo en mili-segundos
	 * @return
	 */
	public List<TimePosition> getDecodedData() {
		return mDecodedData;
	}
	
	/**
	 * Frecuencia de muestreo que se utilizo
	 * @param freq
	 */
	public void setSampleFrequency (long freq){
		sampleFrec = freq;
	}
	
	/**
	 * Frecuencia de muestreo que se utilizo
	 * @return
	 */
	public long getSampleFrequency(){
		return sampleFrec;
	}
	
	public int getBitsNumber(){
		return logicData.length();
	}
	
	/**
	 * Elimina los datos de decodificación y los bits
	 */
	public void reset(){
		mDecodedData.clear();
		logicData.clear();
	}
	
	/**
	 * Agrega un String en la posicion dada sumando el tiempo de inicio initTime en mS
	 * @param text String a agregar
	 * @param startTime tiempo de inicio en segundos
	 * @param stopTime tiempo final en segundos
	 * @param initTime offset de tiempo a agregar en segundos
	 */
	public void addString (String text, double startTime, double stopTime, double initTime){
		if(stopTime >= startTime){
			mDecodedData.add(new TimePosition(text, startTime+initTime, stopTime+initTime));
		}else{
			mDecodedData.add(new TimePosition(text, startTime+initTime, startTime+initTime));
		}
	}
}
