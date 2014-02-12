package com.tokyolasttrain.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.tokyolasttrain.api.HyperdiaApi.LastRoute;

public class Planner
{
	private static Planner instance = null;
	
	public enum Station { Origin, Destination };
	
	private static final int MINUTES_TO_ALARM = 15;
	
	private Map<String, String> _stations;
	private String _originStation, _destinationStation;
	
	private LastRoute _lastRoute;
	private boolean _notifyUser = false;
	
	private Planner(Context context)
	{
		_stations = Reader.getStations(context);
	}
	
	public static Planner getInstance(Context context)
	{
		if(instance == null)
		{
			instance = new Planner(context);
		}
		return instance;
	}
	
	public List<String> getStationList()
	{
		return new ArrayList<String>(_stations.values());
	}
	
	public boolean setStation(Station station, String stationName)
	{
		if (!isStationValid(stationName))
		{
			return false;
		}
		
		switch (station)
		{
		case Origin:
			_originStation = stationName;
			return true;
			
		case Destination:
			_destinationStation = stationName;
			return true;
		
		default:
			return false;
		}
	}
	
	public String getStation(Station station)
	{
		switch (station)
		{
		case Origin:
			return _originStation;
			
		case Destination:
			return _destinationStation;
		
		default:
			return "";
		}
	}
	
	public boolean hasSetStation(Station station)
	{
		switch (station)
		{
		case Origin:
			return _originStation != null;
			
		case Destination:
			return _destinationStation != null;
		
		default:
			return false;
		}
	}
	
	public void setLastRoute(LastRoute lastRoute)
	{
		_lastRoute = lastRoute;
	}
	
	public boolean hasSetLastRoute()
	{
		return _lastRoute != null;
	}
	
	public LastRoute getLastRoute()
	{
		return _lastRoute;
	}
	
	public void notifyUser(boolean notify)
	{
		_notifyUser = notify;
	}
	
	public boolean alarmOn()
	{
		return _notifyUser;
	}
	
	public long getTimeLeftForAlarm()
	{		
		return (_lastRoute.getDepartureTime().minusMinutes(MINUTES_TO_ALARM)).toDateTime().getMillis();
	}
	
	private boolean isStationValid(String station)
	{
		return _stations.containsKey(station) ? true : false;
	}
}