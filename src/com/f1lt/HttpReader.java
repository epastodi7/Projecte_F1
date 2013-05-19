package com.f1lt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;

public class HttpReader 
{		
	private static String email;
	private static String passwd;
	private static Cookie cookie;
	private static String host;
	private static String loginHost;
	private static int decryptionKey = 0;
    private static String RUTA_SAVE = "";

	
	public HttpReader()
	{	
	}
	
	private static void sendAuthenticationCookie(final Handler handler, final Context context, final boolean result)
	{
		Log.d("HttpReader", "sendAuthenticationCookie");
		if (handler == null || context == null)
			return;
		
		handler.post(new Runnable() 
		{
			public void run()
			{
				((F1LTActivity) context).onAuthorized(cookie, result);
			}
		});
	}
	private static void sendKeyFrame(final Handler handler, final DataStreamReader thread, final boolean result, final byte[] buf, final int bytes)
	{
		Log.d("HttpReader", "sendKeyFrame");
		if (handler == null || thread == null)
			return;
		
		handler.post(new Runnable() 
		{
			public void run()
			{
//				((F1LTActivity) context).onKeyFrameObtained(buf, bytes, result);
				((DataStreamReader) thread).onKeyFrameObtained(buf, bytes, result);
			}
		});
	}
	private static void sendDecryptionKey(final Handler handler, final DataStreamReader thread, final boolean result)
	{
		Log.d("HttpReader", "sendDecryptionKey");
		if (handler == null || thread == null)
			return;
		
		handler.post(new Runnable() 
		{
			public void run()
			{
				((DataStreamReader) thread).onDecryptionKeyObtained(decryptionKey, result);
			}
		});
	}
			
	
	public static void authorize(String host, String loginHost, String email, String passwd, Handler handler, Context context)
	{
		Log.d("HttpReader", "authorize");
		HttpReader.host = host;
		HttpReader.loginHost = loginHost;
		HttpReader.email = email;
		HttpReader.passwd = passwd;
		
		try 
		{
       	 	URL url = new URL(loginHost);
       	 
       	 	DefaultHttpClient httpclient = new DefaultHttpClient();

            HttpGet httpget = new HttpGet(url.toString());

            
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                entity.consumeContent();
            }
            

            List<Cookie> cookies = httpclient.getCookieStore().getCookies();
           
            HttpPost httpost = new HttpPost(url.toString());

            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("email", email));
            nvps.add(new BasicNameValuePair("password", passwd));
            httpost.addHeader("Content-Type","application/x-www-form-urlencoded");
            

            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            response = httpclient.execute(httpost);
            
            entity = response.getEntity();

            if (entity != null) {
                entity.consumeContent();
            }

            cookies = httpclient.getCookieStore().getCookies();                                        
            
