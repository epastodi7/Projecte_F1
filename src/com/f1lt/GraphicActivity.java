package com.f1lt;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.androidplot.ui.SizeMetrics;
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
		private ArrayList<Integer> keys_minuts = new ArrayList<Integer>();
		
		//PROVA PER 2 PILOTS
		/*
		DriverData driverData1 = eventData.driversData.get(2);
		DriverData driverData2 = eventData.driversData.get(1);
		List<Integer> pos_history1 = driverData1.posHistory;
		List<Integer> pos_history2 = driverData2.posHistory;
		*/
		
		//PROVA PER DADES TEMPS
		int minutsSessio = eventData.minutsSessio;

		Number[] llista1, llista2;
		
		
	    private XYPlot mySimpleXYPlot;
	 
	    @Override
	    public void onCreate(Bundle savedInstanceState)
	    {
	 
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.graphic);
	        obteOrdenaKeysMinuts();
	        airTrackTEMP();
	        //imprimeixHash();
	        
	    }
	    
	    private void obteOrdenaKeysMinuts() {
	    	Map<Integer, Integer> map = eventData.temps_guardats;
			//Log.d("OBTEKEYS","MINUTS");
			for (Entry<Integer, Integer> entry : map.entrySet()) {
				Integer key = entry.getKey();
				keys_minuts.add(key);
			    Integer value = entry.getValue();
			    //Log.d("OBTEMINUTS GUARDATS I VALUE", Integer.toString(key)+" "+Integer.toString(value));
			}
			ordenaMinuts(keys_minuts);
	    }
	    
	    public class DescendingComparator implements Comparator{
    	   public int compare(Object o1, Object o2) /*descending order*/  {
    	      if (((Integer) o1).intValue() > ((Integer)o2).intValue()) return -1;
    	      else if (((Integer) o1).intValue() < ((Integer)o2).intValue()) return 1;
    	      return 0; /*equal*/
    	   }
    	}

	    	
	    
		@SuppressWarnings("unchecked")
		private void ordenaMinuts(ArrayList<Integer> keys_minuts) {
			Collections.sort(keys_minuts, new DescendingComparator()); //will sort ArrayList of Integers in descending order
			//Log.d("TAULA ORDENADA", "EN PRINCIPI");
			for(int i=0;i<keys_minuts.size();i++){
				//Log.d("VALOR ORDENAT: ",Integer.toString(keys_minuts.get(i)));
			}
		}

		private void imprimeixHash() {
			Map<Integer, Integer> map = eventData.temps_guardats;
			Log.d("IMPRIMEIXHASH","MINUTS");
			for (Entry<Integer, Integer> entry : map.entrySet()) {
				Integer key = entry.getKey();
			    Integer value = entry.getValue();
			    Log.d("MINUTS GUARDATS I VALUE", Integer.toString(key)+" "+Integer.toString(value));
			}
			Map<Integer, Double> map2 = eventData.windDirectionHistory;
			Log.d("IMPRIMEIXHASH","WIND-DIRECTION");
			for (Entry<Integer, Double> entry : map2.entrySet()) {
				Integer key = entry.getKey();
				Double value = entry.getValue();
			    Log.d("WIND-DIR GUARDATS I VALUE", Integer.toString(key)+" "+Double.toString(value));
			}
			
		}

		@SuppressWarnings("null")
		private Number[] CollectionToList(ConcurrentHashMap<Integer, Double> History) {
			Number[] prova = new Number[History.size()];
			int i = 0;
			Log.d("HI HAN X VALORS ", Integer.toString(History.size()));
			for (i=0;i<keys_minuts.size();i++){
				Double value = History.get(keys_minuts.get(i));
				Log.d("temperatura", Double.toString(value));
				prova[i] = value;
			}
			return prova;
			
		}
		
		@SuppressWarnings("null")
		private Number[] CollectionToListWind(ConcurrentHashMap<Integer, Double> History) {
			Number[] prova = new Number[History.size()];
			int i = 0;
			Log.d("HI HAN X VALORS ", Integer.toString(History.size()));
			for (Double value : History.values()){
				Log.d("wind", Double.toString(value));
				prova[i] = value/22.5;
				i++;
			}
			return prova;
			
		}

		private void convert() {
			
			//CONVERTEIX DOS PILOTS
			/*
			llista1=toIntArray(pos_history1);
			llista2=toIntArray(pos_history2);
			*/
		}
		
		private Integer[] toIntArray(List<Integer> list){
			  Integer[] ret = new Integer[list.size()];
			  for(int i = 0;i < ret.length;i++)
			    ret[i] = list.get(i);
			  return ret;
			}
		
		private String[] toStringArray(ArrayList<Integer> list){
			  String[] ret = new String[list.size()];
			  int i;
			  for(i = 0;i < ret.length;i++){
			    ret[i] = Integer.toString(list.get(i));
			  	Log.d("===ITERACIO TOSTRINGARRAY: ", ret[i]);
			  }
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
	            	
	            case R.id.WindDirection:
	            	windDirection();
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

	        llista1=CollectionToList(eventData.flagStatusHistory);
	        llista2=CollectionToList(eventData.wetDryHistory);

	        
	        // initialize our XYPlot reference:
	        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
	 
	        // Create a couple arrays of y-values to plot:
	        Number[] series1Numbers = llista1;
	        Number[] series2Numbers = llista2;
	 
	        // Turn the above arrays into XYSeries':
	        XYSeries series1 = new SimpleXYSeries(
	                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
	                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
	                "Flag Status");                             // Set the display title of the series
	 
	        // same as above
	        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Wet/Dry");
	 
	        // Create a formatter to use for drawing a series using LineAndPointRenderer:
	        LineAndPointFormatter series1Format = new LineAndPointFormatter(
	                Color.rgb(238, 0, 0),                   // line color
	                null,                   // point color
	                null);                                  // fill color (none)
	 
	        // add a new series' to the xyplot:
	        mySimpleXYPlot.addSeries(series1, series1Format);
	        mySimpleXYPlot.addSeries(series2,
	                new LineAndPointFormatter(Color.rgb(0, 178, 255), null, null));
	 
	        
	        
	        // reduce the number of range labels
	        mySimpleXYPlot.setTicksPerRangeLabel(2);
	        mySimpleXYPlot.setTitle("Flag Status / Wet-Dry");
	        
	        //Posem el fons a negre
	        mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
	        
	        // Formatting the Domain Values ( X-Axis )
	        final String[] domain = toStringArray(keys_minuts);
	        
	        mySimpleXYPlot.setDomainValueFormat(new Format() {
	        	 
	        	@Override
	            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
	                return new StringBuffer( domain[ ( (Number)obj).intValue() ]  );
	            }
	 
	            @Override
	            public Object parseObject(String source, ParsePosition pos) {
	                return null; 
	            }
	        });
	        
	        //Marge esquerra
	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(25);
	        mySimpleXYPlot.getGraphWidget().setDomainLabelWidth(20);
	        mySimpleXYPlot.getGraphWidget().setBorderPaint(null);
	        
	        mySimpleXYPlot.setRangeLabel("Flag (1)Green (2)Yellow (4)SC (5)Red");
	        mySimpleXYPlot.setDomainLabel("Minutes");
	        
	        mySimpleXYPlot.getLegendWidget().setMarginBottom((float) 2.5);
	        
	        mySimpleXYPlot.setRangeValueFormat(new DecimalFormat("#"));
	        mySimpleXYPlot.setRangeBoundaries(0, 5, BoundaryMode.FIXED);
	        
	        
	        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);

	        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, getAppropiateDomainStep());
	 
	        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
	        // To get rid of them call disableAllMarkup():
	        mySimpleXYPlot.disableAllMarkup();
			
		}

		private void windDirection() {
			setContentView(R.layout.graphic);

	        llista1=CollectionToList(eventData.windDirectionHistory);

	        
	        // initialize our XYPlot reference:
	        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
	 
	        // Create a couple arrays of y-values to plot:
	        Number[] series1Numbers = llista1;
	 
	        // Turn the above arrays into XYSeries':
	        XYSeries series1 = new SimpleXYSeries(
	                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
	                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
	                "Wind Direction");                             // Set the display title of the series
	 
 
	        // Create a formatter to use for drawing a series using LineAndPointRenderer:
	        LineAndPointFormatter series1Format = new LineAndPointFormatter(
	                Color.rgb(255, 215, 0),                   // line color
	                null,                   // point color
	                null);                                  // fill color (none)
	 
	        // add a new series' to the xyplot:
	        mySimpleXYPlot.addSeries(series1, series1Format);
	 
	 
	        // reduce the number of range labels
	        mySimpleXYPlot.setTicksPerRangeLabel(2);
	        mySimpleXYPlot.setTitle("Wind Direction");

	        //Posem el fons a negre
	        mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);	        
	        
	        // Formatting the Domain Values ( X-Axis )
	        final String[] domain = toStringArray(keys_minuts);
	        
	        mySimpleXYPlot.setDomainValueFormat(new Format() {
	        	 
	        	@Override
	            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
	                return new StringBuffer( domain[ ( (Number)obj).intValue() ]  );
	            }
	 
	            @Override
	            public Object parseObject(String source, ParsePosition pos) {
	                return null; 
	            }
	        });
	        
	        //mySimpleXYPlot.setDomainValueFormat(new DecimalFormat("#"));
	        mySimpleXYPlot.setRangeBoundaries(0, 360, BoundaryMode.FIXED);
	        	        
	        //Marge esquerra
	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(40);
	        mySimpleXYPlot.getGraphWidget().setDomainLabelWidth(20);
	        
	        mySimpleXYPlot.setRangeLabel("Direction (degrees)");
	        mySimpleXYPlot.setDomainLabel("Minutes");
	        
	        mySimpleXYPlot.getLegendWidget().setMarginBottom((float) 2.5);
	        

	        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 20);

	        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, getAppropiateDomainStep());
	 
	        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
	        // To get rid of them call disableAllMarkup():
	        mySimpleXYPlot.disableAllMarkup();
			
		}

		private void windSpeed() {
			setContentView(R.layout.graphic);

	        llista1=CollectionToList(eventData.windSpeedHistory);
	        
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
	                Color.rgb(0, 255, 0),                   // line color
	                null,                   // point color
	                null);                                  // fill color (none)
	 
	        // add a new series' to the xyplot:
	        mySimpleXYPlot.addSeries(series1, series1Format);
	        
	 
	        // reduce the number of range labels
	        mySimpleXYPlot.setTicksPerRangeLabel(2);
	        mySimpleXYPlot.setTitle("Wind Speed");

	        //Posem el fons a negre
	        mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
	        
	        //Marge esquerra
	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(30);
	        mySimpleXYPlot.getGraphWidget().setDomainLabelWidth(20);
	        
	        // Formatting the Domain Values ( X-Axis )
	        final String[] domain = toStringArray(keys_minuts);
	        
	        mySimpleXYPlot.setDomainValueFormat(new Format() {
	        	 
	        	@Override
	            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
	                return new StringBuffer( domain[ ( (Number)obj).intValue() ]  );
	            }
	 
	            @Override
	            public Object parseObject(String source, ParsePosition pos) {
	                return null; 
	            }
	        });
	        
	        mySimpleXYPlot.setRangeLabel("");
	        mySimpleXYPlot.setDomainLabel("Minutes");
	        
	        mySimpleXYPlot.getLegendWidget().setMarginBottom((float) 2.5);
	        
	        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 0.5);

	        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, getAppropiateDomainStep());
	 
	        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
	        // To get rid of them call disableAllMarkup():
	        mySimpleXYPlot.disableAllMarkup();
			
		}

		private void pressure() {
			setContentView(R.layout.graphic);

	        llista1=CollectionToList(eventData.pressureHistory);

	        // initialize our XYPlot reference:
	        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
	 
	        // Create a couple arrays of y-values to plot:
	        Number[] series1Numbers = llista1;
	 
	        // Turn the above arrays into XYSeries':
	        XYSeries series1 = new SimpleXYSeries(
	                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
	                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
	                "Pressure");                             // Set the display title of the series
	 
	 
	        // Create a formatter to use for drawing a series using LineAndPointRenderer:
	        LineAndPointFormatter series1Format = new LineAndPointFormatter(
	                Color.rgb(255, 165, 0),                   // line color
	                null,                   // point color
	                null);                                  // fill color (none)
	 
	        // add a new series' to the xyplot:
	        mySimpleXYPlot.addSeries(series1, series1Format);
	 
	 
	        // reduce the number of range labels
	        mySimpleXYPlot.setTicksPerRangeLabel(2);
	        mySimpleXYPlot.setTitle("Pressure");

	        //Posem el fons a negre
	        mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
	        
	        // Formatting the Domain Values ( X-Axis )
	        final String[] domain = toStringArray(keys_minuts);
	        
	        mySimpleXYPlot.setDomainValueFormat(new Format() {
	        	 
	        	@Override
	            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
	                return new StringBuffer( domain[ ( (Number)obj).intValue() ]  );
	            }
	 
	            @Override
	            public Object parseObject(String source, ParsePosition pos) {
	                return null; 
	            }
	        });
	        
	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(50);
	        mySimpleXYPlot.getGraphWidget().setDomainLabelWidth(20);
	        
	        mySimpleXYPlot.setRangeLabel("");
	        mySimpleXYPlot.setDomainLabel("Minutes");
	        
	        mySimpleXYPlot.getLegendWidget().setMarginBottom((float) 2.5);
	        
	        //PROVA
	        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 0.1);
	        //mySimpleXYPlot.set
	        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, getAppropiateDomainStep());
	        	 
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
	                Color.rgb(255, 255, 255),                   // line color
	                null,                   // point color
	                null);                                  // fill color (none)
	 
	        // add a new series' to the xyplot:
	        mySimpleXYPlot.addSeries(series1, series1Format);
	 
	 
	        // reduce the number of range labels
	        mySimpleXYPlot.setTicksPerRangeLabel(2);
	        mySimpleXYPlot.setTitle("Humidity");
	        
	        mySimpleXYPlot.setDomainValueFormat(new DecimalFormat("#"));
	        mySimpleXYPlot.setRangeValueFormat(new DecimalFormat("#"));
	        
	        //Posem el fons a negre
	        mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
	        
	        //Marge esquerra
	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(25);
	        mySimpleXYPlot.getGraphWidget().setDomainLabelWidth(20);
	        
	        final String[] domain = toStringArray(keys_minuts);
	        
	        mySimpleXYPlot.setDomainValueFormat(new Format() {
	        	 
	        	@Override
	            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
	                return new StringBuffer( domain[ ( (Number)obj).intValue() ]  );
	            }
	 
	            @Override
	            public Object parseObject(String source, ParsePosition pos) {
	                return null; 
	            }
	        });
	        
	        mySimpleXYPlot.setRangeLabel("");
	        mySimpleXYPlot.setDomainLabel("Minutes");
	        
	        mySimpleXYPlot.getLegendWidget().setMarginBottom((float) 2.5);
	        
	        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
	        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, getAppropiateDomainStep());
	        	        
	        mySimpleXYPlot.setRangeTopMax(100);
	        mySimpleXYPlot.setRangeTopMin(0);
	 
	        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
	        // To get rid of them call disableAllMarkup():
	        mySimpleXYPlot.disableAllMarkup();
			
		}

		private void airTrackTEMP() {
			
			setContentView(R.layout.graphic);
	        llista1=CollectionToList(eventData.trackTempHistory);
	        llista2=CollectionToList(eventData.airTempHistory);
	        
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
	                Color.rgb(255, 255, 0),                   // line color
	                null,                   // point color
	                null);                                  // fill color (none)
	 
	        // add a new series' to the xyplot:
	        mySimpleXYPlot.addSeries(series1, series1Format);
	 
	        // same as above:
	        mySimpleXYPlot.addSeries(series2,
	                new LineAndPointFormatter(Color.rgb(255, 0, 255), null, null));
	 
	        // reduce the number of range labels
	        mySimpleXYPlot.setTicksPerRangeLabel(2);
	        mySimpleXYPlot.setTitle("Temperature");
	        
	        //Posem el fons a negre
	        mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
	        
	        
	        // Formatting the Domain Values ( X-Axis )
	        final String[] domain = toStringArray(keys_minuts);
	        
	        mySimpleXYPlot.setDomainValueFormat(new Format() {
	        	 
	        	@Override
	            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
	                return new StringBuffer( domain[ ( (Number)obj).intValue() ]  );
	            }
	 
	            @Override
	            public Object parseObject(String source, ParsePosition pos) {
	                return null; 
	            }
	        });  
	        
	        //Marge Esquerra i Inferior
	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(25);
	        mySimpleXYPlot.getGraphWidget().setDomainLabelWidth(20);
	        
	        mySimpleXYPlot.setRangeLabel("");
	        mySimpleXYPlot.setDomainLabel("Minutes");
	        
	        mySimpleXYPlot.setRangeValueFormat(new DecimalFormat("#"));
	        
	        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 5);
	        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, getAppropiateDomainStep());
        
	        // adjust the padding of the legend widget to look a little nicer:
	        mySimpleXYPlot.getLegendWidget().setPadding(10, 5, 5, 5);
	        mySimpleXYPlot.getLegendWidget().setMarginBottom((float) 2.5);
	        
	        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
	        // To get rid of them call disableAllMarkup():
	        mySimpleXYPlot.disableAllMarkup();
			
		}

		
		public int getAppropiateDomainStep()
		{
			int lapsToDisplay = eventData.temps_guardats.size();
			int result = lapsToDisplay/8;
			if(result<1){
				result = 1;
			}
			return result;
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
