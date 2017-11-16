package cmov.test;

import java.util.HashMap;
import java.util.Map;

public class BikeStationManager {
	
	private Map<String, BikeStation> bikeStations;
	private int numberOfUbibikeBikes = 0; 
	
	public BikeStationManager(){
		this.bikeStations = new HashMap<String, BikeStation>();
	}
	
	public void addBikeStation(BikeStation bikeStation){
		
		int numberOfBikes = bikeStation.getBikesAvailable();
		for(int i=0; i<numberOfBikes; i++){
			numberOfUbibikeBikes++;
		}
		
		this.bikeStations.put(bikeStation.getStationName(), bikeStation);
	}
	
	public BikeStation getBikeStationByName(String bikeStationName){
		return this.bikeStations.get(bikeStationName);
	}
	
	public boolean existsBikeStation(String bikeStationName){
		return this.bikeStations.containsKey(bikeStationName);
	}
	
	public String bookBikeByStation(String bikeStationName){
		return this.getBikeStationByName(bikeStationName).bookBike();
	}
	
	public String pickInformStationByName(String bikeStationName, String hasReservation) {
		return this.getBikeStationByName(bikeStationName).removeBikeFromStation(hasReservation);
	}
	
	public String deliverInformStationByName(String bikeStationName) {
		return this.getBikeStationByName(bikeStationName).addBikeToStation();
	}
	
	public String getAllBikeStations(){
		String allStations = "";
		for(BikeStation bs : bikeStations.values()){
			String bikeStationName = bs.getStationName();
			double longitude = bs.getLongitude();
			double latitude = bs.getLatitude();
			int bikes = bs.getBikesAvailable();
			allStations = allStations + bikeStationName + "," + longitude + "," + latitude + "," + bikes + ";";
		}
		return allStations;
	}

}
