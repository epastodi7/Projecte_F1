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

public class FastestPitStopsActivity extends Activity   implements DataStreamReceiver
{
	private Handler handler = new Handler();
	private DataStreamReader dataStreamReader;
	private int rowWidth;
	private int rowHeight;
	private Bitmap bgBitmap;
	private boolean alertDialogShown = false;
	
	private class PitStopAtom implements Comparable<PitStopAtom>
	{
		public double pitTime;
		public String driver;
		public int driverNo;
		public int pitLap;
		public int posChange;
		
		public PitStopAtom(double t, String d, int dn, int p, int pc)
		{
			pitTime = t;
			driver = d;
			driverNo = dn;
			pitLap = p;
			posChange = pc;
		}
		
		public int compareTo(PitStopAtom pt)
	    {
			if (pitTime > pt.pitTime)
				return 1;
			
			if (pitTime < pt.pitTime)
				return -1;		    
	    	
	    	return 0;
	    }
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pit_stops);	
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		try
		{
			getActionBar().hide();
		}
		catch (NoSuchMethodError e) { }	
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		TableLayout tl = (TableLayout)findViewById(R.id.pitStopsTable);
//		if (tl != null)
//		{
//			if (tl.getChildCount() == 1)
//			{
//				for (int i = 0; i < 6; ++i)
//				{
//					TableRow row = new TableRow(this);
//					tl.addView(row);
//				}
//			}
//		}
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
		
		TableLayout tl = (TableLayout)findViewById(R.id.pitStopsTable);		
		if (tl != null)
		{
			TableRow tr = new TableRow(this);
//			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
			tl.addView(tr);
			
			TextView tv = setItem(tr, 0, "P", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(tr, 1, "Lap", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(tr, 2, "", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(tr, 3, "Name", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(tr, 4, "Time", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(tr, 5, "Pos change", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
		}
		
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
	
	public TextView setItem(TableRow row, int pos, String text, int gravity, int color)
	{
		TextView view = (TextView)row.getChildAt(pos);				
		
		if (view == null)
		{			
			int colWidth = (int)(0.125 * rowWidth);
			float weight = 0.0f;
			switch(pos)
			{
				case 0 : weight = 0.1f; break;
				case 1 : weight = 0.1f; break;
				case 2 : weight = 0.1f; break;
				case 3 : weight = 0.4f; break;
				case 4 : weight = 0.3f; break;
				case 5 : weight = 0.3f; break;
			}
			
			view = new TextView(this);
			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT/*rowHeight*/, weight);
						
			view.setPadding(2, 0, 2, 0);
			row.addView(view, cellLp);			
		}		
		view.setText(text);		
		view.setTextColor(color);
		view.setGravity(gravity);		
	
		return view;
	}
	
	private List<PitStopAtom> getPitStops()
	{
		List<PitStopAtom> pitStopList = new ArrayList<PitStopAtom>();
		
		for (int i = 0; i < EventData.getInstance().driversData.size(); ++i)
		{
			DriverData dd = EventData.getInstance().driversData.get(i);
			for (int j = 0; j < dd.pitData.size(); ++j)
			{
				PitStopAtom pt = null;
				
				double pitTime = 0.0;
				if (!dd.pitData.get(j).pitTime.equals(""))
				{				
					try
					{
						 pitTime = Double.parseDouble(dd.pitData.get(j).pitTime);
					}
					catch(NumberFormatException exc)
					{
						pitTime = 0.0;
					}
				}	
				LapData ld1 = dd.getLapData(dd.pitData.get(j).pitLap);
				LapData ld2 = dd.getLapData(dd.pitData.get(j).pitLap+1);
				
				int posChange = 0;
				if (ld1 != null && ld2 != null)
					posChange = ld1.pos - ld2.pos;
				
				pt = new PitStopAtom(pitTime, dd.driver, dd.number, dd.pitData.get(j).pitLap, posChange);
				pitStopList.add(pt);
			}
		}
		Collections.sort(pitStopList);
		
		return pitStopList;
	}
	
	public void updateView()
	{
		TableLayout tl = (TableLayout)findViewById(R.id.pitStopsTable);
		if (tl != null)
		{
			List<PitStopAtom> pitStopList = getPitStops();			
			
			for (int i = tl.getChildCount(); i <= pitStopList.size(); ++i)
			{
				TableRow tr = new TableRow(this);
				tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
				tl.addView(tr);
			}
			
			for (int i = 0; i < pitStopList.size(); ++i)
			{
				TableRow tr = (TableRow)tl.getChildAt(i+1);
				if (tr != null)
				{
					setItem(tr, 0, "" + (i+1), Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.CYAN]);
					setItem(tr, 1, "" + pitStopList.get(i).pitLap, Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.YELLOW]);
					setItem(tr, 2, "" + pitStopList.get(i).driverNo, Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
					setItem(tr, 3, LTData.getDriverName(pitStopList.get(i).driver), Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
					setItem(tr, 4, "" + pitStopList.get(i).pitTime, Gravity.CENTER, LTData.color[LTData.Colors.YELLOW]);
					
					
					if (pitStopList.get(i).posChange > 0)					
						setItem(tr, 5, "+" + pitStopList.get(i).posChange, Gravity.CENTER, LTData.color[LTData.Colors.GREEN]);
					else if (pitStopList.get(i).posChange == 0)
						setItem(tr, 5, "" + pitStopList.get(i).posChange, Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
					else
						setItem(tr, 5, "" + pitStopList.get(i).posChange, Gravity.CENTER, LTData.color[LTData.Colors.RED]);
					
				}
			}
		}
		
	}
}
