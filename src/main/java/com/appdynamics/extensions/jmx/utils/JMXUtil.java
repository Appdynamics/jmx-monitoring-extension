/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.jmx.utils;

// TODO remove unused imports


import com.appdynamics.extensions.jmx.metrics.MetricDetails;
import com.appdynamics.extensions.jmx.metrics.processor.*;

import javax.management.Attribute;
import javax.management.openmbean.CompositeData;
import java.util.List;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 2/26/18.
 */
public class JMXUtil {

    public static boolean isCompositeObject(String objectName) {
        return (objectName.indexOf('.') != -1);
    }

    public static String getMetricNameFromCompositeObject(String objectName) {
        return objectName.split("\\.")[0];
    }

//    public static List<Metric> checkTypeAndReturnMetrics(MetricDetails metricDetails, Attribute attribute){
//
//        JMXMetricProcessor jmxMetricProcessor = getReference(attribute);
//        List<Metric> metricList = new ArrayList<>();
//        metricList.addAll(jmxMetricProcessor.populateMetricsFromEntity(metricDetails, attribute));
//        return metricList;
//
//    }



}
