package com.tokyolasttrain.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;

import android.content.Context;

public class Reader
{
	private static String loadJSONFromAssets(Context context)
	{
		String json = null;
		try
		{
			InputStream inputStream = context.getAssets().open("stations.json");

			int size = inputStream.available();
			byte[] buffer = new byte[size];
			
			inputStream.read(buffer);
			inputStream.close();
			
			json = new String(buffer, "UTF-8");
		}
		catch (IOException e) {}
		return json;
	}
	
	private static Map<String, String> parseStations(Context context)
	{
		Map<String, String> stations = new TreeMap<String, String>();
		
		String json = loadJSONFromAssets(context);
		if (json == null)
		{
			return stations;
		}
		
		try
		{
			JSONArray jsonArray = new JSONArray(json);
			for (int i = 0; i < jsonArray.length(); i++)
			{
				String station = jsonArray.getString(i);
				stations.put(station.toLowerCase(Locale.US), station);
			}
		}
		catch (Exception e) {}
		return stations;
	}
	
	public static Map<String, String> getStations(Context context)
	{
		loadJSONFromAssets(context);
		return parseStations(context);
	}
}