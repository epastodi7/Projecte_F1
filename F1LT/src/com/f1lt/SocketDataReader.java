package com.f1lt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

import android.os.Handler;
import android.util.Log;

public class SocketDataReader //extends Thread
{
	private String host;
	private int port = 0;
	private boolean connectionEstablished = false;
	private Socket socket;
	private BufferedInputStream inputStream;
	private BufferedOutputStream outputStream;	
	private Handler handler;
	private DataStreamReader context;
	private Thread pingThread;
	private int tryReconnect;
	private byte bytes[] = new byte[8192];
	
	private boolean noPing = false;
	
	public boolean isConnected()
	{
		Log.d("SocketDataReader", "isConnected");
		return connectionEstablished;
	}
	
	public void openStream(final String host, final int port, Handler handler, DataStreamReader context)
	{	
		Log.d("SocketDataReader", "openStream");
		if (host == this.host || connectionEstablished)
			return;
		
		this.host = host;
		this.port = port;
		this.handler = handler;
		this.context = context;
		
		tryReconnect = 3;
		
		try
		{	
			socket = new Socket(host, port);
			outputStream = new BufferedOutputStream(socket.getOutputStream());
			inputStream = new BufferedInputStream(socket.getInputStream());
			connectionEstablished = true;
			
								
			pingThread = new Thread()
			{
				public void run()
				{									
					while(!noPing)
					{
						try
						{
							Thread.sleep(900);
							ping();
						}
						catch (InterruptedException e) 
						{
							break;
						}
					}							
				}													
			};
			pingThread.start();
		
		}
		catch(Exception e)
		{			
			sendDataBlock(null, 0, false);
			
//			if (parentThread != null)
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException ie) { }
			}			
		}					
	}
	
	public void reconnect()
	{
		Log.d("SocketDataReader", "reconnect");
		try
		{
			EventData.getInstance().frame = 0;
			socket.close();
			socket = new Socket(host, port);
			outputStream = new BufferedOutputStream(socket.getOutputStream());
			inputStream = new BufferedInputStream(socket.getInputStream());
			connectionEstablished = true;
		}
		catch (Exception e)
		{
			
		}
	}
	
	public void closeStream()
	{
		Log.d("SocketDataReader", "closeStream");
		try
		{			
			if (socket != null)
				socket.close();
			
			host = "";
			noPing = true;
			connectionEstablished = false;
			
			if (pingThread != null)
				pingThread.interrupt();
		}
		catch(Exception e) 
		{
		}
		
	}
	
	public void ping()
	{
		try
		{
			//Log.d("SocketDataReader", "PingOK");
			outputStream.write(16);
			outputStream.flush();
		}
		catch(Exception e)
		{
			Log.d("SocketDataReader", "PingException");
			if (--tryReconnect > 0)			
				reconnect();
			
			
			else
			{
				sendDataBlock(null, 1, false);				
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException ie) { }
			}
				
		}
	}
	
	public void readStream()
	{			
		try
		{			
			Log.d("SocketDataReader", "reading stream...");
			
			
			int n = 0;
//			while ((n = inputStream.read(bytes, 0, 512)) > 0)
			{
				n = inputStream.read(bytes, 0, 8192);
						
//			char data[] = new char[n];
//				Log.d(this.getClass().getName(), "got " + n + " bytes");
				
	//			for (int i = 0; i < n; ++i)
	//			{
	//				if (bytes[i] < 0)
	//					data[i] = (char)(bytes[i]+256);
	//				else
	//					data[i] = (char)bytes[i];
	//			}
				if (n > 0)
					sendDataBlock(bytes, n, true);
			}
		}
		catch(Exception e)
		{
			if (--tryReconnect > 0)
				reconnect();
			
			
			else
			{
				sendDataBlock(null, 1, false);			
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException ie) { }
			}
//			sendDataBlock(null, 0, false);
		}
		
	}
	private void sendDataBlock(final byte [] data, final int bytes, final boolean result)
	{
		Log.d("SocketDataReader", "sendDataBlock");
		if (handler == null || context == null)
			return;
		
		handler.post(new Runnable() 
		{
			public void run()
			{
				((DataStreamReader) context).onDataBlockObtained(data, bytes, result);
			}
		});
	}	
}
