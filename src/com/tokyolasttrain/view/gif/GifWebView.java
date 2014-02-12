package com.tokyolasttrain.view.gif;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;

public class GifWebView extends WebView
{
	public GifWebView(Context context)
	{
		super(context);
	}
	
	public GifWebView(Context context, String path)
	{
		super(context);
		loadUrl(path);
	}
	
	public void fitScreen()
	{
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);

		getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent e)
			{
				return (e.getAction() == MotionEvent.ACTION_MOVE);
			}
		});
	}
}