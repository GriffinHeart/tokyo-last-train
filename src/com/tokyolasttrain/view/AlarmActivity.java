package com.tokyolasttrain.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.tokyolasttrain.R;

public class AlarmActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setFinishOnTouchOutside(false);
		
		setContentView(R.layout.alarm_activity);
		
		final Vibrator vibrator = startVibration();
		
		((Button) findViewById(R.id.button_alarm_ok)).setOnClickListener(new OnClickListener()
		{	
			@Override
			public void onClick(View v)
			{
				vibrator.cancel();
				finish();
			}
		});
		
		// Set font
		Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Avenir Next.ttc");
		((TextView) findViewById(R.id.label_alarm_msg)).setTypeface(font);
	}
	
	private Vibrator startVibration()
	{
		long[] pattern = { 0, 500, 500 };
		
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(pattern, 0);
		
		return vibrator;
	}
}