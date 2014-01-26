package com.tokyolasttrain.view;

import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tokyolasttrain.R;
import com.tokyolasttrain.api.FetchLastRoute;
import com.tokyolasttrain.api.HyperdiaApi;
import com.tokyolasttrain.api.HyperdiaApi.LastRoute;
import com.tokyolasttrain.api.NetworkTask.OnCompleteListener;
import com.tokyolasttrain.api.NetworkTask.OnExceptionListener;
import com.tokyolasttrain.control.ArrayAutoCompleteAdapter;
import com.tokyolasttrain.control.Planner;
import com.tokyolasttrain.control.Planner.Station;

public class MainActivity extends Activity
{
	private AutoCompleteTextView _originTextView, _destinationTextView;
	private Button _btnOk;
	private ProgressBar _loadingLayout;
	private View _lastTrainLayout, _missedTrainLayout;
	private TextView _time, _timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main_activity);
		
		List<String> stations = Planner.getInstance(getApplicationContext()).getStationList();
		
		_originTextView = (AutoCompleteTextView) findViewById(R.id.textview_origin);
		
		_originTextView.setAdapter(new ArrayAutoCompleteAdapter<String>(this, android.R.layout.select_dialog_item, stations));
		_originTextView.setOnItemClickListener(OriginTextView_OnItemClick);
		_originTextView.setOnKeyListener(OriginTextView_OnKey);
		
		_destinationTextView = (AutoCompleteTextView) findViewById(R.id.textview_destination);
		_destinationTextView.setAdapter(new ArrayAutoCompleteAdapter<String>(this, android.R.layout.select_dialog_item, stations));
		_destinationTextView.setOnItemClickListener(DestinationTextView_OnItemClick);
		_destinationTextView.setOnKeyListener(DestinationTextView_OnKey);
		
		(_btnOk = (Button) findViewById(R.id.ok_button)).setOnClickListener(OkButton_OnClick);
		
		_loadingLayout = (ProgressBar) findViewById(R.id.loading_layout);	//.addView(new GifWebView(this, "file:///android_asset/loading_animation.gif"));
		_lastTrainLayout = findViewById(R.id.last_train_layout);
		_missedTrainLayout = findViewById(R.id.missed_train_layout);
		_time = (TextView) findViewById(R.id.label_departure_time);
		_timer = (TextView) findViewById(R.id.label_timer);
		
		// Set font
		Typeface font = Typeface.createFromAsset(getAssets(), "fonts/KozGoPr6N-Light.otf");
		((TextView) findViewById(R.id.label_title)).setTypeface(font);
		((TextView) findViewById(R.id.label_origin)).setTypeface(font);
		_originTextView.setTypeface(font);
		((TextView) findViewById(R.id.label_destination)).setTypeface(font);
		_destinationTextView.setTypeface(font);
		((TextView) findViewById(R.id.label_missed_train)).setTypeface(font);
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
				_originTextView.dismissDropDown();
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
				_destinationTextView.dismissDropDown();
				processSingleInput((TextView) view, Station.Destination, Station.Origin);
				return true;
			}
			
			return false;
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
		Planner planner = Planner.getInstance(getApplicationContext());
		
		String stationName = textView.getText().toString().toLowerCase(Locale.US);
		
		if (planner.setStation(station, stationName))
		{
			if (planner.hasSetStation(otherStation))
			{
				if (!planner.getStation(station).equals(Planner.getInstance(getApplicationContext()).getStation(otherStation)))
				{
					InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
					
					getLastRoute();
				}
				else
				{
					switch (station)
					{
					case Destination:
						_destinationTextView.requestFocus();
						_destinationTextView.selectAll();
						break;
						
					case Origin:
						_originTextView.requestFocus();
						_originTextView.selectAll();
						break;
					}
					
					Toast.makeText(getApplicationContext(), "ERROR: Invalid station", Toast.LENGTH_LONG).show();
				}
			}
			else
			{
				switch (otherStation)
				{
				case Destination:
					_destinationTextView.requestFocus();
					break;
					
				case Origin:
					_originTextView.requestFocus();
					break;
				}
			}
		}
		else
		{
			switch (station)
			{
			case Destination:
				_destinationTextView.requestFocus();
				_destinationTextView.selectAll();
				break;
				
			case Origin:
				_originTextView.requestFocus();
				_originTextView.selectAll();
				break;
			}
			
			Toast.makeText(getApplicationContext(), "ERROR: Invalid station!", Toast.LENGTH_LONG).show();
		}
	}
	
	private void processInput()
	{
		Planner planner = Planner.getInstance(getApplicationContext());
		
		String originStationName = _originTextView.getText().toString().toLowerCase(Locale.US);
		String destinationStationName = _destinationTextView.getText().toString().toLowerCase(Locale.US);
		
		if (!planner.setStation(Station.Origin, originStationName))
		{
			_originTextView.requestFocus();
			_originTextView.selectAll();
		
			Toast.makeText(getApplicationContext(), "ERROR: Invalid station!", Toast.LENGTH_LONG).show();
		}				
		else if (!planner.setStation(Station.Destination, destinationStationName))
		{
			_destinationTextView.requestFocus();
			_destinationTextView.selectAll();
		
			Toast.makeText(getApplicationContext(), "ERROR: Invalid station!", Toast.LENGTH_LONG).show();
		}
		else
		{
			InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(_originTextView.getWindowToken(), 0);
			inputManager.hideSoftInputFromWindow(_destinationTextView.getWindowToken(), 0);
			
			getLastRoute();
		}
	}
	
	private void getLastRoute()
	{
		Planner planner = Planner.getInstance(getApplicationContext());
		
		FetchLastRoute fetchLastRoute = new FetchLastRoute();
		fetchLastRoute.setOriginStation(planner.getStation(Station.Origin));
		fetchLastRoute.setDestinationStation(planner.getStation(Station.Destination));
		
		fetchLastRoute.setOnCompleteListener(new OnCompleteListener<HyperdiaApi.LastRoute>()
		{
			@Override
			public void onComplete(LastRoute result)
			{
				onGotResults(result);
			}
		});
		
		fetchLastRoute.setOnGenericExceptionListener(new OnExceptionListener()
		{
			@Override
			public void onException(Exception exception)
			{
				// TODO present generic error message
			}
		});
		
		fetchLastRoute.execute();
		
		ShowLoading();
	}
	
	private void onGotResults(LastRoute route)
	{
		LocalDateTime departureTime = route.getDepartureTime();
		_time.setText(String.format("%02d:%02d", departureTime.getHourOfDay(), departureTime.getMinuteOfHour()));
		
		int millisecondsLeft = departureTime.getMillisOfDay() - new LocalTime().getMillisOfDay();
		if (millisecondsLeft <= 0)
		{
			ShowMissedTrain();
		}
		else
		{
			new CountDownTimer(millisecondsLeft, 1000)
			{
				public void onTick(long millisUntilFinished)
				{
					int seconds = (int) (millisUntilFinished / 1000) % 60 ;
					int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
					int hours   = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);

		         _timer.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
				}

				public void onFinish()
				{
					ShowMissedTrain();
				}
			}.start();

			ShowLastTrain();
		}
	}
	
	private void ShowOk()
	{
		_btnOk.setVisibility(View.VISIBLE);
		_loadingLayout.setVisibility(View.GONE);
		_lastTrainLayout.setVisibility(View.GONE);
		_missedTrainLayout.setVisibility(View.GONE);
	}
	
	private void ShowLoading()
	{
		_btnOk.setVisibility(View.GONE);
		_loadingLayout.setVisibility(View.VISIBLE);
		_lastTrainLayout.setVisibility(View.GONE);
		_missedTrainLayout.setVisibility(View.GONE);
	}
	
	private void ShowLastTrain()
	{
		_btnOk.setVisibility(View.GONE);
		_loadingLayout.setVisibility(View.GONE);
		_lastTrainLayout.setVisibility(View.VISIBLE);
		_missedTrainLayout.setVisibility(View.GONE);
	}
	
	private void ShowMissedTrain()
	{
		_btnOk.setVisibility(View.GONE);
		_loadingLayout.setVisibility(View.GONE);
		_lastTrainLayout.setVisibility(View.GONE);
		_missedTrainLayout.setVisibility(View.VISIBLE);
	}
}