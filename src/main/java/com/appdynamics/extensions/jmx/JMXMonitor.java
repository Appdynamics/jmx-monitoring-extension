/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.jmx;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.jmx.commons.JMXConnectionAdapter;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.util.CryptoUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.jmx.utils.Constants.*;
import static com.appdynamics.extensions.jmx.utils.JMXUtil.convertToString;

/**
 * Created by bhuvnesh.kumar on 2/23/18.
 */
public class JMXMonitor extends ABaseMonitor {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(JMXMonitor.class);

    @Override
    protected String getDefaultMetricPrefix() {
        return CUSTOMMETRICS + METRICS_SEPARATOR + MONITORNAME;
    }

    @Override
    public String getMonitorName() {
        return MONITORNAME;
    }

    @Override
    protected void doRun(TasksExecutionServiceProvider taskExecutor) {
        Map<String, ?> config = getContextConfiguration().getConfigYml();
        if (config != null) {
            List<Map> servers = (List) config.get(SERVERS);
            AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
            if (servers != null && !servers.isEmpty()) {
                for (Map server : servers) {
                    try {
                        JMXMonitorTask task = createTask(server, taskExecutor);
                        taskExecutor.submit((String) server.get(DISPLAY_NAME), task);
                    } catch (IOException e) {
                        logger.error("Cannot construct JMX uri for {}", convertToString(server.get(DISPLAY_NAME), ""));
                    }
                }
            } else {
                logger.error("There are no servers configured");
            }
        } else {
            logger.error("The config.yml is not loaded due to previous errors.The task will not run");
        }
    }

    protected List<Map<String, ?>> getServers() {
        List<Map<String, ?>> servers = (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get(SERVERS);
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        return servers;
    }

    private JMXMonitorTask createTask(Map server, TasksExecutionServiceProvider taskExecutor) throws IOException {
        String serviceUrl = convertToString(server.get(SERVICEURL), EMPTY_STRING);
        String host = convertToString(server.get(HOST), EMPTY_STRING);
        String portStr = convertToString(server.get(PORT), EMPTY_STRING);
        int port = (portStr == null || portStr == EMPTY_STRING) ? -1 : Integer.parseInt(portStr);
        String username = convertToString(server.get(USERNAME), EMPTY_STRING);
        String password = getPassword(server);
        JMXConnectionAdapter adapter = JMXConnectionAdapter.create(serviceUrl, host, port, username, password);
        return new JMXMonitorTask.Builder().
                metricPrefix(getContextConfiguration().getMetricPrefix()).
                metricWriter(taskExecutor.getMetricWriteHelper()).
                jmxConnectionAdapter(adapter).server(server).
                mbeans((List<Map>) getContextConfiguration().getConfigYml().get(MBEANS)).
                monitorConfiguration(getContextConfiguration()).build();
    }

    private String getPassword(Map server) {
        if (getContextConfiguration().getConfigYml().get(ENCRYPTION_KEY) != null) {
            String encryptionKey = getContextConfiguration().getConfigYml().get(ENCRYPTION_KEY).toString();
            server.put(ENCRYPTION_KEY, encryptionKey);
        }
        return CryptoUtils.getPassword(server);
    }
}
