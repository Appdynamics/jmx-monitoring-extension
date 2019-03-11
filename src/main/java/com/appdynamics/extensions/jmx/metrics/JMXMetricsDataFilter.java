package com.appdynamics.extensions.jmx.metrics;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.appdynamics.extensions.jmx.JMXUtil.*;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class JMXMetricsDataFilter {
    private static final Logger logger = LoggerFactory.getLogger(JMXMetricsDataFilter.class);

    public static void checkAttributeTypeAndSetDetails(MetricDetails metricDetails, MonitorContextConfiguration monitorContextConfiguration) {
        if (isCurrentObjectComposite(metricDetails.getAttribute())) {
            CompositeMetricsProcessor.setMetricDetailsForCompositeMetrics(metricDetails);
        } else if (isCurrentAttributeMap(metricDetails.getAttribute())) {
            MapMetricsProcessor.setMetricDetailsForMapMetrics(metricDetails, monitorContextConfiguration);
        } else if (isCurrentAttributeList(metricDetails.getAttribute())) {
            ListMetricsProcessor.setMetricDetailsForListMetrics(metricDetails, monitorContextConfiguration);
        } else {
            BaseMetricsProcessor.setMetricDetailsForBaseMetrics(metricDetails);
        }
    }

    public static void checkObjectType(MetricDetails metricDetails, MonitorContextConfiguration monitorContextConfiguration) {
        if (isCurrentObjectMap(metricDetails.getAttribute().getValue()) || isCurrentObjectList(metricDetails.getAttribute().getValue())) {
            checkAttributeTypeAndSetDetails(metricDetails, monitorContextConfiguration);
        } else {
            if (metricDetails.getMetricPropsPerMetricName().containsKey(metricDetails.getAttribute().getName())) {
                BaseMetricsProcessor.setMetricDetailsForBaseMetrics(metricDetails);
            }
        }
    }
}
