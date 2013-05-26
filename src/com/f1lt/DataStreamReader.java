package com.f1lt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.util.ByteArrayBuffer;

import android.app.ProgressDialog;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

class Decrypter
{
	private final int seed = 0x55555555;
	private int mask = seed;
	private int key = -1;
	
	
	public void setKey(int key)
	{
		//Log.d("DataStreamReader", "setKey");
		mask = seed;
		this.key = key;
	}
	
	public int getKey()
	{	
		//Log.d("DataStreamReader", "getKey");
		return key;
	}
	
	public void resetDecryption()
	{
		Log.d("DataStreamReader", "resetDecryption");
		mask = seed;
	}
	
	public byte[] decrypt(byte [] buf)
	{
		//Log.d("DataStreamReader", "decrypt");
		for (int i = 0; i < buf.length; ++i)
		{
			mask = mask >> 1 & 0x7fffffff ^ ((mask & 1) != 1 ? 0 : key);
			byte c = buf[i];
			buf[i] = (byte)(c ^ mask & 0xff);			 
		}
		return buf;
	}
	
	 //very simple passwd encoding algorithm
    static public String encodePasswd(String passwd)
    {
    	Log.d("DataStreamReader", "encodePasswd");
        int sz = passwd.length();
        String ret = "";
        for (int i = 0; i < sz; ++i)
        {
            char c = passwd.charAt(i);
            c ^= (1 << (i%8));
            ret += c;
        }
        return ret;
    }

}

interface DataStreamReceiver
{
	public void onNewDataObtained(boolean updateTimer);
	public void onShowMessageBoard(String msg, boolean rem);
	public void onDataStreamError(int code);
}

public class DataStreamReader
{
	private Handler handler = new Handler();
	private String host = "live-timing.formula1.com";
	private int port = 4321;
//	private String loginHost = "";
	private Decrypter decrypter = new Decrypter();
	private SocketDataReader socketDataReader = new SocketDataReader();
	private EventData eventData = null;
	private SessionTimer sessionTimer;
	private List<Packet> encryptedPackets = new ArrayList<Packet>();
	private Thread httpThread;
	private Thread socketThread;
	private int packets = 0;
	private int blocks = 0;
	//dades per contar cada cop que entrem una dada de temps
	private int values = 0;
	private boolean carpeta_creada = false;
	private boolean delayed = false;
	private int provisionalKey = 0;
	private boolean pKey = false;
	
    private static String RUTA_SAVE = F1LTActivity.getRUTA_SAVE();
	
	private Handler receiverHandler;
	private Handler secondaryReceiverHandler;
	private DataStreamReceiver dataStreamReceiver;
	private DataStreamReceiver secondaryDataStreamReceiver;
	
	private boolean noSession = false;
	boolean connected = false;
	
	private ProgressDialog dialog = null;
	
	private static DataStreamReader dataStreamReader;
	
	ByteArrayBuffer pbuf = new ByteArrayBuffer(512);    
    int pbuf_length = 0;
	
	private DataStreamReader()
	{
		sessionTimer = new SessionTimer(handler);	
		eventData = EventData.getInstance();
		eventData.clear();
	}
	
	public static DataStreamReader getInstance()
	{
		//Log.d("DataStreamReader", "getInstance");
		if (dataStreamReader == null)
			dataStreamReader = new DataStreamReader();
		
		return dataStreamReader;
	}
	
	public void setDataStreamReceiver(Handler receiverHandler, DataStreamReceiver dataStreamReceiver)
	{
		Log.d("DataStreamReader", "setDataStreamReceiver");
		this.dataStreamReceiver = dataStreamReceiver;
		this.receiverHandler = receiverHandler;
	}
	
	public void setSecondaryDataStreamReceiver(Handler receiverHandler, DataStreamReceiver dataStreamReceiver)
	{
		Log.d("DataStreamReader", "setSecondaryDataStreamReceiver");
		this.secondaryDataStreamReceiver = dataStreamReceiver;
		this.secondaryReceiverHandler = receiverHandler;
		
	}
	public void removeSecondaryDataStreamReceiver()
	{
		Log.d("DataStreamReader", "removeSecondaryDataStreamReceiver");
		this.secondaryDataStreamReceiver = null;
		this.secondaryReceiverHandler = null;
		
	}
	
	public DataStreamReceiver getSecondaryDataStreamReceiver()
	{
		Log.d("DataStreamReader", "getSecondaryDataStreamReceiver");
		return secondaryDataStreamReceiver;
	}
	
	public boolean isConnected()
	{
		Log.d("DataStreamReader", "isConnected");
		return connected;
	}
	
//	public void setHandler(Handler receiverHandler)
//	{
//		
//	}
//	
//	public void setDataStreamReceiver(DataStreamReceiver dataStreamReceiver)
//	{
//		this.dataStreamReceiver = dataStreamReceiver;
//	}
	
	public void connectToStream(final String host, final int port)
	{
		Log.d("DataStreamReader", "connectToStream");
		this.host = host;
		this.port = port;
						
		connected = true;
	
		eventData.frame = 1;
		httpThread = HttpReader.attemptObtainKeyFrame(0, handler, DataStreamReader.this, RUTA_SAVE);
//		onKeyFrameObtained(null, 0, true);
//		socketThread = new Thread()
//		{
//			public void run()
//			{
//				socketDataReader.openStream(host, port, handler, DataStreamReader.this);
////				socketDataReader.openStream("192.168.1.2", 6666, handler, DataStreamReader.this);
//				while (connected)
//				{
//					//	just wait for data from socketDataReader and parse it
//					socketDataReader.readStream();
//				}
//			}
//		};
//		socketThread.start();
	}	
	
	public void reconnect()
	{
		Log.d("DataStreamReader", "reconnect");
		connectToStream(host, port);
	}
	
	public void disconnect()
	{
		Log.d("DataStreamReader", "disconnect");
		connected = false;		
		socketDataReader.closeStream();
		
		if (socketThread != null)
			socketThread.interrupt();			
		
		if (sessionTimer != null)
			sessionTimer.stopTimer();
		
		if (httpThread != null && httpThread.isAlive())
			httpThread.interrupt();
		
		blocks = 0;
	}
	
	 public void onSessionTimerUpdated(String time, boolean stop)
	    {
		 	Log.d("DataStreamReader", "onSessionTimerUpdated");
	    	eventData.remainingTime = time;
	    	
//	    	receiverHandler.post(new Runnable() 
//			{
//				public void run()
//				{
//					dataStreamReceiver.onNewDataObtained(true);
//				}
//			});
	    	dataStreamReceiver.onNewDataObtained(true);
			
			if (secondaryDataStreamReceiver != null)
			{
//				secondaryReceiverHandler.post(new Runnable() 
//				{
//					public void run()
//					{
//						if (secondaryDataStreamReceiver != null)
//							secondaryDataStreamReceiver.onNewDataObtained(true);
//					}
//				});
				secondaryDataStreamReceiver.onNewDataObtained(true);
			}
//	    	LTViewFragment lt = (LTViewFragment)getSupportFragmentManager().findFragmentByTag("LTViewFragment");
//			if (lt != null)
//				lt.updateStatus();
	    }
	
	public void onKeyFrameObtained(final byte[] buf, final int bytes, boolean result)
    {
		Log.d("DataStreamReader", "onKeyFrameObtained");
    	if (result)
    	{    		    	
//    		Log.d("KEY FRAME OBTAINED", "" + bytes);
    		decrypter.resetDecryption();
    		parseBlock(buf, bytes);
    		
    		if (eventData.frame == 0)
    			eventData.frame = 1;
    		
    		decrypter.resetDecryption();
    		
    		if (encryptedPackets.isEmpty())
    			newDataObtained();    	
    		
    		if (!socketDataReader.isConnected())
    		{
	    		socketThread = new Thread()
	    		{
	    			public void run()
	    			{
	    				socketDataReader.openStream(host, port, handler, DataStreamReader.this);
	    				
//	    				String dec = "C08983A3"; 	//valencia p1
//	    				String dec = "B121085E";		//valencia qual
//	    				String dec = "A956E788";	//valencia race
//	    				String dec = "F2B24635";	//gbr fp1
//	    				String dec = "E70FD8E4";	//gbr fp2
//	    				String dec = "CA83CBDE";	//gbr fp3
//	    				String dec = "BE7CBDB6";	//gbr q
//	    				String dec = "1BEB";
//	    				String dec = "FD1EA765";	//gbr race
//	    				int ldec = (int)(Long.parseLong(dec, 16) & -1L);
//	    				decrypter.setKey(ldec);
//	    				eventData.key = ldec;
//	    				
//	    				
//	    				socketDataReader.openStream("192.168.1.2", 6666, handler, DataStreamReader.this);
	    				while (connected)
	    				{
	    					Log.d("onKeyFrameObtained", "DINS WHILE CONNECTED");
	    					//	just wait for data from socketDataReader and parse it
	    					socketDataReader.readStream();
	    				}
	    				Log.d("onKeyFrameObtained", "SURT WHILE CONNECTED");
	    			}
	    		};
	    		socketThread.start();
    		}
    	}    
    }
	
