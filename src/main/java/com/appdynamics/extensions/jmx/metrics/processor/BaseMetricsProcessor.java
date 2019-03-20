package com.appdynamics.extensions.jmx.metrics.processor;

import com.appdynamics.extensions.jmx.metrics.MetricDetails;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Attribute;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.jmx.utils.Constants.EMPTY_STRING;
import static com.appdynamics.extensions.jmx.utils.Constants.METRICS_SEPARATOR;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
class BaseMetricsProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BaseMetricsProcessor.class);

    static Metric setMetricDetailsForBaseMetrics(MetricDetails metricDetails, Attribute attribute) {
        String attributeName = attribute.getName();
        Map<String, ?> props = (Map) metricDetails.getMetricPropsPerMetricName().get(attributeName);
        if (props == null) {
            logger.error("Could not find metric properties for {} ", attributeName);
        }
        String instanceKey = getInstanceKey(metricDetails.getInstance(), metricDetails.getmBeanKeys());
        String attrVal = attribute.getValue().toString();
        String[] metricPathTokens = generateMetricPathTokens(attributeName, metricDetails.getDisplayName(), instanceKey);
        return new Metric(attributeName, attrVal, props, metricDetails.getMetricPrefix(), metricPathTokens);
    }

    private static String getInstanceKey(ObjectInstance instance, List<String> mBeanKeys) {
        StringBuilder metricsKey = new StringBuilder();

        for (String key : mBeanKeys) {
            String value = getKeyProperty(instance, key);
            metricsKey.append(Strings.isNullOrEmpty(value) ? EMPTY_STRING : value + METRICS_SEPARATOR);
        }
        return metricsKey.toString();
    }

    private static String[] generateMetricPathTokens(String attributeName, String displayName, String instanceKey) {
        LinkedList<String> metricTokens = new LinkedList<>();
            if (Strings.isNullOrEmpty(displayName)) {
                metricTokens.add(instanceKey);
                metricTokens.add(attributeName);

            } else {
                metricTokens.add(displayName);
                metricTokens.add(instanceKey);
                metricTokens.add(attributeName);
            }
        String[] tokens = new String[metricTokens.size()];
        tokens = metricTokens.toArray(tokens);
        return tokens;
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
}
