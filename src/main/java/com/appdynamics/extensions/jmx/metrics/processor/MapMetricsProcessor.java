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
import java.util.Map;

import static com.appdynamics.extensions.jmx.utils.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
class MapMetricsProcessor {

    // TODO should not use raw types, please change wherever applicable
    static List<Metric> setMetricDetailsForMapMetrics(MetricDetails metricDetails, Attribute attribute) {
        List<Metric> metricList = new ArrayList<Metric>();

        String attributeName = attribute.getName();
        Map attributesFound = (Map) attribute.getValue();
        for (Object metricNameKey : attributesFound.keySet()) {
            String key = attributeName + PERIOD + metricNameKey.toString();
            Object attributeValue = attributesFound.get(metricNameKey);
            Attribute attribute1 = new Attribute(key, attributeValue);
            metricList.addAll(JMXMetricsDataFilter.checkAttributeTypeAndSetDetails(metricDetails, attribute1));
        }
        return metricList;
    }

}