	public void onDecryptionKeyObtained(int key, boolean result)
    {
		Log.d("DataStreamReader", "onDecryptionKeyObtained");
		Log.d("KEY ES", Integer.toString(key));
    	if (result)
    	{
    		decrypter.setKey(key);    		
    		decrypter.resetDecryption();
    		eventData.key = key;
    		for (int i = 0; i < encryptedPackets.size(); ++i)
    	    {
    			Packet packet = encryptedPackets.get(i);
    			packet.longData = decrypter.decrypt(packet.longData);

    	        if(packet.carID != 0)	    	        	
    	        	parseCarPacket(packet);

    	        else
    	        	parseSystemPacket(packet);  
    	            
    	    }
    	    encryptedPackets.clear();
    	    newDataObtained();    	
    	}
    }
	
	public void onDataBlockObtained(final byte [] data, final int bytes, final boolean result)
    {    
		Log.d("DataStreamReader", "onDataBlockObtained");
		if (!connected)
			return;
    	if (result)    	
    	{
    		parseBlock(data, bytes);    
    		newDataObtained();    	
    	}
    	else
    	{    		    		
//    		receiverHandler.post(new Runnable() 
//    		{
//    			public void run()
//    			{
//    				dataStreamReceiver.onDataStreamError(bytes);
//    			}
//    		});
    		dataStreamReceiver.onDataStreamError(bytes);
    		
    		if (secondaryDataStreamReceiver != null)
    		{
//    			secondaryReceiverHandler.post(new Runnable() 
//    			{
//    				public void run()
//    				{
//    					secondaryDataStreamReceiver.onDataStreamError(bytes);
//    				}
//    			});
    			secondaryDataStreamReceiver.onDataStreamError(bytes);
    		}
    	}
    }
	
	public void newDataObtained()
	{
		Log.d("DataStreamReader", "newDataObtained");
//		receiverHandler.post(new Runnable() 
//		{
//			public void run()
//			{
//				dataStreamReceiver.onNewDataObtained(false);
//			}
//		});
		dataStreamReceiver.onNewDataObtained(false);
		
		if (secondaryDataStreamReceiver != null)
		{
//			secondaryReceiverHandler.post(new Runnable() 
//			{
//				public void run()
//				{
//					if (secondaryDataStreamReceiver != null)
//						secondaryDataStreamReceiver.onNewDataObtained(false);
//				}
//			});
			secondaryDataStreamReceiver.onNewDataObtained(false);
		}
	}
	
	public void parseBlock(byte[] data, int bytes)
    {
		blocks = blocks + 1;
		//Log.d("DataStreamReader", "parseBlock: " + blocks);
        AtomicReference<Packet> packet = new AtomicReference<Packet>(new Packet());
        AtomicReference<Integer> pos = new AtomicReference<Integer>(0);   
        
        boolean entra = true;
        if(entra){   	
	        
        	long millis = (System.currentTimeMillis()/1000);
        	//Log.d("MILLIS",Long.toString(millis));
        	
	        //Creem un arxiu per copiar a dins
	        File sdCard = Environment.getExternalStorageDirectory();
	        File dir = new File (sdCard.getAbsolutePath() + RUTA_SAVE);
	        
	        String nom = "Dades";
	        nom=nom.concat(Integer.toString(blocks));
	        nom=nom.concat(".txt");
	        
	        String nom2 = "Temps";
	        nom2=nom2.concat(Integer.toString(blocks));
	        nom2=nom2.concat(".txt");	  
	        
	        String nom3 = "Milis";
	        nom3=nom3.concat(Integer.toString(blocks));
	        nom3=nom3.concat(".txt");
	        
	        byte[] temps = null;
	        byte[] milis = null;
	        
	        if(carpeta_creada==false){
		        dir.mkdirs();
		        carpeta_creada=true;
	        }
	        File file = new File(dir, nom);
	        File file2 = new File(dir, nom2);
	        File file3 = new File(dir, nom3);
	        
	        Log.d("ARREL ARXIU DADES: ", file.toString());
	        //Log.d("ARREL ARXIU TEMPS: ", file2.toString());
	        //Log.d("ARREL ARXIU MILIS: ", file3.toString());
	        String timer = sessionTimer.getTime();
	        try {
				temps = timer.getBytes("UTF8");
				milis = ByteBuffer.allocate(10).putLong(millis).array();
			} catch (UnsupportedEncodingException e) {
				Log.e("ERROR", "Error al passar temps a Bytes", e);
			}
	        
	        // Nomes copiem els bytes del array que contenen elements
	        byte[] dades = new byte [bytes];
	        for(int i=0;i<bytes;i++){
	        	dades[i] = data[i];
	        }
	        
	        try {
	        	
	        	FileOutputStream f = new FileOutputStream(file);
	        	FileOutputStream f2 = new FileOutputStream(file2);
	        	FileOutputStream f3 = new FileOutputStream(file3);
	        	
	        	//f.write(temps);
	            f.write(dades);
	            f.flush();
	            f.close();
	            
	            f2.write(temps);
	            f2.flush();
	            f2.close();
	            
	            f3.write(milis);
	            f3.flush();
	            f3.close();
	            
	            //Log.d("GUARDAT NUM DADES AL ARXIU DADES: ", Integer.toString(dades.length));
	            //Log.d("GUARDAT NUM DADES AL ARXIU TEMPS: ", Integer.toString(temps.length));
	            //Log.d("GUARDAT NUM DADES AL ARXIU MILIS: ", Integer.toString(milis.length));
	        } catch (Exception e) {
	            Log.e("EERROR", "Error opening Log.", e);
	        }
	        
        } // fi del if
        
        
        /*
        // VALORS PARCIALS DE CADA PAQUET
        for(int i=0;i<bytes;i++){
        	int value = data[i];
        	String valor = Integer.toString(value);
        	Log.d("==ITER: Valor i data:", i+ " "+valor);
        }
        */
        
       
        // PROVA SI CADA COP QUE PARSEJA UN PACKET ENTRA AQUI.
        /*
        int lon = data.length;
        Log.d("parseBlock IF LON i BYTES", Integer.toString(lon) + " " + Integer.toString(bytes));
        if(lon==bytes){
        	Log.d("parseBlock lon==bytes", Integer.toString(lon));
        }
        */
        
        while (parsePacket (packet, data, bytes, pos)) 
        { 
        	try
        	{
        		if (!packet.get().encrypted || eventData.key != -1)
        		{
		    		if (packet.get().carID !=0 ) 
		    		{
		    			parseCarPacket (packet.get());
		    		} 
		    		else 
		    		{	    	    			
		    			parseSystemPacket(packet.get());
		    		}    	    		
        		}
        	}
        	catch(Exception e) {}
    		    		    		
        	if (packet.get().longData != null)
        	for (int i = 0; i < packet.get().longData.length; ++i)
        		packet.get().longData[i] = 0;
        	
    		pbuf.clear();
    	}
    }
	
	public void parseBlockDelayed(byte[] data, int bytes)
    {
		delayed = true;
		//sessionTimer.decrementTime();
		blocks = blocks + 1;
		Log.d("DataStreamReader", "parseBlock: " + blocks);
        AtomicReference<Packet> packet = new AtomicReference<Packet>(new Packet());
        AtomicReference<Integer> pos = new AtomicReference<Integer>(0);         
        
        /*
        // VALORS PARCIALS DE CADA PAQUET
        for(int i=0;i<bytes;i++){
        	int value = data[i];
        	String valor = Integer.toString(value);
        	Log.d("==ITER: Valor i data:", i+ " "+valor);
        }
        */
        
       
        // PROVA SI CADA COP QUE PARSEJA UN PACKET ENTRA AQUI.
        /*
        int lon = data.length;
        Log.d("parseBlock IF LON i BYTES", Integer.toString(lon) + " " + Integer.toString(bytes));
        if(lon==bytes){
        	Log.d("parseBlock lon==bytes", Integer.toString(lon));
        }
        */
        
        while (parsePacket (packet, data, bytes, pos)) 
        { 
        	try
        	{
        		if (!packet.get().encrypted || eventData.key != -1)
        		{
		    		if (packet.get().carID !=0 ) 
		    		{
		    			parseCarPacket (packet.get());
		    		} 
		    		else 
		    		{	    	    			
		    			parseSystemPacket(packet.get());
		    		}    	    		
        		}
        	}
        	catch(Exception e) {}
    		    		    		
        	if (packet.get().longData != null)
        	for (int i = 0; i < packet.get().longData.length; ++i)
        		packet.get().longData[i] = 0;
        	
    		pbuf.clear();
    	}
    }
	
