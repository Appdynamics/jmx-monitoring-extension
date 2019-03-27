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

    // TODO create a metrics processor interface with a setMetricDetails method that needs to be implemented by all classes.

    // TODO can be improved, create a create an interface, don't use static methods for setmetricdetails...
    //  depending upon the type assign proper reference to interface instance call one method in the end.
    // TODO there is no logging here or in the methods called, some logging will be useful for debugging
//    public static List<Metric> checkAttributeTypeAndSetDetails(MetricDetails metricDetails, Attribute attribute) {
//        List<Metric> metricList = new ArrayList<Metric>();
//        if (isCurrentAttributeComposite(attribute)) {
//            // TODO the method name setMetricDetails.. should be changed it also collects the metrics
//            metricList.addAll(CompositeMetricsProcessor.setMetricDetailsForCompositeMetrics(metricDetails, attribute));
//        } else if (isCurrentAttributeMap(attribute)) {
//            metricList.addAll(MapMetricsProcessor.setMetricDetailsForMapMetrics(metricDetails, attribute));
//        } else if (isCurrentAttributeList(attribute)) {
//            metricList.addAll(ListMetricsProcessor.setMetricDetailsForListMetrics(metricDetails, attribute));
//        } else {
//            // TODO why is this check required? attributes have been filtered already if above cases are not satisfied then it should be base case
//            if (metricDetails.getMetricPropsPerMetricName().containsKey(attribute.getName())) {
//                metricList.add(BaseMetricsProcessor.setMetricDetailsForBaseMetrics(metricDetails, attribute));
//            }
//        }
//        return metricList;
//    }

    // TODO all this can be done in one method which returns proper reference
    private static boolean isCurrentAttributeComposite(Attribute attribute) {
        return attribute.getValue() instanceof CompositeData;
    }

    private static boolean isCurrentAttributeMap(Attribute attribute) {
        // TODO can just check if instanceof is Map
        return attribute.getValue().getClass().equals(Map.class) || attribute.getValue().getClass().equals(HashMap.class);
    }

    private static boolean isCurrentAttributeList(Attribute attribute) {
        // TODO can just check if instanceof is List or Array
        return attribute.getValue().getClass().equals(List.class) || attribute.getValue().getClass().equals(Array.class) || attribute.getValue().getClass().equals(ArrayList.class);
    }
}
