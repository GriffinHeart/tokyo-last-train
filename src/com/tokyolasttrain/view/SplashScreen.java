package com.tokyolasttrain.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.tokyolasttrain.R;
import com.tokyolasttrain.view.gif.GifWebView;

public class SplashScreen extends Activity
{
	private static int SPLASH_TIMEOUT = 5000;
	private InterruptableRunnable _runnable;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
  
        setContentView(R.layout.splash_screen);
        
        GifWebView splashAnimation = new GifWebView(this, "file:///android_asset/splash_animation.gif");
        splashAnimation.setBackgroundColor(getResources().getColor(android.R.color.black));
        splashAnimation.fitScreen();
        
        ((FrameLayout) findViewById(R.id.splash_animation_layout)).addView(splashAnimation);
        
        findViewById(R.id.dismiss_splash_screen).setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				dismiss();
			}
		});
        
        new Handler().postDelayed(_runnable = new InterruptableRunnable(), SPLASH_TIMEOUT);
    }
    
    private void dismiss()
    {
    	_runnable.interrupt();
    	
    	Intent intent = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    public class InterruptableRunnable implements Runnable
    {
        private volatile boolean _isRunning = true;

        public void interrupt()
        {
        	_isRunning = false;
        }

        @Override
        public void run()
        {
            while (_isRunning)
            {
                dismiss();
                _isRunning = false;
            }
        }
    }
}