	public boolean parsePacket(AtomicReference<Packet> arPacket, byte [] buf, int bytes, AtomicReference<Integer> arPos)
	{	
		//Log.d("DataStreamReader", "parsePacket");
		packets = packets + 1;
		int cent = packets % 40;
		if(cent==0){
			//Log.d("packets", String.valueOf(packets));
		}
		
		guardarEventData();
		
		Packet packet = arPacket.get();
		
	
		int pos = arPos.get();

		
		if (pbuf_length < 2)
		{
			int data_length = Math.min(bytes-pos, 2-pbuf_length);
			pbuf.append(buf, pos, data_length);
			pbuf_length += data_length;
			pos += data_length;
			
			if (pbuf_length < 2)						
				return false;			
		}
		
		
		if (pos == bytes)
			return false;
		
		
		packet.type = LTData.getPacketType(pbuf); 
		packet.carID = LTData.getCarPacket(pbuf);
							
		boolean decrypt = false;
		
		if(packet.carID !=0 )
		{
			switch (packet.type) 
			{
				case LTData.CarPacket.CAR_POSITION_UPDATE:
					packet.length = LTData.getSpecialPacketLength(pbuf);
					packet.data = LTData.getSpecialPacketData(pbuf);					
					break;
					
				case LTData.CarPacket.CAR_POSITION_HISTORY:
					packet.length = LTData.getLongPacketLength(pbuf);
					packet.data = LTData.getLongPacketData(pbuf);
					decrypt = true;
					break;
					
				default:
					packet.length = LTData.getShortPacketLength(pbuf);
					packet.data = LTData.getShortPacketData(pbuf);
					decrypt = true;
					break;
			}
		}
		else
		{
			switch (packet.type) 
			{
			case LTData.SystemPacket.SYS_EVENT_ID:
			case LTData.SystemPacket.SYS_KEY_FRAME:
				packet.length = LTData.getShortPacketLength(pbuf);
				packet.data = LTData.getShortPacketData(pbuf);				
				break;
				
			case LTData.SystemPacket.SYS_TIMESTAMP:
				packet.length = 2;
				packet.data = LTData.getSpecialPacketData(pbuf);
				decrypt = true;
				break;
			case LTData.SystemPacket.SYS_WEATHER:
			case LTData.SystemPacket.SYS_TRACK_STATUS:
				packet.length = LTData.getShortPacketLength(pbuf);
				packet.data = LTData.getShortPacketData(pbuf);
				decrypt = true;
				break;
			case LTData.SystemPacket.SYS_COMMENTARY:
			case LTData.SystemPacket.SYS_NOTICE:
			case LTData.SystemPacket.SYS_SPEED:
				packet.length = LTData.getLongPacketLength(pbuf);
				packet.data = LTData.getLongPacketData(pbuf);
				decrypt = true;				
				break;
				
			case LTData.SystemPacket.SYS_COPYRIGHT:
				packet.length = LTData.getLongPacketLength(pbuf);
				packet.data = LTData.getLongPacketData(pbuf);				
				break;
				
			case LTData.SystemPacket.SYS_VALID_MARKER:
			case LTData.SystemPacket.SYS_REFRESH_RATE:
				packet.length = 0;
				packet.data = 0;				
				break;
				
			default:
				//info (3, _("Unknown system packet type: %d\n"),
				  //    packet.type);
				packet.length = 0;
				packet.data = 0;				
				break;
			}
		}		
//		decrypt = false;	
		packet.encrypted = decrypt;
		
		if (packet.length > 0) 
		{		
			int data_length = Math.min(bytes-pos, (packet.length + 2) - pbuf_length);
			
			pbuf.append(buf, pos, data_length);
			
			pbuf_length += data_length;
			pos += data_length;
		
			if (pbuf_length < (packet.length + 2))
			{
				arPos.set(pos);
				arPacket.set(packet);
				return false;
			}
		}
		
		if (packet.length > 0)
		{
			packet.longData = new byte[packet.length];
			for (int i = 0; i < packet.length; ++i)
				packet.longData[i] = (byte)pbuf.byteAt(i+2);
			
			if (decrypt)
			{
				if (decrypter.getKey() != -1)
					packet.longData = decrypter.decrypt(packet.longData);
				
				else
					encryptedPackets.add(new Packet(packet));
				
			}
		}
		
		
		pbuf_length = 0;
		arPos.set(pos);
		arPacket.set(packet);
		return true;
	}
	
	private void guardarEventData() {
		// GUARDEM HISTORIC DE DADES DE L'EVENT
		String [] timeArray = eventData.remainingTime.split(":");
		int hour = 0, min = 0, sec = 0;
		
		if (timeArray.length == 3)
		{
			hour = Integer.parseInt(timeArray[0]);
			min = Integer.parseInt(timeArray[1]);
			sec = Integer.parseInt(timeArray[2]);
		}
		
		if(sec==0){
			if(hour>0){
				min = min + (hour*60);
			}
			
			guardemDadesTemps(min,sec);
		}
		
	}

	private void guardemDadesTemps(int min, int sec) {
		
		if(!eventData.temps_guardats.containsKey(min)){
			
			Log.d("GUARDEM EVENT DATA",eventData.remainingTime);
			// Ens guardem a la taula de keys guardades - per saber com les anem guardant
			//Log.d("=== GUARDEM EVENT DATA",eventData.remainingTime);
			values=values+1;
			eventData.temps_guardats.put(min, values);
			
			//Ens anem guardant els diferents valors a cada Hash Map
			// Temperatura del Aire
			int temp = (int)eventData.airTemp;
			eventData.airTempHistory.put(min, temp);
			//Log.d("=== SIZE AIR TEMP",Integer.toString(eventData.airTempHistory.size()));
			
			// Temperatura del Asfalt
			temp = (int)eventData.trackTemp;
			eventData.airTrackHistory.put(min, temp);
			//Log.d("=== SIZE TRACK TEMP",Integer.toString(eventData.airTrackHistory.size()));
		}
		
	}

