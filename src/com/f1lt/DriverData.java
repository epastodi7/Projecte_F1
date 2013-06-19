package com.f1lt;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;
import android.util.Pair;

class LapTime
{
	private String time = "";
	
	public LapTime() { }
	public LapTime(String t) { time = new String(t); }
	public LapTime(LapTime t) { time = new String(t.time); }
	public LapTime(int ms)
	{
		int msec = ms;
	    int sec = ms / 1000;
	    int min = 0;

	    if (sec > 0)
	        msec = ms % (sec * 1000);


	    if (sec > 60)
	    {
	        min = sec / 60;

	        if (min > 0)
	            sec = sec % (min * 60);
	    }
	    time = new LapTime(min, sec, msec).toString();
	}
	public LapTime(int m, int s, int ms)
    {
        String strM = Integer.toString(m);
        String strS = Integer.toString(s);
        String strMS = Integer.toString(ms);

        time = strM + (s < 10 ? ":0": ":") + strS + (ms < 100 ? (ms < 10 ? ".00" : ".0") : ".")  + strMS;
    }
	public void setTime(String time)
	{
		//Log.d("DriverData", "setTime-str");
		this.time = new String(time);
	}
	public void setTime(LapTime lt)
	{
		//Log.d("DriverData", "setTime-lt");
		this.time = new String(lt.time);
	}
	public String toString() 
	{ 
		return time; 
	}
	
	public boolean isValid()
    {
		
        if (time == "")
            return false;

        int idx = time.indexOf(":");

        try
        {
	        if (idx > -1)
	        {
	    		Integer.parseInt(time.substring(0, idx));
	        }
	
	        int idx2 = time.indexOf(".", (idx < 0 ? 0 : idx));
	        if (idx2 > -1)
	        {
	        	Integer.parseInt(time.substring(idx+1, idx2));
	
	            Integer.parseInt(time.substring(idx2 + 1, time.length()));
	
	        }
	        else
	        	return false;	        	       
        }
        catch (NumberFormatException e)
        {
        	return false;
        }

        return true;
    }
	
	public int toMsecs()
	{
		if (!isValid())
	        return 0;

	    int idx = time.indexOf(":");
	    int min=0, sec=0, msec=0;

	    if (idx > -1)
	        min = Integer.parseInt(time.substring(0, idx));

	    int idx2 = time.indexOf(".", idx < 0 ? 0 : idx);
	    if(idx2 > -1)
	    {
	        sec = Integer.parseInt(time.substring(idx+1, idx2));
	        String strMS = time.substring(idx2+1, time.length());

	        msec = Integer.parseInt(strMS) * (strMS.length() < 3 ? (strMS.length() < 2 ? 100 : 10) : 1);
	    }

	    msec += sec * 1000 + min * 60000;
	    return msec;
	}
	public String toSecs()
	{
		double sec = (double)(toMsecs() / 1000.0);
		DecimalFormat df = new DecimalFormat("#0.000");
	    return df.format(sec);
	}
	
	public double toDouble()
    {
        if (isValid())
            return (double)(toMsecs() / 1000.0);

        return 0.0;
    }
	
	public LapTime calc107p()
    {
        double msecs = toMsecs();
        msecs = msecs * 1.07;

        return new LapTime((int)(Math.round(msecs)));
    }	
	
	public LapTime sum(final LapTime lt)
	{
		//Log.d("DriverData", "LapTime-sum");
	    return new LapTime(toMsecs() + lt.toMsecs());
	}
	public LapTime diff(final LapTime lt)
	{
		//Log.d("DriverData", "LapTime-diff");
	    return new LapTime(toMsecs() - lt.toMsecs());
	}
	
	public boolean lessThan(LapTime lt)
	{
		//Log.d("DriverData", "LapTime-lessThan");
		if (isValid() && !lt.isValid())
			return true;
		
		if (!isValid() && lt.isValid())
			return false;
		
		return toMsecs() < lt.toMsecs();
	}
	public boolean lessEqual(LapTime lt)
	{
		//Log.d("DriverData", "LapTime-lessEqual");
		if (isValid() && !lt.isValid())
			return true;
		
		if (!isValid() && lt.isValid())
			return false;
		
		return toMsecs() <= lt.toMsecs();
	}
	
