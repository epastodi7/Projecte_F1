package com.f1lt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class LTViewFragment extends Fragment implements OnClickListener
{
	private List<Bitmap> carBitmaps = new ArrayList();
	protected List<Bitmap> statusBitmaps = new ArrayList();
	private List<Bitmap> arrowBitmaps = new ArrayList();
	protected List<Bitmap> backgroundBitmaps = new ArrayList();
 	
	private boolean drawCarThumbnails = true;
	protected boolean printShortNames = false;
	protected int rowHeight = 30;
	protected int rowWidth;
	
	protected TableRow currentRow;
	protected int currentCar;
	
	private int eventType = LTData.EventType.PRACTICE_EVENT;
	
	private int showDiff = 0;	//1 - time (best, q1), 2 - q2, 3 - q3, 4 - interval
	
	public LTViewFragment()	
	{		
		super();
	}
	
	public LTViewFragment(Intent intent)
	{
		super();
				
		drawCarThumbnails = intent.getBooleanExtra("DrawCarThumbnails", false);
		printShortNames = intent.getBooleanExtra("PrintShortNames", false);	
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) 
	{		       
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.lt_view, container, false);
						
		TableLayout tv = (TableLayout)v.findViewById(R.id.ltTable);	
		
		if (tv != null)
		{
			int rows = tv.getChildCount();
			if (rows == 0)
			{						
				int ltRows = LTData.ltTeams.size()*2+1;
				for (int i = 0; i < ltRows; ++i)
				{
					tv.addView(new TableRow(getActivity()));//, rowLp);
					View row = tv.getChildAt(i);
					row.setOnClickListener(this);					
				}
			}
		}
		setupGraphics();						
		return v;
    }
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
								
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		
		rowWidth = metrics.widthPixels;
							
		resetView();		
	}
	
	public void resetView()
	{
//		setupGraphics();
		setupTable();
		resetColumns(false);		
		refreshView();
	}
	
	public void setupGraphics()
	{
		rowHeight = (int)((new TextView(getActivity())).getTextSize()*1.5);//(int)(20.0 * metrics.scaledDensity);
		
				
		if (rowHeight > 30)
			rowHeight = 30;
		 
		if (carBitmaps.isEmpty())
		{
			for (int i = 0; i < LTData.ltTeams.size(); ++i)
			{		
				int carBitmapId = -1;
				switch (LTData.ltTeams.get(i).id)
				{
					case 0: carBitmapId = R.drawable.car_0_small; break;
					case 1: carBitmapId = R.drawable.car_1_small; break;
					case 2: carBitmapId = R.drawable.car_2_small; break;
					case 3: carBitmapId = R.drawable.car_3_small; break;
					case 4: carBitmapId = R.drawable.car_4_small; break;
					case 5: carBitmapId = R.drawable.car_5_small; break;
					case 6: carBitmapId = R.drawable.car_6_small; break;
					case 7: carBitmapId = R.drawable.car_7_small; break;
					case 8: carBitmapId = R.drawable.car_8_small; break;
					case 9: carBitmapId = R.drawable.car_9_small; break;
					case 10: carBitmapId = R.drawable.car_10_small; break;
					case 11: carBitmapId = R.drawable.car_11_small; break;
				}
				Options options = new BitmapFactory.Options();
			    options.inScaled = false;
			    
				Bitmap b = BitmapFactory.decodeResource(getActivity().getResources(), carBitmapId);//, options);
	//			carBitmaps.add(scaledBitmap(carBitmapId, (b.getWidth()*(int)(rowHeight*0.75))/b.getHeight(), (int)(rowHeight*0.75))   );//Bitmap.createScaledBitmap(b, (b.getWidth()*(int)(rowHeight*0.75))/b.getHeight(), (int)(rowHeight*0.75), true));
				carBitmaps.add(b);//Bitmap.createScaledBitmap(b, (b.getWidth()*(int)(rowHeight*0.75))/b.getHeight(), (int)(rowHeight*0.75), true));
			}		
		}
		Bitmap b = null;
		
		if (statusBitmaps.isEmpty())
		{
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.green_light);
	//		statusBitmaps.add(scaledBitmap(R.drawable.green_light, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2), true));	
			statusBitmaps.add(Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(rowHeight/1.5))/b.getHeight(), (int)(rowHeight/1.5), true));
			
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.yellow_light);
	//		statusBitmaps.add(scaledBitmap(R.drawable.yellow_light, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2), true));	
			statusBitmaps.add(Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(rowHeight/1.5))/b.getHeight(), (int)(rowHeight/1.5), true));
			
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.red_light);
	//		statusBitmaps.add(scaledBitmap(R.drawable.red_light, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2), true));
			statusBitmaps.add(Bitmap.createScaledBitmap(b,(int) (b.getWidth()*(rowHeight/1.5))/b.getHeight(), (int)(rowHeight/1.5), true));
						
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.sc);
	//		statusBitmaps.add(scaledBitmap(R.drawable.finish_flag, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2), true));
			statusBitmaps.add(Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(rowHeight/1.5))/b.getHeight(), (int)(rowHeight/1.5), true));
			
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.finish_flag);
	//		statusBitmaps.add(scaledBitmap(R.drawable.finish_flag, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2), true));
			statusBitmaps.add(Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(rowHeight/1.5))/b.getHeight(), (int)(rowHeight/1.5), true));
			
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.weather_dry);
	//		statusBitmaps.add(scaledBitmap(R.drawable.finish_flag, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2), true));
			statusBitmaps.add(Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(rowHeight/1.2))/b.getHeight(), (int)(rowHeight/1.2), true));
							
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.weather_wet);
	//		statusBitmaps.add(scaledBitmap(R.drawable.finish_flag, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2), true));
			statusBitmaps.add(Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(rowHeight/1.2))/b.getHeight(), (int)(rowHeight/1.2), true));
			
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.weather_pressure);
	//		statusBitmaps.add(scaledBitmap(R.drawable.finish_flag, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2), true));
			statusBitmaps.add(Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(rowHeight/1.5))/b.getHeight(), (int)(rowHeight/1.5), true));
			
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.weather_humidity);
	//		statusBitmaps.add(scaledBitmap(R.drawable.finish_flag, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2), true));
			statusBitmaps.add(Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(rowHeight/1.5))/b.getHeight(), (int)(rowHeight/1.5), true));
			
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.weather_wind);
	//		statusBitmaps.add(scaledBitmap(R.drawable.finish_flag, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2), true));
			statusBitmaps.add(Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(rowHeight/1.5))/b.getHeight(), (int)(rowHeight/1.5), true));
			
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.weather_air_temp);
	//		statusBitmaps.add(scaledBitmap(R.drawable.finish_flag, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2), true));
			statusBitmaps.add(Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(rowHeight/1.5))/b.getHeight(), (int)(rowHeight/1.5), true));
			
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.weather_track_temp);
	//		statusBitmaps.add(scaledBitmap(R.drawable.finish_flag, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/2))/b.getHeight(), (rowHeight/2), true));
			statusBitmaps.add(Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(rowHeight/1.5))/b.getHeight(), (int)(rowHeight/1.5), true));
		}
		
		if (arrowBitmaps.isEmpty())
		{
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.down_arrow);
	//		arrowBitmaps.add(scaledBitmap(R.drawable.down_arrow, (b.getWidth()*(rowHeight/3))/b.getHeight(), (rowHeight/3)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/3))/b.getHeight(), (rowHeight/3), true));
			arrowBitmaps.add(Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/3))/b.getHeight(), (rowHeight/3), true));
			
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.up_arrow);
	//		arrowBitmaps.add(scaledBitmap(R.drawable.up_arrow, (b.getWidth()*(rowHeight/3))/b.getHeight(), (rowHeight/3)));//Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/3))/b.getHeight(), (rowHeight/3), true));
			arrowBitmaps.add(Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight/3))/b.getHeight(), (rowHeight/3), true));
		}
		
		if (backgroundBitmaps.isEmpty())
		{
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.rowbg);
			backgroundBitmaps.add(Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight))/b.getHeight(), rowHeight, true));
			
			b = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.rowbgh);
			backgroundBitmaps.add(Bitmap.createScaledBitmap(b, (b.getWidth()*(rowHeight))/b.getHeight(), rowHeight, true));
		}
				
	}
	
	public void setupTable()
	{		
		TableLayout tv = (TableLayout)getView().findViewById(R.id.ltTable);	
		for (int i = 0; i < tv.getChildCount(); ++i)
		{
			TableRow trow = (TableRow)tv.getChildAt(i);
			trow.setBackgroundDrawable(new BitmapDrawable(getResources(), backgroundBitmaps.get(0)));
		}
		
//		tv = (TableLayout)getActivity().findViewById(R.id.ltTable);	
//		
//		if (tv != null)
//		{
//			int rows = tv.getChildCount();
//			if (rows == 0)
//			{						
//				int ltRows = LTData.ltTeams.size()*2+1;
//				for (int i = 0; i < ltRows; ++i)
//				{
//					tv.addView(new TableRow(getActivity()));//, rowLp);
//					View row = tv.getChildAt(i);
//					row.setOnClickListener(this);					
//				}
//			}
//		}
	}
	
	public void setPreferences(Intent intent)
	{
		drawCarThumbnails = intent.getBooleanExtra("DrawCarThumbnails", false);
		printShortNames = intent.getBooleanExtra("PrintShortNames", false);
		
		
		resetColumns(false);
		refreshView();
	}
	
	public Bitmap scaledBitmap(int res, float width, float height)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getActivity().getResources(), res, options);
		int srcWidth = options.outWidth;
		int srcHeight = options.outHeight;
				
		int inSampleSize = 1;
		while(srcWidth / 2 > width)
		{
			srcWidth /= 2;
			srcHeight /= 2;
			inSampleSize *= 2;
		}
		float desiredScale = (float) width / srcWidth;
		 
		// Decode with inSampleSize
		options.inJustDecodeBounds = false;
		options.inDither = false;
		options.inSampleSize = inSampleSize;
		options.inScaled = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap sampledSrcBitmap = BitmapFactory.decodeResource(getActivity().getResources(), res, options);
		 
		// Resize
		Matrix matrix = new Matrix();
		matrix.postScale(desiredScale, desiredScale);
		Bitmap scaledBitmap = Bitmap.createBitmap(sampledSrcBitmap, 0, 0, sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(), matrix, true);		
		 
		return scaledBitmap;		 
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
	
	private OnClickListener itemClickListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			TableRow tr = (TableRow)v.getParent();
			int clickedCar = Integer.parseInt(((TextView)tr.getChildAt(12)).getText().toString());
			
			if (v == tr.getChildAt(5) && EventData.getInstance().eventType != LTData.EventType.RACE_EVENT)
			{
				if (showDiff == 1 && clickedCar == currentCar)
					showDiff = 0;
				else
					showDiff = 1;
			}
			
			if (v == tr.getChildAt(6) && EventData.getInstance().eventType == LTData.EventType.RACE_EVENT)
			{
				if (showDiff == 4 && clickedCar == currentCar)
					showDiff = 0;
				else
					showDiff = 4;
			}
			
			if (v == tr.getChildAt(6) && EventData.getInstance().eventType == LTData.EventType.QUALI_EVENT)
			{
				if (showDiff == 2 && clickedCar == currentCar)
					showDiff = 0;
				else
					showDiff = 2;
			}
			
			if (v == tr.getChildAt(7) && EventData.getInstance().eventType == LTData.EventType.QUALI_EVENT)
			{
				if (showDiff == 3 && clickedCar == currentCar)
					showDiff = 0;
				else
					showDiff = 3;
			}				
			
			if (v == tr.getChildAt(7) && EventData.getInstance().eventType == LTData.EventType.RACE_EVENT)
			{
				if (showDiff == 1 && clickedCar == currentCar)
					showDiff = 0;
				else
					showDiff = 1;
			}
						
			if (showDiff == 0)
			{				
				currentRow.setBackgroundDrawable(new BitmapDrawable(getResources(), backgroundBitmaps.get(0)));
				currentRow = null;					
			}
			
			else if (currentRow != tr)
			{
				if (currentRow != null)
					currentRow.setBackgroundDrawable(new BitmapDrawable(getResources(), backgroundBitmaps.get(0)));
				
				tr.setBackgroundDrawable(new BitmapDrawable(getResources(), backgroundBitmaps.get(1)));
				currentRow = tr;
			}
						
			
			currentCar = clickedCar;
			refreshView();
		}
	};
	
	public void onClick(View v)
	{		
		TableLayout tableLayout = (TableLayout)getActivity().findViewById(R.id.ltTable);
		if (tableLayout != null)
		{
//			Log.d("ltview", "onclick");
			
			if (v != tableLayout.getChildAt(tableLayout.getChildCount()-1) &&
				((TableRow)v).getChildAt(12) != null)
			{
//				if (currentRow != null)
//					currentRow.setBackgroundDrawable(new BitmapDrawable(getResources(), backgroundBitmaps.get(0)));					
//												
//				v.setBackgroundDrawable(new BitmapDrawable(getResources(), backgroundBitmaps.get(1)));
//				currentRow = (TableRow)v;
//				
				int carID = Integer.parseInt(((TextView)(((TableRow)v).getChildAt(12))).getText().toString());
	
				if (carID > 0 && DataStreamReader.getInstance().getSecondaryDataStreamReceiver() == null)
				{
					Intent intent = new Intent(getActivity(), DriverDataActivity.class);
					intent.putExtra("CarID", carID);
			    	startActivity(intent);
				}
			}
//				TableRow row = (TableRow)tableLayout.getChildAt(i);		
//				if (v.equals(row))
//				{
//					Log.d("ltview", "onclick i="+i);
//					row.setBackgroundResource(R.drawable.back_hg);
//					return;
//				}
//			}
		}
	}
	
	public void setHeaders()
	{
		TableRow headerRow = (TableRow)getActivity().findViewById(R.id.headerRow);				
						
		if (headerRow != null)
		{
			
			switch (EventData.getInstance().eventType)
			{
				case LTData.EventType.RACE_EVENT:
					setHeaderItem(headerRow, 0, "P", Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 1, "", Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 2, "", Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 3, "", Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 4, "Name", Gravity.LEFT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 5, "Gap", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 6, "Int.", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 7, "Time", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 8, "S1", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 9, "S2", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 10, "S3", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 11, "Pit", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 12, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					break;
					
				case LTData.EventType.QUALI_EVENT:
					setHeaderItem(headerRow, 0, "P", Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 1, "", Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 2, "", Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 3, "", Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 4, "Name", Gravity.LEFT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 5, "Q1", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 6, "Q2", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 7, "Q3", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 8, "S1", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 9, "S2", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 10, "S3", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 11, "L", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 12, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					break;
					
				case LTData.EventType.PRACTICE_EVENT:
					setHeaderItem(headerRow, 0, "P", Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 1, "", Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 2, "", Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 3, "", Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 4, "Name", Gravity.LEFT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 5, "Best", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 6, "Gap", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);					
					setHeaderItem(headerRow, 7, "S1", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 8, "S2", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 9, "S3", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 10, "L", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 11, "", Gravity.RIGHT, LTData.color[LTData.Colors.DEFAULT]);
					setHeaderItem(headerRow, 12, "", Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
			}					
		}
	}
	
	public void refreshView()
	{	
		if (getActivity() == null)
			return;				
		
		updateStatus();
		setHeaders();		
		
		if (eventType != EventData.getInstance().eventType)
		{
			resetColumns(true);
			eventType = EventData.getInstance().eventType;
		}
		
		switch (EventData.getInstance().eventType)
		{
			case LTData.EventType.RACE_EVENT:
				updateRace(); break;
			
			case LTData.EventType.QUALI_EVENT:
				updateQuali(); break;
				
			case LTData.EventType.PRACTICE_EVENT:
				updatePractice(); break;
		}				
	}	
	
	public void updateStatus()
	{	
		TableRow statusRow = (TableRow)getActivity().findViewById(R.id.statusRow);
		
		if (statusRow != null)
		{
			
//			setStatusRowItem(statusRow, 0, "", Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
			int id = 4;
			switch (EventData.getInstance().flagStatus)
			{
				case LTData.FlagStatus.GREEN_FLAG: id = 0; break;
				case LTData.FlagStatus.YELLOW_FLAG: id = 1; break;
				case LTData.FlagStatus.RED_FLAG: id = 2; break;
				case LTData.FlagStatus.SAFETY_CAR_DEPLOYED: id = 3; break;
				default: id=-1; break;
			}
			if (EventData.getInstance().remainingTime.equals("0:00:00") || 
				(EventData.getInstance().eventType == LTData.EventType.RACE_EVENT && 
				EventData.getInstance().lapsCompleted == EventData.getInstance().eventInfo.laps))
				id = 4;
			
			setStatusIcon(statusRow, id, 0);
			
			if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT)
			{
				int lapsCompleted = EventData.getInstance().lapsCompleted + 1; 
				
				if (lapsCompleted > EventData.getInstance().eventInfo.laps)
					lapsCompleted = EventData.getInstance().eventInfo.laps;
				
				if (!EventData.getInstance().sessionStarted) 					
					lapsCompleted = 0;
				
				setStatusRowItem(statusRow, 1, "" + lapsCompleted + "/"+EventData.getInstance().eventInfo.laps, Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
				setStatusRowItem(statusRow, 2, EventData.getInstance().remainingTime, Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);									
			}
			else
			{
				setStatusRowItem(statusRow, 1, EventData.getInstance().remainingTime, Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
				
				if (EventData.getInstance().eventType == LTData.EventType.QUALI_EVENT)
				{
					String q = "Q" + (EventData.getInstance().qualiPeriod > 0 ? EventData.getInstance().qualiPeriod : "");
					
					setStatusRowItem(statusRow, 2, q, Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
				}
				else
				{
					String fp = "FP";
					
					if (EventData.getInstance().sessionId > 0)
					{
						int practiceNo = (EventData.getInstance().sessionId - EventData.getInstance().firstSessionId) % 6;
						fp += "" + practiceNo;
					}
					setStatusRowItem(statusRow, 2, fp, Gravity.CENTER, LTData.color[LTData.Colors.WHITE]);
				}
			}						
			
			setStatusIcon(statusRow, 10, 3);
			setStatusRowItem(statusRow, 4, "" + EventData.getInstance().airTemp + "°C", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE/*CYAN*/]);
			
			setStatusIcon(statusRow, 11, 5);
			setStatusRowItem(statusRow, 6, "" + EventData.getInstance().trackTemp + "°C", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
			
			setStatusIcon(statusRow, 7, 7);
			setStatusRowItem(statusRow, 8, "" + EventData.getInstance().pressure + "mb", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE/*VIOLET*/]);
			
			setWindStatusIcon(statusRow, 9, 9);
			setStatusRowItem(statusRow, 10, "" + EventData.getInstance().windSpeed + "m/s", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE/*YELLOW*/]);
			
			setStatusIcon(statusRow, 8, 11);
			setStatusRowItem(statusRow, 12, "" + EventData.getInstance().humidity + "%", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
			
			if (EventData.getInstance().wetdry == 1)
				setStatusIcon(statusRow, 6, 13);
			else
				setStatusIcon(statusRow, 5, 13);
						
//			setStatusRowItem(statusRow, 8, EventData.getInstance().wetdry == 1 ? "Wet" : "Dry", Gravity.CENTER, 
//				LTData.color[EventData.getInstance().wetdry == 1 ? LTData.Colors.CYAN : LTData.Colors.YELLOW]);
		}
	}
				
	public void updateRace()
	{		
		TableLayout tl = (TableLayout)getView().findViewById(R.id.ltTable);		
//		if (tl != null)
		{
			List<DriverData> driversData = new ArrayList<DriverData>(EventData.getInstance().driversData);
			Collections.sort(driversData);
							
			int rowToChange = -1;
			for (int i = 0; i < driversData.size(); ++i)
			{									
				TableRow tr = (TableRow)tl.getChildAt(i);
				DriverData dd = driversData.get(i);			
																
				if (tr != null)
				{
					setItem(tr, 0, "" + ((dd.retired || dd.pos < 1) ? "" : dd.pos), Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[dd.colorData[LTData.RacePacket.RACE_POSITION]]);
					
					int lastPos = dd.lastLap.pos, prevPos = 0;
										
					if (dd.lapData.size() > 1)					
						prevPos = dd.lapData.get(dd.lapData.size()-2).pos;
					
					else if (!dd.posHistory.isEmpty())					
						prevPos = dd.posHistory.get((dd.lastLap.numLap == 1) ? 0 : dd.posHistory.size()-1);
					
					Bitmap b = null;
					if (lastPos != prevPos && !dd.retired)
						b = (lastPos < prevPos) ? arrowBitmaps.get(1) : arrowBitmaps.get(0);											
						
					setImageItem(tr, 1, b);
					
					setItem(tr, 2, "" + ((dd.number < 1) ? "" : dd.number), Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[dd.colorData[LTData.RacePacket.RACE_NUMBER]]);
					
//					if (drawCarThumbnails)					
					int id = LTData.getTeamId(dd.number);
					b = (id == -1) ? null : carBitmaps.get(id);
					setImageItem(tr, 3, b);
					
					String name = printShortNames ? LTData.getShortDriverName(dd.driver) : LTData.getDriverName(dd.driver);
					
					String interval = dd.lastLap.interval;
					String lapTime = dd.lastLap.lapTime.toString();
					
					if (showDiff == 1 && dd.carID != currentCar && currentCar > 0 && 
							dd.lastLap.lapTime.isValid() && EventData.getInstance().driversData.get(currentCar-1).lastLap.lapTime.isValid())
					{
						lapTime = DriverData.calculateGap(dd.lastLap.lapTime, 
								EventData.getInstance().driversData.get(currentCar-1).lastLap.lapTime);
						
						if (!lapTime.equals("") && lapTime.getBytes()[0] != '-')
							lapTime = "+"+lapTime;
					}
					if (showDiff == 4 && dd.carID != currentCar && currentCar > 0 )
					{
						interval = EventData.getInstance().calculateInterval(dd, EventData.getInstance().driversData.get(currentCar-1), -1);//EventData.getInstance().lapsCompleted);//.driversData.get(currentCar-1).lastLap.numLap);																				
					}
					if (showDiff == 4 && (dd.carID == currentCar) || dd.retired)
						interval = "";
					
					setItem(tr, 4, name, Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[dd.colorData[LTData.RacePacket.RACE_DRIVER]]);
					setItem(tr, 5, dd.lastLap.gap, Gravity.CENTER, LTData.color[dd.colorData[LTData.RacePacket.RACE_GAP]]);
					setItem(tr, 6, interval, Gravity.CENTER, LTData.color[dd.colorData[LTData.RacePacket.RACE_INTERVAL]]);
					setItem(tr, 7, lapTime, Gravity.CENTER, LTData.color[dd.colorData[LTData.RacePacket.RACE_LAP_TIME]]);
					setItem(tr, 8, dd.lastLap.sector1.toString(), Gravity.CENTER, LTData.color[dd.colorData[LTData.RacePacket.RACE_SECTOR_1]]);
					setItem(tr, 9, dd.lastLap.sector2.toString(), Gravity.CENTER, LTData.color[dd.colorData[LTData.RacePacket.RACE_SECTOR_2]]);
					setItem(tr, 10, dd.lastLap.sector3.toString(), Gravity.CENTER, LTData.color[dd.colorData[LTData.RacePacket.RACE_SECTOR_3]]);
					setItem(tr, 11, "" + (dd.numPits > 0 ? dd.numPits : ""), Gravity.CENTER, LTData.color[dd.colorData[LTData.RacePacket.RACE_NUM_PITS]]);
					setItem(tr, 12, "" + dd.carID, Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					
					if (currentRow != null)
					{
						if (currentRow == tr && currentCar != dd.carID)
						{
							currentRow.setBackgroundDrawable(new BitmapDrawable(getResources(), backgroundBitmaps.get(0)));							
						}
						else
						if (currentRow != tr && currentCar == dd.carID)
						{
							rowToChange = i;							
						}
					}
				}							
			}
			if (rowToChange > -1)
			{
				currentRow = (TableRow)tl.getChildAt(rowToChange);
				currentRow.setBackgroundDrawable(new BitmapDrawable(getResources(), backgroundBitmaps.get(1)));
			}
			TableRow lastRow = (TableRow)tl.getChildAt(driversData.size());
			if (lastRow != null)
			{
				setItem(lastRow, 0, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
//				setItem(lastRow, 1, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
				setImageItem(lastRow, 1, null);
				setItem(lastRow, 2, "FL", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
//				setItem(lastRow, 3, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
				setImageItem(lastRow, 3, null);
				
				String name = printShortNames ? LTData.getShortDriverName(EventData.getInstance().FLDriver) : LTData.getDriverName(EventData.getInstance().FLDriver);
				setItem(lastRow, 4, name, Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.VIOLET]);
				setItem(lastRow, 5, "" + (EventData.getInstance().FLLap > 0 ? ("L" + EventData.getInstance().FLLap) : ""), Gravity.CENTER, LTData.color[LTData.Colors.VIOLET]);
				setItem(lastRow, 6, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
				setItem(lastRow, 7, "" + EventData.getInstance().FLTime.toString(), Gravity.CENTER, LTData.color[LTData.Colors.VIOLET]);
				setItem(lastRow, 8, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
				setItem(lastRow, 9, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
				setItem(lastRow, 10, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
				setItem(lastRow, 11, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
				setItem(lastRow, 12, "", Gravity.LEFT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.WHITE]);
			}
		}
	}
	
	public void updateQuali()
	{		
		TableLayout tl = (TableLayout)getActivity().findViewById(R.id.ltTable);
		if (tl != null)
		{
			List<DriverData> driversData = new ArrayList<DriverData>(EventData.getInstance().driversData);
			Collections.sort(driversData);
					
			int rowToChange = -1;
			for (int i = 0; i < EventData.getInstance().driversData.size(); ++i)
			{						
				TableRow tr = (TableRow)tl.getChildAt(i);
				DriverData dd = driversData.get(i);//EventData.getInstance().getDriverData(i+1);			
				
				if (tr != null && dd != null)
				{
					setItem(tr, 0, "" + ((dd.pos < 1) ? "" : dd.pos), Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[dd.colorData[LTData.QualifyingPacket.QUALI_POSITION]]);
//					setItem(tr, 1, "", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setImageItem(tr, 1, null);
					setItem(tr, 2, "" + ((dd.number < 1) ? "" : dd.number), Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[dd.colorData[LTData.QualifyingPacket.QUALI_NUMBER]]);
					
					int id = LTData.getTeamId(dd.number);
					Bitmap b = (id == -1) ? null : carBitmaps.get(id);
					setImageItem(tr, 3, b);
					
					setItem(tr, 4, printShortNames ? LTData.getShortDriverName(dd.driver) : LTData.getDriverName(dd.driver), 
							Gravity.LEFT |Gravity.CENTER_VERTICAL, LTData.color[dd.colorData[LTData.QualifyingPacket.QUALI_DRIVER]]);
					
					String q1 = dd.q1.toString();					
					if (showDiff == 1 && dd.carID != currentCar && currentCar > 0 && 
							dd.q1.isValid() && EventData.getInstance().driversData.get(currentCar-1).q1.isValid())
					{
						q1 = DriverData.calculateGap(dd.q1, 
								EventData.getInstance().driversData.get(currentCar-1).q1);
						
						if (!q1.equals("") && q1.getBytes()[0] != '-')
							q1 = "+"+q1;
					}
					String q2 = dd.q2.toString();					
					if (showDiff == 2 && dd.carID != currentCar && currentCar > 0 && 
							dd.q2.isValid() && EventData.getInstance().driversData.get(currentCar-1).q2.isValid())
					{
						q2 = DriverData.calculateGap(dd.q2, 
								EventData.getInstance().driversData.get(currentCar-1).q2);
						
						if (!q2.equals("") && q2.getBytes()[0] != '-')
							q2 = "+"+q2;
					}
					
					String q3 = dd.q3.toString();					
					if (showDiff == 3 && dd.carID != currentCar && currentCar > 0 && 
							dd.q3.isValid() && EventData.getInstance().driversData.get(currentCar-1).q3.isValid())
					{
						q3 = DriverData.calculateGap(dd.q3, EventData.getInstance().driversData.get(currentCar-1).q3);
						
						if (!q3.equals("") && q3.getBytes()[0] != '-')
							q3 = "+"+q3;
					}
					
					setItem(tr, 5, q1, Gravity.CENTER, LTData.color[dd.colorData[LTData.QualifyingPacket.QUALI_PERIOD_1]]);
					setItem(tr, 6, q2, Gravity.CENTER, LTData.color[dd.colorData[LTData.QualifyingPacket.QUALI_PERIOD_2]]);
					setItem(tr, 7, q3, Gravity.CENTER, LTData.color[dd.colorData[LTData.QualifyingPacket.QUALI_PERIOD_3]]);
					setItem(tr, 8, dd.lastLap.sector1.toString(), Gravity.CENTER, LTData.color[dd.colorData[LTData.QualifyingPacket.QUALI_SECTOR_1]]);
					setItem(tr, 9, dd.lastLap.sector2.toString(), Gravity.CENTER, LTData.color[dd.colorData[LTData.QualifyingPacket.QUALI_SECTOR_2]]);
					setItem(tr, 10, dd.lastLap.sector3.toString(), Gravity.CENTER, LTData.color[dd.colorData[LTData.QualifyingPacket.QUALI_SECTOR_3]]);
					setItem(tr, 11, "" + dd.lastLap.numLap, Gravity.CENTER, LTData.color[dd.colorData[LTData.QualifyingPacket.QUALI_LAP]]);
					setItem(tr, 12, "" + dd.carID, Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					
					if (currentRow != null)
					{
						if (currentRow == tr && currentCar != dd.carID)
						{
							currentRow.setBackgroundDrawable(new BitmapDrawable(getResources(), backgroundBitmaps.get(0)));							
						}
						else
						if (currentRow != tr && currentCar == dd.carID)
						{
							rowToChange = i;							
						}
					}
				}
			}
			if (rowToChange > -1)
			{
				currentRow = (TableRow)tl.getChildAt(rowToChange);
				currentRow.setBackgroundDrawable(new BitmapDrawable(getResources(), backgroundBitmaps.get(1)));
			}
			TableRow lastRow = (TableRow)tl.getChildAt(EventData.getInstance().driversData.size());
			if (lastRow != null)
			{
				setItem(lastRow, 0, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
//				setItem(lastRow, 1, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setImageItem(lastRow, 1, null);
				setItem(lastRow, 2, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
//				setItem(lastRow, 3, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setImageItem(lastRow, 3, null);
				setItem(lastRow, 4, printShortNames ? "107%" : "Q1 107%", Gravity.LEFT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 5, EventData.getInstance().session107Percent.toString(), Gravity.CENTER, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 6, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 7, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 8, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 9, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 10, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 11, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
			}
		}
	}
	
	public void updatePractice()
	{		
		TableLayout tl = (TableLayout)getActivity().findViewById(R.id.ltTable);
		if (tl != null)
		{
			List<DriverData> driversData = new ArrayList<DriverData>(EventData.getInstance().driversData);
			Collections.sort(driversData);
					
			int rowToChange = -1;
			for (int i = 0; i < driversData.size(); ++i)
			{									
				TableRow tr = (TableRow)tl.getChildAt(i);
				DriverData dd = driversData.get(i);			
				
				if (tr != null)
				{
					setItem(tr, 0, "" + ((dd.pos < 1) ? "" : dd.pos), Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[dd.colorData[LTData.PracticePacket.PRACTICE_POSITION]]);
					setImageItem(tr, 1, null);
//					setItem(tr, 1, "", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setItem(tr, 2, "" + ((dd.number < 1) ? "" : dd.number), Gravity.RIGHT |Gravity.CENTER_VERTICAL, LTData.color[dd.colorData[LTData.PracticePacket.PRACTICE_NUMBER]]);
					
					int id = LTData.getTeamId(dd.number);
					Bitmap b = (id == -1) ? null : carBitmaps.get(id);
					setImageItem(tr, 3, b);
					
					setItem(tr, 4, printShortNames ? LTData.getShortDriverName(dd.driver) : LTData.getDriverName(dd.driver), 
							Gravity.LEFT |Gravity.CENTER_VERTICAL, LTData.color[dd.colorData[LTData.PracticePacket.PRACTICE_DRIVER]]);
					
					String lapTime = dd.lastLap.lapTime.toString();
					if (showDiff == 1 && dd.carID != currentCar && currentCar > 0 && 
							dd.lastLap.lapTime.isValid() && EventData.getInstance().driversData.get(currentCar-1).lastLap.lapTime.isValid())
					{
						lapTime = DriverData.calculateGap(dd.lastLap.lapTime, 
								EventData.getInstance().driversData.get(currentCar-1).lastLap.lapTime);
						
						if (!lapTime.equals("") && lapTime.getBytes()[0] != '-')
							lapTime = "+"+lapTime;
					}
					
					setItem(tr, 5, lapTime, Gravity.CENTER, LTData.color[dd.colorData[LTData.PracticePacket.PRACTICE_BEST]]);
					setItem(tr, 6, dd.pos == 1 ? "" : dd.lastLap.gap, Gravity.CENTER, LTData.color[dd.colorData[LTData.PracticePacket.PRACTICE_GAP]]);
					setItem(tr, 7, dd.lastLap.sector1.toString(), Gravity.CENTER, LTData.color[dd.colorData[LTData.PracticePacket.PRACTICE_SECTOR_1]]);
					setItem(tr, 8, dd.lastLap.sector2.toString(), Gravity.CENTER, LTData.color[dd.colorData[LTData.PracticePacket.PRACTICE_SECTOR_2]]);
					setItem(tr, 9, dd.lastLap.sector3.toString(), Gravity.CENTER, LTData.color[dd.colorData[LTData.PracticePacket.PRACTICE_SECTOR_3]]);
					setItem(tr, 10, "" + dd.lastLap.numLap, Gravity.CENTER, LTData.color[dd.colorData[LTData.PracticePacket.PRACTICE_LAP]]);
					setItem(tr, 11, "", Gravity.RIGHT | Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.DEFAULT]);
					setItem(tr, 12, "" + dd.carID, Gravity.CENTER, LTData.color[LTData.Colors.DEFAULT]);
					
					if (currentRow != null)
					{
						if (currentRow == tr && currentCar != dd.carID)
						{
							currentRow.setBackgroundDrawable(new BitmapDrawable(getResources(), backgroundBitmaps.get(0)));							
						}
						else
						if (currentRow != tr && currentCar == dd.carID)
						{
							rowToChange = i;							
						}
					}
				}
			}
			if (rowToChange > -1)
			{
				currentRow = (TableRow)tl.getChildAt(rowToChange);
				currentRow.setBackgroundDrawable(new BitmapDrawable(getResources(), backgroundBitmaps.get(1)));
			}
			TableRow lastRow = (TableRow)tl.getChildAt(driversData.size());
			if (lastRow != null)
			{
				setItem(lastRow, 0, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
//				setItem(lastRow, 1, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setImageItem(lastRow, 1, null);
				setItem(lastRow, 2, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
//				setItem(lastRow, 3, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setImageItem(lastRow, 3, null);
				setItem(lastRow, 4, "107%", Gravity.LEFT |Gravity.CENTER_VERTICAL, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 5, EventData.getInstance().session107Percent.toString(), Gravity.CENTER, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 6, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 7, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 8, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 9, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 10, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
				setItem(lastRow, 11, "", Gravity.LEFT, LTData.color[LTData.Colors.RED]);
			}
		}
	}	
	
	public float getColWidth(int pos)
	{
		float weight = 0;
								
		int bitmapWidth = carBitmaps.get(0).getWidth()+20;
		int width = drawCarThumbnails ? rowWidth - bitmapWidth : rowWidth;
		
		if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT)
			width -= arrowBitmaps.get(0).getWidth()*2;
		
		
		switch (EventData.getInstance().eventType)
		{
			case LTData.EventType.RACE_EVENT:
				switch (pos)
				{
					case 0: weight = 0.06f; break;
					case 1: weight = 0.03f; break;
					case 2: weight = 0.06f; break;
					case 3: weight = drawCarThumbnails ? 0.16f : 0; break;
					case 4: weight = printShortNames ? 0.14f : 0.25f; break;
					case 5: weight = printShortNames ? 0.11f : 0.08f; break;
					case 6: weight = printShortNames ? 0.11f : 0.095f; break;
					case 7: weight = printShortNames ? 0.175f : 0.15f; break;
					case 8: weight = printShortNames ? 0.1f : 0.09f; break;
					case 9: weight = printShortNames ? 0.1f : 0.09f; break;
					case 10: weight = printShortNames ? 0.1f : 0.09f; break;
					case 11: weight = 0.05f; break;
					case 12: weight = 0.0f; break;
				}
				break;
				
			case LTData.EventType.QUALI_EVENT:
				switch (pos)
				{
					case 0: weight = 0.065f; break;
					case 1: weight = 0; break;
					case 2: weight = 0.065f; break;
					case 3: weight = drawCarThumbnails ? 0.16f : 0; break;
					case 4: weight = printShortNames ? 0.11f : 0.25f; break;
					case 5: weight = printShortNames ? 0.16f : 0.14f; break;
					case 6: weight = printShortNames ? 0.16f : 0.14f; break;
					case 7: weight = printShortNames ? 0.16f : 0.14f; break;
					case 8: weight = printShortNames ? 0.09f : 0.074f; break;
					case 9: weight = printShortNames ? 0.09f : 0.074f; break;
					case 10: weight = printShortNames ? 0.09f : 0.074f; break;						
					case 11: weight = 0.07f; break;
					case 12: weight = 0.0f; break;
				}
				break;
				
			case LTData.EventType.PRACTICE_EVENT:
				switch (pos)
				{
					case 0: weight = 0.065f; break;
					case 1: weight = 0.0f; break;
					case 2: weight = 0.065f; break;
					case 3: weight = drawCarThumbnails ? 0.17f : 0; break;
					case 4: weight = printShortNames ? 0.1f : 0.26f; break;
					case 5: weight = printShortNames ? 0.16f : 0.15f; break;
					case 6: weight = printShortNames ? 0.16f : 0.13f; break;
					case 7: weight = printShortNames ? 0.11f : 0.085f; break;
					case 8: weight = printShortNames ? 0.11f : 0.085f; break;
					case 9: weight = printShortNames ? 0.11f : 0.085f; break;
					case 10: weight = 0.065f; break;
					case 11: weight = 0.0f; break;
					case 12: weight = 0.0f; break;
				}
				break;
		}
		
		return weight;
	}
	
	
	public TextView setItem(TableRow tr, int pos, String text, int gravity, int color)
	{
		TextView view = (TextView)tr.getChildAt(pos);				
		
		if (view == null)
		{			
			float weight = getColWidth(pos);
			
			
			view = new TextView(getActivity());
			TableRow.LayoutParams cellLp = null; 
					
			if (pos < 12)
				cellLp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT/*rowHeight*/, weight);
			else
				cellLp = new TableRow.LayoutParams(0, rowHeight, weight);
						
//			view.setBackgroundResource(R.drawable.back);
			view.setPadding(2, 0, 2, 0);
			tr.addView(view, cellLp);	
			
			TableLayout tl = (TableLayout)tr.getParent();
			if (tr != tl.getChildAt(tl.getChildCount()-1))
			{
				if (EventData.getInstance().eventType == LTData.EventType.QUALI_EVENT &&
						pos >= 5 && pos <= 7)
					view.setOnClickListener(itemClickListener);
				
				else if (EventData.getInstance().eventType == LTData.EventType.PRACTICE_EVENT &&
						pos == 5)
					view.setOnClickListener(itemClickListener);
				
				else if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT &&
						pos >= 6 && pos <= 7)
					view.setOnClickListener(itemClickListener);						
			}
		}		
		view.setText(text);		
		view.setTextColor(color);
		view.setGravity(gravity);		
	
		return view;
	}
	
	public TextView setHeaderItem(TableRow tr, int pos, String text, int gravity, int color)
	{
		TextView view = (TextView)tr.getChildAt(pos);				
		
		if (view == null)
		{			
			float colWidth = getColWidth(pos);			
			
			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, rowHeight, colWidth);
			
			view = new TextView(getActivity());
			tr.addView(view, cellLp);
			
			view.setTextSize(10);
		}		
		view.setText(text);		
		view.setTextColor(color);
		view.setGravity(gravity);	
	
		return view;
	}
	
	public ImageView setImageItem(TableRow tr, int pos, Bitmap bitmap)
	{
		ImageView iview = (ImageView)tr.getChildAt(pos);
		if (iview == null)
		{
			float colWidth = getColWidth(pos);			
//			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);//0, rowHeight, colWidth);
			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, rowHeight, colWidth);
		
			iview = new ImageView(getActivity());
			tr.addView(iview, cellLp);
			iview.setScaleType(ScaleType.CENTER_INSIDE);
			iview.setPadding(2, 2, 2, 0);
//			iview.setBackgroundResource(R.drawable.back);
			
		}	
		iview.setImageBitmap(bitmap);			
		
		return iview;
	}
	
	public ImageView setStatusIcon(TableRow tr, int id, int pos)
	{
		ImageView iview = (ImageView)tr.getChildAt(pos);
		if (iview == null)
		{
			float weight = (pos == 0 ? 0.1f : 0.05f);
			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, rowHeight, weight);
		
			iview = new ImageView(getActivity());
			iview.setScaleType(ScaleType.CENTER);			
//			iview.setBackgroundResource(R.drawable.back);
			tr.addView(iview, cellLp);
		}	
		iview.setImageBitmap(id > -1 ? statusBitmaps.get(id) : null);			
		
		return iview;
	}
	public ImageView setWindStatusIcon(TableRow tr, int id, int pos)
	{
		ImageView iview = (ImageView)tr.getChildAt(pos);
		if (iview == null)
		{
			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, rowHeight, 0.05f);
		
			iview = new ImageView(getActivity());
			iview.setScaleType(ScaleType.CENTER);			
//			iview.setBackgroundResource(R.drawable.back);
			tr.addView(iview, cellLp);
		}	
		
		if (id == -1)
			iview.setImageBitmap(null);
		else
		{
			Matrix matrix = new Matrix();
			matrix.postRotate((float)EventData.getInstance().windDirection);
			iview.setImageBitmap(Bitmap.createBitmap(statusBitmaps.get(id), 0, 0, statusBitmaps.get(id).getWidth(), statusBitmaps.get(id).getHeight(), matrix, true));
		}			
		
		return iview;
	}
	
	public TextView setStatusRowItem(TableRow tr, int pos, String text, int gravity, int color)
	{
		TextView view = (TextView)tr.getChildAt(pos);
		if (view == null)
		{
			int width = rowWidth-statusBitmaps.get(3).getWidth()-20;
			int colWidth = 0;
			float weight = 0.0f;
			
			switch (pos)
			{
				case 0: weight = 0.1f; break;
				case 1: weight = 0.12f; break;
				case 2: weight = 0.14f; break;
				case 3: weight = 0.05f; break;
				case 4: weight = 0.13f; break;		
				case 5: weight = 0.05f; break;
				case 6: weight = 0.13f; break;
				case 7: weight = 0.05f; break;
				case 8: weight = 0.17f; break;
				case 9: weight = 0.05f; break;
				case 10: weight = 0.11f; break;
				case 11: weight = 0.05f; break;
				case 12: weight = 0.11f; break;
				case 13: weight = 0.05f; break;
			}
			
			TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, rowHeight, weight);
			
			view = new TextView(getActivity());
			tr.addView(view, cellLp);
//			view.setBackgroundResource(R.drawable.back);
			view.setTextSize(10);
		}
		view.setText(text);		
		view.setTextColor(color);
		view.setGravity(gravity);
//		view.setPadding(2, 0, 2, 0);
		
		return view;
	}
	
	public void resetColumns(boolean removeViews)
	{
		TableLayout tl = (TableLayout)getActivity().findViewById(R.id.ltTable);
		if (tl != null)
		{
			for (int i = 0; i < tl.getChildCount(); ++i)
			{									
				TableRow tr = (TableRow)tl.getChildAt(i);
				if (tr != null)
				{
					if (removeViews)
					{
//						for (int j = tr.getChildCount()-1; j >= 0; --j)
							tr.removeAllViews();
					}
					else
					{
						for (int j = 0; j < tr.getChildCount(); ++j)
						{						
							View v = tr.getChildAt(j);
	//						v.setOnClickListener(null);
							float weight = getColWidth(j);
							
							TableRow.LayoutParams cellLp = null;						
							if (j < 12 && j != 1 && j != 3)
								cellLp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT/*rowHeight*/, weight);
							else
								cellLp = new TableRow.LayoutParams(0, rowHeight, weight);
							
	//						TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, rowHeight, weight);
							v.setLayoutParams(cellLp);
							
							if (tr != tl.getChildAt(tl.getChildCount()-1))
							{
								if (EventData.getInstance().eventType == LTData.EventType.QUALI_EVENT &&
										j >= 5 && j <= 7)
									v.setOnClickListener(itemClickListener);
								
								else if (EventData.getInstance().eventType == LTData.EventType.PRACTICE_EVENT &&
										j == 5)
									v.setOnClickListener(itemClickListener);
								
								else if (EventData.getInstance().eventType == LTData.EventType.RACE_EVENT &&
										j >= 6 && j <= 7)
									v.setOnClickListener(itemClickListener);							
							}
						}
					}
					
				}
			}
		}
		TableRow tr = (TableRow)getActivity().findViewById(R.id.headerRow);
		if (tr != null)
		{
			for (int j = 0; j < tr.getChildCount(); ++j)
			{
				View v = tr.getChildAt(j);
				float weight = getColWidth(j);
				
				
				TableRow.LayoutParams cellLp = new TableRow.LayoutParams(0, rowHeight, weight);
				v.setLayoutParams(cellLp);
			}
		}
	}
	
}
