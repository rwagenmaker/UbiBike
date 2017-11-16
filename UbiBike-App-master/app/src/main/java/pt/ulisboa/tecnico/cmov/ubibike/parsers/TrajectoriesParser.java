package pt.ulisboa.tecnico.cmov.ubibike.parsers;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.ubibike.domain.Trajectory;

/**
 * Created by Ivopires on 09/05/16.
 */
public class TrajectoriesParser {

    List<Trajectory> alltrajectoriesUpdated;

    public TrajectoriesParser(String trajectoriesUpdated){
        alltrajectoriesUpdated = new ArrayList<>();
        parse(trajectoriesUpdated);
    }

    public List<Trajectory> getAllTrajectories(){
        return this.alltrajectoriesUpdated;
    }

    private void parse(String trajectoriesUpdated) {
        double longitude;
        double latitude;
        int trajIndex = 0;

        Log.d("Traj", trajectoriesUpdated + "dfghjk");

        if(trajectoriesUpdated.contains("|")) {
            for (String route : trajectoriesUpdated.split("\\|", -1)) {
                this.alltrajectoriesUpdated.add(new Trajectory());
                for (String gpsPoint : route.split(";", -1)) {
                    String[] coordinates = gpsPoint.split(",", -1);
                    longitude = Double.parseDouble(coordinates[0]);
                    latitude = Double.parseDouble(coordinates[1]);
                    Log.d("Longitude", coordinates[0]);
                    Log.d("Latitude", coordinates[1]);
                    this.alltrajectoriesUpdated.get(trajIndex).addGPSPoint(latitude, longitude);
                }
                trajIndex++;
            }
        }else{
            this.alltrajectoriesUpdated.add(new Trajectory());

            for (String gpsPoint : trajectoriesUpdated.split(";", -1)) {
                String[] coordinates = gpsPoint.split(",", -1);
                longitude = Double.parseDouble(coordinates[0]);
                latitude = Double.parseDouble(coordinates[1]);

                this.alltrajectoriesUpdated.get(trajIndex).addGPSPoint(latitude, longitude);
            }
        }
    }

}
