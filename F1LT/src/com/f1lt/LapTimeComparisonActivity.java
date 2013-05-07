package com.f1lt;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class LapTimeComparisonActivity extends Activity  implements DataStreamReceiver
{
	private Handler handler = new Handler();
	private DataStreamReader dataStreamReader;
	private EventData eventData = EventData.getInstance();
	
	private static int [] currentNumbers = {-1, -1, -1, -1};
	private static int [] currentIds = {0, 0, 0, 0};
	private boolean savePos = false;
	private int rowHeight;
	
	private boolean alertDialogShown = false;
	
	Bitmap bgBitmap;
	
	private OnItemSelectedListener spinnerItemClickListener = new OnItemSelectedListener()
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
		{
			int tabIdx = 0;
						
			Spinner spinner2 = (Spinner)findViewById(R.id.driver2Spinner);
			Spinner spinner3 = (Spinner)findViewById(R.id.driver3Spinner);
			Spinner spinner4 = (Spinner)findViewById(R.id.driver4Spinner);
			
			if (parent == spinner2)
				tabIdx = 1;
			
			if (parent == spinner3)
				tabIdx = 2;
			
			if (parent == spinner4)
				tabIdx = 3;
			
			TextView tv = (TextView) view;
			String text = tv.getText().toString();
			int idx = text.indexOf(" ");
			if (idx >= 0)
			{
				try
				{
					currentNumbers[tabIdx] = Integer.parseInt(text.substring(0, idx));
				}
				catch (NumberFormatException e) { }
			}
			else
				currentNumbers[tabIdx] = -1;
			
			if (savePos)
				currentIds[tabIdx] = pos;
			
			updateView();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lap_time_comparison);	
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
		
		rowHeight = (int)((new TextView(this)).getTextSize()*1.5);				
		if (rowHeight > 30)
			rowHeight = 30;
		
		Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.rowbg);
		bgBitmap = Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight))/b.getHeight(), rowHeight, true);
		
		Spinner spinner1 = (Spinner)findViewById(R.id.driver1Spinner);
		Spinner spinner2 = (Spinner)findViewById(R.id.driver2Spinner);
		Spinner spinner3 = (Spinner)findViewById(R.id.driver3Spinner);
		Spinner spinner4 = (Spinner)findViewById(R.id.driver4Spinner);
		
				
		savePos = false;
		spinner1.setOnItemSelectedListener(spinnerItemClickListener);
		spinner2.setOnItemSelectedListener(spinnerItemClickListener);
		spinner3.setOnItemSelectedListener(spinnerItemClickListener);
		spinner4.setOnItemSelectedListener(spinnerItemClickListener);		
		
		String [] strArray = new String[eventData.driversData.size()+1];
		
		strArray[0] = new String("");
		
		if (eventData.eventType != LTData.EventType.RACE_EVENT)
		{
			for (int i = 0; i < eventData.driversData.size(); ++i)		
				strArray[i+1] = new String("" + eventData.driversData.get(i).number + " " + LTData.getShortDriverName(eventData.driversData.get(i).driver));
		}
		else
		{
			int j = 1;
			for (int i = 0; j < strArray.length && i < 30; ++i)
			{
				int id = eventData.getDriverId(i);
				if (id > 0)
					strArray[j++] = new String("" + eventData.driversData.get(id-1).number + " " + LTData.getShortDriverName(eventData.driversData.get(id-1).driver));
				
//				strArray[j++] = new String("" + LTData.ltTeams.get(i).driver1Number + " " + LTData.ltTeams.get(i).driver1ShortName);
//				strArray[j++] = new String("" + LTData.ltTeams.get(i).driver2Number + " " + LTData.ltTeams.get(i).driver2ShortName);
			}
			for (; j < strArray.length; ++j)
				strArray[j] = new String("");
		}
		
		SpinnerAdapter adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, strArray);
		spinner1.setAdapter(adapter);
		spinner2.setAdapter(adapter);
		spinner3.setAdapter(adapter);
		spinner4.setAdapter(adapter);
		
		savePos = true;
		
		spinner1.setSelection(currentIds[0]);
		spinner2.setSelection(currentIds[1]);
		spinner3.setSelection(currentIds[2]);
		spinner4.setSelection(currentIds[3]);
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
//			int colWidth = (int)(0.125 * rowWidth);
//			float weight = 0.0f;
//			switch(pos)
//			{
//				case 0 : weight = 0.1f; break;
//				case 1 : weight = 0.1f; break;
//				case 2 : weight = 0.1f; break;
//				case 3 : weight = 0.4f; break;
//				case 4 : weight = 0.3f; break;
//				case 5 : weight = 0.3f; break;
//			}
//			
			view = new TextView(this);
			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
			if (pos == 0)
				cellLp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.0f);
						
			view.setPadding(2, 0, 2, 0);
			row.addView(view, cellLp);
		}		
		view.setText(text);		
		view.setTextColor(color);
		view.setGravity(gravity);		
	
		return view;
	}
	
	public void updateView()
	{
		int firstLap = 99, lastLap = 0;
		int []index = {0, 0, 0, 0};
		for (int i = 0; i < 4; ++i)
		{
			if (currentNumbers[i] > 0)
			{
				int idx = eventData.getDriverId(currentNumbers[i]);
												
				if (idx > 0 && !eventData.driversData.get(idx-1).lapData.isEmpty())
				{
					if (eventData.driversData.get(idx-1).lapData.get(0).numLap < firstLap)
						firstLap = eventData.driversData.get(idx-1).lapData.get(0).numLap;
					
					if (eventData.driversData.get(idx-1).lapData.get(eventData.driversData.get(idx-1).lapData.size()-1).numLap > lastLap)
						lastLap = eventData.driversData.get(idx-1).lapData.get(eventData.driversData.get(idx-1).lapData.size()-1).numLap;
				}
			}
		}
		
		int j = 0, k = lastLap;
		TableLayout tl = (TableLayout)findViewById(R.id.ltcTable);
		for (; k >= firstLap; --k, ++j)
		{
			LapTime []laps = new LapTime[4];
			String []strLaps = {"", "", "", ""};
			TableRow tr = (TableRow)tl.getChildAt(j+1);
			if (tr == null)
			{
				tr = new TableRow(this);
				tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
				tl.addView(tr);
//				for (int i = 0; i < 5; ++i)
//				{
//					setItem(tr, i, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
//				}
			}
			setItem(tr, 0, ""+k, Gravity.RIGHT, LTData.color[LTData.Colors.DEFAULT]);
			for (int i = 0; i < 4; ++i)
			{
				if (currentNumbers[i] > 0)
				{				
					int idx = eventData.getDriverId(currentNumbers[i]);
					if (idx == -1)
					{
						setItem(tr, i+1, "", Gravity.RIGHT, LTData.color[LTData.Colors.DEFAULT]);
						continue;
					}
					
					DriverData dd = eventData.driversData.get(idx-1);
//					int lapIndex = dd.lapData.size() - index[i] - 1;
					LapData ld = dd.getLapData(k);
					
					if (ld != null)
					{
						laps[i] = new LapTime(ld.lapTime);
						strLaps[i] = laps[i].toString();
						
						if (strLaps[i].equals("IN PIT"))
							strLaps[i] += "(" + dd.getPitTime(k) + ")";
						
						++index[i];
					}
//					if (!dd.lapData.isEmpty() && dd.lapData.size() > index[i] && dd.lapData.get(lapIndex).numLap == k)
//					{
//						laps[i] = new LapTime(dd.lapData.get(lapIndex).lapTime);
//						strLaps[i] = laps[i].toString();
//						
//						if (strLaps[i].equals("IN PIT"))
//							strLaps[i] += "(" + dd.getPitTime(k) + ")";
//						
//						++index[i];
//					}
					else
					{
						setItem(tr, i+1, "", Gravity.RIGHT, LTData.color[LTData.Colors.DEFAULT]);						
					}
				}
				else
				{
					setItem(tr, i+1, "", Gravity.RIGHT, LTData.color[LTData.Colors.DEFAULT]);					
				}
			}
			int bestIdx = DriverData.lapDiff(laps);
			int [] colors = {LTData.color[LTData.Colors.GREEN], LTData.color[LTData.Colors.GREEN], LTData.color[LTData.Colors.GREEN], LTData.color[LTData.Colors.GREEN]};
			if (bestIdx != -1)
			{				
				for (int i = 0; i < 4; ++i)
				{
					if (laps[i] != null && i != bestIdx && !strLaps[i].equals("") && !strLaps[i].contains("IN PIT") && !strLaps[i].equals("RETIRED"))
					{			
						DecimalFormat df = new DecimalFormat("#0.0");			
						
						strLaps[i] += " +"+df.format(laps[i].toDouble());
						
						double []msecs = {0, 0, 0};
						int ji = 0;
						for (int jj = 0; jj < 4; ++jj)
						{
							if (jj != bestIdx)
							{
								if (laps[jj] == null || laps[jj].toString().equals(""))
									msecs[ji++] = 999999999;
								else
									msecs[ji++] = laps[jj].toMsecs();
							}
						}
						double maxGap = Math.max(Math.max(msecs[0], msecs[1]), msecs[2]);
						double minGap = Math.min(Math.min(msecs[0], msecs[1]), msecs[2]);
						
						colors[i] = LTData.color[LTData.Colors.YELLOW];
						if (laps[i].toMsecs() == maxGap)
							colors[i] = LTData.color[LTData.Colors.RED];
						if (laps[i].toMsecs() == minGap)
							colors[i] = LTData.color[LTData.Colors.WHITE];
					}
					if (strLaps[i].contains("IN PIT") || strLaps[i].equals("RETIRED"))
						colors[i] = LTData.color[LTData.Colors.RED];
										
					setItem(tr, i+1, strLaps[i], Gravity.CENTER, colors[i]);
				}
			}
//			else
//			{
//				if (strLaps.equals("IN PIT") || strLaps.equals("RETIRED"))
//					colors[bestIdx] = LTData.color[LTData.Colors.RED];
//				
//				setItem(tr, bestIdx+1, strLaps[bestIdx], Gravity.CENTER, colors[bestIdx]);
//			}
		}
		for (k = tl.getChildCount()-1; k > j && k > 0 ; --k)
		{
			tl.removeViewAt(k);
//			TableRow tr = (TableRow)tl.getChildAt(j+1);
//			for (int i = 0; i < 5; ++i)
//				setItem(tr, i, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
		}
	}
		
}
