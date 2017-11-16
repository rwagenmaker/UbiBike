package pt.ulisboa.tecnico.cmov.ubibike.domain;

/**
 * Created by Ivopires on 17/04/16.
 */
public class BikeStation {
    private double longitude;
    private double latitude;
    private String stationName;
    private int bikesAvailable;

    public BikeStation(double longitude, double latitude, String name,
                       int availableBikes){
        setLongitude(longitude);
        setLatitude(latitude);
        setStationName(name);
        setBikesAvailable(availableBikes);
    }

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
}
