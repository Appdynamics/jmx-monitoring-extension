/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
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
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import org.slf4j.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.jmx.utils.Constants.*;

/**
 * Created by bhuvnesh.kumar on 2/23/18.
 */
public class JMXMonitorTask implements AMonitorTaskRunnable {
    // TODO use ExtensionsLoggerFactory
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(JMXMonitorTask.class);
    private Boolean status = true;
    private String metricPrefix;
    private MetricWriteHelper metricWriter;
    private Map server;
    private JMXConnectionAdapter jmxConnectionAdapter;
    private List<Map> configMBeans;
    private MonitorContextConfiguration monitorContextConfiguration;

    private String serverName;


    // TODO if you are using a Builder then constructor should be private,
    // TODO remove builder and use parameterized constructor
    //  if it is used directly object state will be inconsistent

    public void run() {
        serverName = (String) server.get(DISPLAY_NAME);

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

    private void populateAndPrintStats() {
        JMXConnector jmxConnector = null;
        long previousTimestamp = 0;
        long currentTimestamp = 0;
        try {
            previousTimestamp = System.currentTimeMillis();
            // TODO why are you throwing IOException here, you have surrounded this in try you can catch it here itself
            jmxConnector = jmxConnectionAdapter.open();
            currentTimestamp = System.currentTimeMillis();
            logger.debug("Time to open connection for " + serverName + " in milliseconds: " + (currentTimestamp - previousTimestamp));

            for (Map mBean : configMBeans) {
                String configObjName = JMXUtil.convertToString(mBean.get(OBJECT_NAME), EMPTY_STRING);
                logger.debug("Processing mBean {} from the config file", configObjName);
                try {
                    JMXMetricsProcessor jmxMetricsProcessor = new JMXMetricsProcessor(monitorContextConfiguration,
                            jmxConnectionAdapter, jmxConnector);
                    List<Metric> nodeMetrics = jmxMetricsProcessor.getJMXMetrics(mBean,
                            metricPrefix, serverName);
                    if (nodeMetrics.size() > 0) {
                        metricWriter.transformAndPrintMetrics(nodeMetrics);
                    } else {
                        logger.debug("No metrics being sent from : " + serverName);
                    }
// TODO: Apply a more generic catch to cover other JMX exceptions ... JMXExcepetion
                    // TODO CATCH the following MalformedObjectNameException, IOException, IntrospectionException, InstanceNotFoundException,ReflectionException
                } catch (MalformedObjectNameException e) {
                    logger.error("Illegal Object Name {} " + configObjName, e);
                    status = false;
                } catch (ReflectionException e) {
                    e.printStackTrace();
                } catch (IntrospectionException e) {
                    e.printStackTrace();
                } catch (InstanceNotFoundException e) {
                    e.printStackTrace();
                }
                // TODO why does status has to be set to false for any kind of exception, looking at the java doc
                //  comments of the method I think this should happen only in the case of IOException. Also I think that
                //  in case of IOException it will keep repeating for all the mbeans, I am not sure please confirm.
                //  If that is the case then IOException can be moved out to outer try and the inner try can catch other
                //  exception individually. lmk
            }
        } catch (IOException e) {
            logger.error("Unable to close the JMX connection for Server : " + serverName, e);
            status = false;
        } catch (Exception e) {
            logger.error("Unable to close the JMX connection for Server : " + serverName, e);
        } finally {
            //TODO: Missing Heartbeat metrics
            try {
                jmxConnectionAdapter.close(jmxConnector);
                logger.debug("JMX connection is closed for " + serverName);
            } catch (IOException e) {
                logger.error("Unable to close the JMX connection.", e);

            }
        }
    }


    public void onTaskComplete() {
        logger.debug("Task Complete");
        String metricValue = status ? "1" : "0";

            metricWriter.printMetric(metricPrefix + METRICS_SEPARATOR + server.get(DISPLAY_NAME).toString() + METRICS_SEPARATOR + AVAILABILITY, metricValue, "AVERAGE", "AVERAGE", "INDIVIDUAL");
    }

     static class Builder {
        // TODO when using Builder it is better to use parametrized constructor with required fields as parameters to
        //  the constructor for the class that is being built. Also you should not be creating an object before the builder.build() is called.
        //  The build method should check if all required fields are initialized and then call the constructor,
        //  I think for JMXMonitor tasks all fields are required, if I use this builder I can easily create inconsistent
        //  objects example I can forget to initialize MetricWriteHelper or the server, there is nothing stopping me from doing that,
        //  please correct this design

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
