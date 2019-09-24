package com.zhongbenshuo.air.bean;

/**
 * 显示在图表数据的实体类
 * Created at 2019/9/24 19:29
 *
 * @author LiYuliang
 * @version 1.0
 */

public class RealData {

    private String chartName;

    private float value;

    private String unit;

    public RealData(String chartName, float value, String unit) {
        this.chartName = chartName;
        this.value = value;
        this.unit = unit;
    }

    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
