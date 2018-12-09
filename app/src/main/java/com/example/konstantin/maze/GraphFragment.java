package com.example.konstantin.maze;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.List;

import static com.example.konstantin.maze.MazeTestView.getStepCounter;

public class GraphFragment extends Fragment {
    DataPoint[] dataPoints;
    List<Integer> listOfMaxValue;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        dataPoints = new DataPoint[3];
        listOfMaxValue = new ArrayList<>();
        populateGraphView(view);



        Button refreshButton = view.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 getFragmentManager()
                       .beginTransaction()
                       .detach(GraphFragment.this)
                       .attach(GraphFragment.this)
                       .commit();
            }
        });

        return view;
    }

    void populateGraphView(View view) {
        for (int i = 0; i < dataPoints.length; i++) {
            dataPoints[i] = new DataPoint(i + 1,getStepCounter(i));
            listOfMaxValue.add(getStepCounter(i));
        }
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints);
        GraphView graphView = new GraphView(getActivity());
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(dataPoints.length + 1);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(Math.max(listOfMaxValue.get(0),listOfMaxValue.get(listOfMaxValue.size() - 1)) + 2);
        graphView.getGridLabelRenderer().setNumVerticalLabels(dataPoints.length + 1);
        graphView.addSeries(series);
        series.setSpacing(50);
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.BLACK);

        try {
            LinearLayout layout = view.findViewById(R.id.graph1);
            layout.addView(graphView);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

}
