package com.appdynamics.extensions.jmx.metrics.processor;

import com.appdynamics.extensions.jmx.metrics.MetricDetails;

import static com.appdynamics.extensions.jmx.utils.JMXUtil.*;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class JMXMetricsDataFilter {

    public static MetricDetails checkAttributeTypeAndSetDetails(MetricDetails metricDetails) {
        if (isCurrentObjectComposite(metricDetails.getAttribute())) {
            metricDetails = CompositeMetricsProcessor.setMetricDetailsForCompositeMetrics(metricDetails);
        } else if (isCurrentAttributeMap(metricDetails.getAttribute())) {
            metricDetails = MapMetricsProcessor.setMetricDetailsForMapMetrics(metricDetails);
        } else if (isCurrentAttributeList(metricDetails.getAttribute())) {
            metricDetails = ListMetricsProcessor.setMetricDetailsForListMetrics(metricDetails);
        } else {
            metricDetails = BaseMetricsProcessor.setMetricDetailsForBaseMetrics(metricDetails);
        }

        return metricDetails;
    }

    public static MetricDetails checkObjectType(MetricDetails metricDetails) {
        if (isCurrentObjectMap(metricDetails.getAttribute().getValue()) || isCurrentObjectList(metricDetails.getAttribute().getValue())) {
            metricDetails = checkAttributeTypeAndSetDetails(metricDetails);
        } else {
            if (metricDetails.getMetricPropsPerMetricName().containsKey(metricDetails.getAttribute().getName())) {
                metricDetails = BaseMetricsProcessor.setMetricDetailsForBaseMetrics(metricDetails);
            }
        }
        return metricDetails;
    }
}
