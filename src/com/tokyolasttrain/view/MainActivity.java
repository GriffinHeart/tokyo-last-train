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
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tokyolasttrain.R;
import com.tokyolasttrain.api.FetchLastRoute;
import com.tokyolasttrain.api.HyperdiaApi;
import com.tokyolasttrain.api.HyperdiaApi.LastRoute;
import com.tokyolasttrain.api.NetworkTask.OnCompleteListener;
import com.tokyolasttrain.api.NetworkTask.OnExceptionListener;
import com.tokyolasttrain.api.NetworkTask.OnNetworkUnavailableListener;
import com.tokyolasttrain.control.ArrayAutoCompleteAdapter;
import com.tokyolasttrain.control.Planner;
import com.tokyolasttrain.control.Planner.Station;
import com.tokyolasttrain.view.gif.GifDecoderView;

public class MainActivity extends Activity
{
	private CountDownTimer _timer;
	
	private View _background, _layoutLoading, _layoutError, _layoutLastTrain, _layoutMissedTrain;
	private AutoCompleteTextView _textViewOrigin, _textViewDestination;
	private Button _btnOk;
	private TextView _labelError, _labelStation, _labelLine, _labelDepartureTime, _labelTimer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main_activity);
		
		_background = findViewById(R.id.background);
		
		_layoutLoading = findViewById(R.id.layout_loading);
		_layoutError = findViewById(R.id.layout_error);
		_layoutLastTrain = findViewById(R.id.layout_last_train);
		_layoutMissedTrain = findViewById(R.id.layout_missed_train);
		
		List<String> stations = Planner.getInstance(getApplicationContext()).getStationList();
		
		_textViewOrigin = (AutoCompleteTextView) findViewById(R.id.textview_origin);
		_textViewOrigin.setAdapter(new ArrayAutoCompleteAdapter<String>(this, android.R.layout.select_dialog_item, stations));
		_textViewOrigin.setOnItemClickListener(OriginTextView_OnItemClick);
		_textViewOrigin.setOnKeyListener(OriginTextView_OnKey);
		
		_textViewDestination = (AutoCompleteTextView) findViewById(R.id.textview_destination);
		_textViewDestination.setAdapter(new ArrayAutoCompleteAdapter<String>(this, android.R.layout.select_dialog_item, stations));
		_textViewDestination.setOnItemClickListener(DestinationTextView_OnItemClick);
		_textViewDestination.setOnKeyListener(DestinationTextView_OnKey);
		
		(_btnOk = (Button) findViewById(R.id.button_ok)).setOnClickListener(OkButton_OnClick);
		
		_labelStation = (TextView) findViewById(R.id.label_station);
		_labelLine = (TextView) findViewById(R.id.label_line);
		_labelDepartureTime = (TextView) findViewById(R.id.label_departure_time);
		_labelTimer = (TextView) findViewById(R.id.label_timer);
		_labelError = (TextView) findViewById(R.id.label_error);

		// Initialize loading animation
		InputStream stream = null;
        try
        {
        	stream = getAssets().open("loading_animation.gif");
        }
        catch (IOException e) {}
        GifDecoderView splashAnimation = new GifDecoderView(this, stream);
        ((FrameLayout) _layoutLoading).addView(splashAnimation);

		// Set font
		Typeface font = Typeface.createFromAsset(getAssets(), "fonts/KozGoPr6N-Light.otf");
		((TextView) findViewById(R.id.label_title)).setTypeface(font);
		((TextView) findViewById(R.id.label_origin)).setTypeface(font);
		_textViewOrigin.setTypeface(font);
		((TextView) findViewById(R.id.label_destination)).setTypeface(font);
		_textViewDestination.setTypeface(font);
		((TextView) findViewById(R.id.label_missed_train)).setTypeface(font);
		_labelError.setTypeface(font);
		_labelStation.setTypeface(font);
		_labelLine.setTypeface(font);
		_labelDepartureTime.setTypeface(font);
		_labelTimer.setTypeface(font);
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
					
					Toast.makeText(getApplicationContext(), "ERROR: Invalid station", Toast.LENGTH_LONG).show();
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
			
			Toast.makeText(getApplicationContext(), "ERROR: Invalid station!", Toast.LENGTH_LONG).show();
		}
	}
	
	private void processInput()
	{
		Planner planner = Planner.getInstance(getApplicationContext());
		
		String originStationName = _textViewOrigin.getText().toString().toLowerCase(Locale.US);
		String destinationStationName = _textViewDestination.getText().toString().toLowerCase(Locale.US);
		
		if (!planner.setStation(Station.Origin, originStationName))
		{
			_textViewOrigin.requestFocus();
			_textViewOrigin.selectAll();
		
			Toast.makeText(getApplicationContext(), "ERROR: Invalid station!", Toast.LENGTH_LONG).show();
		}				
		else if (!planner.setStation(Station.Destination, destinationStationName))
		{
			_textViewDestination.requestFocus();
			_textViewDestination.selectAll();
		
			Toast.makeText(getApplicationContext(), "ERROR: Invalid station!", Toast.LENGTH_LONG).show();
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
		Planner planner = Planner.getInstance(getApplicationContext());
		
		FetchLastRoute fetchLastRoute = new FetchLastRoute();
		fetchLastRoute.setOriginStation(planner.getStation(Station.Origin));
		fetchLastRoute.setDestinationStation(planner.getStation(Station.Destination));
		
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
		_labelStation.setText(route.getStation());
		_labelLine.setText(route.getLine());
		
		LocalDateTime departureTime = route.getDepartureTime();
		_labelDepartureTime.setText(String.format("%02d:%02d", departureTime.getHourOfDay(), departureTime.getMinuteOfHour()));
		
		Interval interval = new Interval(new DateTime(), departureTime.toDateTime());
		long millisecondsLeft = interval.toDurationMillis();
		if (millisecondsLeft <= 0)
		{
			ShowMissedTrain();
		}
		else
		{
			if (_timer != null)
			{
				_timer.cancel();
			}
			
			_timer = new CountDownTimer(millisecondsLeft, 1000)
			{
				public void onTick(long millisUntilFinished)
				{
					int seconds = (int) (millisUntilFinished / 1000) % 60 ;
					int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
					int hours   = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);

					_labelTimer.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
				}

				public void onFinish()
				{
					ShowMissedTrain();
				}
			}.start();
			
			ShowLastTrain();
		}
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
		_layoutMissedTrain.setVisibility(View.GONE);
		_layoutError.setVisibility(View.GONE);
	}
	
	private void ShowError(String errorMessage)
	{
		_labelError.setText(errorMessage);
		
		_btnOk.setVisibility(View.GONE);
		
		_layoutLoading.setVisibility(View.GONE);
		_layoutLastTrain.setVisibility(View.GONE);
		_layoutMissedTrain.setVisibility(View.GONE);
		_layoutError.setVisibility(View.VISIBLE);
	}
	
	private void ShowLastTrain()
	{
		_btnOk.setVisibility(View.GONE);
		
		_layoutLoading.setVisibility(View.GONE);
		_layoutLastTrain.setVisibility(View.VISIBLE);
		_layoutMissedTrain.setVisibility(View.GONE);
		_layoutError.setVisibility(View.GONE);
	}
	
	private void ShowMissedTrain()
	{
		_btnOk.setVisibility(View.GONE);
		
		_layoutLoading.setVisibility(View.GONE);
		_layoutLastTrain.setVisibility(View.GONE);
		_layoutMissedTrain.setVisibility(View.VISIBLE);
		_layoutError.setVisibility(View.GONE);
	}
}