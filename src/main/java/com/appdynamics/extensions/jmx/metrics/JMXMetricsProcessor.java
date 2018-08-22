/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.jmx.metrics;

import com.appdynamics.extensions.jmx.commons.JMXConnectionAdapter;
import com.appdynamics.extensions.jmx.JMXUtil;
import com.appdynamics.extensions.jmx.filters.IncludeFilter;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.appdynamics.extensions.jmx.metrics.Constants.*;

public class JMXMetricsProcessor {
    private static final Logger logger = LoggerFactory.getLogger(JMXMetricsProcessor.class);
    private JMXConnectionAdapter jmxConnectionAdapter;
    private JMXConnector jmxConnector;

    public JMXMetricsProcessor(JMXConnectionAdapter jmxConnectionAdapter, JMXConnector jmxConnector) {

        this.jmxConnectionAdapter = jmxConnectionAdapter;
        this.jmxConnector = jmxConnector;
    }

    public List<Metric> getJMXMetrics(Map mBean, Map<String, ?> metricsPropertiesMap, String metricPrefix, String displayName) throws
            MalformedObjectNameException, IOException, IntrospectionException, InstanceNotFoundException,
            ReflectionException {
        List<Metric> jmxMetrics = Lists.newArrayList();
        String configObjectName = JMXUtil.convertToString(mBean.get(OBJECT_NAME), NULLSTRING);

        Set<ObjectInstance> objectInstances = jmxConnectionAdapter.queryMBeans(jmxConnector, ObjectName.getInstance
                (configObjectName));
        for (ObjectInstance instance : objectInstances) {
            List<String> metricNamesDictionary = jmxConnectionAdapter.getReadableAttributeNames(jmxConnector, instance);
            List<String> metricNamesToBeExtracted = applyFilters(mBean, metricNamesDictionary);
            List<Attribute> attributes = jmxConnectionAdapter.getAttributes(jmxConnector, instance.getObjectName(),
                    metricNamesToBeExtracted.toArray(new String[metricNamesToBeExtracted.size()]));
            List<String> mBeanKeys = getMBeanKeys(mBean);
            collect(metricPrefix, jmxMetrics, attributes, instance, metricsPropertiesMap, mBeanKeys, displayName);
        }
        return jmxMetrics;
    }

    private List<String> getMBeanKeys(Map aConfigMBean) {
        List<String> mBeanKeys = (List) aConfigMBean.get(MBEANKEYS);

        return mBeanKeys;

    }

    private List<String> applyFilters(Map aConfigMBean, List<String> metricNamesDictionary) throws
            IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        Set<String> filteredSet = Sets.newHashSet();
        Map configMetrics = (Map) aConfigMBean.get(METRICS);
        List includeDictionary = (List) configMetrics.get(INCLUDE);
        new IncludeFilter(includeDictionary).applyFilter(filteredSet, metricNamesDictionary);
        return Lists.newArrayList(filteredSet);
    }

    private void collect(String metricPrefix, List<Metric> jmxMetrics, List<Attribute> attributes, ObjectInstance instance, Map<String, ?> metricPropsPerMetricName, List<String> mBeanKeys,String displayName) {
        for (Attribute attribute : attributes) {
            try {
                String metricName = attribute.getName();
                if (isCurrentObjectComposite(attribute)) {
                    Set<String> attributesFound = ((CompositeDataSupport) attribute.getValue()).getCompositeType()
                            .keySet();
                    for (String str : attributesFound) {
                        String key = metricName + PERIOD + str;
                        if (metricPropsPerMetricName.containsKey(key)) {
                            Object attributeValue = ((CompositeDataSupport) attribute.getValue()).get(str);
                            setMetricDetails(metricPrefix, key, attributeValue, instance, metricPropsPerMetricName, jmxMetrics, mBeanKeys, displayName);
                        }
                    }
                } else {
                    setMetricDetails(metricPrefix, metricName, attribute.getValue(), instance, (Map) metricPropsPerMetricName,
                            jmxMetrics, mBeanKeys, displayName);
                }
            } catch (Exception e) {
                logger.error("Error collecting value for {} {}", instance.getObjectName(), attribute.getName(), e);
            }
        }
    }

    private void setMetricDetails(String metricPrefix, String attributeName, Object attributeValue, ObjectInstance instance, Map<String, ?> metricPropsPerMetricName, List<Metric> jmxMetrics, List<String> mBeanKeys, String displayName) {

        Map<String, ?> props = (Map) metricPropsPerMetricName.get(attributeName);
        if (props == null) {
            logger.error("Could not find metric properties for {} ", attributeName);
        }


        String instanceKey = getInstanceKey(instance, mBeanKeys);
        logger.debug("Instance Key: {}", instanceKey);

        String metricPath;
        if(Strings.isNullOrEmpty(metricPrefix)){
            if(Strings.isNullOrEmpty(displayName)){
                metricPath = instanceKey + attributeName;
            } else {
                metricPath = displayName + METRICS_SEPARATOR + instanceKey + attributeName;
            }
        } else {
            if (Strings.isNullOrEmpty(displayName)) {
                metricPath = metricPrefix + METRICS_SEPARATOR + instanceKey + attributeName;
            } else {
                metricPath = metricPrefix + METRICS_SEPARATOR + displayName + METRICS_SEPARATOR + instanceKey + attributeName;
            }
        }
        String attrVal = attributeValue.toString();
        Metric current_metric = new Metric(attributeName, attrVal, metricPath, props);
        jmxMetrics.add(current_metric);
    }


    private boolean isCurrentObjectComposite(Attribute attribute) {
        return attribute.getValue().getClass().equals(CompositeDataSupport.class);
    }

    private ObjectName getObjectName(ObjectInstance instance) {
        return instance.getObjectName();
    }

    private String getKeyProperty(ObjectInstance instance, String property) {
        if (instance == null) {
            return "";
        }
        return getObjectName(instance).getKeyProperty(property);
    }

    private String getInstanceKey(ObjectInstance instance, List<String> mBeanKeys) {
        StringBuilder metricsKey = new StringBuilder();

        for (String key : mBeanKeys) {
            String value = getKeyProperty(instance, key);
            metricsKey.append(Strings.isNullOrEmpty(value) ? NULLSTRING : value + METRICS_SEPARATOR);
        }
        return metricsKey.toString();
    }

}
