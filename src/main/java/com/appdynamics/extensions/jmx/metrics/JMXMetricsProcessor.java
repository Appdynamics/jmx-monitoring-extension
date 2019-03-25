/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.jmx.metrics;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.jmx.commons.JMXConnectionAdapter;
import com.appdynamics.extensions.jmx.filters.IncludeFilter;
import com.appdynamics.extensions.jmx.metrics.processor.JMXMetricsDataFilter;
import com.appdynamics.extensions.jmx.utils.JMXUtil;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.appdynamics.extensions.jmx.metrics.MetricPropertiesForMBean.getMBeanKeys;
import static com.appdynamics.extensions.jmx.metrics.MetricPropertiesForMBean.getMapOfProperties;
import static com.appdynamics.extensions.jmx.utils.Constants.*;

/**
 * Created by bhuvnesh.kumar on 12/19/18.
 */
public class JMXMetricsProcessor {
    // TODO should not use Raw Types, can you change wherever applicable

    // TODO use ExtensionsLoggerFactory
    private static final Logger logger = LoggerFactory.getLogger(JMXMetricsProcessor.class);
    private JMXConnectionAdapter jmxConnectionAdapter;
    private JMXConnector jmxConnector;
    private MonitorContextConfiguration monitorContextConfiguration;

    public JMXMetricsProcessor(MonitorContextConfiguration monitorContextConfiguration, JMXConnectionAdapter jmxConnectionAdapter, JMXConnector jmxConnector) {
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.jmxConnectionAdapter = jmxConnectionAdapter;
        this.jmxConnector = jmxConnector;
    }

    public List<Metric> getJMXMetrics(Map mBean, String metricPrefix, String displayName) throws
            MalformedObjectNameException, IOException, IntrospectionException, InstanceNotFoundException,
            ReflectionException {
        List<Metric> jmxMetrics = Lists.newArrayList();
        String configObjectName = JMXUtil.convertToString(mBean.get(OBJECT_NAME), EMPTY_STRING);
        // TODO chances of NPE here, please check configObjectName for null references before calling getInstance
        // TODO if configObjectName is resolved to empty why would you proceed with the below steps? should'nt it be a config error?
        Set<ObjectInstance> objectInstances = jmxConnectionAdapter.queryMBeans(jmxConnector, ObjectName.getInstance
                (configObjectName));
        for (ObjectInstance instance : objectInstances) {
            // TODO can you change the name? dictionary is misleading for list of String
            List<String> metricNamesDictionary = jmxConnectionAdapter.getReadableAttributeNames(jmxConnector, instance);
            List<String> metricNamesToBeExtracted = applyFilters(mBean, metricNamesDictionary);
            // TODO the getAttributes method returns empty list if list returned from the mbean server is null so you
            //  should check that and log it will be useful for debugging. YOu can also skip collectMetrics in such case since no work will be done
            List<Attribute> attributes = jmxConnectionAdapter.getAttributes(jmxConnector, instance.getObjectName(),
                    metricNamesToBeExtracted.toArray(new String[metricNamesToBeExtracted.size()]));
            MetricDetails metricDetails = getMetricDetails(mBean, metricPrefix, displayName, instance);
            jmxMetrics.addAll(collectMetrics(metricDetails, attributes));
        }
        return jmxMetrics;
    }

    private MetricDetails getMetricDetails(Map mBean, String metricPrefix, String displayName, ObjectInstance instance) {

        return new MetricDetails.Builder()
                .metricPrefix(metricPrefix)
                .instance(instance)
                .metricPropsPerMetricName(getMapOfProperties(mBean))
                .mBeanKeys(getMBeanKeys(mBean))
                .displayName(displayName)
                .metricCharacterReplacer(getMetricReplacer())
                .separator(getSeparator())
                .build();
    }

    // TODO can you change the name? dictionary is misleading for list of String
    private List<String> applyFilters(Map aConfigMBean, List<String> metricNamesDictionary) {
        Set<String> filteredSet = Sets.newHashSet();
        Map configMetrics = (Map) aConfigMBean.get(METRICS);
        List includeDictionary = (List) configMetrics.get(INCLUDE);
        new IncludeFilter(includeDictionary).applyFilter(filteredSet, metricNamesDictionary);
        return Lists.newArrayList(filteredSet);
    }


    private List<Metric> collectMetrics(MetricDetails metricDetails, List<Attribute> attributes) {
        List<Metric> jmxMetrics = new ArrayList<Metric>();

        for (Attribute attribute : attributes) {
            try {
                jmxMetrics.addAll(JMXMetricsDataFilter.checkAttributeTypeAndSetDetails(metricDetails, attribute));
            } catch (Exception e) {
                logger.error("Error collecting value for {} {}", metricDetails.getInstance().getObjectName(), attribute.getName(), e);
            }
        }
        return jmxMetrics;
    }


// TODO not required
    private List<Map<String, String>> getMetricReplacer() {
        return (List<Map<String, String>>) monitorContextConfiguration.getConfigYml().get(METRIC_CHARACTER_REPLACER);
    }

    private String getSeparator() {
        // TODO first get the separator and check if it null or empty and then use default, in the current logic an empty separator will break things
        String separator = COLON;
        if (monitorContextConfiguration.getConfigYml().get(SEPARATOR_FOR_METRIC_LISTS) != null) {
            separator = monitorContextConfiguration.getConfigYml().get(SEPARATOR_FOR_METRIC_LISTS).toString();
        }
        return separator;
    }


}
