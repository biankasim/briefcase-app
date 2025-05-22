package sk.tuke.bakalarka.tools;

import static sk.tuke.bakalarka.tools.ParseTools.parseDateToString;

import android.graphics.Color;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateField;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.Query;

import java.text.DecimalFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ChartVisualizer {
    private View view;
    private PieChart usageChart;
    private BarChart spendingsChart;

    private HashMap<Integer, Double> spendings;
    private int completionCount;

    private final int currentMonth;
    private final int currentYear;
    public ChartVisualizer() {
        spendings = new HashMap<>();
        completionCount = 0;
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void setSpendingsChart(BarChart spendingsChart) {
        this.spendingsChart = spendingsChart;
    }

    public void setUsageChart(PieChart usageChart) {
        this.usageChart = usageChart;
    }

    public View getView() {
        return view;
    }

    public BarChart getSpendingsChart() {
        return spendingsChart;
    }

    public PieChart getUsageChart() {
        return usageChart;
    }

    public void initPieChart(){
        usageChart.setExtraOffsets(10, 5, 10, 5);
        usageChart.setUsePercentValues(true);
        usageChart.getDescription().setEnabled(false);
        usageChart.getLegend().setEnabled(false);
        usageChart.setRotationEnabled(false);
        usageChart.setRotationAngle(0);
        usageChart.setHighlightPerTapEnabled(true);
        usageChart.setDrawHoleEnabled(false);
        usageChart.setEntryLabelColor(Color.BLACK);
        usageChart.animateXY(1500, 1500);

    }
    public void showUsageChart(int percentage) {
        initPieChart();

        List<PieEntry> yvalues = new ArrayList<>();
        yvalues.add(new PieEntry(percentage, "worn"));
        yvalues.add(new PieEntry(100-percentage, "not worn"));

        PieDataSet dataSet = new PieDataSet(yvalues, "");

        ArrayList<String> xVals = new ArrayList<>();

        xVals.add("worn");
        xVals.add("not worn");

        dataSet.setSliceSpace(5f);
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.parseColor("#C4FFFF"));
        PieData data = new PieData(dataSet);

        data.setValueFormatter(new PercentFormatter(usageChart));
        usageChart.setUsePercentValues(true);


        usageChart.setData(data);
        usageChart.setEntryLabelTextSize(16f);
        usageChart.setEntryLabelColor(Color.parseColor("#C4FFFF"));
        int[] colors = {Color.parseColor("#167001"), Color.parseColor("#B00000")};
        dataSet.setColors(ColorTemplate.createColors(colors));
        usageChart.invalidate();
        usageChart.refreshDrawableState();
    }



    private void initBarChart(){
        spendingsChart.setDrawGridBackground(false);
        spendingsChart.setDrawBarShadow(false);
        spendingsChart.setDrawBorders(false);

        //remove the description label text located at the lower right corner
        Description description = new Description();
        description.setEnabled(false);
        spendingsChart.setDescription(description);

        //setting animation for y-axis, the bar will pop up from 0 to its value within the time we set
        spendingsChart.animateY(1000);
        //setting animation for x-axis, the bar will pop up separately within the time we set
        spendingsChart.animateX(1000);

        XAxis xAxis = spendingsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //set the horizontal distance of the grid line
        xAxis.setGranularity(1f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = spendingsChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);

        YAxis rightAxis = spendingsChart.getAxisRight();
        rightAxis.setDrawAxisLine(false);

        Legend legend = spendingsChart.getLegend();
        legend.setEnabled(false);

    }



    public void getSpendingInMonth(Query query, int currentYear, int currentMonth) {
        String startOfMonth = parseDateToString(currentYear,currentMonth,1);
        String endOfMonth = parseDateToString(currentYear,currentMonth,31);

        query = query
                .whereGreaterThanOrEqualTo("date_of_purchase", startOfMonth)
                .whereLessThanOrEqualTo("date_of_purchase", endOfMonth);

        AggregateQuery sumQuery = query.aggregate(AggregateField.sum("purchasePrice"));
        sumQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    AggregateQuerySnapshot snapshot = task.getResult();
                    Double doubleSum = Double.valueOf(snapshot.get(AggregateField.sum("purchasePrice")).toString());

                    spendings.put(currentMonth, doubleSum);
                    completionCount++;
                    //if all 12 months are set, show bar chart
                    if (completionCount == 12) {
                        showSpendingsYearBarChart();
                        completionCount = 0;
                    }
                }else{
                    spendings.put(currentMonth, 0.00);
                    completionCount++;
                }
            }
        });
    }

    public void getSpendingByMonths(Query query) {
        for(int i=0; i<12; i++) {
            YearMonth yearMonth = YearMonth.of(currentYear, currentMonth).minusMonths(i);
            getSpendingInMonth(query,yearMonth.getYear(),yearMonth.getMonthValue());
        }
    }

    public void showSpendingsYearBarChart() {
        initBarChart();

        ArrayList<Double> valueList = new ArrayList<Double>();
        ArrayList<BarEntry> entries = new ArrayList<>();
        String title = "Money spent";

        final ArrayList<String> xAxisLabel = new ArrayList<>();

        //input data 12 months to past
        for(int month = currentMonth + 1; month <= 12; month++) {
            valueList.add(spendings.get(month));
            xAxisLabel.add(String.valueOf(currentYear-1)+"/"+String.valueOf(month));

        }
        //this year months
        for(int month = 1; month <= currentMonth; month++) {
            valueList.add(spendings.get(month));
            xAxisLabel.add(String.valueOf(currentYear)+"/"+String.valueOf(month));
        }


        //fit the data into a bar
        for (int i = 0; i < valueList.size(); i++) {
            BarEntry barEntry = new BarEntry(i, Float.parseFloat(String.valueOf(valueList.get(i))));
            entries.add(barEntry);
        }


        BarDataSet barDataSet = new BarDataSet(entries, title);
        barDataSet.setValueTextSize(8f);
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DecimalFormat decimalFormat = new DecimalFormat("###,###,##0.00");
                return decimalFormat.format(value);
                //return super.getFormattedValue(value);

            }
        });
        barDataSet.setValueTextColor(Color.parseColor("#C4FFFF"));

        spendingsChart.getAxisLeft().setTextColor(Color.parseColor("#FFFF97"));
        spendingsChart.getAxisRight().setTextColor(Color.parseColor("#FFFF97"));
        spendingsChart.getXAxis().setTextColor(Color.parseColor("#FFFF97"));
        spendingsChart.getLegend().setTextColor(Color.parseColor("#FFFF97"));
        spendingsChart.getDescription().setTextColor(Color.parseColor("#FFFF97"));


        XAxis xAxis = spendingsChart.getXAxis();
        xAxis.setCenterAxisLabels(false);


        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));
        xAxis.setLabelCount(xAxisLabel.size(), false);
        xAxis.setLabelRotationAngle(-60);
        BarData data = new BarData(barDataSet);
        spendingsChart.setData(data);
        spendingsChart.invalidate();
        spendingsChart.refreshDrawableState();
    }
}