	public void parseSystemPacket(Packet packet)
	{
		//Log.d("parseSystemPacket","System");
		Log.d("parseSystemPacket", eventData.remainingTime);
		try
		{
			
//			if (packet.type != LTData.SystemPacket.SYS_COMMENTARY && packet.type != LTData.SystemPacket.SYS_TIMESTAMP)
			String logMsg = "SYS=" + packet.type + " " + packet.data + " " + packet.length + " ";				
			if (packet.type != LTData.SystemPacket.SYS_COMMENTARY && packet.length > 0 && packet.type != LTData.SystemPacket.SYS_TIMESTAMP)
				logMsg += new String(packet.longData, "ISO-8859-1");
			
			if (packet.type == LTData.SystemPacket.SYS_EVENT_ID)
				logMsg += " - " + (int)packet.longData[0];
			
//			Log.d("dsr", logMsg);
			
		} catch (Exception e) { }
		int number = 0, i;
		Packet copyPacket = packet;
		String str = "";
		
		//Log.d("ENCRYPTED= "+packet.encrypted +" CAR=" + packet.carID, " TYPE=" + packet.type + " DATA= " + packet.data + " LEN= " + packet.length + " LONG DATA= " + packet.longData[0]);
		switch(packet.type)
	    {
	        case LTData.SystemPacket.SYS_EVENT_ID:
	        	Log.d("DataStreamReader", "ENTRA SYS_EVENT_ID");
	            number = 0;	            
	            try
	            {
	            	eventData.eventInfo = LTData.getCurrentEvent();
	            	
	            	//Log.d("LLOC EVENT: ",eventData.eventInfo.shortName);
	            	eventData.qualiPeriod = 0;
	            	
	            	// SET THE LAPS OF THE RACE
	            	setLaps(eventData.eventInfo.no);
	            	
	            	String eventNo = (new String(copyPacket.longData, 1, copyPacket.length-1, "ISO-8859-1"));	            		            	
	            	
	            	decrypter.setKey(-1);	
	            	eventData.key = -1;
	            	if (copyPacket.longData[1] == '_')
	            	{
	            		try
	            		{
		            		final String msg = new String(copyPacket.longData, 2, copyPacket.length-2, "ISO-8859-1");
		            		eventData.clear();
//		        			receiverHandler.post(new Runnable() 
//		        			{
//		        				public void run()
//		        				{
//		        					dataStreamReceiver.onShowMessageBoard(msg, true);
//		        				}
//		        			});
		        			dataStreamReceiver.onShowMessageBoard(msg, true);
		        			noSession = true;		        	
	            		}
	            		catch (UnsupportedEncodingException ue) {}
	            	}
	            	else
	            	{
	            		//this is only to catch exception when we've obtained a garbage instead of event number
	            		int tmp = Integer.parseInt(eventNo);
	            		
	            		eventData.eventType = copyPacket.data;
	            		eventData.sessionId = tmp;
	    	            eventData.lapsCompleted = 0;
	            	}
	            		
	            	Log.d("HTTPATTEMPT_EVENTID_L856 NUMBER I KEYFRAME", Integer.toString(number)+" "+Integer.toString(decrypter.getKey()));
	            	if(!delayed){
	            		httpThread = HttpReader.attemptObtainDecryptionKey(eventNo, handler, this);
	            	}
	            	else{
	            		// SI TENIM LA KEY CARREGADA, LA ENVIEM
	            		if(pKey){
	            			onDecryptionKeyObtained(provisionalKey, true);
	            		}
	            	}
	            }
	            catch (Exception e)
	            {
	            	if (copyPacket.longData[1] == '_')
	            	{	            			
	            		try
	            		{
		            		final String msg = new String(copyPacket.longData, 2, copyPacket.length-2, "ISO-8859-1");
//		        			receiverHandler.post(new Runnable() 
//		        			{
//		        				public void run()
//		        				{
//		        					dataStreamReceiver.onShowMessageBoard(msg, true);
//		        				}
//		        			});
		        			dataStreamReceiver.onShowMessageBoard(msg, true);
		        			noSession = true;		        	
	            		}
	            		catch (UnsupportedEncodingException ue) {}
	            	}
	            }	            

	            decrypter.resetDecryption();
	            break;

	        case LTData.SystemPacket.SYS_KEY_FRAME:
	        	Log.d("DataStreamReader", "ENTRA SYS_KEY_FRAME");
	            number = 0;
    
	            number = packet.longData[1] << 8 & 0xff00 | packet.longData[0] & 0xff;
	            decrypter.resetDecryption();

	            Log.d("SYSKEYFRAME_L897 NUMBER I KEYFRAME", Integer.toString(number)+" "+Integer.toString(decrypter.getKey()));
	             if (eventData.frame == 0 && delayed == false)
	             {
	            	Log.d("ENTRA FRAME = 0", "eventData.frame=number");
	                eventData.frame = number;
	                httpThread = HttpReader.attemptObtainKeyFrame(number, handler, DataStreamReader.this, RUTA_SAVE);
	                
	             }
	             else
	                 eventData.frame = number;

	             break;

	        case LTData.SystemPacket.SYS_WEATHER:
	            switch (packet.data)
	            {                
	                case LTData.WeatherPacket.WEATHER_SESSION_CLOCK:
	                	try
	                	{
	                		String time = new String(packet.longData, "ISO-8859-1");	                		
	                		
	                		String [] arr = time.split(":");
	                		if (arr.length > 1)
	                		{	                				                		
	                			if (arr.length == 2)
	                				eventData.remainingTime = "0:"+time;
	                			
	                			sessionTimer.setTime(time);
	                			eventData.remainingTime = sessionTimer.getTime();
	                			int sec = Integer.parseInt(arr[arr.length-1]);
	                			
	                			
	                			if (sec != 0 && 
	                				((eventData.eventType != LTData.EventType.RACE_EVENT) ||
	                				(eventData.lapsCompleted < eventData.eventInfo.laps)) && 
	                				!sessionTimer.isTimerRunning())
	                			{
	                				Log.d("TIMER ABANS START", sessionTimer.getTime());
	                				guardarTempsTotalSessio(sessionTimer.getTime());
	                				sessionTimer.startTimer();
	                				
	                				if (eventData.eventType == LTData.EventType.QUALI_EVENT)
	                					++eventData.qualiPeriod;
	                				
	                				eventData.sessionStarted = true;
	                			}	   
	                			
	                			if ((arr.length == 2 && time.equals("0:00")) || (arr.length == 3 && time.equals("0:00:00")))
	                			{
	                				sessionTimer.stopTimer();
	                				eventData.sessionStarted = false;
	                			}
	                		}
	                		else
	                		{
//	                			eventData.remainingTime = "0:00:00";
//	                			sessionTimer.setTime(eventData.remainingTime);
	                		}
	                				
	                	}
	                	catch (UnsupportedEncodingException e)
	                	{
	                		
	                	}

	                break;
	                case LTData.WeatherPacket.WEATHER_TRACK_TEMP:
	                    try
	                    {
	                    	str = new String(packet.longData, "ISO-8859-1");	                    	                   
	                    	eventData.trackTemp = Double.parseDouble(str);
	                    	// PROVA
	                    	/*
	                    	int size = eventData.airTempHistoric.length;
	                    	Integer temp = Integer.valueOf(str);
	                    	eventData.airTempHistoric[size]=temp;
	                    	*/
	                    }
	                    catch (Exception e) {}
	                    break;

	                case LTData.WeatherPacket.WEATHER_AIR_TEMP:
	                	try
	                    {
	                    	str = new String(packet.longData, "ISO-8859-1");	                    	                   
	                    	eventData.airTemp = Double.parseDouble(str);
	                    }
	                    catch (Exception e) {}
	                    break;

	                case LTData.WeatherPacket.WEATHER_WIND_SPEED:
	                	try
	                    {
	                    	str = new String(packet.longData, "ISO-8859-1");	                    	                   
	                    	eventData.windSpeed = Double.parseDouble(str);
	                    }
	                    catch (Exception e) {}
	                    break;

	                case LTData.WeatherPacket.WEATHER_HUMIDITY:
	                	try
	                    {
	                    	str = new String(packet.longData, "ISO-8859-1");	                    	                   
	                    	eventData.humidity = Double.parseDouble(str);
	                    }
	                    catch (Exception e) {}
	                    break;

	                case LTData.WeatherPacket.WEATHER_PRESSURE:
	                	try
	                    {
	                    	str = new String(packet.longData, "ISO-8859-1");	                    	                   
	                    	eventData.pressure = Double.parseDouble(str);
	                    }
	                    catch (Exception e) {}
	                    break;

	                case LTData.WeatherPacket.WEATHER_WIND_DIRECTION:
	                	try
	                    {
	                    	str = new String(packet.longData, "ISO-8859-1");	                    	                   
	                    	eventData.windDirection = Double.parseDouble(str);
	                    }
	                    catch (Exception e) {}
	                    break;

	                case LTData.WeatherPacket.WEATHER_WET_TRACK:
	                	try
	                    {
	                    	str = new String(packet.longData, "ISO-8859-1");	                    	                   
	                    	eventData.wetdry = Integer.parseInt(str);
	                    }
	                    catch (Exception e) {}
	                    break;

	                default:
	                    break;
	            }
	            break;
	        case LTData.SystemPacket.SYS_TRACK_STATUS:
//	        qDebug() << "SYS_TRACK_STATUS=" << packetLongData[0] << ", " << packet.data;
	            switch (packet.data)
	            {
	                case 1:
	                	try
	                    {
	                    	str = new String(packet.longData, "ISO-8859-1");	                    	                   
	                    	eventData.flagStatus = Integer.parseInt(str);
	                    }
	                    catch (Exception e) {}
	                    break;
	                    
	                    //107%
	                case 4:
	                	try
	                    {
	                    	str = new String(packet.longData, "ISO-8859-1");	
	                    	eventData.session107Percent.setTime(str);
	                    }
	                    catch (Exception e) {}
	                	break;
	            }
	            break;
//	        case LTData::SYS_COPYRIGHT:
//	            break;
	        case LTData.SystemPacket.SYS_NOTICE:
	        	try
	        	{
	        		eventData.notice = new String(packet.longData, "ISO-8859-1");
	        			        		
//	        		setNotice();
	        	}
	        	catch (UnsupportedEncodingException e ){} 
	            break;
	            
	        case LTData.SystemPacket.SYS_TIMESTAMP:
	        	int ts = copyPacket.longData[1] << 8 & 0xff00 | (copyPacket.longData[0] & 0xff | 0 << 16 & 0xff0000);
	        	Log.d("packet", "TIMESTAMP="+ts+" "+copyPacket.longData[1]+" "+copyPacket.longData[0]);//(ts-((System.currentTimeMillis() - startTime) + 200L) / 1000L)+", curr="+System.currentTimeMillis());
//	        	if (ts >= 3600L)
//	        		eventData.remainingTime =  (new StringBuilder(String.valueOf(Long.toString(ts / 3600L)))).append(":").append(twoDigits(Long.toString((ts / 60L) % 60L))).append(":").append(twoDigits(Long.toString(ts % 60L))).toString();
//	        		
//	        	else if (ts >= 60L)
//	        		eventData.remainingTime = (new StringBuilder(String.valueOf(Long.toString(ts / 60L)))).append(":").append(twoDigits(Long.toString(ts % 60L))).toString();
	        	break;
	        case LTData.SystemPacket.SYS_SPEED:
	        	try
	        	{
//	        		str = new String(copyPacket.longData, 0, 1, "ISO-8859-1");
//	        		int sector = Integer.parseInt(str);
		            switch((int)copyPacket.longData[0])
		            {
		                case LTData.SpeedPacket.SPEED_SECTOR1:
		                	int j;
		                    for (i = 1, j=0; i < copyPacket.longData.length && j < 12; i+=8)
		                    {		                    	
		                        eventData.sec1Speed[j++] = new String(copyPacket.longData, i, 3, "ISO-8859-1");//LTData::getDriverNameFromShort(copyPacket.longData.mid(i, 3));
		                        eventData.sec1Speed[j++] = new String(copyPacket.longData, i+4, 3, "ISO-8859-1");
		                    }
		                    break;
		                case LTData.SpeedPacket.SPEED_SECTOR2:
		                    for (i = 1, j=0; i < copyPacket.longData.length && j < 12; i+=8)
		                    {
		                        eventData.sec2Speed[j++] = new String(copyPacket.longData, i, 3, "ISO-8859-1");
		                        eventData.sec2Speed[j++] = new String(copyPacket.longData, i+4, 3, "ISO-8859-1");
		                    }
		                    break;
		                case LTData.SpeedPacket.SPEED_SECTOR3:
		                    for (i = 1, j=0; i < copyPacket.longData.length && j < 12; i+=8)
		                    {
		                        eventData.sec3Speed[j++] = new String(copyPacket.longData, i, 3, "ISO-8859-1");
		                        eventData.sec3Speed[j++] = new String(copyPacket.longData, i+4, 3, "ISO-8859-1");
		                    }
		                    break;
		                case LTData.SpeedPacket.SPEED_TRAP:
		                    for (i = 1, j=0; i < copyPacket.longData.length && j < 12; i+=8)
		                    {
		                        eventData.speedTrap[j++] = new String(copyPacket.longData, i, 3, "ISO-8859-1");
		                        eventData.speedTrap[j++] = new String(copyPacket.longData, i+4, 3, "ISO-8859-1");
		                    }
		                    break;
		                case LTData.SpeedPacket.FL_CAR:
		                    eventData.FLNumber = Integer.parseInt(new String(copyPacket.longData, 1, copyPacket.longData.length-1, "ISO-8859-1"));
		                    break;
	
		                case LTData.SpeedPacket.FL_DRIVER:
		                	
		                	if (eventData.eventType == LTData.EventType.RACE_EVENT)
		                	{
		                		str = new String(copyPacket.longData, 1, copyPacket.longData.length-1, "ISO-8859-1");
		                		eventData.FLDriver = str;//LTData::getDriverName(s);// s.left(4) + s.right(s.size()-4).toLower();
		                	}
		                    break;                    
	
		                case LTData.SpeedPacket.FL_LAP:
		                    eventData.FLLap = Integer.parseInt(new String(copyPacket.longData, 1, copyPacket.longData.length-1, "ISO-8859-1"));
		                    if (eventData.FLNumber > 0)
		                    {
		                        for (i = 0; i < eventData.driversData.size(); ++i)
		                        {
		                        	DriverData dr = eventData.driversData.get(i);
		                            if (dr.number == eventData.FLNumber)
		                            {		                            	
		                                dr.setFastestLap(eventData.FLTime, eventData.FLLap);
		                                eventData.driversData.set(i,  dr);
		                                break;
		                            }
		                        }
		                    }
		                    break;
	
		                case LTData.SpeedPacket.FL_TIME:
		                    if (eventData.eventType == LTData.EventType.RACE_EVENT)
		                        eventData.FLTime.setTime(new String(copyPacket.longData, 1, copyPacket.longData.length-1, "ISO-8859-1"));
	
		                    break;
		            }		            
	            }
	        	catch (Exception e) {}

	            break;
	        case LTData.SystemPacket.SYS_COMMENTARY:    
	        	
	        	try
	        	{
	        		if (eventData.commentary == null)
	        			eventData.commentary = new String();
	        		
		        	if (copyPacket.longData[0] < 32)
		        	{
		        		if ((copyPacket.longData[1] & 2) > 0)
		        			eventData.commentary += new String(copyPacket.longData, 2, copyPacket.longData.length-2, "UTF-16LE");
		        		else
		        			eventData.commentary += new String(copyPacket.longData, 2, copyPacket.longData.length-2, "UTF-8");
		        		
		        		if ((copyPacket.longData[1] & 1) > 0)		        	
			        		eventData.commentary += "\n\n";		        	
		        	}	
		        	else
		        		eventData.commentary += new String(copyPacket.longData, "ISO-8859-1");
	        	}
	        	catch (UnsupportedEncodingException e) {}

	        default:
	            //dd = packet.longData.toDouble();
	            break;
	    }
	}
	
