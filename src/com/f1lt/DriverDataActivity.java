package com.f1lt;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DriverDataActivity extends Activity   implements DataStreamReceiver
{
	private Handler handler = new Handler();
	private DataStreamReader dataStreamReader;
	private int rowWidth;
	private int rowHeight;
	private Bitmap bgBitmap;
	private Bitmap bgBitmap2;
	static private List<Bitmap> carBitmaps = new ArrayList<Bitmap>();
	private DriverData driverData;
	
	private boolean alertDialogShown = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.driver_data);
		
		if (getIntent() != null)
		{
			int carId = getIntent().getIntExtra("CarID", -1);
			if (carId > 0)
				driverData = EventData.getInstance().driversData.get(carId-1);
		}
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
		dataStreamReader = DataStreamReader.getInstance();
		dataStreamReader.setSecondaryDataStreamReceiver(handler, this);	    	
		
		super.onStart();
		
					    
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);						
		rowWidth = metrics.widthPixels;
		
		rowHeight = (int)((new TextView(this)).getTextSize()*1.5);				
		if (rowHeight > 30)
			rowHeight = 30;
		
		Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.rowbg);
		bgBitmap = Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight))/b.getHeight(), rowHeight, true);
		
		b = BitmapFactory.decodeResource(getResources(), R.drawable.carbon);
		bgBitmap2 = b;//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight))/b.getHeight(), rowHeight, true);
		
		if (carBitmaps.isEmpty())
		{
			for (int i = 0; i < LTData.ltTeams.size(); ++i)
			{		
				int carBitmapId = -1;
				switch (LTData.ltTeams.get(i).id)
				{
					case 0: carBitmapId = R.drawable.car_0; break;
					case 1: carBitmapId = R.drawable.car_1; break;
					case 2: carBitmapId = R.drawable.car_2; break;
					case 3: carBitmapId = R.drawable.car_3; break;
					case 4: carBitmapId = R.drawable.car_4; break;
					case 5: carBitmapId = R.drawable.car_5; break;
					case 6: carBitmapId = R.drawable.car_6; break;
					case 7: carBitmapId = R.drawable.car_7; break;
					case 8: carBitmapId = R.drawable.car_8; break;
					case 9: carBitmapId = R.drawable.car_9; break;
					case 10: carBitmapId = R.drawable.car_10; break;
					case 11: carBitmapId = R.drawable.car_11; break;
				}
				
				if (carBitmapId >= 0)
				{
					b = BitmapFactory.decodeResource(getResources(), carBitmapId);
				}
				else
					b = null;
				
	//			carBitmaps.add(scaledBitmap(carBitmapId, (b.getWidth()*(int)(rowHeight*0.75))/b.getHeight(), (int)(rowHeight*0.75))   );//Bitmap.createScaledBitmap(b, (b.getWidth()*(int)(rowHeight*0.75))/b.getHeight(), (int)(rowHeight*0.75), true));
	//			if (b.getWidth() > (rowWidth/2))
	//				carBitmaps.add(Bitmap.createScaledBitmap(b, ((int)(300)), (int)((b.getHeight()*300)/b.getWidth()), true));
	//			else
					carBitmaps.add(b);
			}
		}						 
		
