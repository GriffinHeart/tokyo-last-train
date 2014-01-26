package com.tokyolasttrain.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
		
		boolean isInSameDay = betweenInterval.contains(currentDate);
		
		if(isInSameDay) {
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
			
			if(!isInSameDay) {
				lastRoute.setDepartureTime(lastRoute.getDepartureTime().plusDays(1));
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			// nothing to do for now
		} 
		
		return lastRoute;
	}
	
	private void requestRoute(String query) {
		
		try {
			Document document = Jsoup.connect(query).get();
			Elements elements = document.select("table.route_table > tbody > tr > td > div");
			
			// time
			String fromTime = elements.get(0).text();
			
			// station
			String fromStation = elements.get(1).text();
			
			// line
			String line = elements.get(3).text();
			
			

			lastRoute = new LastRoute();
			lastRoute.setStation(fromStation);
			lastRoute.setLine(line);
			
			String[] results = fromTime.split(":");
			DateTime departureTime = new DateTime();
			departureTime.withHourOfDay(Integer.parseInt(results[0]));
			departureTime.withMinuteOfHour(Integer.parseInt(results[1]));
			lastRoute.setDepartureTime(departureTime);
			
			//destination
			//Element destinationTrElement = elements.get(elements.size()-2);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public class LastRoute
	{
		
		private String station;
		
		private String line;
		
		private DateTime departureTime;

		public String getStation() {
			return station;
		}

		public void setStation(String station) {
			this.station = station;
		}

		public String getLine() {
			return line;
		}

		public void setLine(String line) {
			this.line = line;
		}

		public DateTime getDepartureTime() {
			return departureTime;
		}

		public void setDepartureTime(DateTime departureTime) {
			this.departureTime = departureTime;
		}
		
	}
}