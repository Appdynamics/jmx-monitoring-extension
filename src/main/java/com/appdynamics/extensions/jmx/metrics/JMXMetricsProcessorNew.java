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

    private void collect(String metricPrefix, List<Metric> jmxMetrics, List<Attribute> attributes,
                         ObjectInstance instance, Map<String, ?> metricPropsPerMetricName, List<String> mBeanKeys, String displayName) {
        for (Attribute attribute : attributes) {
            try {
                checkAttributeType(metricPrefix, jmxMetrics, instance, metricPropsPerMetricName, mBeanKeys, displayName, attribute);
            } catch (Exception e) {
                logger.error("Error collecting value for {} {}", instance.getObjectName(), attribute.getName(), e);
            }
        }

    }

    private void checkAttributeType(String metricPrefix, List<Metric> jmxMetrics, ObjectInstance instance, Map<String, ?> metricPropsPerMetricName,
                                    List<String> mBeanKeys, String displayName, Attribute attribute) {
        if (isCurrentObjectComposite(attribute)) {
            setMetricDetailsForCompositeMetrics(metricPrefix, jmxMetrics, instance, metricPropsPerMetricName, mBeanKeys, displayName, attribute);
        } else if (isCurrentAttributeMap(attribute)) {
            setMetricDetailsForMapMetrics(metricPrefix, jmxMetrics, instance, metricPropsPerMetricName, mBeanKeys, displayName, attribute);
        } else {
            setMetricDetailsForNormalMetrics(metricPrefix, jmxMetrics, instance, metricPropsPerMetricName, mBeanKeys, displayName, attribute);
        }

    }

    private void setMetricDetailsForCompositeMetrics(String metricPrefix, List<Metric> jmxMetrics, ObjectInstance instance, Map<String, ?> metricPropsPerMetricName,
                                                     List<String> mBeanKeys, String displayName, Attribute attribute) {
        String attributeName = attribute.getName();
        Set<String> attributesFound = ((CompositeDataSupport) attribute.getValue()).getCompositeType()
                .keySet();
        for (String str : attributesFound) {
            String key = attributeName + PERIOD + str;
            if (metricPropsPerMetricName.containsKey(key)) {
                Object attributeValue = ((CompositeDataSupport) attribute.getValue()).get(str);
                Attribute attribute1 = new Attribute(key, attributeValue);
                setMetricDetailsForNormalMetrics(metricPrefix, jmxMetrics, instance, metricPropsPerMetricName, mBeanKeys, displayName, attribute1);
            }
        }
    }


    private void setMetricDetailsForMapMetrics(String metricPrefix, List<Metric> jmxMetrics, ObjectInstance instance, Map<String, ?> metricPropsPerMetricName,
                                               List<String> mBeanKeys, String displayName, Attribute attribute) {
        String attributeName = attribute.getName();
        Map attributesFound = (Map) attribute.getValue();
        for (Object metricNameKey : attributesFound.keySet()) {
            String key = attributeName + PERIOD + metricNameKey.toString();
            Object attributeValue = attributesFound.get(metricNameKey);
            if (isCurrentObjectMap(attributeValue)) {
                Attribute attribute1 = new Attribute(key, attributeValue);
                setMetricDetailsForMapMetrics(metricPrefix, jmxMetrics, instance, metricPropsPerMetricName, mBeanKeys, displayName, attribute1);
            } else {
                if (metricPropsPerMetricName.containsKey(key)) {
                    Attribute attribute1 = new Attribute(key, attributeValue);
                    setMetricDetailsForNormalMetrics(metricPrefix, jmxMetrics, instance, metricPropsPerMetricName, mBeanKeys, displayName, attribute1);
                }
            }
        }
    }

    private void setMetricDetailsForNormalMetrics(String metricPrefix, List<Metric> jmxMetrics, ObjectInstance instance, Map<String, ?> metricPropsPerMetricName,
                                                  List<String> mBeanKeys, String displayName, Attribute attribute) {
        String attributeName = attribute.getName();
        Map<String, ?> props = (Map) metricPropsPerMetricName.get(attributeName);
        if (props == null) {
            logger.error("Could not find metric properties for {} ", attributeName);
        }
        String instanceKey = getInstanceKey(instance, mBeanKeys);
        String metricPath = generateMetricPath(metricPrefix, attributeName, displayName, instanceKey);
        String attrVal = attribute.getValue().toString();
        Metric current_metric = new Metric(attributeName, attrVal, metricPath, props);
        jmxMetrics.add(current_metric);

    }

    private boolean isCurrentObjectComposite(Attribute attribute) {
        return attribute.getValue().getClass().equals(CompositeDataSupport.class);
    }

    private boolean isCurrentObjectMap(Object attribute) {
        return attribute.getClass().equals(Map.class) || attribute.getClass().equals(HashMap.class);
    }

    private boolean isCurrentAttributeMap(Attribute attribute) {
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

    private String generateMetricPath(String metricPrefix, String attributeName, String displayName, String instanceKey) {
        String metricPath;
        if (Strings.isNullOrEmpty(metricPrefix)) {
            if (Strings.isNullOrEmpty(displayName)) {
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
        return metricPath;
    }


}
