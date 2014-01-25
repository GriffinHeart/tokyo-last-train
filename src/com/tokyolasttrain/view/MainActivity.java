package com.tokyolasttrain.view;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
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
import com.tokyolasttrain.control.Planner;
import com.tokyolasttrain.control.Planner.Station;

public class MainActivity extends Activity
{
	private AutoCompleteTextView _originTextView, _destinationTextView;
	
	private View _resultLayout;
	private TextView _time, _timer;
	
	private long elapsedTime;
	
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
		
		_time = (TextView) findViewById(R.id.time);
		_timer = (TextView) findViewById(R.id.timer);
		_resultLayout = findViewById(R.id.result_layout);
	}
	
	private OnItemClickListener OriginTextView_OnItemClick = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			processInput(view, Station.Origin, Station.Destination);
		}
	};
	
	private OnItemClickListener DestinationTextView_OnItemClick = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			processInput(view, Station.Destination, Station.Origin);
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
				processInput(view, Station.Origin, Station.Destination);
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
				processInput(view, Station.Destination, Station.Origin);
				return true;
			}
			
			return false;
		}
	};
	
	private void processInput(View view, Station station, Station otherStation)
	{
		String stationName = ((TextView) view).getText().toString().replaceAll("\\s","").toLowerCase(Locale.US);
		
		if (Planner.getInstance(getApplicationContext()).setStation(station, stationName))
		{
			if (Planner.getInstance(getApplicationContext()).hasSetStation(otherStation))
			{
				if (!Planner.getInstance(getApplicationContext()).getStation(station).equals(Planner.getInstance(getApplicationContext()).getStation(otherStation)))
				{
					if (Planner.getInstance(getApplicationContext()).getRoute())
					{
						ShowResult();
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
	
	private void ShowResult()
	{
		// DUMMY LAST TRAIN
		Calendar calendar = Calendar.getInstance();
		int hours = calendar.get(Calendar.HOUR);
		int minutes = calendar.get(Calendar.MINUTE) + 3;
		// DUMMY LAST TRAIN
		
		_time.setText(hours + ":" + minutes);
		
		_resultLayout.setVisibility(View.VISIBLE);
	}
	
	public class MyTimer extends CountDownTimer
    {
		public MyTimer(long millisInFuture, long countDownInterval)
		{
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onFinish()
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void onTick(long millisUntilFinished)
		{
			// TODO Auto-generated method stub
		}
    }
}