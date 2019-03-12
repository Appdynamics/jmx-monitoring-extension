package com.appdynamics.extensions.jmx.metrics.processor;

import com.appdynamics.extensions.jmx.metrics.MetricDetails;
import com.appdynamics.extensions.metrics.Metric;

import javax.management.Attribute;
import javax.management.openmbean.CompositeData;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.appdynamics.extensions.jmx.utils.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
class CompositeMetricsProcessor {

    static List<Metric> setMetricDetailsForCompositeMetrics(MetricDetails metricDetails, Attribute attribute) {
        String attributeName = attribute.getName();
        CompositeData metricValue = (CompositeData) attribute.getValue();
        Set<String> attributesFound = metricValue.getCompositeType().keySet();
        List<Metric> metricList = new ArrayList<Metric>();

        for (String str : attributesFound) {
            String key = attributeName + PERIOD + str;
            if (metricDetails.getMetricPropsPerMetricName().containsKey(key)) {
                Object attributeValue = metricValue.get(str);
                Attribute attribute1 = new Attribute(key, attributeValue);
                metricList.add(BaseMetricsProcessor.setMetricDetailsForBaseMetrics(metricDetails, attribute1));
            }
        }
        return metricList;
    }
}