	public boolean equals(Object b)
	{
		//Log.d("DriverData", "LapTime-equals");
		if (this == b)
			return true;
		
		if (b == null)
			return false;
		
		if (!this.getClass().equals(b.getClass()))
			return false;
		
		return toMsecs() == ((LapTime)b).toMsecs();
	}
}

class PitData implements Comparable<PitData>
{
	String pitTime = "";
    int pitLap = -1;
    
    public PitData() { }
    public PitData(String t, int p) 
    {
    	pitTime = t;
    	pitLap = p;
    }
    public PitData(PitData pd) 
    {
    	pitTime = pd.pitTime;
    	pitLap = pd.pitLap;
    }
    
    public int compareTo(PitData pd)
    {
    	if (pitLap < pd.pitLap)
    		return -1;
    	else if (pitLap > pd.pitLap)
    		return 1;
    	
    	return 0;        
    }           
}

class LapData implements Comparable<LapData>
{
	int carID = -1;
    int pos = 0;
    String gap = "";
    String interval = "";

    LapTime lapTime = new LapTime();
    LapTime sector1 = new LapTime();
    LapTime sector2 = new LapTime();
    LapTime sector3 = new LapTime();
    int numLap = 0;

    String sessionTime = "";      //the time when driver set this lap (as remaining time) - used in practice and quali
    int qualiPeriod = 0;        //only for quali - during which period this time was set
    boolean scLap = false;             //only for race - indicates if current lap was behind the safety car
    boolean approxLap = false;         //only for practice and quali - if the current lap was worse than the best lap we can only calculate approximate lap time from the sectors time
    
    public LapData()
    {
    	
    }
    
    public LapData(LapData ld)
    {
    	carID = ld.carID;
    	pos = ld.pos;
    	gap = new String(ld.gap);
    	interval = new String(ld.interval);
    	
    	lapTime.setTime(ld.lapTime);
    	sector1.setTime(ld.sector1);
    	sector2.setTime(ld.sector2);
    	sector3.setTime(ld.sector3);
    	
    	numLap = ld.numLap;
    	sessionTime = new String(ld.sessionTime);
    	qualiPeriod = ld.qualiPeriod;
    	scLap = ld.scLap;
    	approxLap = ld.approxLap;
    }
    
    public int compareTo(LapData ld)
    {
    	if (lapTime.lessThan(ld.lapTime))
    		return -1;
    	
    	if (ld.lapTime.lessThan(lapTime))
    		return 1;
    	
    	return 0;
    }

//    bool operator!=(const LapData ld)
//    {
//        if (gap.compare(ld.gap) != 0 ||
//            interval.compare(ld.interval) != 0 ||
//            lapTime.toString().compare(ld.lapTime.toString()) != 0 ||
//            sector1.toString().compare(ld.sector1.toString()) != 0 ||
//            sector2.toString().compare(ld.sector2.toString()) != 0 ||
//            sector3.toString().compare(ld.sector3.toString()) != 0
//           )
//            return true;
//
//        return false;
//    }

    public boolean lessThan(LapData ld)
    {
        return lapTime.lessThan(ld.lapTime);
    }

//    void clearLapData()
//    {
//        sector1 = LapTime();
//        sector2 = LapTime();
//        sector3 = LapTime();
//    }

    public static LapTime sumSectors(LapTime ls1, LapTime ls2, LapTime ls3)
    {
//        LapTime ls1 = new LapTime(s1);
//        LapTime ls2 = new LapTime(s2);
//        LapTime ls3 = new LapTime(s3);
    	//Log.d("DriverData", "LapTime-sumSectors");
        return (ls1.sum(ls2).sum(ls3));
    }
    /*GregorianCalendar toTime() const
    {
        return QTime::fromString(lapTime.toString(), "m:ss.zzz");
    }*/    
};


public class DriverData implements Comparable<DriverData>
{
	public int carID;
	public String driver = "";
	public int number;
	public int pos = -1;

	public int numPits;

	public boolean retired;
	public boolean releasedFromPits;

