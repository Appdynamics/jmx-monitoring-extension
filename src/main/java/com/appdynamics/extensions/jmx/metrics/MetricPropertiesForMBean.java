package com.appdynamics.extensions.jmx.metrics;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.jmx.utils.Constants.*;

/**
 * Created by bhuvnesh.kumar on 3/13/19.
 */
public class MetricPropertiesForMBean {

    public static Map<String, ?> getMapOfProperties(Map mBean) {

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

    private static void setProps(Map metadata, Map props, String metricName, String alias) {
        if (metadata.get(ALIAS) != null) {
            props.put(ALIAS, metadata.get(ALIAS).toString());
        } else if (!Strings.isNullOrEmpty(alias)) {
            props.put(ALIAS, alias);
        } else {
            if (props.get(ALIAS) == null) {
                props.put(ALIAS, metricName);
            }
        }
        if (metadata.get(MULTIPLIER) != null) {
            props.put(MULTIPLIER, metadata.get(MULTIPLIER).toString());
        } else {
            if (props.get(MULTIPLIER) == null) {
                props.put(MULTIPLIER, "1");
            }
        }
        if (metadata.get(CONVERT) != null) {
            props.put(CONVERT, metadata.get(CONVERT));
        } else {
            if (props.get(CONVERT) == null) {
                props.put(CONVERT, (Map) null);
            }
        }
        if (metadata.get(DELTA) != null) {
            props.put(DELTA, metadata.get(DELTA).toString());

        } else {
            if (props.get(DELTA) == null) {
                props.put(DELTA, FALSE);
            }

        }
        if (metadata.get(CLUSTERROLLUPTYPE) != null) {
            props.put(CLUSTERROLLUPTYPE, metadata.get(CLUSTERROLLUPTYPE).toString());

        } else {
            if (props.get(CLUSTERROLLUPTYPE) == null) {
                props.put(CLUSTERROLLUPTYPE, INDIVIDUAL);
            }
        }
        if (metadata.get(TIMEROLLUPTYPE) != null) {
            props.put(TIMEROLLUPTYPE, metadata.get(TIMEROLLUPTYPE).toString());

        } else {
            if (props.get(TIMEROLLUPTYPE) == null) {
                props.put(TIMEROLLUPTYPE, AVERAGE);
            }
        }
        if (metadata.get(AGGREGATIONTYPE) != null) {
            props.put(AGGREGATIONTYPE, metadata.get(AGGREGATIONTYPE).toString());

        } else {
            if (props.get(AGGREGATIONTYPE) == null) {
                props.put(AGGREGATIONTYPE, AVERAGE);
            }
        }
    }

    public static List<String> getMBeanKeys(Map aConfigMBean) {
        List<String> mBeanKeys = (List) aConfigMBean.get(MBEANKEYS);
        return mBeanKeys;
    }


}
