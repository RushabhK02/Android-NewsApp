package com.example.hw9.ui.trending;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hw9.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrendingFragment extends Fragment {

    LineChart chart;
    String keyword="";
//    String url = "http://10.0.2.2:8080"+"/trends?keyword=";
    String url = "https://wt-hw9-server.wl.r.appspot.com"+"/trends?keyword=";;
    RequestQueue queue;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trending, container, false);
        final Context context = root.getContext();
        queue = Volley.newRequestQueue(context);
        final EditText editText = (EditText) root.findViewById(R.id.search_keyword);

         chart = root.findViewById(R.id.chart);
         chart.setExtraOffsets(0, 10, 0, 0);
         chart.getXAxis().setDrawGridLines(false);
         chart.getAxisLeft().setDrawGridLines(false);
         chart.getAxisRight().setDrawGridLines(false);

         chart.getAxisLeft().setAxisLineColor(R.color.cleanWhite);
         chart.getAxisRight().setTextSize(12);
         chart.getAxisLeft().setTextSize(12);
         chart.getAxisLeft().setDrawGridLines(false);
         chart.getAxisLeft().setDrawAxisLine(false);
         chart.getAxisLeft().setDrawZeroLine(false);
         chart.setDrawBorders(false);
         chart.setDrawGridBackground(false);

         chart.getXAxis().setTextSize(12);

         Description desc = chart.getDescription();
         desc.setText("Description Label");
         desc.setTextSize(12);
         chart.getDescription().setEnabled(true);
         chart.setPinchZoom(true);
         chart.setTouchEnabled(true);
         chart.getLegend().setTextSize(20);

         chart.getAxisRight().setAxisLineWidth(2f);
         chart.getAxisRight().setAxisLineColor(R.color.chartAxis);
         chart.getXAxis().setAxisLineWidth(1f);
         chart.getXAxis().setAxisLineColor(R.color.chartAxis);
         makeTrendRequest("Coronavirus");

        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                makeTrendRequest(editText.getText().toString());
//                Log.i(TAG, "onEditorAction: "+editText.getText());
                return false;
            }
        });
        return root;

    }

    private void makeTrendRequest(String keyword){
        if(!this.keyword.equals(keyword)) this.keyword = keyword;
        else return;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url+keyword, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray trend = response.getJSONArray("trend");
                            ArrayList<Integer> data = new ArrayList<>();
                            for(int i=0; i<trend.length();i++){
                                data.add(trend.getInt(i));
                            }
                            setChartTrends(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error getting trends!",error.getMessage());
                    }
                });

        queue.add(jsonObjectRequest);
    }

    private void setChartTrends(ArrayList<Integer> list){
        Log.i("setChartTrends", String.valueOf(list));
        List<Entry> valsComp1 = new ArrayList<Entry>();
        for(Integer el:list){
            valsComp1.add(new Entry(valsComp1.size(),el));
        }

        LineDataSet setComp1 = new LineDataSet(valsComp1, "Trending Chart for "+this.keyword);
        setComp1.setAxisDependency(YAxis.AxisDependency.RIGHT);
        setComp1.setColor(R.color.trendingColor);
        setComp1.setCircleColor(R.color.trendingColor);
        setComp1.setCircleHoleColor(R.color.trendingColor);
        setComp1.setValueTextSize(10);
        setComp1.setFormSize(15);
        setComp1.setLineWidth(1.5f);
//        setComp1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        setComp1.setValueTextColor(R.color.trendingColor);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);
        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.setScaleX(1f);
        chart.centerViewTo(1.5f, 7.5f, YAxis.AxisDependency.RIGHT);
        chart.invalidate();
    }
}
