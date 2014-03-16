package com.tokyolasttrain.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tokyolasttrain.LocalDateTimeSerializer;
import com.tokyolasttrain.R;
import com.tokyolasttrain.api.FetchLastRoute;
import com.tokyolasttrain.api.HyperdiaApi;
import com.tokyolasttrain.api.HyperdiaApi.LastRoute;
import com.tokyolasttrain.api.NetworkTask.OnCompleteListener;
import com.tokyolasttrain.api.NetworkTask.OnExceptionListener;
import com.tokyolasttrain.api.NetworkTask.OnNetworkUnavailableListener;
import com.tokyolasttrain.control.AlarmReceiver;
import com.tokyolasttrain.control.ArrayAutoCompleteAdapter;
import com.tokyolasttrain.control.Planner;
import com.tokyolasttrain.control.Planner.Station;
import com.tokyolasttrain.view.gif.GifDecoderView;

public class MainActivity extends Activity
{
	private Planner _planner;
	private CountDownTimer _timer;
	
	private PendingIntent _alarmSender;
	private AlarmManager _alarmManager;
	
	private View _background, _layoutLoading, _layoutLastTrain;
	private AutoCompleteTextView _textViewOrigin, _textViewDestination;
	private Button _btnOk;
	private CheckBox _checkBox_alarm;
	private TextView _labelError, _labelStation, _labelLine, _labelDepartureTime, _labelTimer, _labelMissedTrain;
	
	
	@Override
	protected void onStart() {
		super.onStart();
		
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main_activity);
		
		_planner = Planner.getInstance(getApplicationContext());
		
