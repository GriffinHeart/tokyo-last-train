package com.tokyolasttrain.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;

public class Planner
{
	private static Planner instance = null;
	
	public enum Station { Origin, Destination };
	
	private Map<String, String> _stations;
	private String _originStation, _destinationStation;
	
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
	
	public boolean getRoute()
	{
		return true;
	}
	
	private boolean isStationValid(String station)
	{
		return _stations.containsKey(station) ? true : false;
	}
}