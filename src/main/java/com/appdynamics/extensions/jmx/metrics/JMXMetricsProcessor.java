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

    private static final Logger logger = LoggerFactory.getLogger(JMXMetricsProcessor.class);
    private JMXConnectionAdapter jmxConnectionAdapter;
    private JMXConnector jmxConnector;
    private MonitorContextConfiguration monitorContextConfiguration;

    public JMXMetricsProcessor(MonitorContextConfiguration monitorContextConfiguration, JMXConnectionAdapter jmxConnectionAdapter, JMXConnector jmxConnector) {
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.jmxConnectionAdapter = jmxConnectionAdapter;
        this.jmxConnector = jmxConnector;
    }

    //TODO: can throw a common exception for all JMX (JMException) and catch it separately instead of just MalformedObjectNameException
    public List<Metric> getJMXMetrics(Map mBean, String metricPrefix, String displayName) throws
            MalformedObjectNameException, IOException, IntrospectionException, InstanceNotFoundException,
            ReflectionException {
        List<Metric> jmxMetrics = Lists.newArrayList();
        String configObjectName = JMXUtil.convertToString(mBean.get(OBJECT_NAME), EMPTY_STRING);

        Set<ObjectInstance> objectInstances = jmxConnectionAdapter.queryMBeans(jmxConnector, ObjectName.getInstance
                (configObjectName));
        for (ObjectInstance instance : objectInstances) {
//            TODO: add logger debug for instances
            List<String> metricNamesDictionary = jmxConnectionAdapter.getReadableAttributeNames(jmxConnector, instance);
            List<String> metricNamesToBeExtracted = applyFilters(mBean, metricNamesDictionary);
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
                .separator(getSeparator()) //TODO: Separator is not an ObjectInstance level configuration. Should be passed to the processor. Check the feasibility, as it may need lot of refactoring
                .build();
    }

    private List<String> applyFilters(Map aConfigMBean, List<String> metricNamesDictionary) {
        Set<String> filteredSet = Sets.newHashSet();
        Map configMetrics = (Map) aConfigMBean.get(METRICS);
        List includeDictionary = (List) configMetrics.get(INCLUDE);
//        TODO: why pass filteredSet set into applyFilter() and convert to a list again, when you can simply create and return it from applyFilter method itself
        new IncludeFilter(includeDictionary).applyFilter(filteredSet, metricNamesDictionary);
        return Lists.newArrayList(filteredSet);
    }


    private List<Metric> collectMetrics(MetricDetails metricDetails, List<Attribute> attributes) {
        List<Metric> jmxMetrics = new ArrayList<Metric>();

        for (Attribute attribute : attributes) {
            try {
//                TODO: Add logger debug
                jmxMetrics.addAll(JMXMetricsDataFilter.checkAttributeTypeAndSetDetails(metricDetails, attribute));
            } catch (Exception e) {
                logger.error("Error collecting value for {} {}", metricDetails.getInstance().getObjectName(), attribute.getName(), e);
            }
        }
        return jmxMetrics;
    }

    private List<Map<String, String>> getMetricReplacer() {
        return (List<Map<String, String>>) monitorContextConfiguration.getConfigYml().get(METRIC_CHARACTER_REPLACER);
    }

    private String getSeparator() {
        String separator = COLON;
        if (monitorContextConfiguration.getConfigYml().get(SEPARATOR_FOR_METRIC_LISTS) != null) {
            separator = monitorContextConfiguration.getConfigYml().get(SEPARATOR_FOR_METRIC_LISTS).toString();
        }
        return separator;
    }


}
