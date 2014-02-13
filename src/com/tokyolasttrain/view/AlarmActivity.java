package com.tokyolasttrain.view;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.tokyolasttrain.R;
import com.tokyolasttrain.control.Planner;

public class AlarmActivity extends Activity
{
	private TextView _labelTimer;
	private CountDownTimer _timer;
	
	private Vibrator _vibrator;
	private MediaPlayer _player;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setFinishOnTouchOutside(false);
		
		setContentView(R.layout.alarm_activity);
		
		_labelTimer = (TextView) findViewById(R.id.label_alarm_timer);
		
		findViewById(R.id.dismiss_alarm).setOnClickListener(new OnClickListener()
        {	
			@Override
			public void onClick(View v)
			{
				dismissAlarm();
			}
		});
		
		setTimer();
		
		_vibrator = startVibration();
		_player = startSound();
		
		// Set font
		Typeface lightFont = Typeface.createFromAsset(getAssets(), "fonts/FuturaLT-Light.ttf");
		((TextView) findViewById(R.id.label_alarm_timer)).setTypeface(lightFont);
	}
	
	private void setTimer()
	{
		Planner planner = Planner.getInstance(getApplicationContext());
		
		DateTime currentTime = new DateTime();
		if (currentTime.isAfter(planner.getLastRoute().getDepartureTime().toDateTime()))
		{
	        dismissAlarm();
		}
		else
		{
			long millisecondsLeft = new Interval(new DateTime(), planner.getLastRoute().getDepartureTime().toDateTime()).toDurationMillis();
			if (_timer != null)
			{
				_timer.cancel();
			}
			
			_timer = new CountDownTimer(millisecondsLeft, 60000)	// 1000)
			{
				public void onTick(long millisUntilFinished)
				{
					int hours   = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);
					int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
					// int seconds = (int) (millisUntilFinished / 1000) % 60;

					// _labelTimer.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
					_labelTimer.setText(String.format("%02d:%02d", hours, minutes));
				}

				public void onFinish()
				{
					dismissAlarm();
				}
			}.start();
		}
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
	
	private void dismissAlarm()
	{
		stopVibration(_vibrator);
		stopSound(_player);
		
		startActivity(new Intent(AlarmActivity.this, MainActivity.class));
        finish();
	}
}