        _alarmSender = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(MainActivity.this, AlarmReceiver.class), 0);
        _alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		_background = findViewById(R.id.background);
		
		_layoutLoading = findViewById(R.id.layout_loading);
		_layoutLastTrain = findViewById(R.id.layout_last_train);
		
		List<String> stations = _planner.getStationList();
		
		_textViewOrigin = (AutoCompleteTextView) findViewById(R.id.textview_origin);
		_textViewOrigin.setAdapter(new ArrayAutoCompleteAdapter<String>(this, android.R.layout.select_dialog_item, stations));
		_textViewOrigin.setOnItemClickListener(OriginTextView_OnItemClick);
		_textViewOrigin.setOnKeyListener(OriginTextView_OnKey);
		
		_textViewDestination = (AutoCompleteTextView) findViewById(R.id.textview_destination);
		_textViewDestination.setAdapter(new ArrayAutoCompleteAdapter<String>(this, android.R.layout.select_dialog_item, stations));
		_textViewDestination.setOnItemClickListener(DestinationTextView_OnItemClick);
		_textViewDestination.setOnKeyListener(DestinationTextView_OnKey);
		
		(_checkBox_alarm = (CheckBox) findViewById(R.id.checkbox_alarm)).setOnCheckedChangeListener(AlarmCheckBox_OnCheckedChange);
		
		(_btnOk = (Button) findViewById(R.id.button_ok)).setOnClickListener(OkButton_OnClick);
		
		_labelError = (TextView) findViewById(R.id.label_error);
		_labelStation = (TextView) findViewById(R.id.label_station);
		_labelLine = (TextView) findViewById(R.id.label_line);
		_labelDepartureTime = (TextView) findViewById(R.id.label_departure_time);
		_labelTimer = (TextView) findViewById(R.id.label_timer);
		_labelMissedTrain = (TextView) findViewById(R.id.label_missed_train);

		// Initialize loading animation
		InputStream stream = null;
        try
        {
        	stream = getAssets().open("gif/loading.gif");
        }
        catch (IOException e) {}
        GifDecoderView splashAnimation = new GifDecoderView(this, stream);
        ((FrameLayout) _layoutLoading).addView(splashAnimation);

		// Set font
		Typeface regularFont = Typeface.createFromAsset(getAssets(), "fonts/FuturaLT-Book.ttf");
		Typeface lightFont = Typeface.createFromAsset(getAssets(), "fonts/FuturaLT-Light.ttf");
		
		((TextView) findViewById(R.id.label_title)).setTypeface(lightFont);
		((TextView) findViewById(R.id.label_origin)).setTypeface(regularFont);
		((TextView) findViewById(R.id.label_destination)).setTypeface(regularFont);
		((TextView) findViewById(R.id.label_missed_train)).setTypeface(regularFont);
		_textViewOrigin.setTypeface(regularFont);
		_textViewDestination.setTypeface(regularFont);
		_labelError.setTypeface(regularFont);
		_labelStation.setTypeface(regularFont);
		_labelLine.setTypeface(regularFont);
		_labelDepartureTime.setTypeface(regularFont);
		_labelTimer.setTypeface(lightFont);
		
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		
		_planner.notifyUser(sharedPrefs.getBoolean("NotifyUser", false));
		_checkBox_alarm.setChecked(_planner.alarmOn());
		
		String originStation;
		if (!(originStation = sharedPrefs.getString("OriginStation", "")).isEmpty())
		{
			_planner.setStation(Station.Origin, originStation);
			_textViewOrigin.setText(originStation);
		}
		
		String destinationStation;
		if (!(destinationStation = sharedPrefs.getString("DestinationStation", "")).isEmpty())
		{
			_planner.setStation(Station.Destination, destinationStation);
			_textViewDestination.setText(destinationStation);
		}
		
		String json;
		if (!(json = sharedPrefs.getString("LastRoute", "")).isEmpty())
		{		
			try
			{
				Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer()).create();
				LastRoute lastRoute = gson.fromJson(json, LastRoute.class);
				_planner.setLastRoute(lastRoute);
				onGotResults(lastRoute);
			}
			catch(Exception e) { /* there was a problem reading the saved result */ }
		}
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		
		Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
		prefsEditor.putBoolean("NotifyUser", _planner.alarmOn());
		
		if (_planner.hasSetStation(Station.Origin))
		{
			prefsEditor.putString("OriginStation", _planner.getStation(Station.Origin));
		}
		
		if (_planner.hasSetStation(Station.Destination))
		{
			prefsEditor.putString("DestinationStation", _planner.getStation(Station.Destination));
		}

		if (_planner.hasSetLastRoute())
		{
			try
			{
				Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer()).create();
				String json = gson.toJson(_planner.getLastRoute());
				prefsEditor.putString("LastRoute", json);
			}
			catch (Exception e) { /* there was a problem saving the results */ }
		}		
		
		prefsEditor.commit();
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		View prevFocusView = getCurrentFocus();
		boolean ret = super.dispatchTouchEvent(event);

		if (prevFocusView instanceof TextView)
		{
			View nextFocusView = getCurrentFocus();
			
			int scrcoords[] = new int[2];
			nextFocusView.getLocationOnScreen(scrcoords);
			float x = event.getRawX() + nextFocusView.getLeft() - scrcoords[0];
			float y = event.getRawY() + nextFocusView.getTop() - scrcoords[1];

			if (event.getAction() == MotionEvent.ACTION_UP &&
					(x < nextFocusView.getLeft() || x >= nextFocusView.getRight() || y < nextFocusView.getTop() || y > nextFocusView.getBottom()))
			{ 
				if (prevFocusView.getId() == R.id.textview_origin)
				{
					processSingleInput((TextView) prevFocusView, Station.Origin, Station.Destination);
				}
				else if (prevFocusView.getId() == R.id.textview_destination)
				{
					processSingleInput((TextView) prevFocusView, Station.Destination, Station.Origin);
				}
			}
		}
		return ret;
	}
	
	private OnItemClickListener OriginTextView_OnItemClick = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			processSingleInput((TextView) view, Station.Origin, Station.Destination);
		}
	};
	
	private OnItemClickListener DestinationTextView_OnItemClick = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			processSingleInput((TextView) view, Station.Destination, Station.Origin);
		}
	};
	
	private OnKeyListener OriginTextView_OnKey = new OnKeyListener()
	{
		@Override
		public boolean onKey(View view, int keyCode, KeyEvent event)
		{
			if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
					keyCode == EditorInfo.IME_ACTION_DONE ||
						event.getAction() == KeyEvent.ACTION_DOWN &&
							event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
			{
				_textViewOrigin.dismissDropDown();
				processSingleInput((TextView) view, Station.Origin, Station.Destination);
				return true;
			}
			
			return false;
		}
	};
	
	private OnKeyListener DestinationTextView_OnKey = new OnKeyListener()
	{
		@Override
		public boolean onKey(View view, int keyCode, KeyEvent event)
		{
			if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
					keyCode == EditorInfo.IME_ACTION_DONE ||
						event.getAction() == KeyEvent.ACTION_DOWN &&
							event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
			{
				_textViewDestination.dismissDropDown();
				processSingleInput((TextView) view, Station.Destination, Station.Origin);
				return true;
			}
			
			return false;
		}
	};
	
	private OnCheckedChangeListener AlarmCheckBox_OnCheckedChange = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			_planner.notifyUser(isChecked);
			
			if (_planner.alarmOn() && _planner.hasSetLastRoute())
			{
				setAlarm(_planner.getAlarmTime());
			}
			else if (!_planner.alarmOn())
			{
				cancelAlarm();
			}
		}
	};
	
	private OnClickListener OkButton_OnClick = new OnClickListener()
	{	
		@Override
		public void onClick(View v)
		{
			processInput();
		}
	};
	
	private void processSingleInput(TextView textView, Station station, Station otherStation)
	{
		String stationName = textView.getText().toString().toLowerCase(Locale.US);
		
		if (_planner.setStation(station, stationName))
		{
			if (_planner.hasSetStation(otherStation))
			{
				if (!_planner.getStation(station).equals(_planner.getStation(otherStation)))
				{
					dismissKeyboard();
					getLastRoute();
				}
				else
				{
					switch (station)
					{
					case Destination:
						_textViewDestination.requestFocus();
						_textViewDestination.selectAll();
						break;
						
					case Origin:
						_textViewOrigin.requestFocus();
						_textViewOrigin.selectAll();
						break;
					}
					
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.equal_station_error), Toast.LENGTH_LONG).show();
				}
			}
			else
			{
				switch (otherStation)
				{
				case Destination:
					_textViewDestination.requestFocus();
					break;
					
				case Origin:
					_textViewOrigin.requestFocus();
					break;
				}
			}
		}
		else
		{
			switch (station)
			{
			case Destination:
				_textViewDestination.requestFocus();
				_textViewDestination.selectAll();
				break;
				
			case Origin:
				_textViewOrigin.requestFocus();
				_textViewOrigin.selectAll();
				break;
			}
			
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_station_error), Toast.LENGTH_LONG).show();
		}
	}
	
	private void processInput()
	{
		String originStationName = _textViewOrigin.getText().toString().toLowerCase(Locale.US);
		String destinationStationName = _textViewDestination.getText().toString().toLowerCase(Locale.US);
		
		if (!_planner.setStation(Station.Origin, originStationName))
		{
			_textViewOrigin.requestFocus();
			_textViewOrigin.selectAll();
		
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_station_error), Toast.LENGTH_SHORT).show();
		}				
		else if (!_planner.setStation(Station.Destination, destinationStationName))
		{
			_textViewDestination.requestFocus();
			_textViewDestination.selectAll();
		
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_station_error), Toast.LENGTH_SHORT).show();
		}
		else
		{
			dismissKeyboard();
			getLastRoute();
		}
	}
	
	private boolean _done = false;
	private void getLastRoute()
	{
		_planner.setLastRoute(null);
		
		if (_planner.alarmOn())
		{
			cancelAlarm();
		}
		
		FetchLastRoute fetchLastRoute = new FetchLastRoute();
		fetchLastRoute.setOriginStation(_planner.getStation(Station.Origin));
		fetchLastRoute.setDestinationStation(_planner.getStation(Station.Destination));
		
		fetchLastRoute.setOnCompleteListener(new OnCompleteListener<HyperdiaApi.LastRoute>()
		{
			@Override
			public void onComplete(LastRoute result)
			{
				_done = true;
				onGotResults(result);
			}
		});
		
		fetchLastRoute.setOnGenericExceptionListener(new OnExceptionListener()
		{
			@Override
			public void onException(Exception exception)
			{
				// TODO: Check why this exception is always fired
				// _done = true;
				ShowError(getResources().getString(R.string.generic_error));
			}
		});
		
		fetchLastRoute.setOnNetworkUnavailableListener(new OnNetworkUnavailableListener()
		{
			@Override
			public void onNetworkException(NetworkErrorException exception)
			{
				_done = true;
				ShowError(getResources().getString(R.string.network_unavailable_error));	
			}
		});
		
		fetchLastRoute.execute();
		
		if (!_done)
		{
			ShowLoading();
		}
	}
	
	private void onGotResults(LastRoute route)
	{
		_planner.setLastRoute(route);
		
		_labelStation.setText(route.getStation());
		_labelLine.setText(route.getLine());
		_labelDepartureTime.setText(String.format("%02d:%02d", route.getDepartureTime().getHourOfDay(), route.getDepartureTime().getMinuteOfHour()));
		
		if (_planner.alarmOn())
		{
			setAlarm(_planner.getAlarmTime());
		}
		
		DateTime currentTime = new DateTime();
		if (currentTime.isAfter(route.getDepartureTime().toDateTime()))
		{
			ShowMissedTrain();
		}
		else
		{
			long millisecondsLeft = new Interval(new DateTime(), route.getDepartureTime().toDateTime()).toDurationMillis();
			if (_timer != null)
			{
				_timer.cancel();
			}
			
			_timer = new CountDownTimer(millisecondsLeft, 1000)	// 60000)
			{
				public void onTick(long millisUntilFinished)
				{
					int hours   = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);
					int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
					int seconds = (int) (millisUntilFinished / 1000) % 60;

					_labelTimer.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
					//_labelTimer.setText(String.format("%02d:%02d", hours, minutes));
				}

				public void onFinish()
				{
					ShowMissedTrain();
				}
			}.start();
			
			ShowLastTrain();
		}
	}
	
	private void setAlarm(long millisLeft)
	{
		cancelAlarm();
        _alarmManager.set(AlarmManager.RTC_WAKEUP, millisLeft, _alarmSender);
	}
	
	private void cancelAlarm()
	{
		_alarmManager.cancel(_alarmSender);
	}
	
	private void dismissKeyboard()
	{
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	    
	    // HACK !!!
	    _background.requestFocus();
	}
	
	private void ShowLoading()
	{
		_btnOk.setVisibility(View.GONE);
		
		_layoutLoading.setVisibility(View.VISIBLE);
		_layoutLastTrain.setVisibility(View.GONE);
		_labelMissedTrain.setVisibility(View.GONE);
		_labelError.setVisibility(View.GONE);
	}
	
	private void ShowError(String errorMessage)
	{
		_labelError.setText(errorMessage);
		
		_btnOk.setVisibility(View.GONE);
		
		_layoutLoading.setVisibility(View.GONE);
		_layoutLastTrain.setVisibility(View.GONE);
		_labelMissedTrain.setVisibility(View.GONE);
		_labelError.setVisibility(View.VISIBLE);
	}
	
	private void ShowLastTrain()
	{
		_btnOk.setVisibility(View.GONE);
		
		_layoutLoading.setVisibility(View.GONE);
		_layoutLastTrain.setVisibility(View.VISIBLE);
		_labelMissedTrain.setVisibility(View.GONE);
		_labelError.setVisibility(View.GONE);
	}
	
	private void ShowMissedTrain()
	{
		_btnOk.setVisibility(View.GONE);
		
		_layoutLoading.setVisibility(View.GONE);
		_layoutLastTrain.setVisibility(View.GONE);
		_labelMissedTrain.setVisibility(View.VISIBLE);
		_labelError.setVisibility(View.GONE);
	}
}