//		TableLayout tl = (TableLayout)findViewById(R.id.driverDataTable1);		
//		if (tl != null)
//		{
////			tl.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap2));
//			TableRow tr = (TableRow)tl.getChildAt(0);
////			setItem(1, tr, 0, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
////			setItem(1, tr, 1, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);			
////			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap2));
//			
//			
//			tr = (TableRow)tl.getChildAt(1);
////			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap2));		
//			String str = "Grid position:";
//			if (EventData.getInstance().eventType != LTData.EventType.RACE_EVENT)
//				str = "Laps completed:";
//			setItem(1, tr, 0, str, Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 1, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 2, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			
//			str = "Pit stops";
//			if (EventData.getInstance().eventType != LTData.EventType.RACE_EVENT)
//				str = "";
//			setItem(1, tr, 3, str, Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 2);
//			
//			tr = (TableRow)tl.getChildAt(2);
////			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap2));
//			setItem(1, tr, 0, "Current position:", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 1, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 2, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 3, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 4, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 5, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
//			
//			tr = (TableRow)tl.getChildAt(3);
////			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap2));
//			setItem(1, tr, 0, "Gap to leader:", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 1, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 2, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 3, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 4, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 5, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
//			
//			tr = (TableRow)tl.getChildAt(4);
////			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap2));
//			setItem(1, tr, 0, "Last lap:", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 1, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 2, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 3, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 4, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 5, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
//			
//			tr = (TableRow)tl.getChildAt(5);
////			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap2));
//			setItem(1, tr, 0, "Best lap:", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 1, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 2, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 3, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 4, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
//			setItem(1, tr, 5, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
//		}
//		tl = null;
		TableLayout tl = (TableLayout)findViewById(R.id.driverDataTable2);		
		if (tl != null)
		{
			rowWidth = tl.getWidth();
//			TableRow tr = new TableRow(this);
			TableRow tr = (TableRow)findViewById(R.id.ddHeaderRow);
			rowWidth = tr.getWidth();
//			tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
//			tl.addView(tr);
			
			TextView tv = setItem(2, tr, 0, "Lap", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
			tv.setTextSize(10);
			tv = setItem(2, tr, 1, "P", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT], 0);
			tv.setTextSize(10);
			
			String str = "Gap";
			if (EventData.getInstance().eventType != LTData.EventType.RACE_EVENT)
				str = "Sess. time";
			tv = setItem(2, tr, 2, str, Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
			tv.setTextSize(10);
			tv = setItem(2, tr, 3, "Time", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
			tv.setTextSize(10);
			tv = setItem(2, tr, 4, "Diff", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
			tv.setTextSize(10);
			tv = setItem(2, tr, 5, "S1", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
			tv.setTextSize(10);
			tv = setItem(2, tr, 6, "S2", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
			tv.setTextSize(10);
			tv = setItem(2, tr, 7, "S3", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT], 0);
			tv.setTextSize(10);
			
			
		}
		
		TextView tv = (TextView)findViewById(R.id.pitStopsTextView);
		if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT)				
			tv.setText("Pit stops:");
		
		else
			tv.setText("Pit status:");
		
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
	
	public TextView setItem(int table, TableRow row, int pos, String text, int gravity, int color, int spanValue)
	{
		TextView view = (TextView)row.getChildAt(pos);				
		
		boolean raceEvent = false;
		if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT)		
			raceEvent = true;
		
		if (view == null)
		{			
			int colWidth = (int)(0.125 * rowWidth);
			float weight=0f;
			
			if (table == 1)
			{
				switch(pos)
				{
					case 0 : colWidth = (int)(0.325*rowWidth); break;
					case 1 : colWidth = (int)(0.25*rowWidth); break;
					case 2 : colWidth = (int)(0.125*rowWidth); break;
					case 3 : colWidth = (int)(0.1*rowWidth); break;
					case 4 : colWidth = (int)(0.1*rowWidth); break;
					case 5 : colWidth = (int)(0.1*rowWidth); break;
				}
			}
			else
			{
				switch(pos)
				{
					case 0 : weight = 0.1f; break;
					case 1 : weight = 0.1f; break;
					case 2 : weight = (raceEvent ? 0.15f : 0.22f); break;
					case 3 : weight = (raceEvent ? 0.25f : 0.2f); break;
					case 4 : weight = 0.15f; break;
					case 5 : weight = 0.1f; break;
					case 6 : weight = 0.1f; break;
					case 7 : weight = 0.1f; break;					
				}
			}
			
			
			view = new TextView(this);
			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT/*rowHeight*/, weight);
			
			if (spanValue > 0)
				cellLp.span = spanValue;
						
			view.setPadding(2, 0, 10, 0);
			
//			view.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap2));
			row.addView(view, cellLp);			
		}		
		view.setText(text);		
		view.setTextColor(color);
		view.setGravity(gravity);		
	
		return view;
	}
	
	public ImageView setCarImage(TableRow row)
	{
		ImageView view = (ImageView)row.getChildAt(2);
		int id = LTData.getTeamId(driverData.number);
		Bitmap b = (id == -1) ? null : carBitmaps.get(id);
		
		if (view == null)
		{			
			view = new ImageView(this);
								
			int height = rowHeight;
			if (b != null)
				height = (int)(b.getHeight());
			
			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(/*(int)0.425*rowWidth*/b.getWidth(), height);			
//			view.setScaleType(ScaleType.FIT_XY);
			cellLp.span = 4;
			cellLp.gravity = Gravity.CENTER;
			row.addView(view, cellLp);
		}
		view.setImageBitmap(b);
		view.setAdjustViewBounds(true);
		
		return view;
	}
	
	public void printDriverInfo()
	{
		Log.d("DriverDataActivity", "printDriverInfo");
		TextView tv = (TextView)findViewById(R.id.driverTextView);
		tv.setText("" + driverData.number + " " + LTData.getDriverName(driverData.driver));
		
		tv = (TextView)findViewById(R.id.teamTextView);
		tv.setText(LTData.getTeamName(driverData.number));
		
		ImageView iv = (ImageView)findViewById(R.id.carImageView);
		
		int id = LTData.getTeamId(driverData.number);
		Bitmap b = (id == -1) ? null : carBitmaps.get(id);
		iv.setImageBitmap(b);
		
		tv = (TextView)findViewById(R.id.lapsCompletedTextView);
		if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT)
			tv.setText("Grid position:");
		else
			tv.setText("Laps completed:");
		
		tv = (TextView)findViewById(R.id.gridPosTextView);
		if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT && !driverData.posHistory.isEmpty())
			tv.setText("" + driverData.posHistory.get(0));
		else
			tv.setText("" + driverData.lastLap.numLap);
		
		tv = (TextView)findViewById(R.id.posTextView);
		tv.setText(""+driverData.pos);
		
		tv = (TextView)findViewById(R.id.gapTextView);
		tv.setText(""+driverData.lastLap.gap);
		
		if (driverData.pos == 1)
			tv.setText("-");
		
		tv = (TextView)findViewById(R.id.lastLapTextView);
					
		LapTime lt = driverData.lastLap.lapTime;
		if (!driverData.lapData.isEmpty())
			lt = driverData.lapData.get(driverData.lapData.size()-1).lapTime;
		
		String str = lt.toString();
		if (lt.isValid() && !lt.equals(driverData.bestLap.lapTime))
			str += " (+"+DriverData.calculateGap(lt, driverData.bestLap.lapTime) + ")";
		tv.setText(str);
		
		tv = (TextView)findViewById(R.id.bestLapTextView);
		tv.setText(""+driverData.bestLap.lapTime.toString());
		
		int color = LTData.color[LTData.Colors.GREEN];
		if (driverData.bestLap.numLap == EventData.getInstance().FLLap &&
			driverData.driver.equals(EventData.getInstance().FLDriver))
			color = LTData.color[LTData.Colors.VIOLET];
		
		tv.setTextColor(color);
		
		tv = (TextView)findViewById(R.id.pitStopsTextView2);
		if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT)
		{
			tv.setText(""+driverData.numPits);
//				tv.setTextColor(LTData.Colors.WHITE);
		}
		else
		{
			if (driverData.colorData[LTData.RacePacket.RACE_NUMBER] == LTData.Colors.PIT)
			{
				tv.setText("In pits");
				tv.setTextColor(LTData.color[LTData.Colors.PIT]);
			}
			else
			{
				tv.setText("On track");
				tv.setTextColor(LTData.color[LTData.Colors.GREEN]);
			}				
		}
		
		
		tv = (TextView)findViewById(R.id.gapToFrontTextView1);
		if (driverData.pos == 1)
			tv.setText("Gap to P1");
		else
			tv.setText("Gap to P"+(driverData.pos-1));
		
		tv = (TextView)findViewById(R.id.gapToBackTextView1);
		if (driverData.pos == LTData.ltTeams.size()*2)
			tv.setText("Gap to P24");
		else
			tv.setText("Gap to P"+(driverData.pos+1));
		
		tv = (TextView)findViewById(R.id.gapToFrontTextView2);
		if (driverData.pos == 1)
			tv.setText("-");
		else
		{
			if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT)
				tv.setText(driverData.lastLap.interval);
			else
			{
				DriverData dd=EventData.getInstance().getDriverData(driverData.pos-1);
				if (dd != null)
					lt = dd.lastLap.lapTime;
				else
					lt = new LapTime();
				
				String gap = DriverData.calculateGap(driverData.lastLap.lapTime, lt);
				tv.setText(gap);
			}
		}
		
		tv = (TextView)findViewById(R.id.gapToBackTextView2);
		if (driverData.pos == LTData.ltTeams.size()*2)
			tv.setText("-");
		else
		{
			if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT)
			{
				DriverData dd = EventData.getInstance().getDriverData(driverData.pos+1);
				
				String interval = "-";
				if (dd != null)
					interval += dd.lastLap.interval;
				
				tv.setText(interval);
			}
			else
			{
				DriverData dd  =EventData.getInstance().getDriverData(driverData.pos+1);															
				if (dd != null)
					lt = dd.lastLap.lapTime;
				else
					lt = new LapTime();
				
				String gap = DriverData.calculateGap(driverData.lastLap.lapTime, lt);
				tv.setText(gap);
			}
		}
	}
	
	public void updateView()
	{
		Log.d("DriverDataActivity", "updateView");
		printDriverInfo();
		
		TableLayout tl = (TableLayout)findViewById(R.id.driverDataTable2);
		if (tl != null)
		{
			for (int i = tl.getChildCount(); i <= driverData.lapData.size(); ++i)
			{
				TableRow tr = new TableRow(this);
				tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
				tl.addView(tr);
			}
			for (int i = 0; i < driverData.lapData.size(); ++i)
			{				
				TableRow tr = (TableRow)tl.getChildAt(i+1);
				
				if (tr != null)
				{
					LapData ld = driverData.lapData.get(driverData.lapData.size()-1-i);
					
					setItem(2, tr, 0, ""+ld.numLap, Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE], 0);
					setItem(2, tr, 1, ""+ld.pos, Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.CYAN], 0);
					
					String gap = "";
					if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT)
						gap = ld.pos == 1 ? "" : ld.gap;
					else
					{
						gap = ld.sessionTime;
						if (EventData.getInstance().eventType == LTData.EventType.QUALI_EVENT)
						{
							if (gap.length() > 2 && gap.substring(0, 2).equals("0:"))
								gap = gap.substring(2);
							
							gap += " (Q"+ld.qualiPeriod+")";
						}
					}
					setItem(2, tr, 2, gap, Gravity.CENTER, LTData.color[LTData.Colors.YELLOW], 0);
					
					int color = LTData.color[LTData.Colors.WHITE];
					
					String lapTime = ld.lapTime.toString();
					if (ld.numLap == driverData.bestLap.numLap)
						color = LTData.color[LTData.Colors.GREEN];
					
					if (ld.lapTime.equals(EventData.getInstance().FLTime) && driverData.driver.equals(EventData.getInstance().FLDriver))
						color = LTData.color[LTData.Colors.VIOLET];
					
					if (lapTime.equals("IN PIT"))// || ld.lapTime.toString().equals("RETIRED"))
					{
						color = LTData.color[LTData.Colors.RED];
						lapTime += " (" + driverData.getPitTime(ld.numLap) + ")";
					}
					if (ld.lapTime.toString().equals("RETIRED"))
						color = LTData.color[LTData.Colors.RED];
					
					if (ld.approxLap)
						color = LTData.color[LTData.Colors.CYAN];
					
					
					setItem(2, tr, 3, lapTime, Gravity.CENTER, color, 0);
					
					String diff = DriverData.calculateGap(ld.lapTime, driverData.bestLap.lapTime);
					setItem(2, tr, 4, diff, Gravity.CENTER, LTData.color[LTData.Colors.YELLOW], 0);
					
					color = LTData.color[LTData.Colors.WHITE];
					if (ld.numLap == driverData.bestSectors.get(0).second)
						color = LTData.color[LTData.Colors.GREEN];
					
					if (EventData.getInstance().sec1Record[2].equals("" + ld.numLap) && 
						EventData.getInstance().sec1Record[0].equals(driverData.driver))
						color = LTData.color[LTData.Colors.VIOLET];
					
					setItem(2, tr, 5, ld.sector1.toString(), Gravity.CENTER, color, 0);
					
					color = LTData.color[LTData.Colors.WHITE];
					if (ld.numLap == driverData.bestSectors.get(1).second)
						color = LTData.color[LTData.Colors.GREEN];
					
					if (EventData.getInstance().sec2Record[2].equals("" + ld.numLap) && 
						EventData.getInstance().sec2Record[0].equals(driverData.driver))
						color = LTData.color[LTData.Colors.VIOLET];
					
					setItem(2, tr, 6, ld.sector2.toString(), Gravity.CENTER, color, 0);
					
					color = LTData.color[LTData.Colors.WHITE];
					if (ld.numLap == driverData.bestSectors.get(2).second)
						color = LTData.color[LTData.Colors.GREEN];
					
					if (EventData.getInstance().sec3Record[2].equals("" + ld.numLap) && 
						EventData.getInstance().sec3Record[0].equals(driverData.driver))
						color = LTData.color[LTData.Colors.VIOLET];
					
					setItem(2, tr, 7, ld.sector3.toString(), Gravity.CENTER, color, 0);
				}				
			}
		}		
	}
}
