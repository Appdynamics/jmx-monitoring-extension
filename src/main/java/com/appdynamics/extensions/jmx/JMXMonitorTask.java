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
import com.appdynamics.extensions.jmx.metrics.JMXMetricsProcessor;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.jmx.metrics.Constants.*;

/**
 * Created by bhuvnesh.kumar on 2/23/18.
 */
public class JMXMonitorTask implements AMonitorTaskRunnable {
    private Boolean status = true;

    private static final Logger logger = LoggerFactory.getLogger(JMXMonitorTask.class);
    private String metricPrefix;
    private MetricWriteHelper metricWriter;
    private Map server;
    private JMXConnectionAdapter jmxConnectionAdapter;
    private List<Map> configMBeans;

    private String serverName;

    public void run() {
        serverName = JMXUtil.convertToString(server.get(DISPLAY_NAME), "");

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

        try {
            jmxConnector = jmxConnectionAdapter.open();
            logger.debug("JMX Connection is now open");

            for (Map mBean : configMBeans) {
                String configObjName = JMXUtil.convertToString(mBean.get(OBJECT_NAME), "");
                logger.debug("Processing mBean {} from the config file", configObjName);

                try {
                    Map<String, ?> metricProperties = getMapOfProperties(mBean);
                    JMXMetricsProcessor jmxMetricsProcessor = new JMXMetricsProcessor(jmxConnectionAdapter, jmxConnector);

                    List<Metric> nodeMetrics = jmxMetricsProcessor.getJMXMetrics(mBean, metricProperties, metricPrefix);

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
                logger.debug("JMX connection is closed.");
            } catch (IOException e) {
                logger.error("Unable to close the JMX connection.");
            }
        }
    }

    public Map<String, ?> getMapOfProperties(Map mBean) {

        Map<String, ? super Object> metricPropsMap = Maps.newHashMap();
        if (mBean == null || mBean.isEmpty()) {
            return metricPropsMap;
        }

        Map configMetrics = (Map) mBean.get(METRICS);
        List includeMetrics = (List) configMetrics.get(INCLUDE);

        if (includeMetrics != null) {
            for (Object metad : includeMetrics) {
                Map localMetaData = (Map) metad;
                Map.Entry entry = (Map.Entry) localMetaData.entrySet().iterator().next();
                String metricName = entry.getKey().toString();
                String alias = entry.getValue().toString();

                Map<String, ? super Object> metricProperties = new HashMap<String, Object>();
                metricProperties.put(ALIAS, Strings.isNullOrEmpty(alias) ? metricName : alias);

                setProps(mBean, metricProperties, metricName, alias); //global level
                setProps(localMetaData, metricProperties, metricName, alias); //local level
                metricPropsMap.put(metricName, metricProperties);
            }
        }
        return metricPropsMap;
    }

    private void setProps(Map metadata, Map props, String metricName, String alias) {
        if (metadata.get(ALIAS) != null) {
            props.put(ALIAS, metadata.get(ALIAS).toString());
        }else if(!Strings.isNullOrEmpty(alias)){
            props.put(ALIAS, alias);
        }
        else {
            props.put(ALIAS, metricName);
        }
        if (metadata.get(MULTIPLIER) != null) {
            props.put(MULTIPLIER, metadata.get(MULTIPLIER).toString());
        } else {
            props.put(MULTIPLIER, "1");
        }
        if (metadata.get(CONVERT) != null) {
            props.put(CONVERT, metadata.get(CONVERT).toString());
        } else {
            props.put(CONVERT, (Map) null);
        }
        if (metadata.get(DELTA) != null) {
            props.put(DELTA, metadata.get(DELTA).toString());

        } else {
            props.put(DELTA, FALSE);
        }
        if (metadata.get(CLUSTERROLLUPTYPE) != null) {
            props.put(CLUSTERROLLUPTYPE, metadata.get(CLUSTERROLLUPTYPE).toString());

        } else {
            props.put(CLUSTERROLLUPTYPE, INDIVIDUAL);
        }
        if (metadata.get(TIMEROLLUPTYPE) != null) {
            props.put(TIMEROLLUPTYPE, metadata.get(TIMEROLLUPTYPE).toString());

        } else {
            props.put(TIMEROLLUPTYPE, AVERAGE);
        }
        if (metadata.get(AGGREGATIONTYPE) != null) {
            props.put(AGGREGATIONTYPE, metadata.get(AGGREGATIONTYPE).toString());

        } else {
            props.put(AGGREGATIONTYPE, AVERAGE);
        }
    }

    public void onTaskComplete() {
        logger.debug("Task Complete");
        if (status == true) {
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

        JMXMonitorTask build() {
            return task;
        }
    }
}
