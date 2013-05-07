package com.f1lt;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;
import org.xmlpull.v1.XmlPullParser;

import android.graphics.Color;
import android.util.Log;
import android.util.Xml;

class Packet
{
	int length;
	int type;
	int carID;
	int data;
	
	boolean encrypted;
//	List<Character> longData = new ArrayList<Character>(512);
	byte [] longData;
	
	public Packet(Packet p)
	{
		length = p.length;
		type = p.type;
		carID = p.carID;
		data = p.data;
		encrypted = p.encrypted;
		
		longData = new byte[p.longData.length];
		
		for (int i = 0; i < p.longData.length; ++i)
			longData[i] = p.longData[i];
	}
	
	public Packet() { }
}

class LTTeam implements Comparable<LTTeam>
{
	public String name = "";
	public int id = 0;
	
	public String driver1Name = "";
	public String driver1ShortName = "";
	public int driver1Number = 0;
	
	public String driver2Name = "";
	public String driver2ShortName = "";
	public int driver2Number = 0;
	
	public int compareTo(LTTeam lt)
	{
		//Log.d("LTData", "compareTo");
		if (driver1Number < lt.driver1Number)
			return -1;
		
		if (driver1Number > lt.driver1Number)
			return 1;
		
		return 0;
	}
	
	public static LTTeam readTeam(XmlPullParser parser)
	{
		//Log.d("LTData", "readTeam");
		LTTeam team = new LTTeam();					
        
		try
		{
			parser.require(XmlPullParser.START_TAG, null, "Team");
			team.id = Integer.parseInt(parser.getAttributeValue(null, "id"));
			
			
			while (parser.next() != XmlPullParser.END_TAG)
			{
				if (parser.getEventType() != XmlPullParser.START_TAG) 
				 {
				        continue;
				 }
				String name = parser.getName();
				
				if (name.equals("Name")) 
				{
					parser.require(XmlPullParser.START_TAG, null, "Name");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        team.name = parser.getText();
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "Name");
		        } 
				else if (name.equals("Driver1Name")) 
				{
					parser.require(XmlPullParser.START_TAG, null, "Driver1Name");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        team.driver1Name = parser.getText();
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "Driver1Name");
		        } 
				else if (name.equals("Driver1ShortName")) 
				{
					parser.require(XmlPullParser.START_TAG, null, "Driver1ShortName");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        team.driver1ShortName = parser.getText();
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "Driver1ShortName");
		        }
		        else if (name.equals("Driver1Number")) 
				{
		        	parser.require(XmlPullParser.START_TAG, null, "Driver1Number");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        team.driver1Number = Integer.parseInt(parser.getText());
				        parser.nextTag();
				    }
		        	parser.require(XmlPullParser.END_TAG, null, "Driver1Number");
		        }
		        else if (name.equals("Driver2Name")) 
				{
		        	parser.require(XmlPullParser.START_TAG, null, "Driver2Name");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        team.driver2Name = parser.getText();
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "Driver2Name");
		        } 
				else if (name.equals("Driver2ShortName")) 
				{
					parser.require(XmlPullParser.START_TAG, null, "Driver2ShortName");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        team.driver2ShortName = parser.getText();
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "Driver2ShortName");
		        }
		        else if (name.equals("Driver2Number")) 
				{
		        	parser.require(XmlPullParser.START_TAG, null, "Driver2Number");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        team.driver2Number = Integer.parseInt(parser.getText());
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "Driver2Number");
		        }
			}
			parser.require(XmlPullParser.END_TAG, null, "Team");
			
		}
		catch (Exception e) {}
		
		return team;
	}
}

class LTEvent
{
	public int no = 0;
	public String name = "";
	public String shortName = "";
	public String place = "";
	public int laps = 0;
	public String fpDate = "";
	public String raceDate = "";
	
	public LTEvent(LTEvent event)
	{
		no = event.no;
		name = new String(event.name);
		shortName = new String(event.shortName);
		place = new String(event.place);
		fpDate = new String(event.fpDate);
		raceDate = new String(event.raceDate);
		laps = event.laps;
	}
	
	public LTEvent() {} 
	
