/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

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
// TODO this can be simplified. Its very confusing and unclear, all you have to do is iterate thru map instead of Object
//  if you dont' want to use name and alias separately in config, then handle first entry separately after that all you
//  have to do is add the properties to the map. Also there is no example for global properties and global properties
//  are not needed imo, every metric should have own properties. we can discuss this lmk
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
                // TODO you don't have to do this taken care of in commons
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
