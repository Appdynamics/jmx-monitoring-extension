package com.appdynamics.extensions.jmx.metrics.processor;

import com.appdynamics.extensions.jmx.metrics.MetricDetails;
import com.appdynamics.extensions.metrics.Metric;

import javax.management.Attribute;
import java.util.ArrayList;
import java.util.List;

import static com.appdynamics.extensions.jmx.utils.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
class ListMetricsProcessor {

    static List<Metric> setMetricDetailsForListMetrics(MetricDetails metricDetails, Attribute attribute) {
        List<Metric> metricList = new ArrayList<Metric>();

        String attributeName = attribute.getName();
        List attributeValuesFromList = (List) attribute.getValue();
        for (Object metricNameKey : attributeValuesFromList) {
            Attribute listMetric = getListMetric(metricNameKey, metricDetails);
            String key = attributeName + PERIOD + listMetric.getName();
            Object attributeValue = listMetric.getValue();
            Attribute attribute1 = new Attribute(key, attributeValue);
            metricList.addAll(JMXMetricsDataFilter.checkAttributeTypeAndSetDetails(metricDetails, attribute1));
        }
        return metricList;
    }

    private static Attribute getListMetric(Object metricKey, MetricDetails metricDetails) {
        String[] arr = metricKey.toString().split(metricDetails.getSeparator());
        String key = arr[0].trim();
        String value = arr[1].trim();
        return new Attribute(key, value);
    }

}
