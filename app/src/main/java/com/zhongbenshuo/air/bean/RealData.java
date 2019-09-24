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

    public RealData(String chartName, float value) {
        this.chartName = chartName;
        this.value = value;
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
}