	public static LTEvent readEvent(XmlPullParser parser)
	{
		//Log.d("LTData/LTEvent", "readEvent");
		LTEvent event = new LTEvent();
				        
		try
		{			
			parser.require(XmlPullParser.START_TAG, null, "Race");		
			
			while (parser.next() != XmlPullParser.END_TAG)
			{				
				 if (parser.getEventType() != XmlPullParser.START_TAG) 
				 {
				        continue;
				 }
				String name = parser.getName();				
				
				if (name.equals("Number")) 
				{
					parser.require(XmlPullParser.START_TAG, null, "Number");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        event.no = Integer.parseInt(parser.getText());
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "Number");
		        } 
				else if (name.equals("Event")) 
				{
					parser.require(XmlPullParser.START_TAG, null, "Event");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        event.name = parser.getText();
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "Event");
		        } 
				else if (name.equals("ShortName")) 
				{
					parser.require(XmlPullParser.START_TAG, null, "ShortName");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        event.shortName = parser.getText();
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "ShortName");
		        }
				else if (name.equals("Place")) 
				{
					parser.require(XmlPullParser.START_TAG, null, "Place");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        event.place = parser.getText();
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "Place");
		        }
				else if (name.equals("Laps")) 
				{
					parser.require(XmlPullParser.START_TAG, null, "Laps");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        event.laps = Integer.parseInt(parser.getText());
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "Laps");
		        }
				else if (name.equals("PracticeDate")) 
				{
					parser.require(XmlPullParser.START_TAG, null, "PracticeDate");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        event.fpDate = parser.getText();
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "PracticeDate");
		        }
				else if (name.equals("RaceDate")) 
				{
					parser.require(XmlPullParser.START_TAG, null, "RaceDate");
					if (parser.next() == XmlPullParser.TEXT) 
					{
				        event.raceDate = parser.getText();
				        parser.nextTag();
				    }
					parser.require(XmlPullParser.END_TAG, null, "RaceDate");
		        }				
			}
			parser.require(XmlPullParser.END_TAG, null, "Race");
		}
		catch (Exception e) 
		{
		}
		
		return event;
	}
}

public class LTData 
{
	public static List<LTTeam> ltTeams = new ArrayList();
	public static List<LTEvent> ltEvents = new ArrayList();
	public static class CarPacket
    {
        public final static int CAR_POSITION_UPDATE = 0;
        public final static int CAR_POSITION_HISTORY = 15;
        
    }
	public static class RacePacket
    {
		public final static int RACE_POSITION 	= 1;
		public final static int RACE_NUMBER 	= 2;
		public final static int RACE_DRIVER 	= 3;
		public final static int RACE_GAP 		= 4;
		public final static int RACE_INTERVAL	= 5;
		public final static int RACE_LAP_TIME	= 6;
		public final static int RACE_SECTOR_1	= 7;
		public final static int RACE_PIT_LAP_1	= 8;
		public final static int RACE_SECTOR_2	= 9;
		public final static int RACE_PIT_LAP_2	= 10;
		public final static int RACE_SECTOR_3	= 11;
		public final static int RACE_PIT_LAP_3	= 12;
		public final static int RACE_NUM_PITS	= 13;
    }
	public static class PracticePacket
    {
		public final static int PRACTICE_POSITION	= 1;
		public final static int PRACTICE_NUMBER		= 2;
		public final static int PRACTICE_DRIVER		= 3;
		public final static int PRACTICE_BEST		= 4;
		public final static int PRACTICE_GAP		= 5;
		public final static int PRACTICE_SECTOR_1	= 6;
		public final static int PRACTICE_SECTOR_2	= 7;
		public final static int PRACTICE_SECTOR_3	= 8;
		public final static int PRACTICE_LAP		= 9;        
    }

    public static class QualifyingPacket
    {
    	public final static int QUALI_POSITION	= 1;
    	public final static int QUALI_NUMBER	= 2;
    	public final static int QUALI_DRIVER	= 3;
    	public final static int QUALI_PERIOD_1	= 4;
    	public final static int QUALI_PERIOD_2	= 5;
    	public final static int QUALI_PERIOD_3	= 6;
    	public final static int QUALI_SECTOR_1	= 7;
    	public final static int QUALI_SECTOR_2	= 8;
    	public final static int QUALI_SECTOR_3	= 9;
    	public final static int QUALI_LAP		= 10;
    }
    public static class SystemPacket
    {
        public static final int SYS_EVENT_ID 		= 1;
        public static final int SYS_KEY_FRAME 		= 2;
        public static final int SYS_VALID_MARKER 	= 3;
        public static final int SYS_COMMENTARY 		= 4;
        public static final int SYS_REFRESH_RATE 	= 5;
        public static final int SYS_NOTICE 			= 6;
        public static final int SYS_TIMESTAMP 		= 7;
        public static final int SYS_WEATHER 		= 9;
        public static final int SYS_SPEED 			= 10;
        public static final int SYS_TRACK_STATUS 	= 11;
        public static final int SYS_COPYRIGHT 		= 12;        
    }

