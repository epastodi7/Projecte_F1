package com.f1lt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class FastestLapsActivity extends Activity   implements DataStreamReceiver
{
	private Handler handler = new Handler();
	DataStreamReader dataStreamReader;
	int rowWidth;
	int rowHeight;
	Bitmap bgBitmap;
	List<LapData> bestLaps = new ArrayList<LapData>();
	private boolean alertDialogShown = false;
		
	boolean printShortNames = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fastest_laps);
		
		if (getIntent()!=null)
			printShortNames = getIntent().getBooleanExtra("PrintShortNames", false);
		
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		try
		{
			getActionBar().hide();
		}
		catch (NoSuchMethodError e) { }	
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
	
	@Override
	public void onStart()
	{
		super.onStart();
				
		dataStreamReader = DataStreamReader.getInstance();
		dataStreamReader.setSecondaryDataStreamReceiver(handler, this);	    	
	    
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);						
		rowWidth = metrics.widthPixels;
		
		rowHeight = (int)((new TextView(this)).getTextSize()*1.5);				
		if (rowHeight > 30)
			rowHeight = 30;
		
		Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.rowbg);
		bgBitmap = Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight))/b.getHeight(), rowHeight, true);
		
		TableLayout tl = (TableLayout)findViewById(R.id.fastestLapsTable1);		
		if (tl != null)
		{
			TableRow tr = (TableRow)tl.getChildAt(0);
//			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
			TextView tv = setItem(1, tr, 0, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(1, tr, 1, "Time", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(1, tr, 2, "S1", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(1, tr, 3, "S2", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(1, tr, 4, "S3", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(1, tr, 5, "Theor.", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			
			tr = (TableRow)tl.getChildAt(1);
			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
			tv = setItem(1, tr, 0, "BestTimes:", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tr = (TableRow)tl.getChildAt(2);
			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
			tv = setItem(1, tr, 0, "Drivers:", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tr = (TableRow)tl.getChildAt(3);
			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
			tv = setItem(1, tr, 0, "Lap/sess. time:", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
		}
		tl = null;
		tl = (TableLayout)findViewById(R.id.fastestLapsTable2);		
		if (tl != null)
		{
			TableRow tr = new TableRow(this);
//			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
			TextView tv = setItem(2, tr, 0, "P", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(2, tr, 1, "", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(2, tr, 2, "Name", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(2, tr, 3, "Time", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(2, tr, 4, "Gap", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(2, tr, 5, "S1", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(2, tr, 6, "S2", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(2, tr, 7, "S3", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(2, tr, 8, "Lap", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			
			tl.addView(tr);
		}
		for (int i = 0; i < LTData.ltTeams.size()*2; ++i)
			bestLaps.add(new LapData());
		
		updateView();
	}
	
	@Override
	public void onPause()
	{
		dataStreamReader.removeSecondaryDataStreamReceiver();
		super.onPause();
	}
	
	public void onNewDataObtained(boolean updateTimer)
	{
		if (!updateTimer)
			updateView();
	}
	public void onShowMessageBoard(String msg, boolean rem)
	{
		
	}
	
	public void onDataStreamError(int code)
	{
		if (alertDialogShown)
			return;
		
		String msg = "Could not connect to the LT server"; 
		dataStreamReader.disconnect();
		
    	if (code == 1)
    		msg = "Lost connection to the LT server.";
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(msg)
    	       .setCancelable(false)
    	       .setPositiveButton("Retry", new DialogInterface.OnClickListener() 
    	       {
    	           public void onClick(DialogInterface dialog, int id) 
    	           {    
    	        	   dialog.cancel();
    	        	   dataStreamReader.reconnect();
    	        	   alertDialogShown = false;
//    	        	   	
//    	        	   
//    	        	   	Intent intent = new Intent();
//						intent.putExtra("Request login", true);
//												
//						setResult(RESULT_OK, intent);
//						finish();
    	           }
    	       })
    	       .setNegativeButton("Close", new DialogInterface.OnClickListener() 
    	       {
    	           public void onClick(DialogInterface dialog, int id) 
    	           {
    	        	   dialog.cancel();
    	        	   alertDialogShown = false;
    	           }
    	       });
    	AlertDialog alert = builder.create();
    	alert.show();
    	alertDialogShown = true;
	}
	
	public TextView setItem(int table, TableRow row, int pos, String text, int gravity, int color)
	{
		TextView view = (TextView)row.getChildAt(pos);				
		
		if (view == null)
		{			
			int colWidth = (int)(0.125 * rowWidth);
			float weight = 0f;
			
			if (table == 1)
			{
				switch(pos)
				{
					case 0 : colWidth = (int)(0.26*rowWidth); weight = 0.26f; break;
					case 1 : colWidth = (int)(0.16*rowWidth); weight = 0.16f; break;
					case 2 : colWidth = (int)(0.12*rowWidth); weight = 0.14f; break;
					case 3 : colWidth = (int)(0.12*rowWidth); weight = 0.14f; break;
					case 4 : colWidth = (int)(0.12*rowWidth); weight = 0.14f; break;
					case 5 : colWidth = (int)(0.22*rowWidth); weight = 0.16f; break;
				}
			}
			else
			{
				switch(pos)
				{
					case 0 : weight = 0.065f; break;
					case 1 : weight = 0.065f; break;
					case 2 : weight = printShortNames ? 0.12f : 0.27f; break;
					case 3 : weight = 0.15f; break;
					case 4 : weight = printShortNames ? 0.12f : 0.12f; break;
					case 5 : weight = printShortNames ? 0.1f : 0.09f; break;
					case 6 : weight = printShortNames ? 0.1f : 0.09f; break;
					case 7 : weight = printShortNames ? 0.1f : 0.09f; break;
					case 8 : weight = (EventData.getInstance().eventType == LTData.EventType.QUALI_EVENT) ? 0.13f : 0.06f; break;
				}
			}
			
			view = new TextView(this);
			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT/*rowHeight*/, weight);
//			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
						
			view.setPadding(2, 0, 2, 0);
			row.addView(view, cellLp);			
		}		
		view.setText(text);		
		view.setTextColor(color);
		view.setGravity(gravity);		
	
		return view;
	}
	
	public void printBestTimes()
	{
		TableLayout tl = (TableLayout)findViewById(R.id.fastestLapsTable1);
		if (tl != null)
		{
			EventData eventData = EventData.getInstance();
			TableRow tr = (TableRow)tl.getChildAt(1);
			
			String str = new String(eventData.FLTime.toString());
	        if (eventData.eventType != LTData.EventType.RACE_EVENT && !bestLaps.isEmpty())
	            str = new String(bestLaps.get(0).lapTime.toString());
			
	        setItem(1, tr, 1, str, Gravity.CENTER, LTData.color[LTData.Colors.VIOLET]);
	        setItem(1, tr, 2, eventData.sec1Record[1], Gravity.CENTER, LTData.color[LTData.Colors.VIOLET]);
	        setItem(1, tr, 3, eventData.sec2Record[1], Gravity.CENTER, LTData.color[LTData.Colors.VIOLET]);
	        setItem(1, tr, 4, eventData.sec3Record[1], Gravity.CENTER, LTData.color[LTData.Colors.VIOLET]);
	        
	        str = new String();
	        if (!eventData.sec1Record[1].equals("") && !eventData.sec2Record[1].equals("") && !eventData.sec3Record[1].equals(""))
	        	str = LapData.sumSectors(new LapTime(eventData.sec1Record[1]), new LapTime(eventData.sec2Record[1]), new LapTime(eventData.sec3Record[1])).toString();
	        
	        setItem(1, tr, 5, str, Gravity.CENTER, LTData.color[LTData.Colors.CYAN]);
	        
	        tr = (TableRow)tl.getChildAt(2);
			
//	        str = new String();
//	        str = LTData.getShortDriverName(eventData.FLDriver);
//	        if (eventData.eventType != LTData.EventType.RACE_EVENT && !bestLaps.isEmpty() && 
//	        		!eventData.driversData.isEmpty() && bestLaps.get(0).carID-1 > 0)
//	            str = LTData.getShortDriverName(eventData.driversData.get(bestLaps.get(0).carID-1).driver);
			
	        setItem(1, tr, 1, LTData.getShortDriverName(eventData.FLDriver), Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
	        setItem(1, tr, 2, LTData.getShortDriverName(eventData.sec1Record[0]), Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
	        setItem(1, tr, 3, LTData.getShortDriverName(eventData.sec2Record[0]), Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
	        setItem(1, tr, 4, LTData.getShortDriverName(eventData.sec3Record[0]), Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
	        setItem(1, tr, 5, "", Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
	        
	        tr = (TableRow)tl.getChildAt(3);
	        
	        str = new String("L" + eventData.FLLap);
	        if (eventData.FLLap <= 0)
	            str = "";
	        if (eventData.eventType != LTData.EventType.RACE_EVENT && !bestLaps.isEmpty())
	            str = new String(bestLaps.get(0).sessionTime);
	        
	        setItem(1, tr, 1, str, Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
	        
	        str = new String();
	        if ((eventData.eventType == LTData.EventType.RACE_EVENT && !eventData.sec1Record[2].equals("") && Integer.parseInt(eventData.sec1Record[2]) > -1))
	            str = "L" + eventData.sec1Record[2];

	        else if (eventData.eventType != LTData.EventType.RACE_EVENT)
	            str = eventData.sec1Record[3];
	        
	        setItem(1, tr, 2, str, Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
	        
	        str = new String();
	        if ((eventData.eventType == LTData.EventType.RACE_EVENT && !eventData.sec2Record[2].equals("") && Integer.parseInt(eventData.sec2Record[2]) > -1))
	            str = "L" + eventData.sec2Record[2];

	        else if (eventData.eventType != LTData.EventType.RACE_EVENT)
	            str = eventData.sec2Record[3];
	        
	        setItem(1, tr, 3, str, Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
	        
	        str = new String();
	        if ((eventData.eventType == LTData.EventType.RACE_EVENT && !eventData.sec3Record[2].equals("") &&  Integer.parseInt(eventData.sec3Record[2]) > -1))
	            str = "L" + eventData.sec3Record[2];

	        else if (eventData.eventType != LTData.EventType.RACE_EVENT)
	            str = eventData.sec3Record[3];
	        
	        setItem(1, tr, 4, str, Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
	        setItem(1, tr, 5, "", Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
		}
	}
	
	public void updateView()
	{				
		EventData eventData = EventData.getInstance();
		for (int i = 0; i < eventData.driversData.size() && i < bestLaps.size(); ++i)
	    {
	        if (eventData.driversData.get(i).bestLap.lapTime.isValid())
	            bestLaps.set(i, new LapData(eventData.driversData.get(i).bestLap));
	        else
	            bestLaps.set(i, new LapData());
	    }
	    Collections.sort(bestLaps);
	    
	    printBestTimes();
		
		TableLayout tl = (TableLayout)findViewById(R.id.fastestLapsTable2);
		if (tl != null)
		{
			for (int i = tl.getChildCount(); i <= bestLaps.size(); ++i)
			{
				TableRow tr = new TableRow(this);
				tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
				
				tl.addView(tr);
			}
			
			for (int i = 0; i < bestLaps.size(); ++i)
			{
				TableRow tr = (TableRow)tl.getChildAt(i+1);
				if (bestLaps.get(i).carID <= 0)
					return;
				DriverData dd = (bestLaps.get(i).carID > 0) ? new DriverData(eventData.driversData.get(bestLaps.get(i).carID-1)) : new DriverData();
				
				setItem(2, tr, 0, ""+(i+1), Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.CYAN]);
				
				int color = LTData.color[LTData.Colors.VIOLET];
				if (i > 0)
					color = LTData.color[LTData.Colors.WHITE];
				
				setItem(2, tr, 1, ""+dd.number, Gravity.RIGHT | Gravity.CENTER_VERTICAL, color);
				
				String driver = printShortNames ? LTData.getShortDriverName(dd.driver) : LTData.getDriverName(dd.driver);
				setItem(2, tr, 2, ""+driver, Gravity.LEFT | Gravity.CENTER_VERTICAL, color);
				
				if (i > 0)
					color = LTData.color[LTData.Colors.GREEN];
				
				setItem(2, tr, 3, bestLaps.get(i).lapTime.toString(), Gravity.CENTER, color);
				
				if (i == 0)
					setItem(2, tr, 4, "", Gravity.CENTER, LTData.color[LTData.Colors.YELLOW]);
				else
				{
					String gap = "";
					gap = DriverData.calculateGap(bestLaps.get(i).lapTime, bestLaps.get(0).lapTime);
					setItem(2, tr, 4, gap, Gravity.CENTER, LTData.color[LTData.Colors.YELLOW]);
				}
				
				color = LTData.color[LTData.Colors.WHITE];
				if (bestLaps.get(i).numLap == dd.bestSectors.get(0).second)
					color = LTData.color[LTData.Colors.GREEN];
				
				if (eventData.sec1Record[0].equals(dd.driver) &&
					eventData.sec1Record[2].equals("" + bestLaps.get(i).numLap))
					color = LTData.color[LTData.Colors.VIOLET];
				
				setItem(2, tr, 5, bestLaps.get(i).sector1.toString(), Gravity.CENTER, color);
				
				color = LTData.color[LTData.Colors.WHITE];
				if (bestLaps.get(i).numLap == dd.bestSectors.get(1).second)
					color = LTData.color[LTData.Colors.GREEN];
				
				if (eventData.sec2Record[0].equals(dd.driver) &&
					eventData.sec2Record[2].equals("" + bestLaps.get(i).numLap))
					color = LTData.color[LTData.Colors.VIOLET];
				
				setItem(2, tr, 6, bestLaps.get(i).sector2.toString(), Gravity.CENTER, color);
				
				color = LTData.color[LTData.Colors.WHITE];
				if (bestLaps.get(i).numLap == dd.bestSectors.get(2).second)
					color = LTData.color[LTData.Colors.GREEN];
				
				if (eventData.sec3Record[0].equals(dd.driver) &&
					eventData.sec3Record[2].equals("" + bestLaps.get(i).numLap))
					color = LTData.color[LTData.Colors.VIOLET];
				
				setItem(2, tr, 7, bestLaps.get(i).sector3.toString(), Gravity.CENTER, color);
				
				String str = "" + bestLaps.get(i).numLap;
				if (eventData.eventType == LTData.EventType.QUALI_EVENT)
					str += " Q" + bestLaps.get(i).qualiPeriod;
				
				
				
				setItem(2, tr, 8, str, Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
			}
		}
		
	}
}
