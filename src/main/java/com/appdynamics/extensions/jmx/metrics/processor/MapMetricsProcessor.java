package com.appdynamics.extensions.jmx.metrics.processor;

import com.appdynamics.extensions.jmx.metrics.MetricDetails;
import com.appdynamics.extensions.metrics.Metric;

import javax.management.Attribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.jmx.utils.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
class MapMetricsProcessor {

    static List<Metric> setMetricDetailsForMapMetrics(MetricDetails metricDetails, Attribute attribute) {
        List<Metric> metricList = new ArrayList<Metric>();

        String attributeName = attribute.getName();
        Map attributesFound = (Map) attribute.getValue();
        for (Object metricNameKey : attributesFound.keySet()) {
            String key = attributeName + PERIOD + metricNameKey.toString();
            Object attributeValue = attributesFound.get(metricNameKey);
            Attribute attribute1 = new Attribute(key, attributeValue);
            metricDetails.setAttribute(attribute1);
            metricList.addAll(JMXMetricsDataFilter.checkAttributeTypeAndSetDetails(metricDetails, attribute1));
        }
        return metricList;
    }

}