    //quali additional data
	public LapTime q1 = new LapTime();
	public LapTime q2 = new LapTime();
	public LapTime q3 = new LapTime();

	public List<LapData> lapData = new ArrayList<LapData>();
	public List<Integer> posHistory = new ArrayList<Integer>();
	public int colorData[] = new int[14];
	public List<PitData> pitData = new ArrayList<PitData>();
	public List< Pair<LapTime, Integer> > bestSectors = new ArrayList< Pair<LapTime, Integer> >();;

    public LapData lastLap = new LapData();
    public LapData bestLap = new LapData();
    
	public DriverData()
    {
        for (int i = 0; i <14; ++i)
            colorData[i] = LTData.Colors.DEFAULT;

        lastLap.carID = carID;

        for (int i = 0; i < 3; ++i)        
        	bestSectors.add(new Pair<LapTime, Integer>(new LapTime(), new Integer(0)));        	
        
    }
	public DriverData(DriverData dd)
	{
		carID = dd.carID;
		driver = new String(dd.driver);
		number = dd.number;
		pos = dd.pos;

		numPits = dd.numPits;

		retired = dd.retired;
		releasedFromPits = dd.releasedFromPits;

	    //quali additional data
		q1 = new LapTime(dd.q1);
		q2 = new LapTime(dd.q2);
		q3 = new LapTime(dd.q3);

		lapData = new ArrayList<LapData>(dd.lapData);
		posHistory = new ArrayList<Integer>(dd.posHistory);
		
		for (int i = 0; i < 14; ++i)
			colorData[i] = dd.colorData[i];
		
		pitData = new ArrayList<PitData>(dd.pitData);
		bestSectors = new ArrayList< Pair<LapTime, Integer> >(dd.bestSectors);

	    lastLap = new LapData(dd.lastLap);
	    bestLap = new LapData(dd.bestLap);
	}
	
	public int compareTo(DriverData pd)
    {
		//Log.d("DriverData", "DriverData-compareTo");
		if (pos < 0 || pos > pd.pos)
			return 1;
		
		if (pd.pos < 0 || pos < pd.pos)
			return -1;		    
    	
    	return 0;        
    }   

//    DriverData &operator=(const DriverData &dd);

//    bool operator<(const DriverData &dd) const
//    {
//        if (pos < 0)
//            return false;
//
//        if (dd.pos < 0)
//            return true;
//
//        return (pos < dd.pos) ? true : false;
//    }
	public static String calculateGap(LapTime lap1, LapTime lap2)
    {
		//Log.d("DriverData", "DriverData-calculateGap");
        if (lap1.isValid() && lap2.isValid())
        {
        	double ld1 = lap1.toDouble();
        	double ld2 = lap2.toDouble();
            double d = ld1-ld2;//(lap1.diff(lap2)).toDouble();

            if (d != 0)
            {
            	DecimalFormat df = new DecimalFormat("#0.000");
        	    return df.format(d);                
            }
        }
        return "";
    }
        

    //used in the head2head dialog - finds the best time and sets the differences between the best time and the others (max 4 lap times)
    public static int lapDiff(LapTime []lap)
    {
    	//Log.d("DriverData", "DriverData-lapDiff");
        int msec;

        if (lap[0] == null || !lap[0].isValid())
            msec = new LapTime("59:59.999").toMsecs();
        else
            msec = lap[0].toMsecs();

        int idx = 0;

        for (int i = 1; i < 4; ++i)
        {
            if (lap[i] != null && lap[i].isValid() && lap[i].toMsecs() < msec)
            {
                idx = i;
                msec = lap[i].toMsecs();
            }
        }

        for (int i = 0; i < 4; ++i)
        {
            if (i != idx && lap[i] != null && lap[i].isValid())
                lap[i] = lap[i].diff(lap[idx]);
        }
        return idx;
    }

