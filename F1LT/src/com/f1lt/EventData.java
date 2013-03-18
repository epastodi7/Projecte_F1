package com.f1lt;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class EventData 
{
	public int key=-1, frame;    
	
    public String cookie = new String();

    LTEvent eventInfo;

    int eventType = 0;
    int flagStatus = 0;
    int sessionId = 0;
    int firstSessionId = 7066;

    String remainingTime = "";
    int lapsCompleted;
    int qualiPeriod;

    double airTemp = 0, humidity = 0;
    double windSpeed = 0, windDirection = 0;

    double pressure = 0, trackTemp = 0;

    boolean sessionStarted = false;

    int wetdry;

    String notice = new String();

    String commentary;
    String []sec1Speed = new String[12];
    String []sec2Speed = new String[12];
    String []sec3Speed = new String[12];
    String []speedTrap = new String[12];

    int FLNumber;
    String FLDriver = new String();
    LapTime FLTime = new LapTime();
    LapTime session107Percent = new LapTime();
    int FLLap = -1;

    String []sec1Record = new String[4];
    String []sec2Record = new String[4];
    String []sec3Record = new String[4];

    List<DriverData> driversData = new ArrayList<DriverData>();
    
	private EventData() 
	{ 
		for (int i = 0; i < LTData.ltTeams.size()*2; ++i)
		{
			if (i < 12)
			{
				sec1Speed[i] = new String();
				sec2Speed[i] = new String();
				sec3Speed[i] = new String();
				speedTrap[i] = new String();
			}
			
			if (i < 4)
			{
				sec1Record[i] = new String();
				sec2Record[i] = new String();
				sec3Record[i] = new String();
			}		
			driversData.add(new DriverData());
		}
//		FLLap = -1;
//	    sec1Record[2] = "-1";
//	    sec2Record[2] = "-1";
//	    sec3Record[2] = "-1";
//
//	    eventInfo.laps = 0;
//	    eventInfo.eventNo = 0;
//
//	    trackTemp = 0;
//	    airTemp = 0;
//	    windSpeed = 0;
//	    humidity = 0;
//	    pressure = 0;
//	    windDirection = 0;    
//
//	    sessionStarted = false;
	}
	
	public void clear()
	{
		key = -1;
	    lapsCompleted = 0;

	    flagStatus = LTData.FlagStatus.GREEN_FLAG;

	    trackTemp = 0;
	    airTemp = 0;
	    windSpeed = 0;
	    humidity = 0;
	    pressure = 0;
	    windDirection = 0;
	    sessionStarted = false;

	    commentary = "";
	    driversData.clear();
	    for (int i = 0; i < LTData.ltTeams.size()*2; ++i)
	    {
	        driversData.add(new DriverData());
	    }

	    FLNumber = 0;
	    FLDriver = "";
	    FLTime = new LapTime();
	    FLLap = 0;

	    for (int i = 0; i < 12; ++i)
	    {
	        sec1Speed[i] = "";
	        sec2Speed[i] = "";
	        sec3Speed[i] = "";
	        speedTrap[i] = "";

	        if (i < 4)
	        {
	            sec1Record[i] = "";
	            sec2Record[i] = "";
	            sec3Record[i] = "";
	        }
	    }
	}
	
	private static EventData instance = null;
	
	public static EventData getInstance()
	{
		if (instance == null)
			instance = new EventData();
		 
		
		return instance;
	}
	
	public int getDriverId(String driver)
	{
		for (int i = 0; i < driversData.size(); ++i)
	    {
	        if (driversData.get(i).driver == driver)
	            return driversData.get(i).carID;
	    }
	    return -1;
	}  
	
	public int getDriverId(int no)
	{
		for (int i = 0; i < driversData.size(); ++i)
	    {
	        if (driversData.get(i).number == no)
	            return driversData.get(i).carID;
	    }
	    return -1;
	}
	
	public DriverData getDriverData(int pos)
	{
		for (int i = 0; i < driversData.size(); ++i)
		{
			if (driversData.get(i).pos == pos)
				return driversData.get(i);
		}
		return null;
	}
	
	public String calculateInterval(DriverData d1, DriverData d2, int lap)
    {
        LapData ld1 = d1.getLapData(lap);
        LapData ld2 = d2.getLapData(lap);
        
        if (lap == -1 && !d1.lapData.isEmpty() && !d2.lapData.isEmpty())
        {
        	ld1 = d1.lastLap;//d1.lapData.get(d1.lapData.size()-1);
        	ld2 = d2.lastLap;//d2.lapData.get(d2.lapData.size()-1);
        }

        if (ld1 == null || ld2 == null)
        	return "";
        
        String gap1 = ld1.gap;
        String gap2 = ld2.gap;

        if ((ld1.lapTime.toString().equals("") && ld1.gap.equals("")) || 
        	(ld2.lapTime.toString().equals("") && ld2.gap.equals("")))
            return "";
        
        if (ld1.pos == 1)
        	return "-" + (gap2.equals("") ? "1L <" : gap2) + (gap2.contains("L") ? " <" : "");
        
        if (ld2.pos == 1)
        	return "+" + (gap1.equals("") ? "1L <" : gap1) + (gap1.contains("L") ? " <" : "");

        if ((!gap1.equals("") && !gap2.equals("") && gap1.getBytes()[gap1.length()-1] != 'L' && 
        		gap2.getBytes()[gap2.length()-1] != 'L') ||
           ((ld1.pos == 1 && gap1.equals("")) || (ld2.pos == 1 && gap2.equals(""))))
        {
            double interval = 0.0;
            
            try
            {
            	interval = Double.parseDouble(gap1) - Double.parseDouble(gap2);
            }
            catch(NumberFormatException e) {}
            
            DecimalFormat df = new DecimalFormat("#0.0");    	    
            String sInterval  = df.format(interval);
//            String sInterval = QString::number(interval, 'f', 1);
            if (interval > 0)
                sInterval = "+" + sInterval;

            return sInterval;
        }
        else if ((!gap1.equals("") && gap1.contains("L")) || 
        		(!gap2.equals("") && gap2.contains("L")) ||
                (gap1.equals("") || gap2.equals("")))
        {
            int pos1 = ld1.pos;
            int pos2 = ld2.pos;

            boolean neg = true;
            if (pos2 < pos1)
            {
                int tmp = pos1;
                pos1 = pos2;
                pos2 = tmp;
                neg = false;
            }

            List<String> intervals = new ArrayList<String>();
//            intervals.reserve(pos2 - pos1);
            for (int i = 0; i < driversData.size(); ++i)
            {
                LapData ld = driversData.get(i).getLapData(lap);
                
                if (lap == -1 && !driversData.get(i).lapData.isEmpty())
                	ld = driversData.get(i).lastLap;//lapData.get(driversData.get(i).lapData.size()-1);
                
                if (ld == null)
                	continue;
                
                int pos = ld.pos;
                if (pos > pos1 && pos <= pos2)
                {
                    if (ld.interval != "" && ld.interval.contains("L"))
                        return neg ? "-1L <" : "+1L <";

                    intervals.add(ld.interval);
                }
            }
            double interval = 0.0;
            
            try
            {
	            for (int i = 0; i < intervals.size(); ++i)
	                interval += Double.parseDouble(intervals.get(i));
            }
            catch (NumberFormatException e) { }

            if (neg && ld1.lapTime.isValid() && interval > ld1.lapTime.toDouble())
                return "-1L <";
            if (!neg && ld2.lapTime.isValid() && interval > ld2.lapTime.toDouble())
                return "+1L <";


            DecimalFormat df = new DecimalFormat("#0.0");    	    
            String sInterval  = df.format(interval);
            
            if (neg)
                sInterval = "-" + sInterval;
            else
            	sInterval = "+" + sInterval;
                        
            return sInterval;
        }

        return "";
    }
	
}
