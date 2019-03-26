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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class JMXMetricsDataFilter {
//TODO: This class could have been implemented as Factory Design Pattern(MetricsProcessorFactory). feasibility
    /*
    MetricsProcessorFactory
    An abstract class maybe MetricsProcessor
         which should be extended by BaseMetricsProcessor, ListMetricsProcessor, MapMetricsProcessor, CompositeMetricsProcessor
     */
    public static List<Metric> checkAttributeTypeAndSetDetails(MetricDetails metricDetails, Attribute attribute) {
        List<Metric> metricList = new ArrayList<Metric>();
        if (isCurrentAttributeComposite(attribute)) {
            metricList.addAll(CompositeMetricsProcessor.setMetricDetailsForCompositeMetrics(metricDetails, attribute));
        } else if (isCurrentAttributeMap(attribute)) {
            metricList.addAll(MapMetricsProcessor.setMetricDetailsForMapMetrics(metricDetails, attribute));
        } else if (isCurrentAttributeList(attribute)) {
            metricList.addAll(ListMetricsProcessor.setMetricDetailsForListMetrics(metricDetails, attribute));
        } else {
            if (metricDetails.getMetricPropsPerMetricName().containsKey(attribute.getName())) {
                metricList.add(BaseMetricsProcessor.setMetricDetailsForBaseMetrics(metricDetails, attribute));
            }
        }
        return metricList;
    }

    private static boolean isCurrentAttributeComposite(Attribute attribute) {
        return attribute.getValue() instanceof CompositeData;
    }

    private static boolean isCurrentAttributeMap(Attribute attribute) {
        return attribute.getValue().getClass().equals(Map.class) || attribute.getValue().getClass().equals(HashMap.class);
    }

    private static boolean isCurrentAttributeList(Attribute attribute) {
        return attribute.getValue().getClass().equals(List.class) || attribute.getValue().getClass().equals(Array.class) || attribute.getValue().getClass().equals(ArrayList.class);
    }
}
