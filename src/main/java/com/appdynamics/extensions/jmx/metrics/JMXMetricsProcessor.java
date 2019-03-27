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
import com.appdynamics.extensions.jmx.metrics.processor.JMXMetricProcessor;
import com.appdynamics.extensions.jmx.metrics.processor.JMXMetricsDataFilter;
import com.appdynamics.extensions.jmx.utils.JMXUtil;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;

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

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(JMXMetricsProcessor.class);
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
        String configObjectName = (String )mBean.get(OBJECT_NAME);
        AssertUtils.assertNotNull(configObjectName, "Metric Object Name can not be Empty");
        Set<ObjectInstance> objectInstances = jmxConnectionAdapter.queryMBeans(jmxConnector, ObjectName.getInstance
                (configObjectName));
        for (ObjectInstance instance : objectInstances) {
            logger.debug("Processing for Object : {} ", configObjectName);
            List<String> metricNameListFromMbean = jmxConnectionAdapter.getReadableAttributeNames(jmxConnector, instance);
            Set<String> metricNamesToBeExtracted = applyFilters(mBean, metricNameListFromMbean);
            List<Attribute> attributes = jmxConnectionAdapter.getAttributes(jmxConnector, instance.getObjectName(),
                    metricNamesToBeExtracted.toArray(new String[metricNamesToBeExtracted.size()]));
            if (!attributes.isEmpty()) {

                MetricDetails metricDetails = getMetricDetails(mBean, metricPrefix, displayName, instance);
                jmxMetrics.addAll(collectMetrics(metricDetails, attributes));
            } else {
                logger.debug("No attributes found for Object : {} ", configObjectName);
            }
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
                .separator(getSeparator()) //TODO: Separator is not an ObjectInstance level configuration. Should be passed to the processor. Check the feasibility, as it may need lot of refactoring
                .build();
    }

    private Set<String> applyFilters(Map aConfigMBean, List<String> metricNamesList) {
        Set<String> filteredSet = Sets.newHashSet();
        Map configMetrics = (Map) aConfigMBean.get(METRICS);
        List<Map<String, ?>> includeDictionary = (List<Map<String, ?>>) configMetrics.get(INCLUDE);
        new IncludeFilter(includeDictionary).applyFilter(filteredSet, metricNamesList);
        return filteredSet;
    }


    private List<Metric> collectMetrics(MetricDetails metricDetails, List<Attribute> attributes) {
        List<Metric> jmxMetrics = new ArrayList<Metric>();

        for (Attribute attribute : attributes) {
            try {
//                TODO: Add logger debug
                // TODO plan on sending separator as a field to checkAttribute
                jmxMetrics.addAll(JMXMetricProcessor.checkTypeAndReturnMetrics(metricDetails, attribute));
            } catch (Exception e) {
                logger.error("Error collecting value for {} {}", metricDetails.getInstance().getObjectName(), attribute.getName(), e);
            }
        }
        return jmxMetrics;
    }



    private String getSeparator() {
        String separator = (String) monitorContextConfiguration.getConfigYml().get(SEPARATOR_FOR_METRIC_LISTS);
        if (Strings.isNullOrEmpty(separator)) {
            separator = COLON;
        }
        return separator;
    }


}
