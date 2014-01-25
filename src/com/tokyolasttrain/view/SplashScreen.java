package com.tokyolasttrain.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.tokyolasttrain.R;
import com.tokyolasttrain.view.util.GifWebView;

public class SplashScreen extends Activity
{
	private static int SPLASH_TIMEOUT = 5000;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
  
        setContentView(R.layout.splash_screen);
        
        ((FrameLayout) findViewById(R.id.splash_animation_layout)).addView(new GifWebView(this, "file:///android_asset/splash_animation.gif", true));
        
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                dismiss();
            }
        }, SPLASH_TIMEOUT);
        
        findViewById(R.id.splash_img).setOnTouchListener(new OnTouchListener()
        {
			@Override
			public boolean onTouch(View view, MotionEvent event)
			{
				dismiss();
				return true;
			}
		});
    }
    
    private void dismiss()
    {
    	Intent intent = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}