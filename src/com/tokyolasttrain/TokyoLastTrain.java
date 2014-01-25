package com.tokyolasttrain;

import android.app.Application;
import android.content.Context;


public class TokyoLastTrain extends Application {

	private static Context context;
	
	public static Context getAppContext() { return TokyoLastTrain.context; }
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		TokyoLastTrain.context = getApplicationContext();
	}
}
