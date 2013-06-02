package com.f1lt;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class HeadToHeadActivity extends Activity implements DataStreamReceiver 
{
	private Handler handler = new Handler();
	private DataStreamReader dataStreamReader;
	private EventData eventData = EventData.getInstance();
	
	private static int [] currentNumbers = {-1, -1};
	private static int [] currentIds = {0, 0};
	private boolean savePos = false;
	private int rowHeight;
	
	private static int [] drawIdx = {0, 0};
	private boolean alertDialogShown = false;
	
	Bitmap bgBitmap;
	
	private OnItemSelectedListener spinnerItemClickListener = new OnItemSelectedListener()
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
		{
			int tabIdx = 0;
						
			Spinner spinner2 = (Spinner)findViewById(R.id.driver2Spinner);
			
			if (parent == spinner2)
				tabIdx = 1;
									
			if (view != null)
			{
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
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private OnClickListener sector1Listener = new OnClickListener()
	{
		public void onClick(View v)
		{
			++drawIdx[0];
			if (drawIdx[0] == 4)
				drawIdx[0] = 0;
			
			String secText = "Sectors";
			
			if (drawIdx[0] > 0)
				secText = "S" + drawIdx[0]; 
			
			TableLayout tl = (TableLayout)findViewById(R.id.head2headTable);
			if (tl != null)
			{
				TableRow tr = (TableRow)tl.getChildAt(0);
				if (tr != null)		
				{
					setItem(tr, 3, secText, Gravity.CENTER, LTData.color[LTData.Colors.YELLOW]);
					setItem(tr, 7, secText, Gravity.CENTER, LTData.color[LTData.Colors.YELLOW]);
				}
				
				for (int i = 1; i < tl.getChildCount(); ++i)
				{
					tr = (TableRow)tl.getChildAt(i);
					if (tr != null)		
					{
						SectorsImageView siv = (SectorsImageView)tr.getChildAt(3);
						if (siv != null)
						{
							siv.setDrawIdx(drawIdx[0]);
							siv.invalidate();
						}
						siv = (SectorsImageView)tr.getChildAt(7);
						if (siv != null)
						{
							siv.setDrawIdx(drawIdx[0]);
							siv.invalidate();
						}
					}
				}
			}
			
		}
	};
	
	private OnClickListener sector2Listener = new OnClickListener()
	{
		public void onClick(View v)
		{
			++drawIdx[1];
			if (drawIdx[1] == 4)
				drawIdx[1] = 0;
			
			String secText = "Sectors";
			
			if (drawIdx[1] > 0)
				secText = "S" + drawIdx[1]; 
			
			TableLayout tl = (TableLayout)findViewById(R.id.head2headTable);
			if (tl != null)
			{
				TableRow tr = (TableRow)tl.getChildAt(0);
				if (tr != null)			
					setItem(tr, 7, secText, Gravity.CENTER, LTData.color[LTData.Colors.YELLOW]);
				
				for (int i = 1; i < tl.getChildCount(); ++i)
				{
					tr = (TableRow)tl.getChildAt(i);
					if (tr != null)		
					{
						SectorsImageView siv = (SectorsImageView)tr.getChildAt(7);
						if (siv != null)
						{
							siv.setDrawIdx(drawIdx[1]);
							siv.invalidate();
						}
					}
				}
			}
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.head_to_head);	
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
		
				
		savePos = false;
		spinner1.setOnItemSelectedListener(spinnerItemClickListener);
		spinner2.setOnItemSelectedListener(spinnerItemClickListener);		
		
		String [] strArray = new String[eventData.driversData.size()+1];
		
		strArray[0] = new String("");
		
		if (eventData.eventType != LTData.EventType.RACE_EVENT)
		{
			for (int i = 0; i < eventData.driversData.size(); ++i)		
				strArray[i+1] = new String("" + eventData.driversData.get(i).number + " " + LTData.getDriverName(eventData.driversData.get(i).driver));
		}
		else
		{
			int j = 1;
			for (int i = 0; j < strArray.length && i < 30; ++i)
			{
				int id = eventData.getDriverId(i);
				if (id > 0)
					strArray[j++] = new String("" + eventData.driversData.get(id-1).number + " " + LTData.getDriverName(eventData.driversData.get(id-1).driver));
				
//				strArray[j++] = new String("" + LTData.ltTeams.get(i).driver1Number + " " + LTData.ltTeams.get(i).driver1ShortName);
//				strArray[j++] = new String("" + LTData.ltTeams.get(i).driver2Number + " " + LTData.ltTeams.get(i).driver2ShortName);
			}
			for (; j < strArray.length; ++j)
				strArray[j] = new String("");
		}
		
		SpinnerAdapter adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, strArray);
		spinner1.setAdapter(adapter);
		spinner2.setAdapter(adapter);
		
		savePos = true;
		
		spinner1.setSelection(currentIds[0]);
		spinner2.setSelection(currentIds[1]);
		
		TableLayout tl = (TableLayout)findViewById(R.id.head2headTable);
		if (tl != null)
		{
			if (tl.getChildCount() == 0)
			{
				TableRow tr = new TableRow(this);
				tl.addView(tr);
				
				String secText = "Sectors";
				if (drawIdx[0] > 0)
					secText = "S" + drawIdx[0]; 
				
				TextView tv = setItem(tr, 0, "L", Gravity.RIGHT | Gravity.CENTER_VERTICAL,  LTData.color[LTData.Colors.DEFAULT]);
				tv.setTextSize(10);
				tv = setItem(tr, 1, "P", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
				tv.setTextSize(10);
				tv = setItem(tr, 2, "Lap time", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
				tv.setTextSize(10);
				tv = setItem(tr, 3, secText, Gravity.CENTER, LTData.color[LTData.Colors.YELLOW]);
				tv.setTextSize(10);
				tv.setOnClickListener(sector1Listener);
				tv.setPaintFlags(tv.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
				tv = setItem(tr, 4, "Gap", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
				tv.setTextSize(10);
				tv = setItem(tr, 5, "P", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
				tv.setTextSize(10);
				tv = setItem(tr, 6, "Lap time", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
				tv.setTextSize(10);
				tv = setItem(tr, 7, secText, Gravity.CENTER, LTData.color[LTData.Colors.YELLOW]);
				tv.setTextSize(10);
				tv.setPaintFlags(tv.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
				tv.setOnClickListener(sector1Listener);
			}
		}
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
		// TODO Auto-generated method stub

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
    	        	   alertDialogShown = false;
    	        	   dialog.cancel();
    	        	   dataStreamReader.reconnect();
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
    	        	   alertDialogShown = false;
    	        	   dialog.cancel();
    	           }
    	       });
    	AlertDialog alert = builder.create();
    	alert.show();
    	alertDialogShown = true;

	}
	
	public TextView setItem(TableRow row, int pos, String text, int gravity, int color)
	{		
		TextView view = null;
		try
		{
			view = (TextView)row.getChildAt(pos);
		}
		catch (ClassCastException e)
		{
		}
		
		if (view == null)
		{			
			float weight = 0.0f;
			switch(pos)
			{
				case 0 : weight = 0.05f; break;
				case 1 : weight = 0.05f; break;
				case 2 : weight = 0.225f; break;
				case 3 : weight = 0.15f; break;
				case 4 : weight = 0.1f; break;
				case 5 : weight = 0.05f; break;
				case 6 : weight = 0.225f; break;
				case 7 : weight = 0.15f; break;
			}
//			
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
	
	public SectorsImageView setSectorsImageItem(TableRow row, int pos)
	{
		SectorsImageView view = null;
		try
		{
			view = (SectorsImageView)row.getChildAt(pos);
		}

		catch (ClassCastException e)
		{
		}
		if (view == null)
		{
			float weight = 0.0f;
			switch(pos)
			{
				case 0 : weight = 0.05f; break;
				case 1 : weight = 0.05f; break;
				case 2 : weight = 0.225f; break;
				case 3 : weight = 0.15f; break;
				case 4 : weight = 0.1f; break;
				case 5 : weight = 0.05f; break;
				case 6 : weight = 0.225f; break;
				case 7 : weight = 0.15f; break;
			}
//			
			view = new SectorsImageView(this);
			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, rowHeight, weight);
						
			view.setPadding(2, 0, 2, 0);
			row.addView(view, cellLp);
		}
		view.setDrawIdx(drawIdx[0]);//pos == 3 ? 0 : 1]);
		view.clear();
		return view;
	}
	
	public void updateView()
	{
		int firstLap = 99, lastLap = 0;
		int []index = {0, 0, 0, 0};
		for (int i = 0; i < 2; ++i)
		{
			if (currentNumbers[i] > 0)
			{
				int idx = eventData.getDriverId(currentNumbers[i]);
												
				if (idx > 0 && !eventData.driversData.get(idx-1).lapData.isEmpty())
				{
					if (eventData.driversData.get(idx-1).lapData.get(0).numLap < firstLap)
						firstLap = eventData.driversData.get(idx-1).lapData.get(0).numLap;
					
					if (eventData.driversData.get(idx-1).lapData.get(eventData.driversData.get(idx-1).lapData.size()-1).numLap >= lastLap)
					{
						lastLap = eventData.driversData.get(idx-1).lapData.get(eventData.driversData.get(idx-1).lapData.size()-1).numLap;
						
						if (lastLap < eventData.eventInfo.laps &&
			                    !eventData.driversData.get(idx-1).retired &&
			                    eventData.driversData.get(idx-1).lastLap.sector3.toString().equals("") &&
			                    !eventData.driversData.get(idx-1).lastLap.lapTime.toString().equals("IN PIT"))
			                    lastLap++;
					}
				}
			}
		}
		
		int j = 0, k = lastLap;
		TableLayout tl = (TableLayout)findViewById(R.id.head2headTable);
		for (; k >= firstLap; --k, ++j)
		{
			LapTime []laps = new LapTime[4];
			String []strLaps = {"", ""};
			TableRow tr = (TableRow)tl.getChildAt(j+1);
			if (tr == null)
			{
				tr = new TableRow(this);
				tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
				tl.addView(tr);
			}
			setItem(tr, 0, ""+k, Gravity.RIGHT, LTData.color[LTData.Colors.DEFAULT]);
			boolean scLap = false;
			boolean [] newLap = {false, false};
			double [] sectors = {-1, -1, -1, -1, -1, -1};
			for (int i = 0; i < 2; ++i)
			{
				if (currentNumbers[i] > 0 && eventData.getDriverId(currentNumbers[i]) > 0)
				{				
					int idx = eventData.getDriverId(currentNumbers[i]);
					
					DriverData dd = eventData.driversData.get(idx-1);
					int lapIndex = dd.lapData.size() - index[i] - 1;
					
					if (!dd.lapData.isEmpty())// && dd.lapData.size() > index[i] && dd.lapData.get(lapIndex).numLap == k)
					{																		
						LapData ld = dd.getLapData(k);//null;
						
//						if (lapIndex >= 0 && lapIndex < dd.lapData.size())
//							ld = dd.lapData.get(lapIndex);
//						else
//							ld = dd.lapData.get(dd.lapData.size()-1);
																		
						if (k == (dd.lapData.get(dd.lapData.size()-1).numLap+1) && 
							!dd.retired && dd.lastLap.sector3.toString().equals("") && !dd.lastLap.lapTime.toString().equals("IN PIT"))
						{
							newLap[i] = true;
							ld = new LapData(dd.lastLap);
							ld.numLap++;
						}
						if (ld != null)//dd.lapData.size() > index[i] && ld.numLap == k)
						{													
							sectors[0+i*3] = ld.sector1.toDouble();
							sectors[1+i*3] = ld.sector2.toDouble();
							sectors[2+i*3] = ld.sector3.toDouble();
													
						
							laps[i] = newLap[i] ? new LapTime() : new LapTime(ld.lapTime);
							strLaps[i] = laps[i].toString();
							
							
							int color = LTData.color[LTData.Colors.GREEN];
							if (strLaps[i].equals("IN PIT"))
							{
								strLaps[i] += " (" + dd.getPitTime(k) + ")";
								color = LTData.color[LTData.Colors.RED];
							}
							else if (strLaps[i].equals("RETIRED"))
								color = LTData.color[LTData.Colors.RED];
							
							else if (ld.scLap)
							{
								scLap = true;
								color = LTData.color[LTData.Colors.YELLOW];
							}
							
							setItem(tr, 1+i*4, ""+ld.pos, Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.CYAN]);
							TextView v = setItem(tr, 2+i*4, strLaps[i], Gravity.CENTER, color);
							
							SectorsImageView siv = setSectorsImageItem(tr, 3+i*4);
							siv.setSectorTimes(sectors, i*3);
							siv.setTextSize(v.getTextSize());
														
							setItem(tr, 4, "", Gravity.CENTER, LTData.color[LTData.Colors.CYAN]);
							
							if (!newLap[i])
								++index[i];
						}
						else
						{
							setItem(tr, i*4+1, "", Gravity.RIGHT, LTData.color[LTData.Colors.DEFAULT]);
							setItem(tr, i*4+2, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
							SectorsImageView siv = setSectorsImageItem(tr, 3+i*4);
													
							setItem(tr, 4, "", Gravity.CENTER, LTData.color[LTData.Colors.CYAN]);
						}
					}
					else
					{
						setItem(tr, i*4+1, "", Gravity.RIGHT, LTData.color[LTData.Colors.DEFAULT]);
						setItem(tr, i*4+2, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
						SectorsImageView siv = setSectorsImageItem(tr, 3+i*4);
						
						setItem(tr, 4, "", Gravity.CENTER, LTData.color[LTData.Colors.CYAN]);
					}
				}
				else
				{
					setItem(tr, i*4+1, "", Gravity.RIGHT, LTData.color[LTData.Colors.DEFAULT]);
					setItem(tr, i*4+2, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					SectorsImageView siv = setSectorsImageItem(tr, 3+i*4);
					setItem(tr, 4, "", Gravity.CENTER, LTData.color[LTData.Colors.CYAN]);
				}
			}
			
			int bestIdx = DriverData.lapDiff(laps);			
			int i = ((bestIdx+1)%2);
			if (bestIdx != -1 && !strLaps[i].equals(""))
			{				
				if (!strLaps[i].contains("IN PIT") && !strLaps[i].equals("RETIRED"))
				{
					DecimalFormat df = new DecimalFormat("#0.0");
					strLaps[i] += " +" + df.format(laps[i].toDouble());
					
					setItem(tr, 2+i*4, strLaps[i], Gravity.CENTER, LTData.color[scLap ? LTData.Colors.YELLOW : LTData.Colors.WHITE]);
				}
			}
			
			for (int w = 0; w < 3; ++w)
	        {
	            int ic1 = 0, ic2 = 0;
	            if (sectors[w] > 0)
	            {
	                if (scLap)
	                    ic1 = 3;
	                else
	                    ic1 = (sectors[w] <= sectors[w+3]) ? 1 : 2;
	            }
	            if (sectors[w+3] > 0)
	            {
	                if (scLap)
	                    ic2 = 3;
	                else
	                    ic2 = (sectors[w] > sectors[w+3]) ? 1 : 2;	                	                
	            }
	            SectorsImageView siv = (SectorsImageView)tr.getChildAt(3);
	            if (siv != null)
	            {
	                siv.setSector(w, ic1);
	                
	                if (k == lastLap)
	                	siv.invalidate();
	            }

	            siv = (SectorsImageView)tr.getChildAt(7);
	            
	            if (siv != null)
	            {
	            	siv.setSector(w, ic2);
	            	
	            	if (k == lastLap)
	            		siv.invalidate();
	            }	                
	        }
			int idx1 = eventData.getDriverId(currentNumbers[0]);
			int idx2 = eventData.getDriverId(currentNumbers[1]);
			
			if (idx1 > 0 && idx2 > 0)
			{
				DriverData dd1 = eventData.driversData.get(idx1-1);
				DriverData dd2 = eventData.driversData.get(idx2-1);
				String interval = "";
				
				if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT)
				{
					if (newLap[0] || newLap[1])
					{
						interval = EventData.getInstance().calculateInterval(dd1, dd2, k-1);
						double []sumS = {0.0, 0.0};
						double []s1 = {0.0, 0.0};
						double []s2 = {0.0, 0.0};
						
						if (!interval.equals("") && !interval.equals("-1L >") && !interval.equals("+1L >"))
						{
							for (int w = 0; w < 2; ++w)
		                    {
		                        s1[w] = sectors[w];
		                        s2[w] = sectors[w+3];
		                    }
		                    if (s1[0] > 0 && s2[0] > 0)
		                        sumS[0] = s1[0] - s2[0];
	
		                    if (s1[1] > 0 && s2[1] > 0)
		                        sumS[1] = s1[1] - s2[1];
		                    		                    		                    
		                    DecimalFormat df = new DecimalFormat("#0.0");
		                    try
							{
		                    	double sum = Double.parseDouble(interval) + sumS[0] + sumS[1];
		                    	interval = "" + sum;
								interval = df.format(Double.parseDouble(interval));
								
								if (sum > 0)
									interval = "+" + interval;
							}
							catch (NumberFormatException e) { }
						}
					}
					else
					{
						interval = EventData.getInstance().calculateInterval(dd1, dd2, k);						
					}
				}
				else
				{	
					LapData l1 = dd1.getLapData(k);
					LapData l2 = dd2.getLapData(k);
					DecimalFormat df = new DecimalFormat("#0.0");
					try
					{						
						if ((newLap[0] || newLap[1]) &&
							Math.abs(dd1.lastLap.numLap-dd2.lastLap.numLap) <= 1)
						{
							double []sumS = {0.0, 0.0};
							double []s1 = {0.0, 0.0};
							double []s2 = {0.0, 0.0};
							
							for (int w = 0; w < 2; ++w)
		                    {
		                        s1[w] = sectors[w];
		                        s2[w] = sectors[w+3];
		                    }
		                    if (s1[0] > 0 && s2[0] > 0)
		                        sumS[0] = s1[0] - s2[0];
	
		                    if (s1[1] > 0 && s2[1] > 0)
		                        sumS[1] = s1[1] - s2[1];
		                    		                    		
		                    
		                    double sum = sumS[0] + sumS[1];
	                    	interval = "" + sum;
							interval = df.format(Double.parseDouble(interval));
								
							if (sum > 0)
								interval = "+" + interval;																
						}
						else															
						if (l1 != null && l2 != null)							
						{
							interval = DriverData.calculateGap(l1.lapTime, l2.lapTime);
							double d = Double.parseDouble(interval);
							interval = df.format(d);
							if (d > 0)
								interval = "+" + interval;
						}																											
					}
					catch (NumberFormatException e) { }					
				}					
				
				int color = LTData.color[LTData.Colors.VIOLET];
				if (!interval.equals("") && interval.getBytes()[0] != '-')	
				{
					color = LTData.color[LTData.Colors.RED];
//					interval = "+" + interval;
				}
				
				
				setItem(tr, 4, interval, Gravity.CENTER, color);
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
		}
	}

}