    public void addLap(EventData ed)
    {
    	//Log.d("DriverData", "DriverData-addLap");
    	//ok, this looks a bit complicated, but since the LT server doesn't give us the actuall lap number for every driver during the race we have to find another way to gather laps:
        //- first of all - don't append laps if driver has retired (it's rather obvious)
        //- don't add empty lap time, except for the first lap of the race - it's always empty
        //- if this lap is in-lap - add it, if no - add it only if we have the sector 3 time
        //- if the lapData array is empty - check if lapNum is greater than 0
        //- don't add the out lap - "OUT",
        //- finally - check if we don't try to add the same lap again, we use the gap, interval and lap time info for this
        if (ed.eventType == LTData.EventType.RACE_EVENT)
        {
            if (!retired && ed.lapsCompleted > 0 && (lastLap.lapTime.toString() != "" || lastLap.numLap == 1) &&
        //        ((lastLap.lapTime.toString() != "IN PIT" && lastLap.sector3.toString() != "") || lastLap.lapTime.toString() == "IN PIT") &&
                 ((lastLap.numLap > 0 && lapData.isEmpty()) ||
                 (!lapData.isEmpty() &&
        //          (lastLap.numLap > lapData.last().numLap) &&
                  (!lastLap.lapTime.toString().equals("OUT") /*&& !(lastLap.sector3.toString() == "STOP" && lapData.last().sector3.toString() == "STOP")*/) &&
                  !(lapData.get(lapData.size()-1).gap.equals(lastLap.gap) && lapData.get(lapData.size()-1).interval.equals(lastLap.interval) && lapData.get(lapData.size()-1).lapTime.equals(lastLap.lapTime))
                   )))
            {
                //this is tricky - if driver goes to the pits, we get this info before he crosses the finish line, but not always...
                //therefore, we don't correct the lap number, assuming that everything is ok, and the lap number is last lap + 1
                if ((!lastLap.lapTime.toString().equals("IN PIT") && !lapData.isEmpty()) || (lapData.isEmpty() && lastLap.lapTime.toString().equals("OUT")))
                    correctNumLap(ed.lapsCompleted);

                //1st lap is always empty (excluding situations when driver goes to pit or retires), so if we got a valid time on the first lap
                //it means that LT server has sent us some junk (probably time from quali)
                if (lastLap.numLap == 1 && lastLap.lapTime.isValid())
                    lastLap.lapTime = new LapTime();

                //if this is RETIRED lap, update only the lap time
                if (lastLap.lapTime.toString().equals("RETIRED") && !lapData.isEmpty())
                {
                    lapData.get(lapData.size()-1).lapTime.setTime(lastLap.lapTime);
                    return;
                }
                //if we get "IN PIT" before driver crossed the line, we get it again after he crosses, in that case update only gap and interval
                if (!lapData.isEmpty() && lastLap.lapTime.toString().equals("IN PIT") && lapData.get(lapData.size()-1).lapTime.toString().equals("IN PIT") && !releasedFromPits)
                {
                    lapData.get(lapData.size()-1).gap = new String(lastLap.gap);
                    lapData.get(lapData.size()-1).interval = new String(lastLap.interval);
                    return;
                }
                //when connecting to LT during the race and driver was going out of the pits we save this lap as PIT lap
                if (lastLap.lapTime.toString().equals("OUT"))
                    lastLap.lapTime = new LapTime("IN PIT");

                lastLap.carID = carID;

                if (!lapData.isEmpty() && lapData.get(lapData.size()-1).numLap >= lastLap.numLap)
                    lapData.set(lapData.size()-1, new LapData(lastLap));

                else
                {
                    lapData.add(new LapData(lastLap));
                    posHistory.add(lastLap.pos);
                }

                releasedFromPits = false;

                if (lastLap.lessThan(bestLap))
                    bestLap = new LapData(lastLap);

                //best sectors
                if ((lapData.get(lapData.size()-1).sector1.lessEqual(bestSectors.get(0).first) && 
                		bestSectors.get(0).second != 0) || bestSectors.get(0).second == 0)
	            {
	                bestSectors.set(0, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector1), lapData.get(lapData.size()-1).numLap));
	            }
//                else
//                if (lapData.get(lapData.size()-1).sector1.equals(bestSectors.get(0).first) &&
//                	(colorData[LTData.RacePacket.RACE_SECTOR_1] == LTData.Colors.GREEN || 
//                     colorData[LTData.RacePacket.RACE_SECTOR_1] == LTData.Colors.VIOLET))
//                {
//                	bestSectors.set(0, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector1), lapData.get(lapData.size()-1).numLap));
//                }
                
	            if ((lapData.get(lapData.size()-1).sector2.lessEqual(bestSectors.get(1).first) && 
	                		bestSectors.get(1).second != 0) || bestSectors.get(1).second == 0)
	            {
	                bestSectors.set(1, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector2), lapData.get(lapData.size()-1).numLap));
	            }
