package com.tokyolasttrain.view.gif;

import android.content.Context;
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
		getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
	}
}