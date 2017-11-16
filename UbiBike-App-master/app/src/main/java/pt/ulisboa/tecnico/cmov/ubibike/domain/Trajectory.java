package pt.ulisboa.tecnico.cmov.ubibike.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivopires on 07/05/16.
 */
public class Trajectory {

    private List<String> trajectoryLong = new ArrayList<>();
    private List<String> trajectoryLat = new ArrayList<>();

    public void addGPSPoint(Double trajectoryLat, Double trajectoryLong){
        this.trajectoryLat.add(String.valueOf(trajectoryLong));
        this.trajectoryLong.add(String.valueOf(trajectoryLat));
    }

    public List<String> getTrajectoryLong(){return this.trajectoryLong;}
    public List<String> getTrajectoryLat(){return this.trajectoryLat;}

    public float getDistance(){

        int destination = 1;
        String startLat = "", startLong = "", endLat = "", endLong = "";
        double startLatitude, startLongitude, endLatitude, endLongitude, dLng, dLat, a, c;
        double earthRadius = 6371000;
        float fullDistance = 0.0f;

        for (int start = 0; start < this.trajectoryLat.size()-1; start++){
            startLat = this.trajectoryLat.get(start);
            startLong = this.trajectoryLong.get(start);
            endLat = this.trajectoryLat.get(destination);
            endLong = this.trajectoryLong.get(destination);

            startLatitude = Double.parseDouble(startLat);
            startLongitude = Double.parseDouble(startLong);

            endLatitude = Double.parseDouble(endLat);
            endLongitude = Double.parseDouble(endLong);

            dLng = Math.toRadians(endLatitude-startLatitude); //dLat
            dLat = Math.toRadians(endLongitude-startLongitude); //dLng
            a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(Math.toRadians(startLatitude)) * Math.cos(Math.toRadians(endLatitude)) *
                            Math.sin(dLng/2) * Math.sin(dLng/2);
            c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            fullDistance += (float) (earthRadius * c);

            destination++;
        }
        return fullDistance;
       /* startLatitude = Double.parseDouble(startLat);
        startLongitude = Double.parseDouble(startLong);

        endLatitude = Double.parseDouble(endLat);
        endLongitude = Double.parseDouble(endLong); */

        /*double earthRadius = 6371000; //meters
        double dLng = Math.toRadians(endLatitude-startLatitude); //dLat
        double dLat = Math.toRadians(endLongitude-startLongitude); //dLng
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(startLatitude)) * Math.cos(Math.toRadians(endLatitude)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);*/
        /*float[] dist = new float[1];
        Barcode.GeoPoint geoPoint1 = new Barcode.GeoPoint();
        Barcode.GeoPoint geoPoint2 = new Barcode.GeoPoint();
        geoPoint1.lat = Double.parseDouble(getTrajectoryLong().get(0));
        geoPoint1.lng = Double.parseDouble(getTrajectoryLat().get(0));
        geoPoint2.lat = Double.parseDouble(getTrajectoryLong().get(getTrajectoryLong().size()-1));
        geoPoint2.lng = Double.parseDouble(getTrajectoryLat().get(getTrajectoryLat().size()-10));
        double lat1 = (geoPoint1.lat);
        double lng1 = (geoPoint1.lng);
        double lat2 = (geoPoint2.lat);
        double lng2 = (geoPoint2.lng);

        Location.distanceBetween(lat1, lng1, lat2, lng2, dist);

        return dist[0];*/
    }

}
