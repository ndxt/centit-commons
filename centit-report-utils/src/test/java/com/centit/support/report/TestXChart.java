package com.centit.support.report;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.internal.chartpart.Chart;

public class TestXChart {
    public static void main(String[] args) {

        JSONObject style = new JSONObject();
        style.put("xAxisTitle", "年份");
        style.put("yAxisTitle", "收入（百万元）");

        JSONObject data = new JSONObject();
        data.put("xData", new String[]{"2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025"});

        JSONArray seriesArray = new JSONArray();
        JSONObject series = new JSONObject();
        series.put("name", "产品A收入");
        series.put("data", new Double[]{1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0, 1.0, 2.0, 3.0});
        seriesArray.add(series);
        JSONObject series2 = new JSONObject();
        series2.put("name", "产品B收入");
        series2.put("data", new Double[]{3.0, 2.0, 3.0, 1.0, 3.4, 4.5, 3.0, 2.0, 1.0, 2.0, 3.0});
        seriesArray.add(series2);
        JSONObject series3 = new JSONObject();
        series3.put("name", "产品C收入");
        series3.put("data", new Double[]{5.0, 2.0, 3.0, 4.0, 5.0, 4.0, 2.0, 2.0, 1.0, 2.0, 3.0});
        seriesArray.add(series3);
        JSONObject series4 = new JSONObject();
        series4.put("name", "产品D收入");
        series4.put("data", new Double[]{3.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 1.0, 3.0, 2.0, 3.0});
        seriesArray.add(series4);

        data.put("series", seriesArray);

        // 创建图表
        Chart<?,?> chart = ChartImageUtils.createChart(ChartImageUtils.CHART_TYPE_LINE,"公司年度收入构成分析", 1000, 700, style, data);
        // 显示图表
        new SwingWrapper<>(chart).displayChart();
    }
}
