package com.appdynamics.extensions.jmx.metrics;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.jmx.JMXMonitorTask;
import com.appdynamics.extensions.jmx.commons.JMXConnectionAdapter;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.management.*;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.remote.JMXConnector;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bhuvnesh.kumar on 2/25/19.
 */
public class JMXMetricsProcessorForListsTest {

    JMXConnector jmxConnector = mock(JMXConnector.class);
    JMXConnectionAdapter jmxConnectionAdapter = mock(JMXConnectionAdapter.class);
    private MonitorContextConfiguration monitorContextConfiguration = new MonitorContextConfiguration("JMXMonitor",
            "Custom Metrics|JMXMonitor|", Mockito.mock(File.class), Mockito.mock(AMonitorJob.class));
    List<Map<String, String>> metricReplacer = new ArrayList<Map<String, String>>();
    @Before
    public void before() {
        monitorContextConfiguration.setConfigYml("src/test/resources/conf/config_for_lists.yml");
        Map<String, String> replace1 = new HashMap<String, String>();
        replace1.put("replace","ms");
        replace1.put("replaceWith","");

        Map<String, String> replace2 = new HashMap<String, String>();
        replace2.put("replace","%");
        replace2.put("replaceWith","");

        metricReplacer.add(replace1);
        metricReplacer.add(replace2);


    }
    @Test
    public void getListMetricsThroughJMX() throws MalformedObjectNameException, IntrospectionException, ReflectionException,
            InstanceNotFoundException, IOException, OpenDataException {
        Map config = YmlReader.readFromFileAsMap(new File(this.getClass().getResource("/conf/config_with_list.yml").getFile()));
        List<Map> mBeans = (List) config.get("mbeans");
        Set<ObjectInstance> objectInstances = Sets.newHashSet();
        objectInstances.add(new ObjectInstance("org.apache.activemq.metrics:type=ClientRequest,scope=Read,name=Latency", "test"));



        List<String> listData = Lists.newArrayList();
        listData.add("metric one : 11ms");
        listData.add("metric two : 12%");
        listData.add("metric three : 13");

        Attribute listAttribute = new Attribute("listOfString", listData);
        List<Attribute> attributes = Lists.newArrayList();
        attributes.add(listAttribute);
        attributes.add(new Attribute("Max", new BigDecimal(200)));
        attributes.add(new Attribute("HeapMemoryUsage", createCompositeDataSupportObject()));

        List<String> metricNames = Lists.newArrayList();
        metricNames.add("metric1");
        metricNames.add("metric2");
        when(jmxConnectionAdapter.queryMBeans(eq(jmxConnector), Mockito.any(ObjectName.class))).thenReturn(objectInstances);
        when(jmxConnectionAdapter.getReadableAttributeNames(eq(jmxConnector), Mockito.any(ObjectInstance.class))).thenReturn(metricNames);
        when(jmxConnectionAdapter.getAttributes(eq(jmxConnector), Mockito.any(ObjectName.class), Mockito.any(String[].class))).thenReturn(attributes);
//        when(monitorContextConfiguration.getConfigYml().get("metricCharacterReplacer")).thenReturn(metricReplacer);

        JMXMetricsProcessor jmxMetricsProcessor = new JMXMetricsProcessor(monitorContextConfiguration,jmxConnectionAdapter, jmxConnector);
        JMXMonitorTask activeMQMonitorTask = new JMXMonitorTask();
        Map<String, ?> metricPropertiesMap = activeMQMonitorTask.getMapOfProperties(mBeans.get(0));
        List<Metric> metrics = jmxMetricsProcessor.getJMXMetrics(mBeans.get(0), metricPropertiesMap, "", "");

        Assert.assertTrue(metrics.get(0).getMetricPath().equals("ClientRequest|Read|Latency|listOfString.metric one"));
        Assert.assertTrue(metrics.get(0).getMetricName().equals("listOfString.metric one"));
        Assert.assertTrue(metrics.get(0).getMetricValue().equals("11"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));

        Assert.assertTrue(metrics.get(1).getMetricPath().equals("ClientRequest|Read|Latency|listOfString.metric two"));
        Assert.assertTrue(metrics.get(1).getMetricName().equals("listOfString.metric two"));
        Assert.assertTrue(metrics.get(1).getMetricValue().equals("12"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(0).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));

        Assert.assertTrue(metrics.get(2).getMetricPath().equals("ClientRequest|Read|Latency|listOfString.metric three"));
        Assert.assertTrue(metrics.get(2).getMetricName().equals("listOfString.metric three"));
        Assert.assertTrue(metrics.get(2).getMetricValue().equals("13"));
        Assert.assertTrue(metrics.get(2).getMetricProperties().getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(2).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(2).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));

        Assert.assertTrue(metrics.get(3).getMetricPath().equals("ClientRequest|Read|Latency|Max"));
        Assert.assertTrue(metrics.get(3).getMetricName().equals("Max"));
        Assert.assertTrue(metrics.get(3).getMetricValue().equals("200"));
        Assert.assertTrue(metrics.get(3).getMetricProperties().getAggregationType().equals("OBSERVATION"));
        Assert.assertTrue(metrics.get(3).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(3).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));

        Assert.assertTrue(metrics.get(4).getMetricPath().equals("ClientRequest|Read|Latency|HeapMemoryUsage.max"));
        Assert.assertTrue(metrics.get(4).getMetricName().equals("HeapMemoryUsage.max"));
        Assert.assertTrue(metrics.get(4).getMetricValue().equals("100"));
        Assert.assertTrue(metrics.get(4).getMetricProperties().getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(4).getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
        Assert.assertTrue(metrics.get(4).getMetricProperties().getTimeRollUpType().equals("AVERAGE"));
        Assert.assertTrue(metrics.get(4).getMetricProperties().getDelta() == false);
        Assert.assertTrue(metrics.get(4).getMetricProperties().getMultiplier().compareTo(new BigDecimal(10)) == 0);
    }


    private CompositeDataSupport createCompositeDataSupportObject() throws OpenDataException {
        String typeName = "type";
        String description = "description";
        String[] itemNames = {"max", "used"};
        String[] itemDescriptions = {"maxDesc", "usedDesc"};
        OpenType<?>[] itemTypes = new OpenType[]{new OpenType("java.lang.String", "type", "description") {
            @Override
            public boolean isValue(Object obj) {
                return true;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public String toString() {
                return "100";
            }
        }, new OpenType("java.lang.String", "type", "description") {
            @Override
            public boolean isValue(Object obj) {
                return true;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public String toString() {
                return "50";
            }
        }};

        CompositeType compositeType = new CompositeType(typeName, description, itemNames, itemDescriptions, itemTypes);

        String[] itemNamesForCompositeDataSupport = {"max", "used"};
        Object[] itemValuesForCompositeDataSupport = {new BigDecimal(100), new BigDecimal(50)};
        return new CompositeDataSupport(compositeType, itemNamesForCompositeDataSupport,
                itemValuesForCompositeDataSupport);
    }

}
