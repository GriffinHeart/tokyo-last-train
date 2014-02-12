package com.tokyolasttrain.view;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
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
		final MediaPlayer player = startSound();
		
		findViewById(R.id.dismiss_alarm).setOnClickListener(new OnClickListener()
        {	
			@Override
			public void onClick(View v)
			{
				stopVibration(vibrator);
				stopSound(player);
				
				// finish();
			}
		});
		
		// Set font
		Typeface lightFont = Typeface.createFromAsset(getAssets(), "fonts/FuturaLT-Light.ttf");
		((TextView) findViewById(R.id.label_alarm_timer)).setTypeface(lightFont);
	}
	
	private Vibrator startVibration()
	{
		long[] pattern = { 0, 500, 500 };
		
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(pattern, 0);
		
		return vibrator;
	}
	
	private void stopVibration(Vibrator vibrator)
	{
		vibrator.cancel();
	}
	
	private MediaPlayer startSound()
	{
		MediaPlayer player = null;
		try
		{
			AssetFileDescriptor afd = getAssets().openFd("sounds/alarm.mp3");
			
		    player = new MediaPlayer();
		    player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
		    
		    afd.close();
		    
		    player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
		    player.prepare();
		    player.start();
		}
		catch (IOException e)
		{
			Log.d("TokyoLastTrain", e.getMessage());
			e.printStackTrace();
		}
		
		return player;
	}
	
	private void stopSound(MediaPlayer player)
	{
		player.stop();
	}
}