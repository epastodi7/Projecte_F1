package com.f1lt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

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
	    EditText rutaPath = (EditText)findViewById(R.id.savePath);
	    TextView estatInet = (TextView)findViewById(R.id.estatInet);
	    
	    // COMPROVEM SI ESTA CONECTAT A INTERNET
		if(isNetworkAvailable()){
			//Log.d("CONNECTAT", "TIO");
			rutaPath.setText("MON/R-CHECK");
		}
		else{
			//Log.d("NO CONNECTAT", "TIO");
			rutaPath.setEnabled(false);
		    rutaPath.setFocusable(false);
		    estatInet.setText("OFFline");
		    int red = LTData.color[LTData.Colors.RED];
		    estatInet.setTextColor(red);
		}
    	
    }
	
	public OnClickListener listener = new OnClickListener()
	{
		public void onClick(View v)
		{
			EditText emailText = (EditText)findViewById(R.id.emailText);
			EditText passwdText = (EditText)findViewById(R.id.passwdText);
			EditText rutaPath = (EditText)findViewById(R.id.savePath);
			
			Intent intent = new Intent();
			intent.putExtra("Email", emailText.getText().toString());
			intent.putExtra("Passwd", passwdText.getText().toString());
			intent.putExtra("pathSave", rutaPath.getText().toString());
			String prova = intent.getStringExtra("pathSave");
			//Log.d("QUE HI HA A pathSave", prova);
			
			setResult(RESULT_OK, intent);
			finish();
		}
	};
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	
}
