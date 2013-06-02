package com.f1lt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class NoSessionBoardActivity extends Activity implements DataStreamReceiver
{
	public static boolean active = false;
	private Handler handler = new Handler();
	private boolean alertDialogShown = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.no_session_board);	
		
		Intent intent = getIntent();
		if (intent != null)
		{
			String msg = intent.getStringExtra("Message");
			
			TextView msgView = (TextView)findViewById(R.id.msgView);
			if (msgView != null)
			{
				String event = LTData.getCurrentEvent().name;
				msgView.setText(event + "\n" + encodeDate(msg));
			}
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
		active = true;
		super.onStart();
		
		DataStreamReader.getInstance().setSecondaryDataStreamReceiver(handler, this);
		
	}
	
	@Override
    public void onBackPressed()
    {
    
    }
	
	@Override
	public void onPause()
	{
		active = false;
		DataStreamReader.getInstance().removeSecondaryDataStreamReceiver();
		super.onPause();
	}
	
	public void onNewDataObtained(boolean updateTimer)
	{		
	}
	public void onShowMessageBoard(String msg, boolean show)
	{		
		if (!show)
		{
			finish();
		}			
	}
	
	public String encodeDate(String date)
	{
		int year = Integer.parseInt("20" + date.substring(0, 2));
		int month = Integer.parseInt(date.substring(2, 4));
		int day = Integer.parseInt(date.substring(4, 6));
		int hour = Integer.parseInt(date.substring(6, 8)) + 1;
		
		String str = "" + year + "." + (month < 10 ? "0" + month : month) + "." + (day < 10 ? "0" + day : day) + " - " + hour + ":00 GMT";
		return str;
	}
	
	public void onDataStreamError(int code)
	{
		if (alertDialogShown)
			return;
		
		String msg = "Could not connect to the LT server"; 
		DataStreamReader.getInstance().disconnect();
		
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
    	        	   DataStreamReader.getInstance().reconnect();
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
}


