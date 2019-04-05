/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.jmx.metrics.processor;

import com.appdynamics.extensions.jmx.metrics.MetricDetails;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.base.Strings;
import org.slf4j.Logger;

import javax.management.Attribute;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.jmx.utils.Constants.EMPTY_STRING;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class BaseMetricsProcessor {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(BaseMetricsProcessor.class);
    protected List<Metric> metrics;

    public BaseMetricsProcessor() {
        this.metrics = new ArrayList<>();
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    private static LinkedList<String> getInstanceKey(ObjectInstance instance, List<String> mBeanKeys, LinkedList<String> metricTokens) {
        for (String key : mBeanKeys) {
            String value = getKeyProperty(instance, key);
            metricTokens.add(Strings.isNullOrEmpty(value) ? EMPTY_STRING : value);
        }
        return metricTokens;
    }

    private static LinkedList<String> generateMetricPathTokens(String attributeName, String displayName, LinkedList<String> metricTokens) {
        if (Strings.isNullOrEmpty(displayName)) {
            metricTokens.add(attributeName);
        } else {
            metricTokens.add(displayName);
            metricTokens.add(attributeName);
        }
        return metricTokens;
    }

    private static ObjectName getObjectName(ObjectInstance instance) {
        return instance.getObjectName();
    }

    private static String getKeyProperty(ObjectInstance instance, String property) {
        if (instance == null) {
            return EMPTY_STRING;
        }
        return getObjectName(instance).getKeyProperty(property);
    }

    public void populateMetricsFromEntity(MetricDetails metricDetails, Attribute attribute) {
        String attributeName = attribute.getName();
        Map<String, ?> props = (Map<String, ?>) metricDetails.getMetricPropsPerMetricName().get(attributeName);
        if (props == null) {
            logger.error("Could not find metric properties for {} ", attributeName);
        } else {
            LinkedList<String> metricTokens = new LinkedList<>();
            // TODO if you are passing metricTokens you need not return it
            metricTokens = getInstanceKey(metricDetails.getInstance(), metricDetails.getmBeanKeys(), metricTokens);
            metricTokens = generateMetricPathTokens(attributeName, metricDetails.getDisplayName(), metricTokens);
            String attrVal = attribute.getValue().toString();
            // TODO -ve values will be converted to positive, sign values of number should be preserved only trailing non numeric data should be removed. Everything else should be invalid metric value
            attrVal = attrVal.replaceAll("[^0-9.]", "");
            String[] tokens = new String[metricTokens.size()];
            tokens = metricTokens.toArray(tokens);
            metrics.add(new Metric(attributeName, attrVal, props, metricDetails.getMetricPrefix(), tokens));
        }
    }

}