//	            else
//                if (lapData.get(lapData.size()-1).sector2.equals(bestSectors.get(1).first) &&
//                	(colorData[LTData.RacePacket.RACE_SECTOR_2] == LTData.Colors.GREEN || 
//                     colorData[LTData.RacePacket.RACE_SECTOR_2] == LTData.Colors.VIOLET))
//                {
//                	bestSectors.set(0, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector2), lapData.get(lapData.size()-1).numLap));
//                }

	            if ((lapData.get(lapData.size()-1).sector3.lessEqual(bestSectors.get(2).first) && 
	                		bestSectors.get(2).second != 0) || bestSectors.get(2).second == 0)
	            {
	                bestSectors.set(2, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector3), lapData.get(lapData.size()-1).numLap));                        
	            }
//	            else
//                if (lapData.get(lapData.size()-1).sector3.equals(bestSectors.get(2).first) &&
//                	(colorData[LTData.RacePacket.RACE_SECTOR_3] == LTData.Colors.GREEN || 
//                     colorData[LTData.RacePacket.RACE_SECTOR_3] == LTData.Colors.VIOLET))
//                {
//                	bestSectors.set(0, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector3), lapData.get(lapData.size()-1).numLap));
//                }

                if (ed.flagStatus == LTData.FlagStatus.SAFETY_CAR_DEPLOYED || ed.flagStatus == LTData.FlagStatus.RED_FLAG)
                    lapData.get(lapData.size()-1).scLap = true;

                else
                    lapData.get(lapData.size()-1).scLap = false;

            }

            //driver was set out from the pits, if he pits again on the next lap we will add it
            if (lastLap.lapTime.toString().equals("OUT"))
                releasedFromPits = true;
        }
        else
        {
            //during practice and quali we only save timed laps
            if ((!lastLap.lapTime.toString().equals("")) && (lapData.isEmpty() ||
                (/*(lastLap.numLap > lapData.last().numLap) &&*/ !lastLap.sector1.toString().equals("") && !lastLap.sector2.toString().equals("") && !lastLap.sector3.toString().equals(""))))
            {            
            	boolean correction = false;
            	//sometimes servers messes up with lap numbers, we catch this if the numlap is <= than the last one 
            	if (!lapData.isEmpty() && lastLap.numLap <= lapData.get(lapData.size()-1).numLap)
            	{
            		correction = true;
            		boolean approx = lapData.get(lapData.size()-1).approxLap;
            		int numlap = lapData.get(lapData.size()-1).numLap;
            		lapData.set(lapData.size()-1, new LapData(lastLap));    
            		lapData.get(lapData.size()-1).approxLap = approx;
            		
            		if (lapData.size() > 1)
            			lapData.get(lapData.size()-1).numLap = numlap;            		
            			
            		
            		if (bestLap.numLap == numlap)
            			bestLap.numLap = lapData.get(lapData.size()-1).numLap;
            	}
            	else
            	{

	                //if decryption fails, replace the garbage we obtained with the best lap time
	                if (!lastLap.lapTime.toString().equals("") && !lastLap.lapTime.isValid())
	                    lastLap.lapTime.setTime(bestLap.lapTime);
	
	//                DecimalFormat df = new DecimalFormat("#0.000");
	//                lastLap.gap = df.format(lastLap.lapTime.diff(ed.FLTime).toDouble());
	                
	                lastLap.sessionTime = new String(ed.remainingTime);                
	                lapData.add(new LapData(lastLap));
	
	//                if (ed.eventType == LTData.EventType.QUALI_EVENT)
	                
	                	if (ed.eventType == LTData.EventType.QUALI_EVENT)
	                	{
		                    if (!q1.toString().equals(""))
		                        lapData.get(lapData.size()-1).qualiPeriod = 1;
		
		                    if (!q2.toString().equals(""))
		                        lapData.get(lapData.size()-1).qualiPeriod = 2;
		
		                    if (!q3.toString().equals(""))
		                        lapData.get(lapData.size()-1).qualiPeriod = 3;
	
	                	}
            	}
                //best sectors
//                    if ((colorData[LTData.QualifyingPacket.QUALI_SECTOR_1] == LTData.Colors.GREEN || 
//                    		colorData[LTData.QualifyingPacket.QUALI_SECTOR_1] == LTData.Colors.VIOLET) &&
                if ((lapData.get(lapData.size()-1).sector1.lessEqual(bestSectors.get(0).first) &&                     	
                     bestSectors.get(0).second != 0) || bestSectors.get(0).second == 0)
                {
                    bestSectors.set(0, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector1), lapData.get(lapData.size()-1).numLap));
                }
