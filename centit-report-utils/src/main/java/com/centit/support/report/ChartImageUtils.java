package com.centit.support.report;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.common.ObjectException;
import org.knowm.xchart.*;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.colors.ChartColor;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.style.markers.XChartSeriesMarkers;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public abstract class ChartImageUtils {
    public static final String CHART_TYPE_BAR = "bar";
    public static final String CHART_TYPE_LINE = "line";
    public static final String CHART_TYPE_PIE = "pie";

    public static final Color[] COLORS = new Color[]{
        new Color(0xFF, 0x00, 0x00),
        new Color(0x00, 0xFF, 0x00),
        new Color(0x00, 0x00, 0xFF),
        new Color(0x00, 0x80, 0x80),
        new Color(0x00, 0xFF, 0xFF),
        new Color(150, 60, 103),
        new Color(0xFF, 0x00, 0xFF),
        new Color(0xFF, 0x80, 0x00),
        new Color(0x80, 0x80, 0x00),
        new Color(0x80, 0x00, 0x80),
        new Color(0xFF, 0xFF, 0x00)
    };
    public  static Chart<?, ?> createChart(String chartType, String chartTitle, int width, int height, JSONObject style, JSONObject data) {
        if(CHART_TYPE_BAR.equals(chartType)) {
            return createBarChart(chartTitle, width, height, style, data);
        }
        if(CHART_TYPE_LINE.equals(chartType)) {
            return createLineChart(chartTitle, width, height, style, data);
        }
        if(CHART_TYPE_PIE.equals(chartType)) {
            return createPieChart(chartTitle, width, height, style, data);
        }
        return null;
    }

    private static List<Number> castObjectToNumbers(Object obj, String errorMessage) {
        List<Object> series = CollectionsOpt.objectToList(obj);
        if(series == null || series.isEmpty()){
            throw new ObjectException(ObjectException.PARAMETER_NOT_CORRECT, errorMessage);
        }
        List<Number> numbers = new ArrayList<>();
        for (Object o : series) {
            numbers.add(NumberBaseOpt.castObjectToNumber(o));
        }
        return numbers;
    }

    public  static XYChart createLineChart(String chartTitle, int width, int height, JSONObject style, JSONObject data){
        XYChart chart = new XYChartBuilder()
            .width(width)
            .height(height)
            .title(chartTitle)
            .xAxisTitle(style.getString("xAxisTitle"))
            .yAxisTitle(style.getString("yAxisTitle"))
            .build();

        // 高级样式配置
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideN);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(ChartColor.GREY.getColor());
        chart.getStyler().setPlotGridLinesColor(new Color(255, 255, 255));
        chart.getStyler().setChartTitleBoxBackgroundColor(new Color(0, 0, 0, 50));
        chart.getStyler().setChartTitleBoxVisible(true);
        chart.getStyler().setChartTitleBoxBorderColor(Color.BLACK);
        chart.getStyler().setAxisTickLabelsColor(Color.DARK_GRAY);
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setMarkerSize(10);

        Object obj = data.get("xData");
        List<Number> xData = castObjectToNumbers(obj, "data.xData 格式不正确, 不能作为X轴数据");

        obj = data.get("series");
        XChartSeriesMarkers seriesMarkers = new XChartSeriesMarkers();
        int seriesCount = seriesMarkers.getSeriesMarkers().length;
        if(obj instanceof JSONArray){
            JSONArray seriesArray = (JSONArray) obj;
            for(int i = 0; i < seriesArray.size(); i++){
                JSONObject series = seriesArray.getJSONObject(i);
                String name = series.getString("name");
                List<Number> yData = castObjectToNumbers(series.get("data"), "data.series 格式不正确, 不能作为数据");
                chart.addSeries(name, xData, yData)
                    .setMarker(seriesMarkers.getSeriesMarkers()[i % seriesCount])
                    .setLineColor(COLORS[i%COLORS.length]);
            }
        } else if(obj instanceof JSONObject){
            JSONObject series = (JSONObject)obj;
            String name = series.getString("name");
            List<Number> yData = castObjectToNumbers(series.get("data"), "data.series 格式不正确, 不能作为数据");
            chart.addSeries(name, xData, yData)
                .setMarker(SeriesMarkers.CIRCLE)
                .setLineColor(new Color(255, 0, 0));
        } else {
            throw new ObjectException(ObjectException.PARAMETER_NOT_CORRECT, "data.series 格式不正确, 不能作为数据系列");
        }
        // 显示图表
        return chart;
    }

    public  static PieChart createPieChart(String chartTitle, int width, int height, JSONObject style, JSONObject data){
        // 创建饼状图
        PieChart chart = new PieChartBuilder()
            .width(width)
            .height(height)
            .title(chartTitle)
            .build();
        // 自定义样式
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideN);
        /*chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
        chart.getStyler().setDefaultSeriesRenderStyle(PieSeries.PieSeriesRenderStyle.Pie);
        chart.getStyler().setPlotBackgroundColor(ChartColor.DARK_GREY.getColor());
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setChartTitleBoxBackgroundColor(new Color(0, 0, 0, 50));
        chart.getStyler().setChartTitleBoxVisible(true);
        chart.getStyler().setChartTitleBoxBorderColor(Color.BLACK);
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setMarkerSize(10);
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setPlotContentSize(.7);*/
        // 添加数据
        Object obj = data.get("series");
        if(obj instanceof JSONArray){
            JSONArray seriesArray = (JSONArray) obj;
            for(int i = 0; i < seriesArray.size(); i++){
                JSONObject series = seriesArray.getJSONObject(i);
                chart.addSeries(series.getString("name"), NumberBaseOpt.castObjectToNumber(series.get("value")));
            }
        }
        return chart;
    }

    public  static CategoryChart createBarChart(String chartTitle, int width, int height, JSONObject style, JSONObject data){
        // 创建图表
        CategoryChart chart = new CategoryChartBuilder()
            .width(width)
            .height(height)
            .title(chartTitle)
            .xAxisTitle(style.getString("xAxisTitle"))
            .yAxisTitle(style.getString("yAxisTitle"))
            .build();

        // 自定义样式
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideN);//OutsideE
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
        chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
        chart.getStyler().setStacked(true);
        chart.getStyler().setPlotBackgroundColor(ChartColor.DARK_GREY.getColor());
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setPlotGridLinesColor(new Color(220, 220, 220));
        chart.getStyler().setXAxisLabelRotation(45); // X轴标签旋转角度


        Object obj = data.get("xData");
        List<Object> xData = CollectionsOpt.objectToList(obj);
        // 数据
        if(xData == null || xData.isEmpty()){
            throw new ObjectException(ObjectException.PARAMETER_NOT_CORRECT, "data.xData is not Array, 不能作为横坐标数据");
        }

        obj = data.get("series");
        if(obj instanceof JSONArray){
            JSONArray seriesArray = (JSONArray) obj;
            for(int i = 0; i < seriesArray.size(); i++){
                JSONObject series = seriesArray.getJSONObject(i);
                String name = series.getString("name");
                List<Number> yData = castObjectToNumbers(series.get("data"),"data.series 格式不正确, 不能作为数据");
                chart.addSeries(name, xData, yData)
                    .setFillColor(COLORS[i%COLORS.length]);
            }
        } else if(obj instanceof JSONObject){
            JSONObject series = (JSONObject)obj;
            String name = series.getString("name");
            List<Number> yData = castObjectToNumbers(series.get("data"), "data.series 格式不正确, 不能作为数据");
            chart.addSeries(name, xData, yData)
                .setFillColor(new Color(255, 0, 0));
        } else {
            throw new ObjectException(ObjectException.PARAMETER_NOT_CORRECT, "data.series 格式不正确, 不能作为数据系列");
        }

        return chart;
    }

    public static BufferedImage createChartImage(String chartType, String chartTitle, int width, int height, JSONObject style, JSONObject data) {
        Chart<?,?> chart = createChart(chartType, chartTitle, width, height, style, data);
        if(chart == null) return null;
        return BitmapEncoder.getBufferedImage(chart);
    }
}
