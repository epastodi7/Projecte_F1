package com.f1lt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

public class CommentaryActivity extends Activity implements DataStreamReceiver
{
	private Handler handler = new Handler();
	DataStreamReader dataStreamReader;
	private boolean alertDialogShown = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.commentary);	
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
//		updateCommentary();
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
	    		    
		updateCommentary(true);
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
			updateCommentary(false);
		
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
	
	public void updateCommentary(boolean scrollToBottom)
	{		
		TextView commentaryTextView = (TextView)findViewById(R.id.commentaryTextView);		
		final ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView1);
		
		if (commentaryTextView != null && EventData.getInstance().commentary != null && scrollView != null)
		{			
			String commentary = new String(EventData.getInstance().commentary);
			int comLength = commentaryTextView.getText().toString().length();
			
			if (commentary.length() > comLength)
			{
				
				commentaryTextView.setText(commentary);
//				commentaryTextView.append(commentary.substring(comLength-1));
//				commentaryTextView.invalidate();
				
				if (scrollToBottom)
				{
					scrollView.postDelayed(new Runnable()
					{
						public void run()
						{
							scrollView.fullScroll(ScrollView.FOCUS_DOWN);
						}
					}, 200);
				}
					
			}						
		}
	}
}