	private void guardarTempsTotalSessio(String time) {
		String [] timeArray = time.split(":");
		
		int hour = 0, min = 0, sec = 0;
		
		if (timeArray.length == 3)
		{
			hour = Integer.parseInt(timeArray[0]);
			min = Integer.parseInt(timeArray[1]);
			sec = Integer.parseInt(timeArray[2]);
		}
		
		if(hour>0){
			min = min + (hour*60);
		}
		
		eventData.minutsSessio=min+1;
		
	}

	private void setLaps(int no) {
		switch(no) {
			case 1:
				eventData.eventInfo.laps=58;
				break;
			case 2:
				eventData.eventInfo.laps=56;
				break;
			case 3:
				eventData.eventInfo.laps=56;
				break;
			case 4:
				eventData.eventInfo.laps=57;
				break;
			case 5:
				eventData.eventInfo.laps=66;
				break;
			case 6:
				eventData.eventInfo.laps=78;
				break;
			case 7:
				eventData.eventInfo.laps=70;
				break;
			case 8:
				eventData.eventInfo.laps=57;
				break;
			case 9:
				eventData.eventInfo.laps=52;
				break;
			case 10:
				eventData.eventInfo.laps=60;
				break;
			case 11:
				eventData.eventInfo.laps=70;
				break;
			case 12:
				eventData.eventInfo.laps=44;
				break;
			case 13:
				eventData.eventInfo.laps=53;
				break;
			case 14:
				eventData.eventInfo.laps=61;
				break;
			case 15:
				eventData.eventInfo.laps=53;
				break;
			case 16:
				eventData.eventInfo.laps=55;
				break;
			case 17:
				eventData.eventInfo.laps=60;
				break;
			case 18:
				eventData.eventInfo.laps=55;
				break;
			case 19:
				eventData.eventInfo.laps=56;
				break;
			case 20:
				eventData.eventInfo.laps=71;
				break;
		}
		
	}

