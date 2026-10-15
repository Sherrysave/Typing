package com.sjt.typing.validation;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 反作弊校验阈值配置，对应 application.yml 的 typing.validation.*。
 * 通过启动类上的 @ConfigurationPropertiesScan 注册为 Bean。
 */
@ConfigurationProperties(prefix = "typing.validation")
public class ResultValidationProperties {

    /** WPM 人类极限阈值，超过即拒绝 */
    private double maxWpm = 350;

    /** 相邻击键间隔标准差下限（毫秒），低于则疑似机器级恒定间隔，转待审 */
    private double minConsistencySdMs = 5;

    /** 挂机时长占测试总时长比例上限，超过转待审 */
    private double maxAfkRatio = 0.3;

    /** chartData 累计错误数与 charStats 错误数允许的相对偏差 */
    private double chartToleranceRatio = 0.15;

    public double getMaxWpm() {
        return maxWpm;
    }

    public void setMaxWpm(double maxWpm) {
        this.maxWpm = maxWpm;
    }

    public double getMinConsistencySdMs() {
        return minConsistencySdMs;
    }

    public void setMinConsistencySdMs(double minConsistencySdMs) {
        this.minConsistencySdMs = minConsistencySdMs;
    }

    public double getMaxAfkRatio() {
        return maxAfkRatio;
    }

    public void setMaxAfkRatio(double maxAfkRatio) {
        this.maxAfkRatio = maxAfkRatio;
    }

    public double getChartToleranceRatio() {
        return chartToleranceRatio;
    }

    public void setChartToleranceRatio(double chartToleranceRatio) {
        this.chartToleranceRatio = chartToleranceRatio;
    }
}