            boolean result = false;
            if (!cookies.isEmpty()) 
            {
                for (int i = 0; i < cookies.size(); i++) 
                {
                	if(cookies.get(i).getName().equals("USER"))
                	{   
                		HttpReader.cookie = cookies.get(i);
                		result = true;            
                		break;  
                	}
                }
            }           
            HttpReader.sendAuthenticationCookie(handler, context, result);
//            return sb.toString();
       	 
            
        } 
		catch (IOException e) 
		{
			HttpReader.sendAuthenticationCookie(handler, context, false);
//            return "Unable to retrieve web page. URL may be invalid.";
		}
	}
	public static Thread attemptAuth(final String host, final String loginHost, final String email, final String passwd, final Handler handler, final Context context)
	{
		Log.d("HttpReader", "attemptAuth");
		Thread thread = new Thread()
		{
			public void run()
			{
				HttpReader.authorize(host, loginHost, email, passwd, handler, context);				
			}
		};
		thread.start();
		return thread;
	}
	
	public static Thread attemptAuth(final Handler handler, final Context context)
	{
		Log.d("HttpReader", "attemptAuth");
		Thread thread = new Thread()
		{
			public void run()
			{
				HttpReader.authorize(host, loginHost, email, passwd, handler, context);				
			}
		};
		thread.start();
		return thread;
	}
	
	public static void obtainKeyFrame(final Handler handler, final DataStreamReader thread, final int frame)
	{
		Log.d("HttpReader", "obtainKeyFrame");
		try
		{
			String url = "";
			if (frame > 0) 
			{
				int ftmp = Integer.toString(frame).length();
				String zeros = "";
				for(int i=ftmp; i<5; ++i)
					zeros += "0";
				url = "/keyframe_" + zeros + Integer.toString(frame) + ".bin";
			} 
			else 
				url = "/keyframe.bin";			
			
			DefaultHttpClient httpclient = new DefaultHttpClient();

	        HttpGet httpget = new HttpGet(host + url);

	        HttpResponse response = httpclient.execute(httpget);
	        HttpEntity entity = response.getEntity();

	        
	        InputStream is = entity.getContent();
//	    	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    	byte[] data = new byte[65535];
	    	ByteArrayBuffer buf = new ByteArrayBuffer(0);
	    	int n = 0;
//	    	char[] dataBlock = new char[65535];
	    	while ((n = is.read(data, 0, data.length)) > 0)
	    	{
	    		buf.append(data, 0, n);
	    	}
	    	
	    	if (entity != null) 	    	
	            entity.consumeContent();
	        
//	    	String asdf = "";
//	    	for(int i = 0; i < firstCount; ++i)
//	    	{
//	    		if(data[i] < 0)
//	    			dataBlock[i] = (char)(data[i] + 256);
//	    		else
//	    			dataBlock[i] = (char)(data[i]);
//	    		asdf += dataBlock[i];
//	    	}
//	    	EventData.getInstance().frame = frame == 0 ? 1 : frame;
	    	sendKeyFrame(handler, thread, true, buf.toByteArray(), buf.length());
//	    	buffer.flush();
		}
		catch (IOException e) 
		{
			sendKeyFrame(handler, thread, false, null, 0);
//            return "Unable to retrieve web page. URL may be invalid.";
		}
	}
	public static Thread attemptObtainKeyFrame(final int frame, final Handler handler, final DataStreamReader thread, String ruta)
	{
		Log.d("HttpReader", "attemptObtainKeyFrame");
		RUTA_SAVE = ruta;
		Thread _thread = new Thread()
		{
			public void run()
			{
				HttpReader.obtainKeyFrame(handler, thread, frame);				
			}
		};
		_thread.start();
		return _thread;
	}
	
	public static int obtainDecryptionKey(final Handler handler, final DataStreamReader thread, final String eventNo)
	{
		Log.d("HttpReader", "obtainDecryptionKey");
		try
		{
			
			
//			
//			DefaultHttpClient httpclient = new DefaultHttpClient();
//
//			Log.d("ENC", "ENC get");
//	        HttpGet httpget = new HttpGet(url);
//
//	        Log.d("ENC", "ENC response");
//	        HttpResponse response = httpclient.execute(httpget);
//	        Log.d("HttpReader dec", "ENC STATUS " + response.getStatusLine().toString());
//	        HttpEntity entity = response.getEntity();
//
//	        
//	        InputStream is = entity.getContent();
//	    	
////	    	if (entity != null) 	    	
////	            entity.consumeContent();
//	        
////	    	BufferedReader in = null;
//
////            in = new BufferedReader(new InputStreamReader(is));
//	        
//	        byte[] tmp = new byte[1024];
//	        
//	        int n = is.read(tmp, 0, 1024);
//	        String str = "";
//	        str = (new StringBuilder(String.valueOf(str))).append(new String(tmp, 0, n, "ISO-8859-1")).toString();
//	        decryptionKey = (int)(Long.parseLong(str, 16) & -1L);
//	        Log.d("ENC", "ENC dk="+decryptionKey+ ", str="+str);
	        
	        
			URL url = new URL(host + "/reg/getkey/" + eventNo + ".asp?auth=" + cookie.getValue());
	        
	        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	        try 
	        {
	        	InputStream in = new BufferedInputStream(urlConnection.getInputStream());
	        	
	        	byte[] tmp = new byte[1024];
	        	
	        	int n = in.read(tmp, 0, 1024);
	        	
		        String str = "";
		        str = (new StringBuilder(String.valueOf(str))).append(new String(tmp, 0, n, "ISO-8859-1")).toString();
		        decryptionKey = (int)(Long.parseLong(str, 16) & -1L);
		        
		        
		        //Creem un arxiu per copiar a dins
		        /*
		        Time time = new Time();
		        time.setToNow();
		        */
		        File sdCard = Environment.getExternalStorageDirectory();
		        File dir = new File (sdCard.getAbsolutePath() + RUTA_SAVE);
		        String nom = "DadesKEY.txt";
		        //nom=nom.concat(time.toMillis(false)+".txt");
		        dir.mkdirs();
		        File file = new File(dir, nom);
		        Log.d("Nom fitxer KEY: ", file.getName());
		        
		        String key = Integer.toString(decryptionKey);
		        Log.d("GUARDAT KEY.", key);
		        byte[] bytes = key.getBytes("UTF8");
		        
		        try {
		        	FileOutputStream f = new FileOutputStream(file);
		            f.write(bytes);
		            f.flush();
		            f.close();
		            Log.d("GUARDAT KEY STRING", key);
		        } catch (Exception e) {
		            Log.e("ERROR", "Guardant Key", e);
		        }
		        
//	          readStream(in);	         
	        }
	        catch (Exception e)
	        {
	        }
	        finally 
	        {
	        	urlConnection.disconnect();
	        }
	        

	        sendDecryptionKey(handler, thread, true);
	        return decryptionKey;
	        
	        
//			for (int i = 0; i < tmpCount; i++) 
//			{
//				if ((tmp[i] >= '0') && (tmp[i] <= '9')) 
//				{
//					decryptionKey = (decryptionKey << 4) | (tmp[i] - '0');
//				} 
//				else if ((tmp[i] >= 'a') && (tmp[i] <= 'f')) 
//				{
//					decryptionKey = (decryptionKey << 4) | (tmp[i] - 'a' + 10);
//				}
//				else if ((tmp[i] >= 'A') && (tmp[i] <= 'F')) 
//				{
//					decryptionKey = (decryptionKey << 4) | (tmp[i] - 'A' + 10);
//				} 
//				else 
//				{
//					break;
//				}
//			}
			
//	    	buffer.flush();
		}
		catch (IOException e) 
		{
			sendDecryptionKey(handler, thread, false);	
			return 0;
//            return "Unable to retrieve web page. URL may be invalid.";
		}
	}
	public static Thread attemptObtainDecryptionKey(final String eventNo, final Handler handler, final DataStreamReader thread)
	{
		Log.d("HttpReader", "attemptObtainDecryptionKey");
		Thread _thread = new Thread()
		{
			public void run()
			{
				HttpReader.obtainDecryptionKey(handler, thread, eventNo);				
			}
		};
		_thread.start();
		return _thread;
	}
}