//                    else
//                    if (lapData.get(lapData.size()-1).sector1.equals(bestSectors.get(0).first) &&
//                    		
//                    	((ed.eventType == LTData.EventType.QUALI_EVENT &&
//                    	(colorData[LTData.QualifyingPacket.QUALI_SECTOR_1] == LTData.Colors.GREEN || 
//                         colorData[LTData.QualifyingPacket.QUALI_SECTOR_1] == LTData.Colors.VIOLET)) ||
//                         
//                        (ed.eventType == LTData.EventType.PRACTICE_EVENT &&
//                     	(colorData[LTData.PracticePacket.PRACTICE_SECTOR_1] == LTData.Colors.GREEN || 
//                         colorData[LTData.PracticePacket.PRACTICE_SECTOR_1] == LTData.Colors.VIOLET))))
//                    {
//                    	bestSectors.set(0, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector1), lapData.get(lapData.size()-1).numLap));
//                    }
                		
                		
//                    if ((colorData[LTData.QualifyingPacket.QUALI_SECTOR_2] == LTData.Colors.GREEN || 
//                    		colorData[LTData.QualifyingPacket.QUALI_SECTOR_2] == LTData.Colors.VIOLET) &&
                if ((lapData.get(lapData.size()-1).sector2.lessEqual(bestSectors.get(1).first) && 
                    		bestSectors.get(1).second != 0) || bestSectors.get(1).second == 0)
                {
                    bestSectors.set(1, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector2), lapData.get(lapData.size()-1).numLap));
                }
//                    else
//                    if (lapData.get(lapData.size()-1).sector2.equals(bestSectors.get(1).first) &&
//                    		
//                    	((ed.eventType == LTData.EventType.QUALI_EVENT &&
//                    	(colorData[LTData.QualifyingPacket.QUALI_SECTOR_2] == LTData.Colors.GREEN || 
//                         colorData[LTData.QualifyingPacket.QUALI_SECTOR_2] == LTData.Colors.VIOLET)) ||
//                         
//                        (ed.eventType == LTData.EventType.PRACTICE_EVENT &&
//                     	(colorData[LTData.PracticePacket.PRACTICE_SECTOR_2] == LTData.Colors.GREEN || 
//                         colorData[LTData.PracticePacket.PRACTICE_SECTOR_2] == LTData.Colors.VIOLET))))
//                    {
//                    	bestSectors.set(0, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector2), lapData.get(lapData.size()-1).numLap));
//                    }
                
//                    if ((colorData[LTData.QualifyingPacket.QUALI_SECTOR_3] == LTData.Colors.GREEN || 
//                    		colorData[LTData.QualifyingPacket.QUALI_SECTOR_3] == LTData.Colors.VIOLET) &&
                if ((lapData.get(lapData.size()-1).sector3.lessEqual(bestSectors.get(2).first) && 
                    		bestSectors.get(2).second != 0) || bestSectors.get(2).second == 0)
                {
                	bestSectors.set(2, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector3), lapData.get(lapData.size()-1).numLap));                        
                }
