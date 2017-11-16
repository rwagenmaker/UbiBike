package cmov.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BikeStation {
	private double longitude;
	private double latitude;
	private String stationName;
	private int bikesAvailable;
	private int bikesBooked;
	private String ERROR_NO_BIKES_AVAILABLE = "-1";
	//private Map<String, Bike> bikesInStation;
	
	public BikeStation(double longitude, double latitude, String name,	int availableBikes){
		setLongitude(longitude);
		setLatitude(latitude);
		setStationName(name);
		setBikesAvailable(availableBikes);
		
		
	}
	
	// ===================
	// 		Getters
	// ===================
	
	public double getLongitude(){
		return this.longitude;
	}
	
	public double getLatitude(){
		return this.latitude;
	}
	
	public String getStationName(){
		return this.stationName;
	}
	
	public int getBikesAvailable(){
		return this.bikesAvailable;
	}
	

	
	
	// ===================
	// 		Setters
	// ===================
	
	public void setLongitude(double longi){
		this.longitude = longi;
	}
	
	public void setLatitude(double lat){
		this.latitude = lat;
	}
	
	public void setStationName(String name){
		this.stationName = name;
	}
	
	public void setBikesAvailable(int bikesAvailable){
		this.bikesAvailable = bikesAvailable;
	}

	
	public String bookBike(){
		if(this.bikesAvailable > 0){
			this.bikesAvailable--;
			
			return this.bikesAvailable+"";
			
		}
		else
			return ERROR_NO_BIKES_AVAILABLE + "_NoBike";
	}

	public String addBikeToStation() {
		bikesAvailable++;
		return "bikeDeliverInformDone";
	}
	
	public String removeBikeFromStation(String hasReservation) {
		
		//Verificar se a bike está nos booked ou nos available
		if (hasReservation.equals("true")) {
			bikesBooked--;
		} else {
			bikesAvailable--;
		}
		return "bikePickInformDone";
	}
	

	
}
