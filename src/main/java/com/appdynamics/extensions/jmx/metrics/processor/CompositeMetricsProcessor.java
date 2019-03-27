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
import javax.management.openmbean.CompositeData;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.appdynamics.extensions.jmx.utils.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
class CompositeMetricsProcessor implements JMXMetricProcessor {

    public CompositeMetricsProcessor() {
    }

//    static List<Metric> setMetricDetailsForCompositeMetrics(MetricDetails metricDetails, Attribute attribute) {
//        String attributeName = attribute.getName();
//        CompositeData metricValue = (CompositeData) attribute.getValue();
//        Set<String> attributesFound = metricValue.getCompositeType().keySet();
//        List<Metric> metricList = new ArrayList<Metric>();
//
//        for (String str : attributesFound) {
//            String key = attributeName + PERIOD + str;
//            if (metricDetails.getMetricPropsPerMetricName().containsKey(key)) {
//                Object attributeValue = metricValue.get(str);
//                Attribute attribute1 = new Attribute(key, attributeValue);
//                // TODO please check this if attributeValue can be of any other type. If not sure then I think it would be better to check the type again. We can discuss this
//                metricList.add(BaseMetricsProcessor.setMetricDetailsForBaseMetrics(metricDetails, attribute1));
//            }
//        }
//        return metricList;
//    }

    @Override
    public List<Metric> populateMetricsFromEntity(MetricDetails metricDetails, Attribute attribute) {
        String attributeName = attribute.getName();
        CompositeData metricValue = (CompositeData) attribute.getValue();
        Set<String> attributesFound = metricValue.getCompositeType().keySet();
        List<Metric> metricList = new ArrayList<Metric>();

        for (String str : attributesFound) {
            String key = attributeName + PERIOD + str;
            if (metricDetails.getMetricPropsPerMetricName().containsKey(key)) {
                Object attributeValue = metricValue.get(str);
                Attribute attribute1 = new Attribute(key, attributeValue);
                BaseMetricsProcessor baseMetricsProcessor = new BaseMetricsProcessor();
                // TODO please check this if attributeValue can be of any other type. If not sure then I think it would be better to check the type again. We can discuss this
                metricList.addAll(baseMetricsProcessor.populateMetricsFromEntity(metricDetails, attribute1));
            }
        }
        return metricList;
    }
}