//                    else
//                    if (lapData.get(lapData.size()-1).sector3.equals(bestSectors.get(2).first) &&
//                    		
//                    	((ed.eventType == LTData.EventType.QUALI_EVENT &&
//                    	(colorData[LTData.QualifyingPacket.QUALI_SECTOR_3] == LTData.Colors.GREEN || 
//                         colorData[LTData.QualifyingPacket.QUALI_SECTOR_3] == LTData.Colors.VIOLET)) ||
//                         
//                        (ed.eventType == LTData.EventType.PRACTICE_EVENT &&
//                     	(colorData[LTData.PracticePacket.PRACTICE_SECTOR_3] == LTData.Colors.GREEN || 
//                         colorData[LTData.PracticePacket.PRACTICE_SECTOR_3] == LTData.Colors.VIOLET))))
//                    {
//                    	bestSectors.set(0, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector3), lapData.get(lapData.size()-1).numLap));
//                    }
                
                if (!correction)
                {
	                if (lastLap.lessThan(bestLap))
	                    bestLap = new LapData(lapData.get(lapData.size()-1));            
	
	
	
	                //if the current lap time is the same as the best lap, probably the driver hasn't improved so we have to calculate the real lap time from the sectors time
	                else if (lastLap.lapTime.equals(bestLap.lapTime))
	                {
	                    lapData.get(lapData.size()-1).lapTime = LapData.sumSectors(lapData.get(lapData.size()-1).sector1, lapData.get(lapData.size()-1).sector2, lapData.get(lapData.size()-1).sector3);
	                    lapData.get(lapData.size()-1).approxLap = true;
	                }
	                else
	                    lapData.get(lapData.size()-1).approxLap = false;
                }	
                lapData.get(lapData.size()-1).gap = Double.toString((lapData.get(lapData.size()-1).lapTime.diff(ed.FLTime)).toDouble());

                posHistory.add(lastLap.pos);
            }
            
        }
    }
    public void correctNumLap(int raceNumLap)
    {
    	//Log.d("DriverData", "DriverData-correctNumLap");
    	//first of all check if we are lapped
        int lapped = 0;
        if (!lastLap.gap.equals("") && lastLap.gap.charAt(lastLap.gap.length()-1) == 'L')
            lapped = Integer.parseInt(lastLap.gap.substring(0, lastLap.gap.length()-1));

        //now correct the lap number, raceNumLap is obtained from the leaders interval info - that's the only way here to get it
        if (lastLap.numLap + lapped != raceNumLap)
            lastLap.numLap = raceNumLap - lapped;

        //sometimes, when the driver is close to being lapped server doesn't send his gap data,
        //so if the lastLap.numLap is greater by 2 or more laps than lapData.last().numLap - we have to correct it...
        if (lastLap.gap.equals("") && !lapData.isEmpty() && lastLap.numLap > lapData.get(lapData.size()-1).numLap+1)
            lastLap.numLap = lapData.get(lapData.size()-1).numLap+1;
    }
    
    //used in quali and practice
