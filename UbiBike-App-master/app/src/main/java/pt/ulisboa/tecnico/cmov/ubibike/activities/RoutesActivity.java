package pt.ulisboa.tecnico.cmov.ubibike.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.adapters.RouteListAdapter;
import pt.ulisboa.tecnico.cmov.ubibike.domain.Trajectory;
import pt.ulisboa.tecnico.cmov.ubibike.storage.DataHolder;

public class RoutesActivity extends GeneralDrawerActivity {

    public final static String ROUTE_POSITION = "pt.ulisboa.tecnico.cmov.ubibike.ROUTE_POSITION";

    private List<Trajectory> trajectoryList= new ArrayList<Trajectory>();

    private ListView listView;
    private RouteListAdapter routesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        listView = (ListView) findViewById(R.id.routeList);
    }

    @Override
    protected void onResume() {
        super.onResume();

        trajectoryList = DataHolder.getInstance().getTrajectoriesUpdated();
        routesAdapter = new RouteListAdapter(this, trajectoryList);
        listView.setAdapter(routesAdapter);
        listView.setOnItemClickListener(new ListClickHandler());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                routesAdapter.notifyDataSetChanged();
            }
        });
    }

    public class ListClickHandler implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {

            Intent showRouteIntent = new Intent(RoutesActivity.this, ShowRouteActivity.class);
            showRouteIntent.putExtra(ROUTE_POSITION, position+"");

            startActivity(showRouteIntent);
        }
    }
}
