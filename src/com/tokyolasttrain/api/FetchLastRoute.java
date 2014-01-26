package com.tokyolasttrain.api;

import java.io.IOException;

import com.tokyolasttrain.api.HyperdiaApi.LastRoute;

public class FetchLastRoute extends NetworkTask<Void, Void, LastRoute> {

	private String originStation;
	private String destinationStation;
	
	@Override
	protected LastRoute doNetworkAction() throws IOException {
		HyperdiaApi api = new HyperdiaApi();
		
		return api.GetLastRouteFor(originStation, destinationStation);
	}

	public void setDestinationStation(String destinationStation) {
		this.destinationStation = destinationStation;
	}

	public void setOriginStation(String originStation) {
		this.originStation = originStation;
	}

}
