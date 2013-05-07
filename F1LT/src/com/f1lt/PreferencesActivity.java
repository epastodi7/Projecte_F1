package com.f1lt;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class PreferencesActivity extends Activity 
{
	@Override
    public void onCreate(Bundle savedInstanceState) 
    { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		
		Intent intent = getIntent();
		CheckBox dctBox = (CheckBox)(findViewById(R.id.carThumbnailsCheckBox));
		CheckBox psnBox = (CheckBox)(findViewById(R.id.printShortCheckBox));
		CheckBox scomBox = (CheckBox)(findViewById(R.id.showCommentaryCheckBox));
		
		dctBox.setChecked(intent.getBooleanExtra("DrawCarThumbnails", false));
		psnBox.setChecked(intent.getBooleanExtra("PrintShortNames", false));
		scomBox.setChecked(intent.getBooleanExtra("ShowCommentaryLine", false));
		
		try
		{
			getActionBar().hide();
		}
		catch (NoSuchMethodError e) { }	
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		Button but = (Button)findViewById(R.id.okButton);
		if (but != null)
        	but.setOnClickListener(listener);
    }
	
	@Override
    protected void onStart()
    {
    	super.onStart();
    	
    	ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
	    LinearLayout root = (LinearLayout) decorView.getChildAt(0);
	    FrameLayout titleContainer = (FrameLayout) root.getChildAt(0);
	    View title = titleContainer.getChildAt(0);
	    title.setVisibility(View.GONE);
    	
//    	if (!restarted)
    	{
    		         
    	}
    	
    }
	
	public OnClickListener listener = new OnClickListener()
	{
		public void onClick(View v)
		{
			CheckBox dctBox = (CheckBox)(findViewById(R.id.carThumbnailsCheckBox));
			CheckBox psnBox = (CheckBox)(findViewById(R.id.printShortCheckBox));
			CheckBox scomBox = (CheckBox)(findViewById(R.id.showCommentaryCheckBox));
			
			boolean drawCarThumbnails = dctBox.isChecked();
			boolean printShortNames = psnBox.isChecked();
			boolean showCommentaryLine = scomBox.isChecked();
						
			Intent intent = new Intent();
			intent.putExtra("DrawCarThumbnails", drawCarThumbnails);
			intent.putExtra("PrintShortNames", printShortNames);
			intent.putExtra("ShowCommentaryLine", showCommentaryLine);
						
			setResult(RESULT_OK, intent);
			finish();
		}
	};
}
