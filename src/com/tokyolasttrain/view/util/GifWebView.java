package com.tokyolasttrain.view.util;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;

public class GifWebView extends WebView
{
	public GifWebView(Context context)
	{
		super(context);
	}
	
	public GifWebView(Context context, String path)
	{
		super(context);
		setBackgroundColor(getResources().getColor(android.R.color.black));
		loadUrl(path);
	}
	
	public GifWebView(Context context, String path, boolean fullscreen)
	{
		this(context, path);
		if (fullscreen)
		{
			getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		}
	}
}