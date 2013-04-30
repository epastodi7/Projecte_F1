package com.f1lt;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.cookie.Cookie;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;



public class F1LTActivity extends FragmentActivity  implements DataStreamReceiver
{
	
	private Thread authThread;
    private final Handler handler = new Handler();
    private DataStreamReader dataStreamReader;
    
    private EventData eventData = EventData.getInstance();
    
    private ProgressDialog dialog;
    LTViewFragment ltViewFragment;
        
    private boolean restarted = false;
    private boolean portrait = false;
    private boolean messageBoardShown = false;
    private boolean showCommentaryLine = true;
    private boolean delayed;
    
    private final String PREFS_NAME = "F1LTPrefs";    
    private final int GET_LOGIN = 1;
    private final int GET_PREFERENCES = 2;
    private final int SHOW_COMMENTARY = 3;
    
    private boolean alertDialogShown = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {    
    	Log.d("F1LTActivity", "onCreate");
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		try
		{
			LTData.loadSeasonData(getAssets().open("season.xml"));
		}
		catch (IOException e)
		{
			
		}
		
		dataStreamReader = DataStreamReader.getInstance();
				
		dataStreamReader.setDataStreamReceiver(handler, F1LTActivity.this);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);		
		
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
				
		
		// However, if we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
//        if (savedInstanceState != null) 
//            return;
		
		// Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) 
        {
                                                
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            boolean drawCarThumbnails = settings.getBoolean("DrawCarThumbnails", true);
        	boolean printShortNames = settings.getBoolean("PrintShortNames", false);
        	showCommentaryLine = settings.getBoolean("ShowCommentaryLine", true);
        	
        	Intent data = new Intent();
			data.putExtra("DrawCarThumbnails", drawCarThumbnails);
			data.putExtra("PrintShortNames", printShortNames);
        	
        	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();            
            
            ltViewFragment = (LTViewFragment)getSupportFragmentManager().findFragmentByTag("LTViewFragment");
            if (ltViewFragment == null)
            	ltViewFragment =  new LTViewFragment(data);
            
            transaction.add(R.id.fragment_container, ltViewFragment, "LTViewFragment");
            transaction.commit();
            

            TextView commentaryTextView = (TextView)findViewById(R.id.commentaryTextView);
    		if (commentaryTextView != null)
    		{
    			commentaryTextView.setOnClickListener(
    					new OnClickListener() 
    					{
							
							public void onClick(View arg0) 
							{
								showCommentary();								
							}
						}
    					);
    			
    			if (!showCommentaryLine)
    				commentaryTextView.setVisibility(View.GONE);
    		}
            
    		if (!dataStreamReader.isConnected())
    		{
    			Log.d("F1LTAct L139","No conectat");
    			login(true);
//    			onAuthorized(null, true);
    		}
    		else
    			Log.d("F1LTAct L144","Conectat");
    			updateCommentaryLine();
        }
                            
    }    
    
    @Override
    protected void onSaveInstanceState (Bundle outState)
    {
    	Log.d("F1LTActivity", "onSaveInstanceState");
    	super.onSaveInstanceState(outState);
    	outState.putBoolean("connected", true);
    }
    
    @Override
    protected void onStart()
    {
    	Log.d("F1LTActivity", "onStart");
    	super.onStart();
    	
//    	getActionBar().hide();
//    	ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
//	    LinearLayout root = (LinearLayout) decorView.getChildAt(0);
//	    FrameLayout titleContainer = (FrameLayout) root.getChildAt(0);
//	    View title = titleContainer.getChildAt(0);
//	    title.setVisibility(View.GONE);
    	
//    	if (!restarted)
    	{
    		         
    	}
    	
    }
    
    @Override
    protected void onRestart()
    {
    	Log.d("F1LTActivity", "onRestart");
    	restarted = true;
    	super.onRestart();    	
    }
    
    @Override 
    protected void onDestroy()
    {
    	Log.d("F1LTActivity", "onDestroy");
    	super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	Log.d("F1LTActivity", "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	Log.d("F1LTActivity", "onOptionsItemSelected");
        // Handle item selection
        switch (item.getItemId()) 
        {
            case R.id.preferences:
               	showPreferences();
                return true;
                
            case R.id.login:
            	login(true);
            	return true;
            	
            case R.id.commentary:
            	showCommentary();
            	return true;
            	
            case R.id.sr:
            	showSpeedRecords();
            	return true;
            	
            case R.id.fps:
            	showFastestPitStops();
            	return true;
            	
            case R.id.fl:
            	showFastestLaps();
            	return true;
            	
            case R.id.ltc:
            	showLapTimeComparison();
            	return true;
            	
            case R.id.h2h:
            	showHeadToHead();
            	return true;
            	
            case R.id.item1:
            	showHeadToHead();
            	return true;
            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
    	Log.d("F1LTActivity", "onActivityResult");
    	Log.d("ResultCode dins F1LT", Integer.toString(resultCode));
    	
		if (requestCode == GET_LOGIN && resultCode == RESULT_OK)
		{			
			String email = data.getStringExtra("Email");
			String passwd = data.getStringExtra("Passwd");
		
			onConnectedClicked(email, passwd);
		}
		if (requestCode == GET_PREFERENCES && resultCode == RESULT_OK)
		{
			
			ltViewFragment = (LTViewFragment)getSupportFragmentManager().findFragmentByTag("LTViewFragment");
            if (ltViewFragment != null)
            	ltViewFragment.setPreferences(data);
			
			
            boolean drawCarThumbnails = data.getBooleanExtra("DrawCarThumbnails", false);
			boolean printShortNames = data.getBooleanExtra("PrintShortNames", false);
			boolean showCommentaryLine = data.getBooleanExtra("ShowCommentaryLine", false);
									
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putBoolean("DrawCarThumbnails", drawCarThumbnails);
		    editor.putBoolean("PrintShortNames", printShortNames);
		    editor.putBoolean("ShowCommentaryLine", showCommentaryLine);
		    editor.commit();
		    
		    TextView commentaryTextView = (TextView)findViewById(R.id.commentaryTextView);
		    if (commentaryTextView != null)
		    {
		    	if (!showCommentaryLine)
		    		commentaryTextView.setVisibility(View.GONE);
		    	else
		    		commentaryTextView.setVisibility(View.VISIBLE);
		    }
		    	
		}
		
		if (requestCode == SHOW_COMMENTARY)
		{
//			dataStreamReader.setDataStreamReceiver(handler, this);
			
			if (data != null)
			{
				boolean loginRequested = data.getBooleanExtra("Request login", false);
				if (loginRequested)
					login(true);
			}
			
			LTViewFragment lt = (LTViewFragment)getSupportFragmentManager().findFragmentByTag("LTViewFragment");
			if (lt != null)
				lt.refreshView();
		}
				
	}
    
    private void delayed() {

    	class DelayThread extends Thread {
    	    @Override
    	    public void run(){
    	    	Log.d("delayed", "FUNCIO DIFERIT");
    	    	int blocks, bytes = 0;
    	    	File sdCard = Environment.getExternalStorageDirectory();
    	        File dir = new File (sdCard.getAbsolutePath() + "/PROVA/F1/BAH/R-1");
    	        File[] llista = dir.listFiles();
    	        Log.d("NUM FITXERS CARPETA:", Integer.toString(llista.length));
    	        byte[] dades = new byte[65535];
    	    	//LTViewFragment lt = (LTViewFragment)getSupportFragmentManager().findFragmentByTag("LTViewFragment");

    	        
    	        //for(blocks=1;blocks<=llista.length;blocks++){
    	    	for(blocks=1;blocks<=250;blocks++){
    	        	
    		        String nom = "Dades";
    		        nom=nom.concat(Integer.toString(blocks));
    		        nom=nom.concat(".txt");
    		        
    		        File file = new File(dir, nom);
    		        Log.d("ARREL ARXIU: ", file.toString());
    		        dades = null;
    		        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    		        byte[] data = new byte[65535];
    		    
    		        try {
    		        	FileInputStream f = new FileInputStream(file);
    		        	int nRead = 0;
    		
    		        	while ((nRead = f.read(data, 0, data.length)) != -1) {
    		        	  buffer.write(data, 0, nRead);
    		        	}
    		
    		        	dades = buffer.toByteArray();
    		        	bytes = dades.length;
    		        	buffer.flush();
    		        	
    		    		Log.d("NUM DADES BYTES:", Integer.toString(bytes));
    		        	//f.read(temps);
    		            //f.read(dades);
    		            f.close();
    		            
    		        } catch (Exception e) {
    		            Log.e("ERROR", "Error opening Log.", e);
    		        }
    		        
    		        dataStreamReader.parseBlockDelayed(dades, bytes);
    		        //onNewDataObtained(false);
    		        //lt.refreshView();
    		        //SystemClock.sleep(1000);
    	        }	
    	    }
    	}

    	DelayThread mThread = new DelayThread();
    	mThread.start();
				
	}
    
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) 
//    {
//        super.onConfigurationChanged(newConfig);
//
//        // Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 
//        {
//            
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.fragment_container, ltViewFragment, "LTViewFragment");
////            transaction.addToBackStack(null);
//            transaction.commit();
//            
//            portrait = false;
//            
////            ltViewFragment = (LTViewFragment)getSupportFragmentManager().findFragmentByTag("LTViewFragment");
////            if (ltViewFragment != null)
////            {
////            	ltViewFragment.resetColumns();
////            	ltViewFragment.refreshView();
////            }
//        } 
//        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
//        {
////            ltViewFragment = (LTViewFragment)getSupportFragmentManager().findFragmentByTag("LTViewFragment");
////            if (ltViewFragment != null)
////            {
////            	ltViewFragment.resetColumns();
////            	ltViewFragment.refreshView();
////            }
//            
//            portrait = true;
//            
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            
//            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//        	boolean printShortNames = settings.getBoolean("PrintShortNames", false);
//            
//            Intent data = new Intent();
//			data.putExtra("PrintShortNames", printShortNames);
//            
////            ltViewFragmentPortrait = (LTViewFragmentPortrait)getSupportFragmentManager().findFragmentByTag("LTViewFragment");
//            if (ltViewFragmentPortrait == null)
//            	ltViewFragmentPortrait =  new LTViewFragmentPortrait(data);
//            
//            transaction.replace(R.id.fragment_container, ltViewFragmentPortrait, "LTViewFragment");
////            transaction.addToBackStack(null);
//            transaction.commit();
//            
////            ltViewFragmentPortrait.refreshView();
//        }               
//    }
    
    @Override
    public void onBackPressed()
    {
    	Log.d("F1LTActivity", "onBackPressed");
    	//super.onBackPressed();
    	//Ask user if he wants to exit, and if yes - kill the app
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Are you sure you want to exit?")
    	       .setCancelable(false)
    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
    	       {
    	           public void onClick(DialogInterface dialog, int id) 
    	           {
    	        	   dataStreamReader.disconnect();   
    	        	   eventData.clear();
    	        	   LTData.ltTeams.clear();
    	        	   LTData.ltEvents.clear();
    	               F1LTActivity.this.finish();
    	           }
    	       })
    	       .setNegativeButton("No", new DialogInterface.OnClickListener() 
    	       {
    	           public void onClick(DialogInterface dialog, int id) 
    	           {
    	                dialog.cancel();
    	           }
    	       });
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    public void login(boolean showDialog)
    {
    	Log.d("F1LTActivity", "login");
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String email = settings.getString("email", "");
        String passwd = settings.getString("passwd", "");
        
    	if (!showDialog && !email.equals("") && !passwd.equals(""))
    	{	    		       
	        onConnectedClicked(email, Decrypter.encodePasswd(passwd));	        
    	}
    	else
    	{
    		Intent intent = new Intent(this, LoginActivity.class);
        	startActivityForResult(intent, GET_LOGIN);
    	}
    }
    
    public void showPreferences()
    {
    	Log.d("F1LTActivity", "showPreferences");
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	boolean drawCarThumbnails = settings.getBoolean("DrawCarThumbnails", true);
    	boolean printShortNames = settings.getBoolean("PrintShortNames", false);
    	boolean showCommentaryLine = settings.getBoolean("ShowCommentaryLine", true);
    	
    	Intent intent = new Intent(this, PreferencesActivity.class);
    	intent.putExtra("DrawCarThumbnails", drawCarThumbnails);
		intent.putExtra("PrintShortNames", printShortNames);
		intent.putExtra("ShowCommentaryLine", showCommentaryLine);
		
		startActivityForResult(intent, GET_PREFERENCES);
    }
    
    public void showCommentary()
    {
    	Log.d("F1LTActivity", "showCommentary");
    	Intent intent = new Intent(this, CommentaryActivity.class);    	
    	startActivityForResult(intent, SHOW_COMMENTARY);
    }
    
    public void showSpeedRecords()
    {
    	Log.d("F1LTActivity", "showSpeedRecords");
    	Intent intent = new Intent(this, SpeedRecordsActivity.class);    	
    	startActivity(intent);
    }
    
    public void showFastestPitStops()
    {
    	Log.d("F1LTActivity", "showFastestPitStops");
    	Intent intent = new Intent(this, FastestPitStopsActivity.class);    	
    	startActivity(intent);
    }
    
    public void showFastestLaps()
    {
    	Log.d("F1LTActivity", "showFastestLaps");
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);    	
    	boolean printShortNames = settings.getBoolean("PrintShortNames", false);
    	
    	Intent intent = new Intent(this, FastestLapsActivity.class);
    	intent.putExtra("PrintShortNames", printShortNames);
    	startActivity(intent);
    }
    
    public void showLapTimeComparison()
    {   
    	Log.d("F1LTActivity", "showLapTimeComparison");
    	Intent intent = new Intent(this, LapTimeComparisonActivity.class);
    	startActivity(intent);
    }
    public void showHeadToHead()
    {   
    	Log.d("F1LTActivity", "showHeadToHead");
    	Intent intent = new Intent(this, HeadToHeadActivity.class);
    	startActivity(intent);
    }
    
    public void onConnectedClicked(String email, String passwd) 
    {
    	email = "ernestpd@hotmail.com";
    	passwd = "010185epd";
    	Log.d("F1LTActivity", "onConnectedClicked");
    	String url = "http://formula1.com/reg/login?";
    	String url2 = "http://live-timing.formula1.com";
    	
    	ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString("email", email);
	    editor.putString("passwd", Decrypter.encodePasswd(passwd));
	    editor.commit();
        
//        EditText t2 = (EditText)findViewById(R.id.editText3);
        if (networkInfo != null && networkInfo.isConnected()) 
        {
//        	t2.setText("TRYING TO...");
        	authThread = HttpReader.attemptAuth(url2, url, email, passwd, handler, F1LTActivity.this);
        } 
        else 
        {
        	Toast error = Toast.makeText(this, "Could not find any active network connections!", Toast.LENGTH_LONG);
        	error.show();
        	
        	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        	    
        		@Override
        	    public void onClick(DialogInterface dialog, int which) {
        	        switch (which){
        	        case DialogInterface.BUTTON_POSITIVE:
        	            delayed = true;
        	            Log.d("BOTO RESULTAT", "OK TIO");
        	        	if(delayed){
        	        		delayed();
        	        	}
        	            break;

        	        case DialogInterface.BUTTON_NEGATIVE:
        	            delayed = false;
        	            Log.d("BOTO RESULTAT", "ROKA TIO");
        	            break;
        	        }
        	    }
        	};

        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("Not Connected. You want to play a session completed?").setPositiveButton("Yes", dialogClickListener)
        	    .setNegativeButton("No", dialogClickListener).show();
        	
//        	EditText t = (EditText)findViewById(R.id.editText3);                    
//        	t.setText("No network connection available.");
        }
    	
    }    
    
    public void onAuthorized(Cookie cookie, boolean result)
    {
    	Log.d("F1LTActivity", "onAuthorized");
//    	EditText t = (EditText)findViewById(R.id.editText3);
    	if (result)    	
    	{
    		dialog = ProgressDialog.show(this, "",
                    "Connecting. Please wait...", true);
    		
    		dataStreamReader.connectToStream("live-timing.formula1.com", 4321);
    	}
    	else
    	{
    		Toast toast = Toast.makeText(getApplicationContext(), "Wrong email or password!", Toast.LENGTH_LONG);
    		toast.setGravity(Gravity.CENTER, 0, 0);
    		toast.show();
    		
    		Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, GET_LOGIN);
    	}
    }
    
    public void onNewDataObtained(boolean updateTimer)
    {   
    	Log.d("F1LTActivity", "onNewDataObtained");
    	if (dialog != null && dialog.isShowing())
    	{
    		dialog.dismiss();
    	}    		
    	
    	LTViewFragment lt = (LTViewFragment)getSupportFragmentManager().findFragmentByTag("LTViewFragment");
		if (lt != null)
		{
			if (updateTimer){
				lt.updateStatus();
				Log.d("onNewDataObtained", "updateStatus");
			}
			else{
				lt.refreshView();
				Log.d("onNewDataObtained", "refreshView");
			}
		}	
    	updateCommentaryLine();
    }
    
    public void updateCommentaryLine()
    {
    	Log.d("F1LTActivity", "updateCommentaryLine");
    	if (eventData.commentary != null)
    	{
	    	String [] arr = eventData.commentary.split("\n");
	    	if (arr.length > 0)
	    	{
	    		String commentLine = "";
	    		if (!arr[arr.length-1].equals(""))
	    			commentLine = arr[arr.length-1];
	    		
	    		else if (arr.length > 1 && !arr[arr.length-2].equals(""))
	    			commentLine = arr[arr.length-2];
	    		
	    		TextView commentaryTextView = (TextView)findViewById(R.id.commentaryTextView);
	    		if (commentaryTextView != null)
	    		{
	    			if (commentaryTextView.getText().toString().length() != commentLine.length())
	    				commentaryTextView.setText(commentLine);
	    		}	    		
	    	}
    	}
    }
    
    public void onDataStreamError(int msgId)
    {
    	Log.d("F1LTActivity", "onDataStreamError");
    	if (alertDialogShown)
    		return;
    	
    	dataStreamReader.disconnect();
 	   
    	String msg = "Could not connect to the LT server"; 
    			
    	if (msgId == 1)
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
//    	        	   login(false);
    	           }
    	       })
    	       .setNegativeButton("Close", new DialogInterface.OnClickListener() 
    	       {
    	           public void onClick(DialogInterface dialog, int id) 
    	           {
//    	        	   dataStreamReader.disconnect();
//    	        	   sessionTimer.interrupt();
    	        	   dialog.cancel();
    	        	   alertDialogShown = false;
    	           }
    	       });
    	AlertDialog alert = builder.create();
    	alert.show();
    	alertDialogShown = true;
    }
            
