package com.appdynamics.extensions.jmx.metrics.processor;

import com.appdynamics.extensions.jmx.metrics.MetricDetails;

import javax.management.Attribute;
import java.util.Map;

import static com.appdynamics.extensions.jmx.utils.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class MapMetricsProcessor {

    static MetricDetails setMetricDetailsForMapMetrics(MetricDetails metricDetails) {
        String attributeName = metricDetails.getAttribute().getName();
        Map attributesFound = (Map) metricDetails.getAttribute().getValue();
        for (Object metricNameKey : attributesFound.keySet()) {
            String key = attributeName + PERIOD + metricNameKey.toString();
            Object attributeValue = attributesFound.get(metricNameKey);
            Attribute attribute1 = new Attribute(key, attributeValue);
            metricDetails.setAttribute(attribute1);
            metricDetails = JMXMetricsDataFilter.checkObjectType(metricDetails);
        }
        return metricDetails;
    }

}