    public static class WeatherPacket
    {
    	public static final int WEATHER_SESSION_CLOCK 	= 0;
    	public static final int WEATHER_TRACK_TEMP		= 1;
    	public static final int WEATHER_AIR_TEMP		= 2;
    	public static final int WEATHER_WET_TRACK		= 3;
    	public static final int WEATHER_WIND_SPEED		= 4;
    	public static final int WEATHER_HUMIDITY		= 5;
    	public static final int WEATHER_PRESSURE		= 6;
    	public static final int WEATHER_WIND_DIRECTION	= 7;        
    }

    public static class SpeedPacket
    {
    	public static final int SPEED_SECTOR1	= 1;
    	public static final int SPEED_SECTOR2	= 2;
    	public static final int SPEED_SECTOR3	= 3;
    	public static final int SPEED_TRAP		= 4;
    	public static final int FL_CAR			= 5;
    	public static final int FL_DRIVER		= 6;
    	public static final int FL_TIME			= 7;
    	public static final int FL_LAP			= 8;        
    }
    public static class EventType
    {
        public static final int RACE_EVENT = 1;
        public static final int PRACTICE_EVENT = 2;
        public static final int QUALI_EVENT = 3;
//        private int value;
//        public EventType(int val)
//        {
//        	this.value = val;
//        }
//        public void setValue(int i) { value = i; }
//        public int getValue() { return value; }
    }

    public static class FlagStatus
    {
    	public static final int GREEN_FLAG = 1;
    	public static final int YELLOW_FLAG = 2;
    	public static final int SAFETY_CAR_STANDBY = 3;
    	public static final int SAFETY_CAR_DEPLOYED = 4;
    	public static final int RED_FLAG = 5;
//        private int value;
//        private FlagStatus(int val)
//        {
//        	this.value = val;
//        }
//        public int getValue() { return value; }
    }

    public static class Colors
    {
    	public static final int DEFAULT		= 0;
    	public static final int WHITE		= 1;
    	public static final int PIT			= 2;
    	public static final int GREEN		= 3;
    	public static final int VIOLET		= 4;
    	public static final int CYAN		= 5;
    	public static final int YELLOW		= 6;
    	public static final int RED			= 7;
    	public static final int BACKGROUND	= 8;
    	public static final int BACKGROUND2	= 9;
    }
    
    public static int [] color = 
    	{
    		Color.rgb(150,150,150),
    		Color.rgb(220,220,220),
    		Color.rgb(231,31,31),
    		Color.rgb(0,255,0),
    		Color.rgb(255,0,255),
    		Color.rgb(0,255,255),
    		Color.rgb(255,255,0),
    		Color.rgb(231,31,31),
    		Color.rgb(0,0,0),
    		Color.rgb(27,27,27)
    	};
    
    public static int getPacketType(ByteArrayBuffer buf)
    {   
    	//Log.d("LTData", "getPacketType");
    	return ((buf.byteAt(0) & 0xe0) >> 5 & 7) | ((buf.byteAt(1) & 0x01) << 3);
    }
    public static int getCarPacket(ByteArrayBuffer buf)
    {   
    	//Log.d("LTData", "getCarPacket");
        return buf.byteAt(0) & 0x1f;
    }
    public static int getLongPacketData(ByteArrayBuffer buf)
    {
    	//Log.d("LTData", "getLongPacketData");
        return 0;
    }   
    //c
    public static int getShortPacketData(ByteArrayBuffer buf)
    {      
    	//Log.d("LTData", "getShortPacketData");
        return (buf.byteAt(1) & 0xe) >> 1;
    }
    //v
    public static int getSpecialPacketData(ByteArrayBuffer buf)
    {
    	//Log.d("LTData", "getSpecialPacketData");
        return (buf.byteAt(1) & 0xfe) >> 1;
    }
    //v
    public static int getLongPacketLength(ByteArrayBuffer buf)
    {
    	//Log.d("LTData", "getLongPacketLength");
        return (buf.byteAt(1) & 0xfe) >> 1;
    }
    //l
    public static int getShortPacketLength(ByteArrayBuffer buf)
    {
    	//Log.d("LTData", "getShortPacketLength");
        return (buf.byteAt(1) & 0xf0) == 0xf0 ? -1 : ((buf.byteAt(1) & 0xf0) >> 4);
    }
    public static int getSpecialPacketLength(ByteArrayBuffer buf)
    {
    	//Log.d("LTData", "getSpecialPacketLength");
        return 0;
    }
    
