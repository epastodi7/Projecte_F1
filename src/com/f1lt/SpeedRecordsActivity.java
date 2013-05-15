package com.f1lt;

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

public class SpeedRecordsActivity extends Activity  implements DataStreamReceiver
{
	private Handler handler = new Handler();
	private DataStreamReader dataStreamReader;
	private int rowWidth;
	private int rowHeight;
	private Bitmap bgBitmap;
	
	private boolean alertDialogShown = false;
	@Override
    public void onCreate(Bundle savedInstanceState) 
    { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speed_records);	
		
		
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		try
		{
			getActionBar().hide();
		}
		catch (NoSuchMethodError e) { }	
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		TableLayout tl = (TableLayout)findViewById(R.id.speedRecordsTable);
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
		
		TableLayout tl = (TableLayout)findViewById(R.id.speedRecordsTable);
		if (tl != null)
		{
			
			TableRow row = (TableRow)tl.getChildAt(0);
			
			TextView tv = setItem(row, 0, "S1", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(row, 1, "KPH", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			
			
			tv = setItem(row, 2, "S2", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(row, 3, "KPH", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
						
			tv = setItem(row, 4, "S3", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(row, 5, "KPH", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
						
			tv = setItem(row, 6, "ST", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			tv = setItem(row, 7, "KPH", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
			tv.setTextSize(10);
			
			for (int i = 1; i < tl.getChildCount(); ++i)
			{
				TableRow tr = (TableRow)tl.getChildAt(i); 				
				tr.setBackgroundDrawable(new BitmapDrawable(getResources(), bgBitmap));
			}
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
			float weight = 0.125f;
			
//			int colWidth = getColWidth(pos);
			
//			Log.d("ltview", "pos="+pos+", colWidth="+colWidth+", text="+text);
			
			view = new TextView(this);
			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT/*rowHeight*/, weight);
						
//			view.setBackgroundResource(R.drawable.back);
			view.setPadding(2, 0, 10, 0);
			row.addView(view, cellLp);			
		}		
		view.setText(text);		
		view.setTextColor(color);
		view.setGravity(gravity);		
	
		return view;
	}
	
	public void updateView()
	{
		TableLayout tl = (TableLayout)findViewById(R.id.speedRecordsTable);
		for (int i = 0; i < 6; ++i)
		{
			TableRow row = (TableRow)tl.getChildAt(i+1);
			String driver = new String(EventData.getInstance().sec1Speed[2*i]);
			String speed = new String(EventData.getInstance().sec1Speed[2*i+1]);
			
			setItem(row, 0, driver, Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
			setItem(row, 1, speed, Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.YELLOW]);
			
			driver = new String(EventData.getInstance().sec2Speed[2*i]);
			speed = new String(EventData.getInstance().sec2Speed[2*i+1]);
			
			setItem(row, 2, driver, Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
			setItem(row, 3, speed, Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.YELLOW]);
			
			driver = new String(EventData.getInstance().sec3Speed[2*i]);
			speed = new String(EventData.getInstance().sec3Speed[2*i+1]);
			
			setItem(row, 4, driver, Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
			setItem(row, 5, speed, Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.YELLOW]);
			
			driver = new String(EventData.getInstance().speedTrap[2*i]);
			speed = new String(EventData.getInstance().speedTrap[2*i+1]);
			
			setItem(row, 6, driver, Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
			setItem(row, 7, speed, Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.YELLOW]);
		}
	}
}
