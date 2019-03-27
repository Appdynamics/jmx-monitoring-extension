/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

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
class ListMetricsProcessor implements JMXMetricProcessor {

//    static List<Metric> setMetricDetailsForListMetrics(MetricDetails metricDetails, Attribute attribute) {
//        List<Metric> metricList = new ArrayList<Metric>();
//
//        String attributeName = attribute.getName();
//        List attributeValuesFromList = (List) attribute.getValue();
//        for (Object metricNameKey : attributeValuesFromList) {
//            // TODO something is not right here, you are creating an attribute whose value is already of type String,
//            //  then you are checking the attribute type. Please check this, something is definitely wrong
//            Attribute listMetric = getListMetric(metricNameKey, metricDetails);
//            String key = attributeName + PERIOD + listMetric.getName();
//            Object attributeValue = listMetric.getValue();
//            Attribute attribute1 = new Attribute(key, attributeValue);
//            metricList.addAll(JMXMetricsDataFilter.checkAttributeTypeAndSetDetails(metricDetails, attribute1));
//        }
//        return metricList;
//    }

    private static Attribute getListMetric(Object metricKey, MetricDetails metricDetails) {
        String[] arr = metricKey.toString().split(metricDetails.getSeparator());
        String key = arr[0].trim();
        String value = arr[1].trim();
        return new Attribute(key, value);
    }

    @Override
    public List<Metric> populateMetricsFromEntity(MetricDetails metricDetails, Attribute attribute) {
        List<Metric> metricList = new ArrayList<Metric>();

        String attributeName = attribute.getName();
        List attributeValuesFromList = (List) attribute.getValue();
        for (Object metricNameKey : attributeValuesFromList) {
            // TODO something is not right here, you are creating an attribute whose value is already of type String,
            //  then you are checking the attribute type. Please check this, something is definitely wrong
            Attribute listMetric = getListMetric(metricNameKey, metricDetails);
            String key = attributeName + PERIOD + listMetric.getName();
            Object attributeValue = listMetric.getValue();
            Attribute attribute1 = new Attribute(key, attributeValue);
            metricList.addAll(JMXMetricProcessor.checkTypeAndReturnMetrics(metricDetails, attribute1));
        }
        return metricList;
    }
}
