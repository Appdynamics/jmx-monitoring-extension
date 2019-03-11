package com.appdynamics.extensions.jmx.metrics.processor;

import com.appdynamics.extensions.jmx.metrics.MetricDetails;

import javax.management.Attribute;
import java.util.List;

import static com.appdynamics.extensions.jmx.utils.JMXUtil.getMetricAfterCharacterReplacement;
import static com.appdynamics.extensions.jmx.utils.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class ListMetricsProcessor {

    static MetricDetails setMetricDetailsForListMetrics(MetricDetails metricDetails) {
        String attributeName = metricDetails.getAttribute().getName();
        List attributesFound = (List) metricDetails.getAttribute().getValue();
        for (Object metricNameKey : attributesFound) {
            Attribute listMetric = getListMetric(metricNameKey, metricDetails);
            String key = attributeName + PERIOD + listMetric.getName();
            Object attributeValue = listMetric.getValue();
            Attribute attribute1 = new Attribute(key, attributeValue);
            metricDetails.setAttribute(attribute1);
            metricDetails = JMXMetricsDataFilter.checkObjectType(metricDetails);
        }

        return metricDetails;
    }

    private static Attribute getListMetric(Object metricKey, MetricDetails metricDetails) {
        String[] arr = metricKey.toString().split(metricDetails.getSeparator());
        String key = arr[0].trim();
        String value = arr[1].trim();
        key = getMetricAfterCharacterReplacement(key, metricDetails.getMetricCharacterReplacer());
        value = getMetricAfterCharacterReplacement(value, metricDetails.getMetricCharacterReplacer());
        return new Attribute(key, value);
    }

}
