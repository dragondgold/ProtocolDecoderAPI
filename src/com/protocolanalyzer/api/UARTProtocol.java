package com.protocolanalyzer.api;

import com.protocolanalyzer.api.utils.Configuration;

public class UARTProtocol extends Protocol{

	private static final boolean DEBUG = false;
	public static enum Parity{
		Even, Odd, NoParity
	}
	
	private int baudRate = 9600;
	private boolean is9Bits = false;
	private boolean twoStopBits = false;
	private Parity mParity = Parity.NoParity;
	
	/**
	 * Propiedades que deben existir en el objeto Properties pasado:
	 * "BaudRate" + id	-> Baudios
	 * "nineData" + id	-> Dato de 9 bits
	 * "dualStop" + id	-> Indica si hay o no dos bits de stop
	 * "Parity"   + id  -> Sin paridad, paridad even, paridad odd dependiendo del numero ordinal del enum 
	 * (Parity.Even.ordinal())
	 * @param freq
	 * @param prop
	 * @param id
	 */
	public UARTProtocol(long freq, Configuration prop, int id) {
		super(freq, prop, id);
		loadFromProperties();
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
		return loadFromProperties();
	}
	
	private boolean loadFromProperties(){
		if(mProperties == null) return false;
		try {
			baudRate = mProperties.getInteger("BaudRate" + mID);
			is9Bits = mProperties.getBoolean("nineData" + mID);
			twoStopBits = mProperties.getBoolean("dualStop" + mID);
			
			int parity = mProperties.getInteger("Parity" + mID);
			setParity(Parity.values()[parity]);
			
		} catch (NullPointerException e) {
			throw new NullPointerException("No se han definido todos los parametro en protocolo UART canal " + mID);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Decodifica el LogicBitSet (grupo de bits) que contiene el canal segun protocolo UART
	 * @param startTime, offset del tiempo que se desea agregar
	 * @return LogicData que contiene Strings con los datos decodificados siendo:
	 * 			[S] -> bit de Start
	 * 			numero -> valor de los 8 bits
	 * 			[SP] -> bit de Stop
	 * 				[SP1] -> bit de Stop 1
	 * 				[SP2] -> bit de Stop 2
	 * 			[P]	-> Paridad existe y es correcta
	 * 				[P*] -> Paridad existe y no coincide con la cantidad de unos
	 * @see http://stackoverflow.com/questions/2978569/android-java-append-string-int (para StringBuilder)
	 * @see http://www.dosideas.com/noticias/java/339-string-vs-stringbuffer-vs-stringbuilder.html (datos sobre StringBuilder y StringBuffer)
	 */
	@Override
	public void decode(double startTime) {
		loadFromProperties();
			
		if(DEBUG) System.out.println("UARTDecode - UART Protocol decode");
		if(DEBUG) System.out.println("UARTDecode - Source lenght: " + logicData.length());
		if(DEBUG) System.out.println("UARTDecode - Dart: " 			+ logicData.toString());
		
		int n = 0;				// Index
		int tempIndex;			// Index para guardado temporal
		boolean parityBit = false;
		final int dataBits;
		final double sampleTime = 1.0d/sampleFrec;					// Cuanto tiempo demora cada muestreo
		final int samplesPerBit = (int)Math.ceil((1.0d/baudRate) / sampleTime);
		final int halfBit = (int)Math.ceil(samplesPerBit/2.0);		// Tiempo hasta la mitad del bit
		
		if(is9Bits) dataBits = 9;
		else dataBits = 8;
		
		if(DEBUG) System.out.println("UARTDecode - samplesPerBit: " + samplesPerBit);
		if(DEBUG) System.out.println("UARTDecode - halfBit: " + halfBit);
		
		// Comprueba que halla al menos 3 samples por cada bit para asegura un buen muestreo
		if( ((1.0d/baudRate) / sampleTime) < 3.0d) return;
		
		// Si llege al final del array de datos salgo del bucle (el (samplesPerBit*10 es porque necesito
		// al menos 10 bits para la trama del UART, si hay menos esta incompleta)
		while(n <= (logicData.length()-(samplesPerBit*10))){
			n = logicData.nextFallingEdge(n); if(n == -1) break;		// Busco un flanco de bajada (Start)
			
			if(DEBUG) System.out.println("UARTDecode - n Falling Edge: " + n);
			
			// Voy a la mitad del bit de Start para verificar si es 0
			n += halfBit;
			if(DEBUG) System.out.println("UARTDecode - n Start: " + n);
			if(logicData.get(n) == false){ 		// Si el siguiente bit es 0 entonces es el bit de Start
				tempIndex = n - halfBit;		// Lugar de inicio del bit de Start
				if(DEBUG) System.out.println("UARTDecode - n de inicio de byte: " + n);
				int dataByte = 0;
				
				// Empiezo leyendo desde el LSB y lo voy colocando en el byte
				for(int bit = 0; bit < dataBits; ++bit){		// Voy tomando los bits y armo el byte del dato
					n += samplesPerBit;
					dataByte = LogicHelper.bitSet(dataByte, logicData.get(n), (dataBits-1) - bit);
				}
				
				// Si hay bit de paridad
				if(mParity != Parity.NoParity){
					n += samplesPerBit;
					parityBit = logicData.get(n);
				}
				
				if(DEBUG) System.out.println("UARTDecode - dataByte: " + Integer.toBinaryString(dataByte) + " - dataByte: " + dataByte);
				n += samplesPerBit;
				
				// Si a continuación tengo el/los bit de stop entonces escribo en el String los datos
				// Sino es simplemente un error y no escribo nada
				if(logicData.get(n) == true){
					// Si tengo dos bits de stop compruebo que a continuación este el segundo bit
					if(twoStopBits){
						if(!logicData.get(n + samplesPerBit)) continue;
					}
					if(DEBUG) System.out.println("UARTDecode - n stopBit: " + n);
					// Bit de Start
					addString("[S]", tempIndex*sampleTime, (tempIndex+samplesPerBit)*sampleTime, startTime);
					
					// Dato
					addString(""+dataByte, (tempIndex+samplesPerBit)*sampleTime,
							(tempIndex+samplesPerBit+(samplesPerBit*dataBits)*sampleTime), startTime);
					
					// Bit de paridad
					if(mParity != Parity.NoParity){
						if(checkParity(dataByte, parityBit))
							addString("[P]", (n-halfBit)*sampleTime, (n+halfBit)*sampleTime, startTime);
						else
							addString("[P*]", (n-halfBit)*sampleTime, (n+halfBit)*sampleTime, startTime);
					}
					
					// Bit(s) de Stop
					if(!twoStopBits) addString("[SP]", (n-halfBit)*sampleTime, (n+halfBit)*sampleTime, startTime);
					else{
						// Bits de stop
						addString("[SP1]", ((n-halfBit)*sampleTime), ((n-halfBit)+samplesPerBit)*sampleTime, startTime);	
						n += samplesPerBit;
						addString("[SP2]", ((n-halfBit)*sampleTime), ((n-halfBit)+samplesPerBit)*sampleTime, startTime);
						continue;
					}
				}
				n -= halfBit;
			}
			if(DEBUG) System.out.println("UARTDecode - n before while: " + n);
			if(DEBUG) System.out.println("UARTDecode - Condition: " + (logicData.length()-(samplesPerBit*10.0)) );
		}
		if(DEBUG) System.out.println("UARTDecode - Exit while()");
		if(DEBUG) System.out.println("UARTDecode - String size: " + mDecodedData.size());
	}

	@Override
	public ProtocolType getProtocol() {
		return ProtocolType.UART;
	}
	
	/**
	 * Comprueba que la paridad coincida con la cantida de '1' en el dato
	 * @param data	dato a comprobar
	 * @param parityBit bit de paridad a comprobar
	 * @return true si la paridad es correcta, false de otro modo
	 */
	private boolean checkParity (int data, boolean parityBit){
		int counter = 0;
		for(int n = 0; n < 9; ++n){
			if(LogicHelper.bitTest(data, n)) ++counter;
		}
		
		// Numero IMPAR de '1', paridad par y el bit de paridad es 1
		if(counter % 2 != 0 && mParity == Parity.Even && parityBit) return true;
		// Numero PAR de '1', paridad par y el bit de paridad es 0
		if(counter % 2 == 0 && mParity == Parity.Even && !parityBit) return true;
		
		// Numero IMPAR de '1', paridad impar y el bit de paridad es 0 
		if(counter % 2 != 0 && mParity == Parity.Odd && !parityBit) return true;
		// Numero PAR de '1', paridad impar y el bit de paridad es 1 
		if(counter % 2 == 0 && mParity == Parity.Odd && parityBit) return true;
		
		return false;
	}
	
	/**
	 * Define la velocidad en baudios del UART
	 * @param baud
	 */
	public void setBaudRate (int baud){
		baudRate = baud;
	}
	
	/**
	 * Obtiene la velocidad en baudios del UART. Por defecto 9600 baudios.
	 * @return
	 */
	public int getBaudRate() {
		return baudRate;
	}
	
	/**
	 * Define si se transmite con el modo de 9 bits o no
	 * @param state
	 */
	public void set9BitsMode (boolean state){
		is9Bits = state;
		if(state) mParity = Parity.NoParity;
	}
	
	public boolean is9BitsMode (){
		return is9Bits;
	}
	
	/**
	 * Setea la paridad siendo posible UARTProtocol.Even, UARTProtocol.Odd o UARTProtocol.NoParity
	 * @param mParity
	 */
	public void setParity (Parity mParity){
		this.mParity = mParity;
	}
	
	public Parity getParity (){
		return mParity;
	}
	
	/**
	 * Determina si se usan dos bits de stop o solo uno
	 * @param twoStopBits
	 */
	public void setTwoStopBits (boolean twoStopBits){
		this.twoStopBits = twoStopBits;
	}
	
	public boolean isTwoStopBits (){
		return twoStopBits;
	}

	@Override
	public boolean hasClock() {
		return false;
	}
}
