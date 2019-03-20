/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.jmx;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.jmx.commons.JMXConnectionAdapter;
import com.appdynamics.extensions.jmx.metrics.JMXMetricsProcessor;
import com.appdynamics.extensions.jmx.utils.JMXUtil;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.jmx.utils.Constants.*;

/**
 * Created by bhuvnesh.kumar on 2/23/18.
 */
public class JMXMonitorTask implements AMonitorTaskRunnable {
    private static final Logger logger = LoggerFactory.getLogger(JMXMonitorTask.class);
    private Boolean status = true;
    private String metricPrefix;
    private MetricWriteHelper metricWriter;
    private Map server;
    private JMXConnectionAdapter jmxConnectionAdapter;
    private List<Map> configMBeans;
    private MonitorContextConfiguration monitorContextConfiguration;

    private String serverName;

    public void run() {
        serverName = JMXUtil.convertToString(server.get(DISPLAY_NAME), EMPTY_STRING);

        try {
            logger.debug("JMX monitoring task initiated for server {}", serverName);
            populateAndPrintStats();
        } catch (Exception e) {
            logger.error("Error in JMX Monitoring Task for Server {}", serverName, e);
            status = false;
        } finally {
            logger.debug("JMX Monitoring Task Complete.");
        }
    }

    private void populateAndPrintStats() throws IOException {
        JMXConnector jmxConnector = null;
        long previousTimestamp = 0;
        long currentTimestamp = 0;
        try {
            previousTimestamp = System.currentTimeMillis();
            jmxConnector = jmxConnectionAdapter.open();
            currentTimestamp = System.currentTimeMillis();
            logger.debug("Time to open connection for " + serverName + " in milliseconds: " + (currentTimestamp - previousTimestamp));

            for (Map mBean : configMBeans) {
                String configObjName = JMXUtil.convertToString(mBean.get(OBJECT_NAME), EMPTY_STRING);
                logger.debug("Processing mBean {} from the config file", configObjName);
                try {
                    JMXMetricsProcessor jmxMetricsProcessor = new JMXMetricsProcessor(monitorContextConfiguration,
                            jmxConnectionAdapter, jmxConnector);
                    AssertUtils.assertNotNull(server.get(DISPLAY_NAME), DISPLAY_NAME+" can not be null in the config.yml");
                    List<Metric> nodeMetrics = jmxMetricsProcessor.getJMXMetrics(mBean,
                            metricPrefix, server.get(DISPLAY_NAME).toString());
                    if (nodeMetrics.size() > 0) {
                        metricWriter.transformAndPrintMetrics(nodeMetrics);
                    }
                } catch (MalformedObjectNameException e) {
                    logger.error("Illegal Object Name {} " + configObjName, e);
                    status = false;
                } catch (Exception e) {
                    logger.error("Error fetching JMX metrics for {} and mBean = {}", serverName, configObjName, e);
                    status = false;
                }
            }
        } finally {
            try {
                jmxConnectionAdapter.close(jmxConnector);
                logger.debug("JMX connection is closed for " + serverName);
            } catch (IOException e) {
                logger.error("Unable to close the JMX connection.");
            }
        }
    }


    public void onTaskComplete() {
        logger.debug("Task Complete");
        if (status ) {
            metricWriter.printMetric(metricPrefix + METRICS_SEPARATOR + server.get(DISPLAY_NAME).toString() + METRICS_SEPARATOR + AVAILABILITY, "1", "AVERAGE", "AVERAGE", "INDIVIDUAL");
        } else {
            metricWriter.printMetric(metricPrefix + METRICS_SEPARATOR + server.get(DISPLAY_NAME).toString() + METRICS_SEPARATOR + AVAILABILITY, "0", "AVERAGE", "AVERAGE", "INDIVIDUAL");
        }
    }

    public static class Builder {
        private JMXMonitorTask task = new JMXMonitorTask();

        Builder metricPrefix(String metricPrefix) {
            task.metricPrefix = metricPrefix;
            return this;
        }

        Builder metricWriter(MetricWriteHelper metricWriter) {
            task.metricWriter = metricWriter;
            return this;
        }

        Builder server(Map server) {
            task.server = server;
            return this;
        }

        Builder jmxConnectionAdapter(JMXConnectionAdapter adapter) {
            task.jmxConnectionAdapter = adapter;
            return this;
        }

        Builder mbeans(List<Map> mBeans) {
            task.configMBeans = mBeans;
            return this;
        }

        Builder monitorConfiguration(MonitorContextConfiguration monitorContextConfiguration) {
            task.monitorContextConfiguration = monitorContextConfiguration;
            return this;
        }

        JMXMonitorTask build() {
            return task;
        }
    }
}
