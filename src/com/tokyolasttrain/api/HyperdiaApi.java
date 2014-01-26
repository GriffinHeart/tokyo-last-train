package com.tokyolasttrain.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class HyperdiaApi {

	
	public static String URL_FORMAT = 
			"http://www.hyperdia.com/cgi/search/smartphone/android_free/en/hyperdia2.cgi?search_target=route" +
			"&dep_node=%s&arv_node=%s&via_node01=&via_node02=&via_node03=&year=%s&month=%s&day=%s&hour=%s&minute=%s" +
			"&search_type=lasttrain&search_way=time&sort=time&max_route=1&sum_target=7"; 
	
	public LastRoute lastRoute;
	
	public LastRoute GetLastRouteFor(String fromStation, String toStation) {
		DateTime currentDate = new DateTime();
		DateTime midnightDate = new DateTime().withHourOfDay(0).withMinuteOfHour(0);
		DateTime threeamDate = new DateTime().withHourOfDay(3).withMinuteOfHour(0);
		Interval betweenInterval = new Interval(midnightDate, threeamDate);
		
		if(betweenInterval.contains(currentDate)) {
			currentDate = currentDate.minusDays(1).withHourOfDay(23).withMinuteOfHour(00);
		}
		
		//remove space before parenthesis
		fromStation = fromStation.replaceAll("(.*)\\s\\((.*)\\)", "$1($2)");
		toStation = toStation.replaceAll("(.*)\\s\\((.*)\\)", "$1($2)");
		try {
			String query = String.format(URL_FORMAT,
					URLEncoder.encode(fromStation, "UTF-8"),
					URLEncoder.encode(toStation, "UTF-8"),
					currentDate.getYear(),
					currentDate.getMonthOfYear(),
					currentDate.getDayOfMonth(),
					currentDate.getHourOfDay(),
					currentDate.getMinuteOfHour()
					);
			
			requestRoute(query);
			
			return new LastRoute();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			// nothing to do for now
		} 
		
		return null;
	}
	
	private void requestRoute(String query) {
		
		try {
			Document document = Jsoup.connect(query).get();
			Elements elements = document.select("table.route_table > tbody > tr");
			
			
			
			//from
			Element fromTrElement = elements.get(0);
			// Line
			Element lineTrElement = elements.get(2);
			
			//destination
			Element destinationTrElement = elements.get(elements.size()-2);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
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