    public static boolean loadSeasonData(InputStream is)
    {
    	//Log.d("LTData", "loadSeasonData");
    	ltTeams.clear();
    	ltEvents.clear();
    	XmlPullParser parser = Xml.newPullParser();
    	
    	try
    	{
	        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	        parser.setInput(is, null);
	        parser.nextTag();
	        
	        parser.require(XmlPullParser.START_TAG, null, "Season");	        
	        
	        while (parser.next() != XmlPullParser.END_TAG) 
	        {
	            if (parser.getEventType() != XmlPullParser.START_TAG) 
	            {
	                continue;
	            }
	            
	            String name = parser.getName();
		        if (name.equals("Year"))
	            {
		        	parser.require(XmlPullParser.START_TAG, null, "Year");
		        	if (parser.next() == XmlPullParser.TEXT) 
					{
				        parser.nextTag();
				    }
		        	parser.require(XmlPullParser.END_TAG, null, "Year");		        	
	            }	            	            	                 
		        else if (name.equals("Races")) 
	            {
	            	parser.require(XmlPullParser.START_TAG, null, "Races");
	            	while (parser.next() != XmlPullParser.END_TAG) 
	            	{
	                    if (parser.getEventType() != XmlPullParser.START_TAG) 
	                    {
	                        continue;
	                    }
	                    name = parser.getName();
	                    if (name.equals("Race")) 
	                    {	                    	
	                    	ltEvents.add(LTEvent.readEvent(parser));
	                    	
//	                    	//Log.d("EVENT", "" + ltEvents.get(ltEvents.size()-1).no + " " + ltEvents.get(ltEvents.size()-1).name + " "
//	                    			+ ltEvents.get(ltEvents.size()-1).shortName + " " + ltEvents.get(ltEvents.size()-1).place + " " + ltEvents.get(ltEvents.size()-1).laps + 
//	                    			" " + ltEvents.get(ltEvents.size()-1).fpDate + " " + ltEvents.get(ltEvents.size()-1).raceDate);
	                    }
	            	}
	            	parser.require(XmlPullParser.END_TAG, null, "Races");
	            } 
	            else
	            if (name.equals("Teams"))
	            {
	            	parser.require(XmlPullParser.START_TAG, null, "Teams");
	            	while (parser.next() != XmlPullParser.END_TAG) 
		            {
		            	if (parser.getEventType() != XmlPullParser.START_TAG) 
	                    {
	                        continue;
	                    }
	                    name = parser.getName();
	                    if (name.equals("Team")) 
	                    {
	                    	ltTeams.add(LTTeam.readTeam(parser));
	                    	
//	                    	//Log.d("TEAM", "" + ltTeams.get(ltTeams.size()-1).id + " " + ltTeams.get(ltTeams.size()-1).name + " " + ltTeams.get(ltTeams.size()-1).driver1Name + 
//	                    			" " + ltTeams.get(ltTeams.size()-1).driver1ShortName + " " + ltTeams.get(ltTeams.size()-1).driver1Number);
	                    }
		            }
	            	parser.require(XmlPullParser.END_TAG, null, "Teams");
	            }
	        }  
	        parser.require(XmlPullParser.END_TAG, null, "Season");
	        
	        Collections.sort(ltTeams);
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
    	return  true;
    }
    
    public static LTEvent getEvent(String date)
    {
    	//Log.d("LTData/LTEvent", "getEvent");
    	String [] arr = date.split("-");
    	if (arr.length == 3)
    	{
    		int day = Integer.parseInt(arr[0]);
    		int month = Integer.parseInt(arr[1]);
    		
	    	for (int i = 0; i < ltEvents.size(); ++i)
	    	{
	    		String [] fp1Arr = ltEvents.get(i).fpDate.split("-");
	    		int day1 = Integer.parseInt(fp1Arr[0]);
	    		int month1 = Integer.parseInt(fp1Arr[1]);
	    		
	    		if (i < ltEvents.size()-1)
	    		{
		    		String [] fp2Arr = ltEvents.get(i+1).fpDate.split("-");		    				    		
		    		int day2 = Integer.parseInt(fp2Arr[0]);		    				    		
		    		int month2 = Integer.parseInt(fp2Arr[1]);
		    		
		    		
		    		if ((month1 == month2 && month == month1 && day >= day1 && day < day2) ||
		    			(month2 > month1 && month == month1 && day >= day1))// ||//month <= month2 &&  day >= day1))// && day < day2))
		    			return new LTEvent(ltEvents.get(i));
		    			
//		    		else if (month2 > month1 && month == month2 && day <= day2)
//		    			return new LTEvent(ltEvents.get(i+1));
	    		}
	    		else
	    		{
	    			if (month >= month1 && day >= day1)
			    		return new LTEvent(ltEvents.get(i));
	    		}
	    	}	    	
    	}
    	return new LTEvent();
    }
    
    public static int getTeamId(String driver)
    {
    	//Log.d("LTData/LTEvent", "getTeamId");
    	for (int i = 0; i < ltTeams.size(); ++i)
    	{
    		if (driver.toLowerCase().equals(ltTeams.get(i).driver1Name.toLowerCase()) || 
    			driver.toLowerCase().equals(ltTeams.get(i).driver2Name.toLowerCase()))
    			return ltTeams.get(i).id;
    	}
    	return -1;
    }
    
    public static int getTeamId(int number)
    {
    	//Log.d("LTData/LTEvent", "getTeamId");
    	for (int i = 0; i < ltTeams.size(); ++i)
    	{
    		if (number == ltTeams.get(i).driver1Number || 
    			number == ltTeams.get(i).driver2Number)
    			return i;
    	}
    	return -1;
    }
    
    public static String getShortDriverName(String driver)
    {
    	//Log.d("LTData", "getShortDriverName");
    	if (driver == null)
    		return "";
    	    	    	
    	for (int i = 0; i < ltTeams.size(); ++i)
    	{    		
    		if (driver.toLowerCase().equals(ltTeams.get(i).driver1Name.toLowerCase()))
    			return new String(ltTeams.get(i).driver1ShortName);
    		
    		if (driver.toLowerCase().equals(ltTeams.get(i).driver2Name.toLowerCase()))
    			return new String(ltTeams.get(i).driver2ShortName);
    	}
    	if (driver.length() < 6)
    		return driver;
    	return driver.substring(3, 6);
    }
    
    public static LTEvent getCurrentEvent()
    {
    	//Log.d("LTData/LTEvent", "getCurrentEvent");
    	Calendar gc = Calendar.getInstance();
    	String currDate = new StringBuilder().append(gc.get(Calendar.DAY_OF_MONTH)).append("-").append(gc.get(Calendar.MONTH)+1).append("-").append(gc.get(Calendar.YEAR)).toString();
    	
    	return getEvent(currDate);
    }
    
    public static String getDriverName(String driver)
    {
    	//Log.d("LTData", "getDriverName");
    	if (driver == null)
    		return "";
    	
    	if (driver.length() < 5)
    		return driver;
    	
    	for (int i = 0; i < ltTeams.size(); ++i)
    	{    		
    		if (driver.toLowerCase().equals(ltTeams.get(i).driver1Name.toLowerCase()))
    			return new String(ltTeams.get(i).driver1Name);
    		
    		if (driver.toLowerCase().equals(ltTeams.get(i).driver2Name.toLowerCase()))
    			return new String(ltTeams.get(i).driver2Name);
    	}
    	return new StringBuilder(driver.substring(0, 4)).append(driver.substring(4).toLowerCase()).toString();
    }
    
    public static String getTeamName(String driver)
    {
    	//Log.d("LTData", "getTeamName");
    	if (driver == null)
    		return "";
    
    	for (int i = 0; i < ltTeams.size(); ++i)
    	{    		
    		if (driver.toLowerCase().equals(ltTeams.get(i).driver1Name.toLowerCase()) || 
    				driver.toLowerCase().equals(ltTeams.get(i).driver2Name.toLowerCase()))
    			return new String(ltTeams.get(i).name);
    		
    	}
    	return "";
    }
    
    public static String getTeamName(int no)
    {
    	//Log.d("LTData", "getTeamName");
    	for (int i = 0; i < ltTeams.size(); ++i)
    	{    		
    		if (no == ltTeams.get(i).driver1Number || 
        		no == ltTeams.get(i).driver2Number)
    			return new String(ltTeams.get(i).name);
    		
    	}
    	return "";
    }
}
