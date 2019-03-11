package com.appdynamics.extensions.jmx.metrics.processor;

import com.appdynamics.extensions.jmx.metrics.MetricDetails;

import javax.management.Attribute;
import javax.management.openmbean.CompositeData;
import java.util.Set;

import static com.appdynamics.extensions.jmx.utils.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class CompositeMetricsProcessor {

    static MetricDetails setMetricDetailsForCompositeMetrics(MetricDetails metricDetails) {
        String attributeName = metricDetails.getAttribute().getName();
        CompositeData metricValue = (CompositeData) metricDetails.getAttribute().getValue();
        Set<String> attributesFound = metricValue.getCompositeType().keySet();

        for (String str : attributesFound) {
            String key = attributeName + PERIOD + str;
            if (metricDetails.getMetricPropsPerMetricName().containsKey(key)) {
                Object attributeValue = metricValue.get(str);
                Attribute attribute1 = new Attribute(key, attributeValue);
                metricDetails.setAttribute(attribute1);
                metricDetails = BaseMetricsProcessor.setMetricDetailsForBaseMetrics(metricDetails);
            }
        }
        return metricDetails;
    }

}