package com.f1lt;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import android.os.Message;
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
    //private final Handler handler = new Handler();
	
    private final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
        	// handle the message sent from delay thread
        	byte[] dades = new byte[65535];
        	int num_dades=0;
        	Bundle bundle = msg.getData();
        	dades = bundle.getByteArray("Data");
        	//Log.d("Handle Message", dades.toString());
        	num_dades = dades.length;
        	
        	//Log.d("Handle Message", Integer.toString(num_dades));
        	
        	dataStreamReader.parseBlockDelayed(dades, num_dades);
        	onNewDataObtained(false);
    		
        }
    };
    private DataStreamReader dataStreamReader;
    
    private EventData eventData = EventData.getInstance();
    
    private ProgressDialog dialog;
    LTViewFragment ltViewFragment;
        
    private boolean restarted = false;
    private boolean portrait = false;
    private boolean messageBoardShown = false;
    private boolean showCommentaryLine = true;
    private boolean delayed;
    
    private static String RUTA_SAVE = "/PROVA/F1/MON/R-CHECK";
    private static String RUTA_LOAD = "/PROVA/F1/MON/R";
    
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
            	graphic();
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
    	String ruta = "/PROVA/F1/";
    	
		if (requestCode == GET_LOGIN && resultCode == RESULT_OK)
		{
			//Log.d("ENTRA", "ON VULL");
			String email = data.getStringExtra("Email");
			String passwd = data.getStringExtra("Passwd");
			String rutaPath = data.getStringExtra("pathSave");
	        //Log.d("RUTAPATH", rutaPath);
	        ruta=ruta.concat(rutaPath);
	        //Log.d("RUTA", ruta);
	        setRUTA_SAVE(ruta);
	        dataStreamReader.setRUTA_SAVE(getRUTA_SAVE());
		
			onConnectedClicked(email, passwd);
		}
		if (requestCode == GET_PREFERENCES && resultCode == RESULT_OK)
		{
			//Log.d("NO ENTRA", "ON VULL");
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
    	
    	loadKEYFrame();
    	
    	Thread DelayThread = new Thread(){
    	//class DelayThread extends Thread {
    	    
    		@Override
    	    public void run(){
    	    	Log.d("delayed", "FUNCIO DIFERIT");
    	    	int blocks, bytes = 0;
    			long segonsDelay=0, Delay=0;
    	    	File sdCard = Environment.getExternalStorageDirectory();
    	        File dir = new File (sdCard.getAbsolutePath() + RUTA_LOAD);
    	        File[] llista = dir.listFiles();
    	        Log.d("NUM FITXERS CARPETA:", Integer.toString(llista.length));
    	        byte[] dades = new byte[65535];
    	    	//LTViewFragment lt = (LTViewFragment)getSupportFragmentManager().findFragmentByTag("LTViewFragment");

    	        eventData.sessionStarted=true;
    	        //CORRECTE
    	        int fitxersDades = (llista.length-1)/3;
    	        //PROVA NO MILIS I TEMPS
    	        int fitxersDades2 = llista.length-1;
    	        
    	        // Arribem fins un arxiu abans perque hi ha el DadesKEYS
    	        Log.d("ARXIUS A REPRODUIR: ",Integer.toString(fitxersDades));
    	        
    	        for(blocks=1;blocks<=fitxersDades;blocks++){   	
	        	//for(blocks=1;blocks<=150;blocks++){   	    	
    	        	
    	        	segonsDelay=gestionaMilis(blocks,fitxersDades, dir);
    	        	Log.d("SEGONS DE DELAY", Long.toString(segonsDelay));
    	        	
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
    		            Log.d("ERROR", "Error obrint fitxer");
    		        }
    		        try{
    		        	//PROVES PER ANAR MES RAPID
    		        	segonsDelay = 1;
    		        	Delay = 900;
    		        	Thread.sleep(segonsDelay*Delay);
    		        	//dataStreamReader.parseBlockDelayed(dades, bytes);
    		        	
    		        	Message msg = new Message();
    		            Bundle datas = new Bundle();
    		           
    		            datas.putString("Event", "Delay timeout");        //we can put anything we want here, is just a plain text
    		            datas.putByteArray("Data", dades);                //we need to add byte array with data parsed from the file
    		            msg.setData(datas);
    		            handler.sendMessage(msg);                        //this points to the handler from the main thread
    		        } catch (Exception e) {
    		            Log.e("ERROR", "Error Pasant dades al handle", e);
    		        }
    		        

    	        }
	        	//dataStreamReader.disconnect();
	        	//dataStreamReader.connected = false;
    	    }

			private long gestionaMilis(int blocks, int fitxersDades, File dir) {
				
				long resultat1 = 0, resultat2 = 0;
				
				//Arxiu de lectura actual
				String nom = "Milis";
		        nom=nom.concat(Integer.toString(blocks));
		        nom=nom.concat(".txt");
		        
		        //Arxiu de lectura posterior
				String nom2 = "Milis";
		        nom2=nom2.concat(Integer.toString(blocks+1));
		        nom2=nom2.concat(".txt");
				
				if(blocks==fitxersDades){
					return 1;
				}
				else{
					resultat1 = obreArxiuMilis(nom, dir);
					resultat2 = obreArxiuMilis(nom2, dir);
				}
		        
		        
				return resultat2-resultat1;
				
			}

			private long obreArxiuMilis(String nom, File dir) throws BufferUnderflowException {
				byte[] dades = new byte[10];
				int bytes = 0;
				long res = 0;
				
				File file = new File(dir, nom);
		        //Log.d("ARREL ARXIU: ", file.toString());
		        dades = null;
		        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		        byte[] data = new byte[10];
		    
		        try {
		        	FileInputStream f = new FileInputStream(file);
		        	int nRead = 0;
		
		        	while ((nRead = f.read(data, 0, data.length)) != -1) {
		        	  buffer.write(data, 0, nRead);
		        	}
		
		        	dades = buffer.toByteArray();
		        	//bytes = dades.length;
		        	
		        	ByteBuffer buf = ByteBuffer.wrap(dades);
		        	buf = ByteBuffer.wrap(dades);
		        	buf.order(ByteOrder.BIG_ENDIAN);
		        	res = buf.getLong();
		        	
		        	buffer.flush();
		        	
		    		//Log.d("NUM DADES BYTES:", Integer.toString(bytes));

		            f.close();
		            
		        } catch (Exception e) {
		            Log.d("ERROR", "Error obrint fitxer Milis");
		        }
				return res;
			}
    	};

    	//DelayThread mThread = new DelayThread();
    	DelayThread.start();
				
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
    
    private void loadKEYFrame() {
    	File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + RUTA_LOAD);
        
        String nom = "DadesKEY.txt";
        
        File file = new File(dir, nom);
        Log.d("ARREL ARXIU KEY: ", file.toString());
        byte[] dades = new byte[100];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[65535];
    
        try {
        	FileInputStream f = new FileInputStream(file);
        	int nRead = 0;

        	while ((nRead = f.read(data, 0, data.length)) != -1) {
        	  buffer.write(data, 0, nRead);
        	}
        	
        	dades = buffer.toByteArray();
        	String text = new String(dades, "UTF8");
        	//Log.d("NUM KEY FRAME PRE VALUE OF:", text);
        	
        	int key = Integer.valueOf(text);
        	
    		Log.d("NUM KEY FRAME:", Integer.toString(key));
    		dataStreamReader.guardarKey(key);
        	//f.read(temps);
            //f.read(dades);
            f.close();
            
        } catch (Exception e) {
            Log.e("ERROR", "Error obrint fitxer", e);
        }
		
	}

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
    	        	   //dataStreamReader.borrablocks();
    	        	   eventData.clear();
    	        	   LTData.ltTeams.clear();
    	        	   LTData.ltEvents.clear();
    	               F1LTActivity.this.finish();
    	               clearApplicationData();
    	               System.exit(0);
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
    
    public void graphic()
    {   
    	Log.d("F1LTActivity", "graphic");
    	Intent intent = new Intent(this, GraphicActivity.class);
    	startActivity(intent);
    }
    
    public void onConnectedClicked(String email, String passwd) 
    {
    	email = "pdernest@gmail.com";
    	passwd = "ernestpd85";
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
        	final Toast error = Toast.makeText(this, "Could not find any active network connections!", Toast.LENGTH_LONG);
        	
        	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        	    
        		@Override
        	    public void onClick(DialogInterface dialog, int which) {
        	        switch (which){
        	        case DialogInterface.BUTTON_POSITIVE:
        	            delayed = true;
        	        	if(delayed){
        	        		delayed();
        	        	}
        	            break;

        	        case DialogInterface.BUTTON_NEGATIVE:
        	            delayed = false;
        	            error.show();
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
			dataStreamReader.guardarEventData();
			if (updateTimer){
				lt.updateStatus();
				//Log.d("onNewDataObtained", "updateStatus");
			}
			else{
				lt.refreshView();
				//Log.d("onNewDataObtained", "refreshView");
			}
		}	
    	updateCommentaryLine();
    }
    
    public void updateCommentaryLine()
    {
    	//Log.d("F1LTActivity", "updateCommentaryLine");
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
    
    public void clearApplicationData() 
    {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) 
    {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

	public static String getRUTA_SAVE() {
		//Log.d("getRUTA", RUTA_SAVE);
		return RUTA_SAVE;
	}

	public static void setRUTA_SAVE(String rUTA_SAVE) {
		//Log.d("setRUTA ABANS", RUTA_SAVE);
		//Log.d("setRUTA ENTRANT", rUTA_SAVE);
		RUTA_SAVE = rUTA_SAVE;
	}
}
