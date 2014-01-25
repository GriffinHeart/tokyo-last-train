package com.tokyolasttrain.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.tokyolasttrain.R;
import com.tokyolasttrain.view.util.GifWebView;

public class SplashScreen extends Activity
{
	private static int SPLASH_TIMEOUT = 6000;
	
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
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIMEOUT);
    }
}