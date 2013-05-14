package com.f1lt;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class SessionTimer// extends Thread
{
	private String time = "";
	private boolean timerRunning = false;
	private Thread timerThread; 
	
	private Handler handler;	
			
	public SessionTimer(Handler h)
	{
		handler = h;
	}
	
	public boolean isTimerRunning()
	{
		Log.d("SessionTimer", "isTimerRunning");
		return timerRunning;
	}
	
	public void setTime(String t)
	{
		Log.d("SessionTimer", "setTime");
		String [] arr = t.split(":");
		if (arr.length == 3)
		{
			time = new String(t);
			
			if (arr[1].length() == 1)
			{
				arr[1] = "0" + arr[1];
			
				time = arr[0] + ":" + arr[1] + ":" + arr[2];
			}
		}
		
		if (arr.length == 2)
		{
			time = "0:";
			
			if (arr[0].length() == 1)
				arr[0] = "0" + arr[0];
			
			time += arr[0] + ":" + arr[1];
		}
	}
	
	public String getTime()
	{
		Log.d("SessionTimer", "getTime");
		return time;
	}
	
	public void startTimer()
	{
		Log.d("SessionTimer", "startTimer");
		if (timerRunning)
			return;
		
		timerRunning = true;
		
		timerThread = new Thread()
		{
			public void run()
			{
				while (timerRunning)
				{
					decrementTime();			
								
					try
					{
						Thread.sleep(1000);
					}
					catch(InterruptedException ie) 
					{
						break;
					}
				}
			}
		};
		timerThread.start();
		
//		if (!isAlive())
//		{
//			Log.d("timers", "startingtimer");
//			this.start();
//		}
	}
	
	public void stopTimer()
	{
		Log.d("SessionTimer", "stopTimer");
		timerRunning = false;
	}
	
//	public void run()
//	{
//		while (timerRunning)
//		{
//			decrementTime();			
//						
//			try
//			{
//				Thread.sleep(1000);
//			}
//			catch(InterruptedException ie) 
//			{
//				break;
//			}
//		}
//	}
	
	public void decrementTime()
	{
		Log.d("SessionTimer", "decrementTime");
		if (EventData.getInstance().eventType != LTData.EventType.PRACTICE_EVENT && EventData.getInstance().flagStatus == LTData.FlagStatus.RED_FLAG)
			return;
		
		int hour = 0;
		int min = 0;
		int sec = 0;
		
		if (time != "")
		{
			String [] timeArray = time.split(":");
			
			if (timeArray.length == 3)
			{
				hour = Integer.parseInt(timeArray[0]);
				min = Integer.parseInt(timeArray[1]);
				sec = Integer.parseInt(timeArray[2]);
				
//				if (min == 0)
//					min = Integer.parseInt(timeArray[1].substring(1));
//				
//				if (sec == 0)
//					sec = Integer.parseInt(timeArray[2].substring(1));
			}
			else if (timeArray.length == 2)
			{
				min = Integer.parseInt(timeArray[0]);
				sec = Integer.parseInt(timeArray[1]);
				
//				if (min == 0)
//					min = Integer.parseInt(timeArray[1].substring(1));
//				
//				if (sec == 0)
//					sec = Integer.parseInt(timeArray[2].substring(1));
			}
			
			--sec;
			if (sec < 0)
			{
				sec = 59;
				--min;
				
				if (min < 0)
				{
					min = 59;
					--hour;
					
					if (hour < 0)
					{
						sec = min = hour = 0;
						timerRunning = false;
						timerStopped();
						//emit STOP!!
					}
				}
			}
			
			time = new StringBuilder(""+hour).append(min < 10 ? ":0" +min : ":" + min).
					append(sec < 10 ? ":0"+sec : ":"+sec).toString();
			
			timerUpdated();
		}
	}
	
	public void timerUpdated()
	{
		Log.d("SessionTimer", "timerUpdated");
		if (handler == null)
			return;
		
		handler.post(new Runnable() 
		{
			public void run()
			{
				DataStreamReader.getInstance().onSessionTimerUpdated(time, false);
			}
		});
	}
	
	public void timerStopped()
	{
		Log.d("SessionTimer", "timerStopped");
		if (handler == null)
			return;
		
		handler.post(new Runnable() 
		{
			public void run()
			{
				DataStreamReader.getInstance().onSessionTimerUpdated(time, true);
			}
		});
	}
}