//    public void onSessionTimerUpdated(String time, boolean stop)
//    {
//    	eventData.remainingTime = time;
//    	LTViewFragment lt = (LTViewFragment)getSupportFragmentManager().findFragmentByTag("LTViewFragment");
//		if (lt != null)
//			lt.updateStatus();
//    } 
    
    public void onShowMessageBoard(String msg, boolean show)
    {
    	Log.d("F1LTActivity", "onShowMessageBoard");
//    	LTViewFragment lt = (LTViewFragment)getSupportFragmentManager().findFragmentByTag("LTViewFragment");
//		if (lt != null)
		{
			if (show)
			{
				if (!messageBoardShown)
				{					
					TextView commentaryTextView = (TextView)findViewById(R.id.commentaryTextView);
				    if (commentaryTextView != null && showCommentaryLine)			    
				    	commentaryTextView.setVisibility(View.GONE);
				    
				    Intent intent = new Intent(this, NoSessionBoardActivity.class);
				    intent.putExtra("Message", msg);
				    startActivity(intent);
				    
				    messageBoardShown = true;
				}
			    
//			    lt.showMessageBoard(msg);
			}
				
			else
			{
				messageBoardShown = false;
				
				TextView commentaryTextView = (TextView)findViewById(R.id.commentaryTextView);
			    if (commentaryTextView != null && showCommentaryLine)			    			    
			    	commentaryTextView.setVisibility(View.VISIBLE);
			    
//				lt.removeMessageBoard();
			}
		}    	    	
    }
}
