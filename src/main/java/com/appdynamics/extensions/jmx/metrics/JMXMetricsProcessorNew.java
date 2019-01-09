package com.appdynamics.extensions.jmx.metrics;

import com.appdynamics.extensions.jmx.JMXUtil;
import com.appdynamics.extensions.jmx.commons.JMXConnectionAdapter;
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
import java.util.*;

import static com.appdynamics.extensions.jmx.metrics.Constants.*;

/**
 * Created by bhuvnesh.kumar on 12/19/18.
 */
public class JMXMetricsProcessorNew {

    private static final Logger logger = LoggerFactory.getLogger(JMXMetricsProcessor.class);
    private JMXConnectionAdapter jmxConnectionAdapter;
    private JMXConnector jmxConnector;

    public JMXMetricsProcessorNew(JMXConnectionAdapter jmxConnectionAdapter, JMXConnector jmxConnector) {
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

    private List<String> applyFilters(Map aConfigMBean, List<String> metricNamesDictionary) throws
            IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        Set<String> filteredSet = Sets.newHashSet();
        Map configMetrics = (Map) aConfigMBean.get(METRICS);
        List includeDictionary = (List) configMetrics.get(INCLUDE);
        new IncludeFilter(includeDictionary).applyFilter(filteredSet, metricNamesDictionary);
        return Lists.newArrayList(filteredSet);
    }

    private List<String> getMBeanKeys(Map aConfigMBean) {
        List<String> mBeanKeys = (List) aConfigMBean.get(MBEANKEYS);
        return mBeanKeys;
    }

    private void collect(String metricPrefix, List<Metric> jmxMetrics, List<Attribute> attributes, ObjectInstance instance, Map<String, ?> metricPropsPerMetricName, List<String> mBeanKeys, String displayName) {
        for (Attribute attribute : attributes) {
            try {
                String metricName = attribute.getName();
                if (isCurrentObjectComposite(attribute)) {
                    setMetricDetailsForCompositeMetrics(metricPrefix, jmxMetrics, instance, metricPropsPerMetricName, mBeanKeys, displayName, attribute, metricName);
                } else if (isCurrentObjectMap(attribute)) {
                    setMetricDetailsForMapMetrics(metricPrefix, jmxMetrics,attributes, instance, metricPropsPerMetricName, mBeanKeys, displayName, attribute, metricName);
                } else {
                    setMetricDetailsForNormalMetrics(metricPrefix, metricName, attribute.getValue(), instance, metricPropsPerMetricName,
                            jmxMetrics, mBeanKeys, displayName);
                }
            } catch (Exception e) {
                logger.error("Error collecting value for {} {}", instance.getObjectName(), attribute.getName(), e);
            }
        }

    }

    private boolean isCurrentObjectComposite(Attribute attribute) {
        return attribute.getValue().getClass().equals(CompositeDataSupport.class);
    }

    private boolean isCurrentObjectMap(Attribute attribute) {
        return attribute.getValue().getClass().equals(Map.class) || attribute.getValue().getClass().equals(HashMap.class);
    }

    private boolean isCurrentObjectList(Attribute attribute) {
        return attribute.getValue().getClass().equals(List.class) || attribute.getValue().getClass().equals(ArrayList.class);
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