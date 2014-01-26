package com.tokyolasttrain.view;

import java.util.List;
import java.util.Locale;

import org.joda.time.LocalTime;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.tokyolasttrain.R;
import com.tokyolasttrain.api.FetchLastRoute;
import com.tokyolasttrain.api.HyperdiaApi;
import com.tokyolasttrain.api.HyperdiaApi.LastRoute;
import com.tokyolasttrain.api.NetworkTask.OnCompleteListener;
import com.tokyolasttrain.api.NetworkTask.OnExceptionListener;
import com.tokyolasttrain.control.Planner;
import com.tokyolasttrain.control.Planner.Station;

public class MainActivity extends Activity
{
	private AutoCompleteTextView _originTextView, _destinationTextView;
	
	private View _resultLayout;
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
		_originTextView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, stations));
		_originTextView.setOnItemClickListener(OriginTextView_OnItemClick);
		_originTextView.setOnKeyListener(OriginTextView_OnKey);
		
		_destinationTextView = (AutoCompleteTextView) findViewById(R.id.textview_destination);
		_destinationTextView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, stations));
		_destinationTextView.setOnItemClickListener(DestinationTextView_OnItemClick);
		_destinationTextView.setOnKeyListener(DestinationTextView_OnKey);
		
		findViewById(R.id.submit_button).setOnClickListener(SubmitButton_OnClick);
		
		_time = (TextView) findViewById(R.id.time);
		_timer = (TextView) findViewById(R.id.timer);
		_resultLayout = findViewById(R.id.result_layout);
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
	
	private OnClickListener SubmitButton_OnClick = new OnClickListener()
	{	
		@Override
		public void onClick(View v)
		{
			processInput(_originTextView, _destinationTextView);
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
					ShowResult();
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
	
	private void processInput(TextView originTextView, TextView destinationTextView)
	{
		Planner planner = Planner.getInstance(getApplicationContext());
		
		String originStationName = originTextView.getText().toString().toLowerCase(Locale.US);
		String destinationStationName = destinationTextView.getText().toString().toLowerCase(Locale.US);
		
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
			ShowResult();
		}
	}
	
	private void ShowResult()
	{
		Planner planner = Planner.getInstance(getApplicationContext());
		
		FetchLastRoute fetchLastRoute = new FetchLastRoute();
		fetchLastRoute.setOriginStation(planner.getStation(Station.Origin));
		fetchLastRoute.setDestinationStation(planner.getStation(Station.Destination));
		
		fetchLastRoute.setOnCompleteListener(new OnCompleteListener<HyperdiaApi.LastRoute>() {

			@Override
			public void onComplete(LastRoute result) { 
				//TODO stop the loading and display the results
				onGotResults(result);
			}
		});
		
		fetchLastRoute.setOnGenericExceptionListener(new OnExceptionListener() {
			@Override
			public void onException(Exception exception) {
				// TODO present generic error message
			}
		});
		
		//TODO set the loading
		fetchLastRoute.execute();
		
	}
	
	private void onGotResults(LastRoute route) {
		LocalTime currentTime = new LocalTime();
		_time.setText(String.format("%02d:%02d", currentTime.getHourOfDay(), currentTime.getMinuteOfHour()));
		
		int millisecondsLeft = route.getDepartureTime().getMillisOfDay() - currentTime.getMillisOfDay();
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
		    	 _timer.setText("MISSED!");
		     }
		  }.start();
		
		_resultLayout.setVisibility(View.VISIBLE);
	}
}