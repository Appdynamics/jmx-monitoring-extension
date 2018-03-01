/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.jmx;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TaskInputArgs;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.jmx.JMXUtil.convertToString;
import static com.appdynamics.extensions.jmx.metrics.Constants.*;

/**
 * Created by bhuvnesh.kumar on 2/23/18.
 */
public class JMXMonitor extends ABaseMonitor {


    private static final Logger logger = LoggerFactory.getLogger(JMXMonitor.class);

    //Required for MonitorConfiguration initialisation
    @Override
    protected String getDefaultMetricPrefix() {
        return CUSTOMMETRICS + METRICS_SEPARATOR + MONITORNAME;
    }

    //Required for MonitorConfiguration initialisation
    @Override
    public String getMonitorName() {
        return MONITORNAME;
    }


    @Override
    protected void doRun(TasksExecutionServiceProvider taskExecutor) {
        Map<String, ?> config = configuration.getConfigYml();
        if (config != null) {
            List<Map> servers = (List) config.get(SERVERS);
            AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
            if (servers != null && !servers.isEmpty()) {
                for (Map server : servers) {
                    try {
                        JMXMonitorTask task = createTask(server, taskExecutor);
                        taskExecutor.submit((String) server.get(NAME), task);
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

    @Override
    protected int getTaskCount() {
        List<Map<String, String>> servers = (List<Map<String, String>>) configuration.getConfigYml().get(SERVERS);
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        return servers.size();
    }

    private JMXMonitorTask createTask(Map server, TasksExecutionServiceProvider taskExecutor) throws IOException {

        String serviceUrl = convertToString(server.get(SERVICEURL), "");
        String host = convertToString(server.get(HOST), "");
        String portStr = convertToString(server.get(PORT), "");
        int port = (portStr == null || portStr == "") ? -1 : Integer.parseInt(portStr);
        String username = convertToString(server.get(USERNAME), "");
        String password = getPassword(server);

        JMXConnectionAdapter adapter = JMXConnectionAdapter.create(serviceUrl, host, port, username, password);
        return new JMXMonitorTask.Builder().
                metricPrefix(configuration.getMetricPrefix()).
                metricWriter(taskExecutor.getMetricWriteHelper()).
                jmxConnectionAdapter(adapter).server(server).
                mbeans((List<Map>) configuration.getConfigYml().get(MBEANS)).build();
    }

    private String getPassword(Map server) {
        String password = convertToString(server.get(PASSWORD), "");
        if (!Strings.isNullOrEmpty(password)) {
            return password;
        }
        String encryptionKey = convertToString(configuration.getConfigYml().get(ENCRYPTION_KEY), "");
        String encryptedPassword = convertToString(server.get(ENCRYPTEDPASSWORD), "");
        if (!Strings.isNullOrEmpty(encryptionKey) && !Strings.isNullOrEmpty(encryptedPassword)) {
            java.util.Map<String, String> cryptoMap = Maps.newHashMap();
            cryptoMap.put(PASSWORD_ENCRYPTED, encryptedPassword);
            cryptoMap.put(TaskInputArgs.ENCRYPTION_KEY, encryptionKey);
            return CryptoUtil.getPassword(cryptoMap);
        }
        return null;
    }


    public static void main(String[] args) throws TaskExecutionException {
        JMXMonitor JMXMonitor = new JMXMonitor();
        Map<String, String> argsMap = new HashMap<String, String>();
        argsMap.put("config-file", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/jmx-monitoring-extension/src/test/resources/conf/config.yml");
        JMXMonitor.execute(argsMap, null);
    }
}
