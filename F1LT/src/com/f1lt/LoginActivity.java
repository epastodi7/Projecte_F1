package com.f1lt;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class LoginActivity extends Activity 
{
	@Override
    public void onCreate(Bundle savedInstanceState) 
    { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		Button but = (Button)findViewById(R.id.connectButton);
		if (but != null)
        	but.setOnClickListener(listener);
		
		try
		{
			getActionBar().hide();
		}
		catch (NoSuchMethodError e) { }	
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
    	
    }
	
	public OnClickListener listener = new OnClickListener()
	{
		public void onClick(View v)
		{
			EditText emailText = (EditText)findViewById(R.id.emailText);
			EditText passwdText = (EditText)findViewById(R.id.passwdText);
			
			Intent intent = new Intent();
			intent.putExtra("Email", emailText.getText().toString());
			intent.putExtra("Passwd", passwdText.getText().toString());
			
			
			setResult(RESULT_OK, intent);
			finish();
		}
	};
	
	
}
