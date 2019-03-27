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
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 3/27/19.
 */
public interface JMXMetricProcessor {

    List<Metric> populateMetricsFromEntity(MetricDetails metricDetails, Attribute attribute);

    static List<Metric> checkTypeAndReturnMetrics(MetricDetails metricDetails, Attribute attribute){

        JMXMetricProcessor jmxMetricProcessor = getReference(attribute);
        List<Metric> metricList = new ArrayList<Metric>();
        metricList.addAll(jmxMetricProcessor.populateMetricsFromEntity(metricDetails, attribute));
        return metricList;

    }


    static JMXMetricProcessor getReference(Attribute attribute){

        Object object = attribute.getValue();

        if(object instanceof CompositeData){
            return new CompositeMetricsProcessor();
        } else if(object instanceof List ){
            return new ListMetricsProcessor();
        } else if(object instanceof Map ){
            return new MapMetricsProcessor();
        } else {
            return new BaseMetricsProcessor();
        }

    }
}
