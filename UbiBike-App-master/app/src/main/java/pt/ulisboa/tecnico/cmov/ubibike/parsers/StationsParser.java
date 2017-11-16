package pt.ulisboa.tecnico.cmov.ubibike.parsers;

import java.util.HashMap;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.ubibike.domain.BikeStation;

/**
 * Created by Ivopires on 17/04/16.
 */
public class StationsParser {

    private Map<String, BikeStation> allStations;

    public StationsParser(String stationsToParse)  {
        allStations = new HashMap<>();
        parse(stationsToParse);
    }

    public Map<String, BikeStation> getAllStations(){
        return this.allStations;
    }

    private void parse(String stationsToParse){
        String stationName;
        double longitude;
        double latitude;
        int bikes;

        for(String station :stationsToParse.split(";")) {
            String[] args = station.split(",", -1);
            stationName = args[0];
            longitude = Double.parseDouble(args[1]);
            latitude = Double.parseDouble(args[2]);
            bikes = Integer.parseInt(args[3]);

            this.allStations.put(stationName, new BikeStation(longitude, latitude, stationName, bikes));
        }
    }
}