//    public void correctNumLap()
//    {
//    	if (!lapData.isEmpty())
//    	{
//	    	for (int i = lapData.size()-2, j = 0; i >= 0; --i, ++j)
//	    	{
//	    		if (lapData.get(i).numLap >= lapData.get(i+1).numLap)
//	    		{
//	    			int numLap = lapData.get(i+1).numLap;
//	    			lapData.get(i).numLap = numLap;
//	    			lapData.remove(i+1);
//	    			break;
//	    		}
//	    	}
//    	}
//    }
    public void updateLastLap()
    {
    	//Log.d("DriverData", "DriverData-updateLastLap");
    	if (!lapData.isEmpty() && lapData.get(lapData.size()-1).numLap == lastLap.numLap)
        {
            if (!lapData.get(lapData.size()-1).lapTime.toString().equals("IN PIT") && lapData.get(lapData.size()-1).sector3.toString().equals("") && !lastLap.sector3.toString().equals(""))
            {
                if (/*lapData.last().lapTime.toString() != "IN PIT" && lapData.last().sector1.toString() == "" &&*/ !lastLap.sector1.toString().equals(""))
                    lapData.get(lapData.size()-1).sector1.setTime(lastLap.sector1);

                if (/*lapData.last().lapTime.toString() != "IN PIT" && lapData.last().sector2.toString() == "" && */!lastLap.sector2.toString().equals(""))
                    lapData.get(lapData.size()-1).sector2.setTime(lastLap.sector2);

                lapData.get(lapData.size()-1).sector3.setTime(lastLap.sector3);
                
                if ((lapData.get(lapData.size()-1).sector3.lessEqual(bestSectors.get(2).first) && 
                		bestSectors.get(2).second != 0) || bestSectors.get(2).second == 0)
	            {
	                bestSectors.set(2, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector3), lapData.get(lapData.size()-1).numLap));                        
	            }
//                else
//                if (lapData.get(lapData.size()-1).sector3.equals(bestSectors.get(2).first) &&
//                	(colorData[LTData.RacePacket.RACE_SECTOR_3] == LTData.Colors.GREEN || 
//                     colorData[LTData.RacePacket.RACE_SECTOR_3] == LTData.Colors.VIOLET))
//                {
//                	bestSectors.set(0, Pair.create(new LapTime(lapData.get(lapData.size()-1).sector3), lapData.get(lapData.size()-1).numLap));
//                }

                if (lapData.get(lapData.size()-1).numLap == bestLap.numLap)
                {
                    bestLap.sector1.setTime(lapData.get(lapData.size()-1).sector1);
                    bestLap.sector2.setTime(lapData.get(lapData.size()-1).sector2);
                    bestLap.sector3.setTime(lapData.get(lapData.size()-1).sector3);
                }

            }
        }
    }
    public void updateInPit()
    {
    	//Log.d("DriverData", "DriverData-updateInPit");
    	 if (!lapData.isEmpty())
    	    {
    	        lapData.get(lapData.size()-1).pos = lastLap.pos;
    	        lapData.get(lapData.size()-1).gap = new String(lastLap.gap);
    	        lapData.get(lapData.size()-1).interval = new String(lastLap.interval);    	        
    	        posHistory.set(posHistory.size()-1, lastLap.pos);
    	    }
    }
    public void updateGaps(EventData ed)
    {
    	//Log.d("DriverData", "DriverData-updateGaps");
//    	DecimalFormat df = new DecimalFormat("#0.000");   
//    	for (int i = 0; i < lapData.size(); ++i)
//    	{    		 	   
//            lapData.get(i).gap = df.format((lapData.get(i).lapTime.diff(ed.FLTime)).toDouble());
//    	}
//    	lastLap.gap = df.format(lastLap.lapTime.diff(ed.FLTime).toDouble());
    }

    public void addPitStop(PitData pd)
    {
    	//Log.d("DriverData", "DriverData-addPitStop");
        if (pd.pitLap == 0)
            return;
        for (int i = 0; i < pitData.size(); ++i)
        {
            if (pitData.get(i).pitLap == pd.pitLap)
            {
                if (pitData.get(i).pitTime == "")
                    pitData.get(i).pitTime = new String(pd.pitTime);

                return;
            }
        }
        pitData.add(new PitData(pd));
        Collections.sort(pitData);
//        sort(pitData);
    }

    LapData getLapData(int lap)
    {
    	//Log.d("DriverData", "DriverData-getLapData");
        for (int i = 0; i < lapData.size(); ++i)
        {
            if (lap == lapData.get(i).numLap)
                return lapData.get(i);
        }
        return null;
    }

    void setFastestLap(LapTime lapTime, int lapNo)
    {
    	//Log.d("DriverData", "DriverData-setFastestLap");
        if (lapNo == bestLap.numLap && lapTime.equals(bestLap.lapTime))
            return;

        bestLap.carID = carID;
        bestLap.lapTime.setTime(lapTime.toString());
        bestLap.numLap = lapNo;
        bestLap.sector1 = new LapTime();
        bestLap.sector2 = new LapTime();
        bestLap.sector3 = new LapTime();
    }

    String getPitTime(int lap)
    {
    	//Log.d("DriverData", "DriverData-getPitTime");
        for (int i = 0; i < pitData.size(); ++i)
        {
            if (lap == pitData.get(i).pitLap)
                return pitData.get(i).pitTime;
        }
        return "";
    }    
}
