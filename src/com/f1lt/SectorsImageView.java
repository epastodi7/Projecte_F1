package com.f1lt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SectorsImageView extends View 
{
	private int [] sectorIdx = {0, 0, 0};
	private static Bitmap [] sectorImages = new Bitmap[6];
	private static boolean bitmapsLoaded = false;
	private Rect [] sectorRects = new Rect[3];
	private boolean dontDraw = false;
	
	private double [] sectorTimes = {0.0, 0.0, 0.0};
	private int drawIdx = 1;	//0 - sector images, 1 - s1, 2 - s2, 3 - s3
	
	private int left = 0, top = 0;
	private int textX = 0, textY = 0;
	private static int height = 0;
	private Paint paint;
	
	public SectorsImageView(Context ctx, AttributeSet attrs) 
	{
		
        super(ctx, attrs);                
    }
	
	public SectorsImageView(Context ctx) 
	{
        super(ctx);    
        
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }
	
	public void setSector(int i, int idx) 
	{ 
		Log.d("SectorsImage", "setSector");
		if (i >= 0 && i < 3) 
		{
			sectorIdx[i] = idx;
			dontDraw = false;
		}
	}
	
	public void setTextSize(float size)
	{
		Log.d("SectorsImage", "setTextSize");
		paint.setTextSize(size);
		
		int height = getHeight() - (getPaddingTop() + getPaddingBottom());
		
		textX = (getWidth() - (int)paint.getTextSize()*2)/2;
		textY = (height + (int)paint.getTextSize()-3)/2;
	}
	
	public int getDrawIdx()
	{
		Log.d("SectorsImage", "getDrawIdx");
		return drawIdx;
	}
	
	public void setDrawIdx(int d)
	{
		Log.d("SectorsImage", "setDrawIdx");
		drawIdx = d;
	}
	
	public void setSectorTimes(double [] times, int offset)
	{
		Log.d("SectorsImage", "setSectorTimes");
		for (int i = 0; i < 3; ++i)
			sectorTimes[i] = times[i+offset];
	}
	
	public void clear()
	{
		Log.d("SectorsImage", "clear");
		dontDraw = true;
	}
	
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh)
	{		
		Log.d("SectorsImage", "onSizeChanged");
		if (!bitmapsLoaded || (h - (getPaddingTop() + getPaddingBottom())) != height)
		{
			height = h - (getPaddingTop() + getPaddingBottom());
			
			Bitmap b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.light);
			sectorImages[0] = Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(height*0.75))/b.getHeight(), (int)(height*0.75), true);
			
			b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.green_light);
			sectorImages[1] = Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(height*0.75))/b.getHeight(), (int)(height*0.75), true);
			
			b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.white_light);
			sectorImages[2] = Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(height*0.75))/b.getHeight(), (int)(height*0.75), true);
			
			b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.yellow_light);
			sectorImages[3] = Bitmap.createScaledBitmap(b, (int)(b.getWidth()*(height*0.75))/b.getHeight(), (int)(height*0.75), true);
			
			bitmapsLoaded = true;
		}
		
		
		int imagesWidth = 3 * (sectorImages[0].getWidth()+3);
		
		textX = (w - (int)paint.getTextSize()*2)/2;
		textY = (height + (int)paint.getTextSize()-3)/2;
				
		left = (w - imagesWidth)/2;
		top = (height - sectorImages[0].getHeight())/2;
//		Log.d("h2h", "www="+w+", "+sectorImages[0].getWidth()+", "+height+", "+left);
		sectorRects[0] = new Rect(left, top, left+sectorImages[0].getWidth(), top+sectorImages[0].getHeight());
		
		left += sectorImages[0].getWidth()+3;
		sectorRects[1] = new Rect(left, top, left+sectorImages[0].getWidth(), top+sectorImages[0].getHeight());
		
		left += sectorImages[0].getWidth()+3;
		sectorRects[2] = new Rect(left, top, left+sectorImages[0].getWidth(), top+sectorImages[0].getHeight());
		
		left = (w - imagesWidth)/2;
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{
		Log.d("SectorsImage", "onDraw");
		super.onDraw(canvas);
				
		if (!dontDraw)
		{
//			Log.d("siv", "DRAW " + sectorRects[2].left+", "+sectorRects[2].top+", "+sectorRects[2].right+", "+sectorRects[2].bottom);			
//			p.setColor(LTData.color[LTData.Colors.WHITE]);
			
//			canvas.drawText("TESXT", left, top-sectorImages[0].getHeight(), p);
			
			switch (drawIdx)
			{
				case 0:
					if (sectorIdx[0] != 0)
						canvas.drawBitmap(sectorImages[sectorIdx[0]], null, sectorRects[0], paint);
					if (sectorIdx[1] != 0)
						canvas.drawBitmap(sectorImages[sectorIdx[1]], null, sectorRects[1], paint);
					if (sectorIdx[2] != 0)
						canvas.drawBitmap(sectorImages[sectorIdx[2]], null, sectorRects[2], paint);
					break;
					
				default:
					switch (sectorIdx[drawIdx-1])
					{
						case 1: paint.setColor(LTData.color[LTData.Colors.GREEN]); break;
						case 2: paint.setColor(LTData.color[LTData.Colors.WHITE]); break;
						case 3: paint.setColor(LTData.color[LTData.Colors.YELLOW]); break;
					}
					canvas.drawText("" + sectorTimes[drawIdx-1], textX, textY, paint);
					break;
			}
				
		}
	}
}
