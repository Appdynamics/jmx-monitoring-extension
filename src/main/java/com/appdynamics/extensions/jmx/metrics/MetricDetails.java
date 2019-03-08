package com.appdynamics.extensions.jmx.metrics;

import com.appdynamics.extensions.metrics.Metric;

import javax.management.Attribute;
import javax.management.ObjectInstance;
import java.util.List;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 3/7/19.
 */
public class MetricDetails {
    private String metricPrefix;
    private List<Metric> jmxMetrics;
    private ObjectInstance instance;
    private Map<String, ?> metricPropsPerMetricName;
    private java.util.List<String> mBeanKeys;
    private String displayName;
    private Attribute attribute;


//    public MetricDetails(String metricPrefix, List<Metric> jmxMetrics, ObjectInstance instance, Map<String, ?> metricPropsPerMetricName, List<String> mBeanKeys, String displayName, Attribute attribute) {
//        this.metricPrefix = metricPrefix;
//        this.jmxMetrics = jmxMetrics;
//        this.instance = instance;
//        this.metricPropsPerMetricName = metricPropsPerMetricName;
//        this.mBeanKeys = mBeanKeys;
//        this.displayName = displayName;
//        this.attribute = attribute;
//    }

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }

    public void addToJmxMetrics(Metric metric) {
        jmxMetrics.add(metric);
    }

    public List<Metric> getJmxMetrics() {
        return jmxMetrics;
    }

    public void setJmxMetrics(List<Metric> jmxMetrics) {
        this.jmxMetrics = jmxMetrics;
    }

    public ObjectInstance getInstance() {
        return instance;
    }

    public void setInstance(ObjectInstance instance) {
        this.instance = instance;
    }

    public Map<String, ?> getMetricPropsPerMetricName() {
        return metricPropsPerMetricName;
    }

    public void setMetricPropsPerMetricName(Map<String, ?> metricPropsPerMetricName) {
        this.metricPropsPerMetricName = metricPropsPerMetricName;
    }

    public List<String> getmBeanKeys() {
        return mBeanKeys;
    }

    public void setmBeanKeys(List<String> mBeanKeys) {
        this.mBeanKeys = mBeanKeys;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public static class Builder {
        private MetricDetails task = new MetricDetails();

        Builder metricPrefix(String metricPrefix) {
            task.metricPrefix = metricPrefix;
            return this;
        }

        Builder jmxMetrics(List<Metric> jmxMetrics) {
            task.jmxMetrics = jmxMetrics;
            return this;
        }

        Builder instance(ObjectInstance instance) {
            task.instance = instance;
            return this;
        }

        Builder metricPropsPerMetricName(Map<String, ?> metricPropsPerMetricName) {
            task.metricPropsPerMetricName = metricPropsPerMetricName;
            return this;
        }

        Builder mBeanKeys(List<String> mBeanKeys) {
            task.mBeanKeys = mBeanKeys;
            return this;
        }

        Builder displayName(String displayName) {
            task.displayName = displayName;
            return this;
        }

        Builder attribute(Attribute attribute) {
            task.attribute = attribute;
            return this;
        }

        MetricDetails build() {
            return task;
        }
    }


}
