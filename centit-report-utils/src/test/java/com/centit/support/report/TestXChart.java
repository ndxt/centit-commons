package com.centit.support.report;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.colors.ChartColor;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class TestXChart {
    public static void main(String[] args) {

        // 创建图表
        CategoryChart chart = new CategoryChartBuilder()
            .width(1000)
            .height(700)
            .title("公司年度收入构成分析")
            .xAxisTitle("年份")
            .yAxisTitle("收入（百万元）")
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

        // 数据
        List<String> years = Arrays.asList("2020", "2021", "2022", "2023");

        // 各业务线收入数据
        List<Number> productSales = Arrays.asList(45, 52, 60, 68);
        List<Number> serviceRevenue = Arrays.asList(25, 30, 35, 42);
        List<Number> consultingRevenue = Arrays.asList(15, 18, 22, 28);
        List<Number> otherRevenue = Arrays.asList(5, 8, 10, 12);

        // 添加系列并自定义颜色
        chart.addSeries("产品销售", years, productSales)
            .setFillColor(new Color(65, 105, 225)); // 蓝色
        chart.addSeries("服务收入", years, serviceRevenue)
            .setFillColor(new Color(34, 139, 34)); // 绿色
        chart.addSeries("咨询收入", years, consultingRevenue)
            .setFillColor(new Color(255, 140, 0)); // 橙色
        chart.addSeries("其他收入", years, otherRevenue)
            .setFillColor(new Color(148, 0, 211)); // 紫色

        // 显示图表
        new SwingWrapper<>(chart).displayChart();
    }
}
