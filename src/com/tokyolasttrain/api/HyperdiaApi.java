package com.tokyolasttrain.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

public class HyperdiaApi {

	public static String URL_FORMAT = 
			"http://www.hyperdia.com/cgi/search/smartphone/android_free/en/hyperdia2.cgi?search_target=route" +
			"&dep_node=%s&arv_node=%s&via_node01=&via_node02=&via_node03=&year=%s&month=%s&day=%s&hour=%s&minute=%s" +
			"&search_type=lasttrain&search_way=time&sort=time&max_route=1&sum_target=7"; 
	
	public LastRoute GetLastRouteFor(String fromStation, String toStation) {
		DateTime date = new DateTime();
		
		//remove space before parenthesis
		fromStation = fromStation.replaceAll("(.*)\\s\\((.*)\\)", "$1($2)");
		toStation = toStation.replaceAll("(.*)\\s\\((.*)\\)", "$1($2)");
		try {
			String query = String.format(URL_FORMAT,
					URLEncoder.encode(fromStation, "UTF-8"),
					URLEncoder.encode(toStation, "UTF-8"),
					date.getYear(),
					date.getMonthOfYear(),
					date.getDayOfMonth(),
					date.getHourOfDay(),
					date.getMinuteOfHour()
					);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			// nothing to do for now
		}
		
		return new LastRoute();
	}
	
	public class LastRoute
	{
		public String getStation()
		{
			return "Shinjuku (JR)";
		}
		
		public String getLine()
		{
			return "Yamanote Line";
		}
		
		public LocalTime getTime()
		{
			LocalTime dummyTime = new LocalTime();
			int hour = dummyTime.getHourOfDay();
			int minutes = dummyTime.getMinuteOfHour() + 2;
			return new LocalTime(hour, minutes);
		}
	}
}