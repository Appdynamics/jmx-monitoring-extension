package com.appdynamics.extensions.jmx.metrics;

import javax.management.Attribute;
import java.util.Map;

import static com.appdynamics.extensions.jmx.metrics.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class MapMetricsProcessor {

    static void setMetricDetailsForMapMetrics(MetricDetails metricDetails) {
        String attributeName = metricDetails.getAttribute().getName();
        Map attributesFound = (Map) metricDetails.getAttribute().getValue();
        for (Object metricNameKey : attributesFound.keySet()) {
            String key = attributeName + PERIOD + metricNameKey.toString();
            Object attributeValue = attributesFound.get(metricNameKey);
            Attribute attribute1 = new Attribute(key, attributeValue);
            metricDetails.setAttribute(attribute1);
            JMXMetricsDataFilter.checkObjectType(metricDetails);
        }
    }

}