	public void parseCarPacket(Packet packet)
	{    
		//Log.d("parseCarPacket","Car");
		//Log.d("parseCarPacket", eventData.remainingTime);
		if (noSession)
		{
//			receiverHandler.post(new Runnable() 
//			{
//				public void run()
//				{
//					dataStreamReceiver.onShowMessageBoard("", false);
//				}
//			});
			dataStreamReceiver.onShowMessageBoard("", false);
			
//			secondaryReceiverHandler.post(new Runnable() 
//			{
//				public void run()
//				{
//					secondaryDataStreamReceiver.onShowMessageBoard("", false);
//				}
//			});
			if (secondaryDataStreamReceiver != null)
				secondaryDataStreamReceiver.onShowMessageBoard("", false);
			
			noSession = false;
			eventData.frame = 0;
		}
		
	    if (packet.carID > eventData.driversData.size() || packet.carID < 1)
	    {
	        return;
	    }

	    Packet copyPacket = packet;
//	    if (packet.type != LTData::CAR_POSITION_HISTORY && !checkDecryption(packet.longData))
//	    {
//	        packet.longData = "";
//	        packet.length = 0;
////	        eventData.frame = 0;
//	    }

	    String longData = "";
	    try
	    {
	    	if (packet.length > 0)
	    		longData = new String(packet.longData, 0, packet.length, "ISO-8859-1");
	    }
	    catch (UnsupportedEncodingException e ) 
	    {
	    }
	    
	    Log.d("ParseCarPacket CAR=" + packet.carID, " CAR TYPE=" + packet.type + " CAR DATA= " + packet.data + " CAR LEN= " + packet.length + " LONG DATA= " + longData);// + new String(packet.longData, 0, packet.length, "ISO-8859-1") + " " + eventData.driversData.size());
	    
	    //return;
	    DriverData dd = eventData.driversData.get(packet.carID-1);
	    int size_dd = dd.lapData.size();
	    //Log.d("MIDA VOLTES-CARPACKET ABANS: ",Integer.toString(size_dd));
	    int lap = 0;
	    int ibuf = 0;
	    PitData pd = new PitData();
	    switch(packet.type)
	    {    
	        case LTData.CarPacket.CAR_POSITION_UPDATE:
//	            dd.carID = packet.carID;
//	            dd.pos = packet.data;
//	            dd.numPits = 0;
//	            dd.colorData[LTData.RacePacket.RACE_POSITION] = LTData.Colors.RED;

//	            for (int i = 0; i < 14; ++i)
//	                dd.colorData[i] = LTData.Colors.DEFAULT;

	            if ((packet.carID-1) < eventData.driversData.size() && (packet.carID-1) >= 0 && dd.lapData.isEmpty())
	            {
	                dd.carID = packet.carID;
	                dd.lastLap.carID = packet.carID;
	                dd.pos = packet.data;
	                dd.colorData[LTData.RacePacket.RACE_POSITION] = LTData.Colors.RED;
	            }

	            break;

	        case LTData.RacePacket.RACE_POSITION:
	        //case LTData::PRACTICE_POSITION:
	        //case LTData::QUALI_POSITION:
	        	try
	        	{
		            ibuf = Integer.parseInt(longData);
		            if (ibuf > 0)
		            {
		                    dd.pos = dd.lastLap.pos = ibuf;
		                    dd.retired = false;
		            }
		            else
		                if ((dd.lastLap.sector1.toString().equals("STOP") ||
		                    dd.lastLap.sector2.toString().equals("STOP") ||
		                    dd.lastLap.sector3.toString().equals("STOP") ||
		                    dd.lastLap.lapTime.toString().equals("RETIRED")) &&
		                    eventData.flagStatus != LTData.FlagStatus.RED_FLAG)
		                dd.retired = true;
	        	}
	        	catch (Exception e) {}

	            //If the packet data isn't a number, probably the decryption has failed. Set the frame number to 0 and wait for another SYS=2 packet to obtain the keyframe again
//	            if (!ok)
//	                eventData.frame = 0;

	            if ((eventData.eventType == LTData.EventType.PRACTICE_EVENT || eventData.eventType == LTData.EventType.QUALI_EVENT) 
	            		&& !dd.lapData.isEmpty())
	            {
	            	LapData ld = dd.lapData.get(dd.lapData.size()-1);
	                ld.pos = ibuf;
	                dd.lapData.set(dd.lapData.size()-1, ld);

	                
	                if (!dd.posHistory.isEmpty())
	                    dd.posHistory.set(dd.posHistory.size()-1, ibuf);
	            }
	            dd.colorData[LTData.RacePacket.RACE_POSITION] = packet.data;
	            break;

	        case LTData.RacePacket.RACE_NUMBER:
	        //case LTData::PRACTICE_NUMBER:
	        //case LTData::QUALI_NUMBER:
	        	try
	        	{
	        		dd.colorData[LTData.RacePacket.RACE_NUMBER] = packet.data;
	        		
		            ibuf = Integer.parseInt(longData);
		            if (ibuf > 0)
		                dd.number = ibuf;			           
	        	}
	        	catch (Exception e) {}
	            break;

	        case LTData.RacePacket.RACE_DRIVER:
	        //case LTData::PRACTICE_DRIVER:
	        //case LTData::QUALI_DRIVER:
	        	try
	        	{
		            if (longData != "")// /*&& packet.length > 0*/ && QString(packet.longData).indexOf(QRegExp("[A-Z]")) != -1)//eventData.driversData[packet.carID-1].driver == "")
		            {	                
		                dd.driver = new String(longData);//LTData::getDriverName(s);
		            }
		            dd.colorData[LTData.RacePacket.RACE_DRIVER] = packet.data;
	        	} catch (Exception e) { }
	            break;

	        case LTData.RacePacket.RACE_GAP:
	        //case LTData::PRACTICE_BEST:
	        //case LTData::QUALI_PERIOD_1:
	            switch (eventData.eventType)
	            {
	                case LTData.EventType.RACE_EVENT:
	                	try
	                	{
		                    if (packet.length > -1)                    
		                    {
		                        dd.lastLap.gap = new String(longData);
	
		                        //after the start of the race we don't get the lap time so we have to add the lap here
		                        if (dd.lapData.isEmpty() && dd.posHistory.size() <= 1)
		                        {
		                            dd.lastLap.numLap = 1;
		                            dd.addLap(eventData);
		                        }
		                    }
		                    dd.colorData[LTData.RacePacket.RACE_GAP] = packet.data;
	
		                    //when driver is in the pits we need to update his position, gap and interval data
		                    if (dd.lastLap.lapTime.toString().equals("IN PIT"))
		                        dd.updateInPit();		                    		                    

	                	}catch (Exception e) { }
	                    break;

	                case LTData.EventType.PRACTICE_EVENT:
	                	try
	                	{
		                    if (packet.length > 0)
		                        dd.lastLap.lapTime.setTime(longData);
	
		                    dd.colorData[LTData.PracticePacket.PRACTICE_BEST] = packet.data;
	
//		                    if (dd.pos == 1 && dd.lastLap.lapTime.isValid() &&
//		                        dd.lastLap.lapTime.lessThan(eventData.FLTime))
		                    if (dd.pos == 1 && dd.lastLap.lapTime.isValid())
		                    {
		                        eventData.FLTime.setTime(dd.lastLap.lapTime);		                        
		                        eventData.FLDriver = new String(dd.driver);
		                        eventData.session107Percent = dd.lastLap.lapTime.calc107p();
//		                        for (int i = 0; i < eventData.driversData.size(); ++i)
//		                            eventData.driversData.get(i).updateGaps(eventData);
		                    }
	                	}catch (Exception e) { }
	                    break;

	                case LTData.EventType.QUALI_EVENT:
	                	try
	                	{
		                    dd.q1.setTime(longData);
		                    dd.lastLap.lapTime.setTime(longData);
	
		                    if (dd.pos == 1 && dd.q1.isValid() &&
		                            dd.q1.lessThan(eventData.FLTime))
		                    {
		                        eventData.FLTime.setTime(dd.q1.toString());
		                        for (int i = 0; i < eventData.driversData.size(); ++i)
		                            eventData.driversData.get(i).updateGaps(eventData);
		                    }
		                    if (dd.pos == 1 && dd.lastLap.lapTime.isValid())
		                    {
		                        eventData.FLTime.setTime(dd.lastLap.lapTime);
		                        eventData.FLDriver = new String(dd.driver);
		                        eventData.session107Percent = dd.lastLap.lapTime.calc107p();	
		                        		                        
		                    	eventData.qualiPeriod = 1;
		                    }
	
	
		                    dd.colorData[LTData.QualifyingPacket.QUALI_PERIOD_1] = packet.data;
	                	}catch (Exception e) { }
	                    break;
	            }
	            break;


	        case LTData.RacePacket.RACE_INTERVAL:
	        //case LTData::PRACTICE_GAP:
	        //case LTData::QUALI_PERIOD_2:
	            switch (eventData.eventType)
	            {
	                case LTData.EventType.RACE_EVENT:
	                	try
	                	{
		                    if (dd.pos == 1 && eventData.sessionStarted)
		                    {
		                        eventData.lapsCompleted = Integer.parseInt(longData);
		                        
		                        Log.d("dsr", "laps="+eventData.lapsCompleted);
		                        
		                        if (eventData.lapsCompleted == eventData.eventInfo.laps)
		                        	sessionTimer.stopTimer();
		                        
		                        else if (eventData.lapsCompleted > eventData.eventInfo.laps)
		                        	eventData.lapsCompleted = 0;
		                    }
	
		                    if (packet.length > -1)
		                        dd.lastLap.interval = new String(longData);
	
		                    if (dd.pos == 1 &&
		                       (dd.lapData.isEmpty() && dd.posHistory.size() <= 1))
		                        {
		                            dd.lastLap.numLap = 1;
		                            dd.addLap(eventData);
		                        }
	
		                    dd.colorData[LTData.RacePacket.RACE_INTERVAL] = packet.data;
	
		                    //when driver is in the pits we need to update his position, gap and interval data
		                    if (dd.lastLap.lapTime.toString().equals("IN PIT"))
		                        dd.updateInPit();
	                	}
	                	catch (Exception e) {}
	                    break;

	                case LTData.EventType.PRACTICE_EVENT:
	                    try
	                    {
		                	if (packet.length > 0)
		                    {
		                        dd.lastLap.gap = new String(longData);
	//	                        if (!eventData.driversData[packet.carID-1].lapData.isEmpty())
	//	                            eventData.driversData[packet.carID-1].lapData.last().gap = packet.longData;
		                    }
		                    dd.colorData[LTData.PracticePacket.PRACTICE_GAP] = packet.data;
	                    }catch (Exception e) { }
	                    break;

	                case LTData.EventType.QUALI_EVENT:
	                	try
	                	{
		                    dd.lastLap.lapTime.setTime(new String(longData));
		                    dd.q2.setTime(new String(longData));
		                    dd.colorData[LTData.QualifyingPacket.QUALI_PERIOD_2] = packet.data;
	
		                    if (dd.pos == 1 && dd.q2.isValid())
		                    {
		                    	if (dd.q2.lessThan(eventData.FLTime))
		                    	{
			                        eventData.FLTime.setTime(dd.q2);
			                        eventData.FLDriver = new String(dd.driver);
			                        for (int i = 0; i < eventData.driversData.size(); ++i)
			                            eventData.driversData.get(i).updateGaps(eventData);
		                    	}
		                    	eventData.qualiPeriod = 2;
		                    }		                    	
	                	}catch (Exception e) { }
	                    break;
	            }
	            break;

	        case LTData.RacePacket.RACE_LAP_TIME:
	        //case LTData::PRACTICE_SECTOR_1:
	        //case LTData::QUALI_PERIOD_3:
	        	
	            switch (eventData.eventType)
	            {
	                case LTData.EventType.RACE_EVENT:
	                    if (packet.length > -1)
	                    {
	                    	try
	                    	{
		                    	String str = new String(longData);
		                    	//Log.d("RACE_LAP_TIME", str);
		                        dd.lastLap.lapTime.setTime(str);		                        		                       
	
		                        if (!dd.lapData.isEmpty() && !str.equals("OUT"))
		                            dd.lastLap.numLap = dd.lapData.get(dd.lapData.size()-1).numLap + 1;
	
	
		                        if (dd.lapData.isEmpty() &&
		                            dd.lastLap.lapTime.toString().equals("IN PIT") &&
		                            dd.lastLap.numLap < eventData.lapsCompleted)
		                            dd.lastLap.numLap++;
	
		                        dd.addLap(eventData);
		                        size_dd = dd.lapData.size();
		                	    //Log.d("MIDA VOLTES-CARPACKET DESPRES: ",Integer.toString(size_dd));
		                        
	                    	} catch (Exception e) { }
	                    }
	                    dd.colorData[LTData.RacePacket.RACE_LAP_TIME] = packet.data;

	                    if (dd.lastLap.lapTime.toString().equals("RETIRED"))
	                        dd.retired = true;

	                    else if (/*eventData.driversData[packet.carID-1].retired &&*/
	                             !dd.lastLap.sector1.toString().equals("STOP") &&
	                             !dd.lastLap.sector2.toString().equals("STOP") &&
	                             !dd.lastLap.sector3.toString().equals("STOP"))
	                        dd.retired = false;

	                    break;

	                case LTData.EventType.PRACTICE_EVENT:
	                	try
	                	{
		                    if (packet.length > -1)
		                        dd.lastLap.sector1.setTime(new String(longData));
	
		                    dd.colorData[LTData.PracticePacket.PRACTICE_SECTOR_1] = packet.data;
	
		                    //save the session fastest sector 1 time
		                    if (packet.data == LTData.Colors.VIOLET)
		                    {
		                        if (dd.lastLap.sector1.toString() != "")
		                        {
		                            eventData.sec1Record[0] = new String(dd.driver);
		                            eventData.sec1Record[1] = new String(longData);
	
		                            eventData.sec1Record[2] = "" + dd.lastLap.numLap+1;
		                            eventData.sec1Record[3] = new String(sessionTimer.getTime());
		                        }
		                    }
	                	} catch (Exception e) { }
	                    break;	                    

	                case LTData.EventType.QUALI_EVENT:
	                	try
	                	{
		                    dd.lastLap.lapTime.setTime(new String(longData));
		                    dd.q3.setTime(new String(longData));
		                    dd.colorData[LTData.QualifyingPacket.QUALI_PERIOD_3] = packet.data;
	
		                    if (dd.pos == 1 && dd.q3.isValid())
		                    {
		                    	if (dd.q3.lessThan(eventData.FLTime))
		                    	{
			                        eventData.FLTime.setTime(dd.q3);
			                        eventData.FLDriver = new String(dd.driver);
			                        for (int i = 0; i < eventData.driversData.size(); ++i)
			                            eventData.driversData.get(i).updateGaps(eventData);
		                    	}
		                        
		                        eventData.qualiPeriod = 3;
		                    }
	                	} catch (Exception e) { }
	                    break;
	            }
	            break;        

	        case LTData.RacePacket.RACE_SECTOR_1:
	        //case LTData::PRACTICE_SECTOR_2:
	        //case LTData::QUALI_SECTOR_1:
	            switch (eventData.eventType)
	            {
	                case LTData.EventType.RACE_EVENT:
	                case LTData.EventType.QUALI_EVENT:
	                	try
	                	{
		                    if (packet.length > -1)
		                        dd.lastLap.sector1.setTime(new String(longData));
	
		                       dd.colorData[LTData.RacePacket.RACE_SECTOR_1] = packet.data;
	
		                    if (packet.data == LTData.Colors.VIOLET)
		                    {                                                  
		                        if (!dd.lastLap.sector1.toString().equals("") && dd.lastLap.sector1.lessEqual(new LapTime(eventData.sec1Record[1])))
		                        {
		                            eventData.sec1Record[0] = new String(dd.driver);
		                            eventData.sec1Record[1] = new String(dd.lastLap.sector1.toString());
	
		                            if (eventData.eventType == LTData.EventType.RACE_EVENT)
		                                eventData.sec1Record[2] = "" + (dd.lastLap.numLap+1);
		                            else
		                            {
		                                eventData.sec1Record[2] = "" + (dd.lastLap.numLap+1);
		                                eventData.sec1Record[3] = new String(eventData.remainingTime);
		                            }
		                        }
		                    }
	
		                    if (dd.lastLap.sector1.toString().equals("STOP") && eventData.flagStatus != LTData.FlagStatus.RED_FLAG)
		                        dd.retired = true;
	                	} catch (Exception e) { }
	                    break;

	                case LTData.EventType.PRACTICE_EVENT:
	                	try
	                	{
		                    if (packet.length > -1)
		                        dd.lastLap.sector2.setTime(new String(longData));
	
		                    dd.colorData[LTData.PracticePacket.PRACTICE_SECTOR_2] = packet.data;
	
		                    if (packet.data == LTData.Colors.VIOLET)
		                    {
		                        if (!dd.lastLap.sector2.toString().equals(""))
		                        {
		                            eventData.sec2Record[0] = new String(dd.driver);
		                            eventData.sec2Record[1] = new String(dd.lastLap.sector2.toString());
	
		                            eventData.sec2Record[2] = "" + (dd.lastLap.numLap+1);
		                            eventData.sec2Record[3] = new String(sessionTimer.getTime());
		                        }
		                    }
	                	}catch (Exception e) { }
	            }
	            break;

	        case LTData.RacePacket.RACE_PIT_LAP_1:
	        //case LTData::PRACTICE_SECTOR_3:
	        //case LTData::QUALI_SECTOR_2:
	            switch (eventData.eventType)
	            {
	                case LTData.EventType.RACE_EVENT:

	                	try
	                	{
		                    lap = Integer.parseInt(new String(packet.longData, 1, packet.longData.length-1, "ISO-8859-1"));
	
		                    if (lap != 0)
		                    {
		                        String time = "";
		                        switch (dd.numPits)
		                        {
		                            case 0: time = new String(dd.lastLap.sector3.toString()); break;
		                            case 1: time = new String(dd.lastLap.sector2.toString()); break;
		                            default: time = new String(dd.lastLap.sector1.toString()); break;
		                        }
	
		                        pd = new PitData(time, lap);
		                        dd.addPitStop(pd);
		                    }
		                    dd.colorData[LTData.RacePacket.RACE_PIT_LAP_1] = packet.data;
	                	}catch (UnsupportedEncodingException e) { }
	                    break;

	                case LTData.EventType.PRACTICE_EVENT: 
	                	try
	                	{
		                    if (packet.length > -1)                    
		                        dd.lastLap.sector3.setTime(longData);
	
		                    dd.colorData[LTData.PracticePacket.PRACTICE_SECTOR_3] = packet.data;
	
		                    if (packet.data == LTData.Colors.VIOLET)
		                    {
		                        if (!dd.lastLap.sector3.toString().equals(""))
		                        {
		                            eventData.sec3Record[0] = new String(dd.driver);
		                            eventData.sec3Record[1] = new String(dd.lastLap.sector3.toString());
	
		                            eventData.sec3Record[2] = "" + (dd.lastLap.numLap+1);
		                            eventData.sec3Record[3] = new String(sessionTimer.getTime());
		                        }
		                    }
	                	} catch (Exception e) { }
	                    break;

	                case LTData.EventType.QUALI_EVENT:
	                	try
	                	{
		                    if (packet.length > -1)
		                        dd.lastLap.sector2.setTime(longData);
	
		                    dd.colorData[LTData.QualifyingPacket.QUALI_SECTOR_2] = packet.data;
	
		                    if (packet.data == LTData.Colors.VIOLET)
		                    {
		                        if (!dd.lastLap.sector2.toString().equals("") && dd.lastLap.sector2.lessEqual(new LapTime(eventData.sec2Record[1])))
		                        {
		                            eventData.sec2Record[0] = new String(dd.driver);
		                            eventData.sec2Record[1] = new String(dd.lastLap.sector2.toString());
	
		                            eventData.sec2Record[2] = "" + (dd.lastLap.numLap+1);
		                            eventData.sec2Record[3] = new String(eventData.remainingTime);
		                        }
		                    }
	                	}catch (Exception e) { }
	                    break;
	            }
	            break;

	        case LTData.RacePacket.RACE_SECTOR_2:
	        //case LTData::PRACTICE_LAP:
	        //case LTData::QUALI_SECTOR_3:
	            switch (eventData.eventType)
	            {
	                case LTData.EventType.RACE_EVENT:
	                	try
	                	{
	                		dd.colorData[LTData.RacePacket.RACE_SECTOR_2] = packet.data;
	                		
		                    if (packet.length > -1)
		                        dd.lastLap.sector2.setTime(longData);
//		                    else
//		                    	dd.lastLap.sector2.setTime("");
	
		                    if (packet.data == LTData.Colors.VIOLET)
		                    {
		                        if (!dd.lastLap.sector2.toString().equals(""))
		                        {
		                            eventData.sec2Record[0] = new String(dd.driver);
		                            eventData.sec2Record[1] = new String(dd.lastLap.sector2.toString());
	
		                            eventData.sec2Record[2] = "" + (dd.lastLap.numLap+1);
		                        }
		                    }
	
		                    if (dd.lastLap.sector2.toString().equals("STOP")/* && eventData.flagStatus != LTData::RED_FLAG*/)
		                        dd.retired = true;
	                	} catch (Exception e) { }
	                    break;

	                case LTData.EventType.PRACTICE_EVENT:
//	                    if (packet.length > 0)
	                    
	                	try
	                	{
		                	ibuf = Integer.parseInt(longData);
		                    if (ibuf > 0)
		                        dd.lastLap.numLap = ibuf;
	
		                    dd.addLap(eventData);
		                    dd.colorData[LTData.PracticePacket.PRACTICE_LAP] = packet.data;
	                	} catch (Exception e) { }
	                    break;

	                case LTData.EventType.QUALI_EVENT:
	                	try
	                	{
		                    if (packet.length > -1)
		                        dd.lastLap.sector3.setTime(longData);
	
		                    dd.colorData[LTData.QualifyingPacket.QUALI_SECTOR_3] = packet.data;
	
		                    if (packet.data == LTData.Colors.VIOLET)
		                    {
		                        if (!dd.lastLap.sector3.toString().equals("") && dd.lastLap.sector3.lessEqual(new LapTime(eventData.sec3Record[1])))
		                        {
		                            eventData.sec3Record[0] = new String(dd.driver);
		                            eventData.sec3Record[1] = new String(dd.lastLap.sector3.toString());
	
		                            eventData.sec3Record[2] = "" + (dd.lastLap.numLap+1);
		                            eventData.sec3Record[3] = new String(eventData.remainingTime);
		                        }
		                    }
	                	} catch (Exception e) { }
	                    break;
	            }
	            break;

	        case LTData.RacePacket.RACE_PIT_LAP_2:
	        //case LTData::QUALI_LAP:
	            switch (eventData.eventType)
	            {
	                case LTData.EventType.RACE_EVENT:
	                	try
	                	{
		                    lap = Integer.parseInt(new String(packet.longData, 1, packet.longData.length-1, "ISO-8859-1"));
	
		                    if (lap != 0)
		                    {
		                        String time = "";
		                        switch (dd.numPits)
		                        {
		                            case 1: time = new String(dd.lastLap.sector3.toString()); break;
		                            default: time = new String(dd.lastLap.sector2.toString()); break;
		                        }
	
		                        pd = new PitData(time, lap);
		                        dd.addPitStop(pd);
		                    }
	
		                    dd.colorData[LTData.RacePacket.RACE_PIT_LAP_2] = packet.data;
	                	}catch (UnsupportedEncodingException e) { }
	                    break;

	                case LTData.EventType.QUALI_EVENT:
	                	try
	                	{
		                    dd.lastLap.numLap = Integer.parseInt(new String(longData));
		                    dd.addLap(eventData);
		                    dd.colorData[LTData.QualifyingPacket.QUALI_LAP] = packet.data;
	                	}catch (Exception e) { }
	                    break;
	            }
	            break;

	        case LTData.RacePacket.RACE_SECTOR_3:

	        	try
	        	{
	        		dd.colorData[LTData.RacePacket.RACE_SECTOR_3] = packet.data;
		            if (packet.length > -1)
		                dd.lastLap.sector3.setTime(new String(longData));
			            	            	
		            if ((packet.data == LTData.Colors.VIOLET) || (packet.data == LTData.Colors.GREEN))
		            {
		                if (packet.data == LTData.Colors.VIOLET && !dd.lastLap.sector3.toString().equals(""))
		                {		                	
		                    eventData.sec3Record[0] = new String(dd.driver);
		                    eventData.sec3Record[1] = new String(dd.lastLap.sector3.toString());
	
		                    eventData.sec3Record[2] = "" + (dd.lastLap.numLap);//.driversData[packet.carID-1].lastLap.numLap);
		                }
	
//		                dd.bestSectors.set(2, Pair.create(new LapTime(new String(longData)), dd.lapData.get(dd.lapData.size()-1).numLap));		                
		            }
		            //sector 3 time is sent after the lap time, therefore we have to update recently added lap
		            dd.updateLastLap();	
	
		            if (dd.lastLap.sector3.toString().equals("STOP") && eventData.flagStatus != LTData.FlagStatus.RED_FLAG)
		                dd.retired = true;
	        	}catch (Exception e) { }
	            break;

	        case LTData.RacePacket.RACE_PIT_LAP_3:
	        	try
	        	{
		            lap = Integer.parseInt(new String(packet.longData, 1, packet.longData.length-1, "ISO-8859-1"));
	
		            pd = new PitData(new String(dd.lastLap.sector3.toString()), lap);
		            dd.addPitStop(pd);
	
		            dd.colorData[LTData.RacePacket.RACE_PIT_LAP_3] = packet.data;
	        	}catch (Exception e) { }
	            break;

	        case LTData.RacePacket.RACE_NUM_PITS:
	        	try
	        	{
		            dd.numPits = Integer.parseInt(new String(longData));
		            dd.colorData[LTData.RacePacket.RACE_NUM_PITS] = packet.data;
	        	}catch (Exception e) { }
	            break;

	        case LTData.CarPacket.CAR_POSITION_HISTORY:
//	            if (packet.length - 1 > eventData.lapsCompleted)
//	                eventData.lapsCompleted = copyPacket.length - 1;
	            	            

	            if (dd.lapData.isEmpty())
	                dd.lastLap.numLap = copyPacket.length - 1;

	            dd.posHistory.clear();
	            for (int i = dd.posHistory.size(); i < copyPacket.longData.length; ++i)
	            {
	                dd.posHistory.add(new Integer((int)copyPacket.longData[i]));
	            }

	            break;

	         default:
//	            std::cout<<"CAR DEFAULT!! "<<packet.type<<" "<<packet.carID<<" "<<packet.data<<std::endl;
	            break;
	    }
	    dd.carID = packet.carID;
	    eventData.driversData.set(packet.carID-1, dd);	    

	    
//	    if (emitSignal)
//	        emit driverDataChanged(packet.carID);
	}

	public void guardarKey(int key) {
		provisionalKey = key;
		pKey = true;
		
	}
}
