package com.appdynamics.extensions.jmx.metrics;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
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
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.appdynamics.extensions.jmx.JMXUtil.*;
import static com.appdynamics.extensions.jmx.metrics.Constants.*;

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

    public List<Metric> getJMXMetrics(Map mBean, Map<String, ?> metricsPropertiesMap, String metricPrefix, String displayName) throws
            MalformedObjectNameException, IOException, IntrospectionException, InstanceNotFoundException,
            ReflectionException {
        List<Metric> jmxMetrics = Lists.newArrayList();
        String configObjectName = JMXUtil.convertToString(mBean.get(OBJECT_NAME), EMPTY_STRING);

        Set<ObjectInstance> objectInstances = jmxConnectionAdapter.queryMBeans(jmxConnector, ObjectName.getInstance
                (configObjectName));
        for (ObjectInstance instance : objectInstances) {
            List<String> metricNamesDictionary = jmxConnectionAdapter.getReadableAttributeNames(jmxConnector, instance);
            List<String> metricNamesToBeExtracted = applyFilters(mBean, metricNamesDictionary);
            List<Attribute> attributes = jmxConnectionAdapter.getAttributes(jmxConnector, instance.getObjectName(),
                    metricNamesToBeExtracted.toArray(new String[metricNamesToBeExtracted.size()]));
            List<String> mBeanKeys = getMBeanKeys(mBean);
            MetricDetails metricDetails = getMetricDetails(metricsPropertiesMap, metricPrefix, displayName, jmxMetrics, instance, mBeanKeys);
            collectMetrics(metricDetails, attributes, instance);
            int a = 1+ 1;
        }
        return jmxMetrics;
    }

    private MetricDetails getMetricDetails(Map<String, ?> metricsPropertiesMap, String metricPrefix, String displayName, List<Metric> jmxMetrics, ObjectInstance instance, List<String> mBeanKeys) {
        return new MetricDetails.Builder()
                        .metricPrefix(metricPrefix)
                        .jmxMetrics(jmxMetrics)
                        .instance(instance)
                        .metricPropsPerMetricName(metricsPropertiesMap)
                        .mBeanKeys(mBeanKeys)
                        .displayName(displayName)
                        .build();
    }


    private List<String> applyFilters(Map aConfigMBean, List<String> metricNamesDictionary) {
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

    private void collectMetrics(MetricDetails metricInfo, List<Attribute> attributes, ObjectInstance instance) {
        for (Attribute attribute : attributes) {
            try {
                metricInfo.setAttribute(attribute);
                checkAttributeTypeAndSetDetails(metricInfo);
            } catch (Exception e) {
                logger.error("Error collecting value for {} {}", instance.getObjectName(), attribute.getName(), e);
            }
        }
    }


    //TODO this is just too ugly. There is a reason Java belongs to the family of OOP languages.
    // TODO While I understand what you have done because of our meetings, there is no way I can approve this school code.

    private void checkAttributeTypeAndSetDetails(MetricDetails metricDetails) {
        if (isCurrentObjectComposite(metricDetails.getAttribute())) {
            setMetricDetailsForCompositeMetrics(metricDetails);
        } else if (isCurrentAttributeMap(metricDetails.getAttribute())) {
            setMetricDetailsForMapMetrics(metricDetails);
        } else if (isCurrentAttributeList(metricDetails.getAttribute())) {
            setMetricDetailsForListMetrics(metricDetails);
        } else {
            setMetricDetailsForNormalMetrics(metricDetails);
        }
    }


    private void checkObjectType(MetricDetails metricDetails) {
        if (isCurrentObjectMap(metricDetails.getAttribute().getValue())) {
            checkAttributeTypeAndSetDetails(metricDetails);
        } else if (isCurrentObjectList(metricDetails.getAttribute().getValue())) {
            checkAttributeTypeAndSetDetails(metricDetails);
        } else {
            if (metricDetails.getMetricPropsPerMetricName().containsKey(metricDetails.getAttribute().getName())) {
                setMetricDetailsForNormalMetrics(metricDetails);
            }
        }
    }

    private void setMetricDetailsForCompositeMetrics(MetricDetails metricDetails) {
        String attributeName = metricDetails.getAttribute().getName();
        CompositeData metricValue = (CompositeData) metricDetails.getAttribute().getValue();
        Set<String> attributesFound = metricValue.getCompositeType().keySet();

        for (String str : attributesFound) {
            String key = attributeName + PERIOD + str;
            if (metricDetails.getMetricPropsPerMetricName().containsKey(key)) {
                Object attributeValue = metricValue.get(str);
                Attribute attribute1 = new Attribute(key, attributeValue);
                metricDetails.setAttribute(attribute1);
                setMetricDetailsForNormalMetrics(metricDetails);
            }
        }
    }

    private void setMetricDetailsForMapMetrics(MetricDetails metricDetails) {
        String attributeName = metricDetails.getAttribute().getName();
        Map attributesFound = (Map) metricDetails.getAttribute().getValue();
        for (Object metricNameKey : attributesFound.keySet()) {
            String key = attributeName + PERIOD + metricNameKey.toString();
            Object attributeValue = attributesFound.get(metricNameKey);
            Attribute attribute1 = new Attribute(key, attributeValue);
            metricDetails.setAttribute(attribute1);
            checkObjectType(metricDetails);
        }
    }

    private void setMetricDetailsForListMetrics(MetricDetails metricDetails) {
        String attributeName = metricDetails.getAttribute().getName();
        List attributesFound = (List) metricDetails.getAttribute().getValue();
        for (Object metricNameKey : attributesFound) {
            Attribute listMetric = getListMetric(metricNameKey);
            String key = attributeName + PERIOD + listMetric.getName();
            Object attributeValue = listMetric.getValue();
            Attribute attribute1 = new Attribute(key, attributeValue);
            metricDetails.setAttribute(attribute1);
            checkObjectType(metricDetails);
        }
    }

    private Attribute getListMetric(Object metricKey) {
        String[] arr = metricKey.toString().split(getSeparator());
        String key = arr[0].trim();
        String value = arr[1].trim();
        List<Map<String, String>> metricReplacer = getMetricReplacer();
        key = getMetricAfterCharacterReplacement(key, metricReplacer);
        value = getMetricAfterCharacterReplacement(value, metricReplacer);
        return new Attribute(key, value);
    }

    private List<Map<String, String>> getMetricReplacer() {
        return (List<Map<String, String>>) monitorContextConfiguration.getConfigYml().get("metricCharacterReplacer");
    }

    private String getSeparator() {
        String separator = ":";
        if (monitorContextConfiguration.getConfigYml().get("separatorForMetricLists") != null) {
            separator = monitorContextConfiguration.getConfigYml().get("separatorForMetricLists").toString();
        }
        return separator;
    }

    private void setMetricDetailsForNormalMetrics(MetricDetails metricDetails) {
        String attributeName = metricDetails.getAttribute().getName();
        Map<String, ?> props = (Map) metricDetails.getMetricPropsPerMetricName().get(attributeName);
        if (props == null) {
            logger.error("Could not find metric properties for {} ", attributeName);
        }
        String instanceKey = getInstanceKey(metricDetails.getInstance(), metricDetails.getmBeanKeys());
        String metricPath = generateMetricPath(metricDetails.getMetricPrefix(), attributeName, metricDetails.getDisplayName(), instanceKey);
        String attrVal = metricDetails.getAttribute().getValue().toString();
        Metric current_metric = new Metric(attributeName, attrVal, metricPath, props);
        metricDetails.addToJmxMetrics(current_metric);
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
            metricsKey.append(Strings.isNullOrEmpty(value) ? EMPTY_STRING : value + METRICS_SEPARATOR);
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
