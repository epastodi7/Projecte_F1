package com.f1lt;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

public class GraphicActivity extends Activity implements DataStreamReceiver{

		private boolean alertDialogShown = false;
		DataStreamReader dataStreamReader;
		private Handler handler = new Handler();
		private EventData eventData = EventData.getInstance();
		
		//PROVA PER 2 PILOTS
		DriverData driverData1 = eventData.driversData.get(2);
		DriverData driverData2 = eventData.driversData.get(1);
		List<Integer> pos_history1 = driverData1.posHistory;
		List<Integer> pos_history2 = driverData2.posHistory;
		
		//PROVA PER DADES TEMPS
		int minutsSessio = eventData.minutsSessio;
		//Collection<Double> airC = eventData.airTempHistory.values();
		//Collection<Double> trackC = eventData.airTempHistory.values();
		Number[] llista1, llista2;
		
		
	    private XYPlot mySimpleXYPlot;
	 
	    @Override
	    public void onCreate(Bundle savedInstanceState)
	    {
	 
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.graphic);
	        airTrackTEMP();
	        
	    }
	    
		@SuppressWarnings("null")
		private Number[] CollectionToList(ConcurrentHashMap<Integer, Double> History) {
			Number[] prova = new Number[History.size()];
			int i = 0;
			Log.d("HI HAN X VALORS DE track temperature", Integer.toString(History.size()));
			for (Double value : History.values()){
				Log.d("temperatura", Double.toString(value));
				prova[i] = value;
				i++;
			}
			return prova;
			
		}

		private void convert() {
			
			//CONVERTEIX DOS PILOTS
			llista1=toIntArray(pos_history1);
			llista2=toIntArray(pos_history2);
			
		}
		
		private Integer[] toIntArray(List<Integer> list){
			  Integer[] ret = new Integer[list.size()];
			  for(int i = 0;i < ret.length;i++)
			    ret[i] = list.get(i);
			  return ret;
			}

		private void info() {
			Log.d("GRAPHIC ACT", "INFO");
			Log.d("LLISTA airTempHistoric", Integer.toString(eventData.airTempHistory.size()));
			Collection<Integer> airTemp;
			//Log.d("LLista despres air", Integer.toString(airC.size()));
			
			/*
			for(int i=0;i<eventData.driversData.size();i++){
				DriverData driverData = eventData.driversData.get(i);
				List<Integer> pos_history = driverData.posHistory;
				int size = pos_history.size();
				int event_info = eventData.eventInfo.laps;
				Log.d("POSICIO I NOM PILOT: ",i+" "+driverData.driver);
			}
			*/
			//Log.d("POS PILOT: ",Integer.toString(driverData.pos));
			//Log.d("VOLTES GUARDADES PILOT: ",Integer.toString(driverData.lapData.size()));
			//Log.d("SIZE POSICIONS: ", Integer.toString(size));
			//Log.d("EVENT INFO / LAPS: ", Integer.toString(event_info));
		}

		//@Override
		public void onStart()
		{
			super.onStart();
			
			dataStreamReader = DataStreamReader.getInstance();
			dataStreamReader.setSecondaryDataStreamReceiver(handler, this);
			
			
		}

		@Override
	    public boolean onCreateOptionsMenu(Menu menu) 
	    {
	    	Log.d("Grafic", "onCreateOptionsMenu");
	    	/*
	    	menu.add(0, AirTrackTEMP, 0, "Air/Track Temp");
	    	menu.add(0, Humidity, 0, "Humidity");
	    	menu.add(0, Pressure, 0, "Pressure");
	    	menu.add(0, WetDry, 0, "Wet/Dry");
	    	*/
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.menu_graphic, menu);
	    	
	        return true;
	    }
		
		@Override
	    public boolean onOptionsItemSelected(MenuItem item) 
	    {
	    	Log.d("Grafic", "onOptionsItemSelected");
	        // Handle item selection
	        switch (item.getItemId()) 
	        {
	            case R.id.AirTrackTEMP:
	               	airTrackTEMP();
	                return true;
	                
	            case R.id.Humidity:
	            	humidity();
	            	return true;
	            	
	            case R.id.Pressure:
	            	pressure();
	            	return true;
	            	
	            case R.id.WindSpeed:
	            	windSpeed();
	            	return true;
	            	
	            case R.id.WetDry:
	            	wetDry();
	            	return true;
	            
	            case R.id.FlagStatus:
	            	flags();
	            	return true;
	            
	            default:
	                return super.onOptionsItemSelected(item);
	        }
	    }
		
		
		private void flags() {
			setContentView(R.layout.graphic);
			//convert();
	        llista1=CollectionToList(eventData.flagStatusHistory);
	        //info();
	        
	        // initialize our XYPlot reference:
	        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
	 
	        // Create a couple arrays of y-values to plot:
	        Number[] series1Numbers = llista1;
	 
	        // Turn the above arrays into XYSeries':
	        XYSeries series1 = new SimpleXYSeries(
	                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
	                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
	                "Flag Status");                             // Set the display title of the series
	 
	 
	        // Create a formatter to use for drawing a series using LineAndPointRenderer:
	        LineAndPointFormatter series1Format = new LineAndPointFormatter(
	                Color.rgb(0, 200, 0),                   // line color
	                Color.rgb(0, 100, 0),                   // point color
	                null);                                  // fill color (none)
	 
	        // add a new series' to the xyplot:
	        mySimpleXYPlot.addSeries(series1, series1Format);
	 
	 
	        // reduce the number of range labels
	        mySimpleXYPlot.setTicksPerRangeLabel(2);
	        mySimpleXYPlot.setTitle("Flag Status");

	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(50);
	        
	        mySimpleXYPlot.setRangeBoundaries(0, 10, BoundaryMode.FIXED);
	        
	        //PROVA
	        //mySimpleXYPlot.setRangeBottomMax(22);
	        //mySimpleXYPlot.setRangeBottomMax(1);
	        
	        //PROVA
	        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 5);
	        //mySimpleXYPlot.set
	        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
	 
	        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
	        // To get rid of them call disableAllMarkup():
	        mySimpleXYPlot.disableAllMarkup();
			
		}

		private void wetDry() {
			setContentView(R.layout.graphic);
			//convert();
	        llista1=CollectionToList(eventData.wetDryHistory);
	        llista2=CollectionToList(eventData.windDirectionHistory);
	        //info();
	        
	        // initialize our XYPlot reference:
	        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
	 
	        // Create a couple arrays of y-values to plot:
	        Number[] series1Numbers = llista1;
	        Number[] series2Numbers = llista2;
	 
	        // Turn the above arrays into XYSeries':
	        XYSeries series1 = new SimpleXYSeries(
	                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
	                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
	                "Wet/Dry");                             // Set the display title of the series
	 
	        // same as above
	        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Wind Direction");
	 
	        // Create a formatter to use for drawing a series using LineAndPointRenderer:
	        LineAndPointFormatter series1Format = new LineAndPointFormatter(
	                Color.rgb(0, 200, 0),                   // line color
	                Color.rgb(0, 100, 0),                   // point color
	                null);                                  // fill color (none)
	 
	        // add a new series' to the xyplot:
	        mySimpleXYPlot.addSeries(series1, series1Format);
	 
	        // same as above:
	        mySimpleXYPlot.addSeries(series2,
	                new LineAndPointFormatter(Color.rgb(0, 0, 200), Color.rgb(0, 0, 100), null));
	 
	        // reduce the number of range labels
	        mySimpleXYPlot.setTicksPerRangeLabel(2);
	        mySimpleXYPlot.setTitle("Wet/Dry & Wind Direction");

	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(50);
	        
	        mySimpleXYPlot.setRangeBoundaries(0, 360, BoundaryMode.FIXED);
	        
	        //PROVA
	        //mySimpleXYPlot.setRangeBottomMax(22);
	        //mySimpleXYPlot.setRangeBottomMax(1);
	        
	        //PROVA
	        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 20);
	        //mySimpleXYPlot.set
	        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
	 
	        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
	        // To get rid of them call disableAllMarkup():
	        mySimpleXYPlot.disableAllMarkup();
			
		}

		private void windSpeed() {
			setContentView(R.layout.graphic);
			//convert();
	        llista1=CollectionToList(eventData.windSpeedHistory);
	        //info();
	        
	        // initialize our XYPlot reference:
	        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
	 
	        // Create a couple arrays of y-values to plot:
	        Number[] series1Numbers = llista1;
	 
	        // Turn the above arrays into XYSeries':
	        XYSeries series1 = new SimpleXYSeries(
	                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
	                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
	                "Wind Speed");                             // Set the display title of the series
	 
	 
	        // Create a formatter to use for drawing a series using LineAndPointRenderer:
	        LineAndPointFormatter series1Format = new LineAndPointFormatter(
	                Color.rgb(0, 200, 0),                   // line color
	                Color.rgb(0, 100, 0),                   // point color
	                null);                                  // fill color (none)
	 
	        // add a new series' to the xyplot:
	        mySimpleXYPlot.addSeries(series1, series1Format);
	 
	 
	        // reduce the number of range labels
	        mySimpleXYPlot.setTicksPerRangeLabel(2);
	        mySimpleXYPlot.setTitle("Wind Speed");

	        
	        //PROVA
	        //mySimpleXYPlot.setRangeBottomMax(22);
	        //mySimpleXYPlot.setRangeBottomMax(1);
	        
	        mySimpleXYPlot.setRangeBoundaries(0, 30, BoundaryMode.FIXED);
	        
	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(50);
	        
	        //PROVA
	        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 5);
	        //mySimpleXYPlot.set
	        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
	 
	        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
	        // To get rid of them call disableAllMarkup():
	        mySimpleXYPlot.disableAllMarkup();
			
		}

		private void pressure() {
			setContentView(R.layout.graphic);
			//convert();
	        llista1=CollectionToList(eventData.pressureHistory);
	        //info();
	        
	        // initialize our XYPlot reference:
	        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
	 
	        // Create a couple arrays of y-values to plot:
	        Number[] series1Numbers = llista1;
	 
	        // Turn the above arrays into XYSeries':
	        XYSeries series1 = new SimpleXYSeries(
	                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
	                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
	                "Humidity");                             // Set the display title of the series
	 
	 
	        // Create a formatter to use for drawing a series using LineAndPointRenderer:
	        LineAndPointFormatter series1Format = new LineAndPointFormatter(
	                Color.rgb(0, 200, 0),                   // line color
	                Color.rgb(0, 100, 0),                   // point color
	                null);                                  // fill color (none)
	 
	        // add a new series' to the xyplot:
	        mySimpleXYPlot.addSeries(series1, series1Format);
	 
	 
	        // reduce the number of range labels
	        mySimpleXYPlot.setTicksPerRangeLabel(2);
	        mySimpleXYPlot.setTitle("Pressure");

	        
	        //PROVA
	        //mySimpleXYPlot.setRangeBottomMax(22);
	        //mySimpleXYPlot.setRangeBottomMax(1);
	        
	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(50);
	        
	        //PROVA
	        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 5);
	        //mySimpleXYPlot.set
	        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
	        
	        
	        mySimpleXYPlot.setRangeBoundaries(950, 1100, BoundaryMode.FIXED);
	 
	        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
	        // To get rid of them call disableAllMarkup():
	        mySimpleXYPlot.disableAllMarkup();
			
		}

		private void humidity() {
			
			setContentView(R.layout.graphic);
	        llista1=CollectionToList(eventData.humidityHistory);
	        
	        // initialize our XYPlot reference:
	        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
	 
	        // Create a couple arrays of y-values to plot:
	        Number[] series1Numbers = llista1;
	 
	        // Turn the above arrays into XYSeries':
	        XYSeries series1 = new SimpleXYSeries(
	                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
	                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
	                "Humidity");                             // Set the display title of the series
	 
	 
	        // Create a formatter to use for drawing a series using LineAndPointRenderer:
	        LineAndPointFormatter series1Format = new LineAndPointFormatter(
	                Color.rgb(0, 200, 0),                   // line color
	                Color.rgb(0, 100, 0),                   // point color
	                null);                                  // fill color (none)
	 
	        // add a new series' to the xyplot:
	        mySimpleXYPlot.addSeries(series1, series1Format);
	 
	 
	        // reduce the number of range labels
	        mySimpleXYPlot.setTicksPerRangeLabel(2);
	        mySimpleXYPlot.setTitle("Humidity");

	        
	        //PROVA
	        //mySimpleXYPlot.setRangeBottomMax(22);
	        //mySimpleXYPlot.setRangeBottomMax(1);
	        
	        mySimpleXYPlot.setRangeBoundaries(0, 100, BoundaryMode.FIXED);
	        
	        //PROVA
	        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 5);
	        //mySimpleXYPlot.set
	        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
	        
	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(50);
	        
	        mySimpleXYPlot.setRangeTopMax(100);
	        mySimpleXYPlot.setRangeTopMin(0);
	 
	        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
	        // To get rid of them call disableAllMarkup():
	        mySimpleXYPlot.disableAllMarkup();
			
		}

		private void airTrackTEMP() {
			
			setContentView(R.layout.graphic);
			//convert();
	        llista1=CollectionToList(eventData.trackTempHistory);
	        llista2=CollectionToList(eventData.airTempHistory);
	        //info();
	        
	        // initialize our XYPlot reference:
	        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
	 
	        // Create a couple arrays of y-values to plot:
	        Number[] series1Numbers = llista1;
	        Number[] series2Numbers = llista2;
	 
	        // Turn the above arrays into XYSeries':
	        XYSeries series1 = new SimpleXYSeries(
	                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
	                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
	                "Track Temp");                             // Set the display title of the series
	 
	        // same as above
	        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Air Temp");
	 
	        // Create a formatter to use for drawing a series using LineAndPointRenderer:
	        LineAndPointFormatter series1Format = new LineAndPointFormatter(
	                Color.rgb(0, 200, 0),                   // line color
	                Color.rgb(0, 100, 0),                   // point color
	                null);                                  // fill color (none)
	 
	        // add a new series' to the xyplot:
	        mySimpleXYPlot.addSeries(series1, series1Format);
	 
	        // same as above:
	        mySimpleXYPlot.addSeries(series2,
	                new LineAndPointFormatter(Color.rgb(0, 0, 200), Color.rgb(0, 0, 100), null));
	 
	        // reduce the number of range labels
	        mySimpleXYPlot.setTicksPerRangeLabel(2);
	        mySimpleXYPlot.setTitle("Temperature");

	        mySimpleXYPlot.setRangeBoundaries(0, 70, BoundaryMode.FIXED);
	        
	        //PROVA
	        //mySimpleXYPlot.setRangeBottomMax(22);
	        //mySimpleXYPlot.setRangeBottomMax(1);
	        
	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(50);
	        
	        //PROVA
	        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 5);
	        //mySimpleXYPlot.set
	        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
	 
	        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
	        // To get rid of them call disableAllMarkup():
	        mySimpleXYPlot.disableAllMarkup();
			
		}

		//@Override
		public void onNewDataObtained(boolean updateTimer)
		{

		}
		
		//@Override
		public void onPause()
		{
			dataStreamReader.removeSecondaryDataStreamReceiver();
			super.onPause();
		}

		//@Override
		public void onShowMessageBoard(String msg, boolean rem) {
			// TODO Auto-generated method stub
			
		}

		//@Override
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
//	    	        	   	Intent intent = new Intent();
//							intent.putExtra("Request login", true);
//													
//							setResult(RESULT_OK, intent);
//							finish();
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
