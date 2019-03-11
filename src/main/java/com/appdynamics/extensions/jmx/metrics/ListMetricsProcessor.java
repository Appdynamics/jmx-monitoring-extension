package com.appdynamics.extensions.jmx.metrics;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;

import javax.management.Attribute;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.jmx.JMXUtil.getMetricAfterCharacterReplacement;
import static com.appdynamics.extensions.jmx.metrics.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class ListMetricsProcessor {

    static void setMetricDetailsForListMetrics(MetricDetails metricDetails, MonitorContextConfiguration monitorContextConfiguration) {
        String attributeName = metricDetails.getAttribute().getName();
        List attributesFound = (List) metricDetails.getAttribute().getValue();
        for (Object metricNameKey : attributesFound) {
            Attribute listMetric = getListMetric(metricNameKey, monitorContextConfiguration);
            String key = attributeName + PERIOD + listMetric.getName();
            Object attributeValue = listMetric.getValue();
            Attribute attribute1 = new Attribute(key, attributeValue);
            metricDetails.setAttribute(attribute1);
            JMXMetricsDataFilter.checkObjectType(metricDetails, monitorContextConfiguration);
        }
    }

    private static Attribute getListMetric(Object metricKey, MonitorContextConfiguration monitorContextConfiguration) {
        String[] arr = metricKey.toString().split(getSeparator(monitorContextConfiguration));
        String key = arr[0].trim();
        String value = arr[1].trim();
        List<Map<String, String>> metricReplacer = getMetricReplacer(monitorContextConfiguration);
        key = getMetricAfterCharacterReplacement(key, metricReplacer);
        value = getMetricAfterCharacterReplacement(value, metricReplacer);
        return new Attribute(key, value);
    }

    private static List<Map<String, String>> getMetricReplacer(MonitorContextConfiguration monitorContextConfiguration) {
        return (List<Map<String, String>>) monitorContextConfiguration.getConfigYml().get("metricCharacterReplacer");
    }

    private static String getSeparator(MonitorContextConfiguration monitorContextConfiguration) {
        String separator = ":";
        if (monitorContextConfiguration.getConfigYml().get("separatorForMetricLists") != null) {
            separator = monitorContextConfiguration.getConfigYml().get("separatorForMetricLists").toString();
        }
        return separator;
    }

}
