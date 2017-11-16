package pt.ulisboa.tecnico.cmov.ubibike.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.domain.Trajectory;

public class RouteListAdapter extends ArrayAdapter<Trajectory> {

    private Context context;
    private List<Trajectory> routes = null;

    private Trajectory route = null;


    public RouteListAdapter(Context context, List routes) {
        super(context, 0, routes);
        this.context = context;
        this.routes = routes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        route = this.routes.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.routes_rows, parent, false);
        }

        TextView routeDistance = (TextView) convertView.findViewById(R.id.distance);
        routeDistance.setText((int) route.getDistance()+" m");

        return convertView;
    }
}
