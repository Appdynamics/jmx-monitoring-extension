/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */


package com.appdynamics.extensions.jmx.utils;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.util.PathResolver;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public  class SSLUtilsTest {

    @Before
    public void setUpConnectionWithoutSSL(){

        Properties props = new Properties();
        props.setProperty("com.sun.management.jmxremote.authenticate", "false");
        props.setProperty("com.sun.management.jmxremote.ssl", "false");
        props.setProperty("com.sun.management.jmxremote.registry.ssl", "false");
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");

        JMXConnectorServer server = sun.management.jmxremote.ConnectorBootstrap
                .startRemoteConnectorServer("11199", props);
    }

    @Test
    public void whenNotUsingSslThenTestServerConnection() throws Exception {

        JMXServiceURL serviceUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://127.0.0.1:11199/jmxrmi");
        Map env = new HashMap();
        JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, env);
        Assert.assertNotNull(jmxConnector);

    }

//    @Before
//    public void setUpConnectionWithSslAndCorrectKeys(){
//        System.setProperty("javax.net.ssl.keyStore", "src/test/resources/keystore/broker.ks");
//        System.setProperty("javax.net.ssl.keyStorePassword", "password");
//        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
//        System.setProperty("com.sun.management.jmxremote.port", "11199");
//        MonitorContextConfiguration contextConfiguration = new MonitorContextConfiguration
//                ("JMX Monitor",
//                        "Custom Metrics|JMX Monitor|", PathResolver.resolveDirectory(AManagedMonitor.class),
//                        Mockito.mock(AMonitorJob.class));
//        contextConfiguration.setConfigYml("src/test/resources/conf/config_ssl_correct_keys.yml");
//        Map configMap = contextConfiguration.getConfigYml();
//        SslUtils sslUtils = new SslUtils();
//        sslUtils.setSslProperties(configMap);
//        Properties connectionProperties = new Properties();
//        connectionProperties.setProperty("com.sun.management.jmxremote.authenticate", "false");
//        connectionProperties.setProperty("com.sun.management.jmxremote.ssl", "true");
//        connectionProperties.setProperty("com.sun.management.jmxremote.registry.ssl", "false");
//
//        JMXConnectorServer server = sun.management.jmxremote.ConnectorBootstrap
//              .startRemoteConnectorServer("11199", connectionProperties);
//    }
//
//    @Test
//    public void whenUsingSslAndCorrectKeysThenTestServerConnection() throws Exception {
//        JMXServiceURL serviceUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://127.0.0.1:11199/jmxrmi");
//        Map env = new HashMap();
//        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new SslRMIClientSocketFactory());
//        env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, new SslRMIServerSocketFactory());
//        JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, env);
//        Assert.assertNotNull(jmxConnector);
//    }

//    @Before
//    public void setUpConnectionWithIncorrectKeys(){
//        System.setProperty("javax.net.ssl.keyStore", "src/test/resources/keystore/broker.ks");
//        System.setProperty("javax.net.ssl.keyStorePassword", "password");
//        MonitorContextConfiguration contextConfiguration = new MonitorContextConfiguration
//                ("Kafka Monitor",
//                        "Custom Metrics|JMX Monitor|", PathResolver.resolveDirectory(AManagedMonitor.class),
//                        Mockito.mock(AMonitorJob.class));
//        contextConfiguration.setConfigYml("src/test/resources/conf/config_ssl_incorrect_keys.yml");
//        Map configMap = contextConfiguration.getConfigYml();
//        SslUtils sslUtils = new SslUtils();
//        sslUtils.setSslProperties(configMap);
//        Properties props = new Properties();
//        props.setProperty("com.sun.management.jmxremote.authenticate", "false");
//        props.setProperty("com.sun.management.jmxremote.ssl", "true");
//        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
//        JMXConnectorServer server = sun.management.jmxremote.ConnectorBootstrap
//              .startRemoteConnectorServer("11199", props);
//    }
//
//
//    @Test
//    public void testSSLServerConnectionWithIncorrectTrustStore() {
//        try {
//            JMXServiceURL serviceUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://127.0.0.1:11199/jmxrmi");
//            Map env = new HashMap();
//            env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new SslRMIClientSocketFactory());
//            env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, new SslRMIServerSocketFactory());
//            JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, env);
//        } catch (MalformedURLException e) {
//
//        } catch (IOException e) {
//            Assert.assertEquals( e.getCause().toString(),
//                    "javax.net.ssl.SSLException: java.lang.RuntimeException: " +
//                            "Unexpected error: java.security.InvalidAlgorithmParameterException: " +
//                            "the trustAnchors parameter must be non-empty");
//        }
//    }



}