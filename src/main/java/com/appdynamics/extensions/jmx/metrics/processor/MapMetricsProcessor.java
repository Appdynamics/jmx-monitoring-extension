/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.jmx.metrics.processor;

import com.appdynamics.extensions.jmx.metrics.MetricDetails;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import javax.management.Attribute;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.jmx.utils.Constants.PERIOD;

/**
 * Created by bhuvnesh.kumar on 3/11/19.
 */
public class MapMetricsProcessor extends BaseMetricsProcessor {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MapMetricsProcessor.class);

    @Override
    public void populateMetricsFromEntity(MetricDetails metricDetails, Attribute attribute) {
        String attributeName = attribute.getName(); // CompanyCount
        Map<String, Object> attributesValue = (Map<String, Object>) attribute.getValue(); //Map of regions
        List<Attribute> attributes = processAttributeValue(attributeName, attributesValue);
        for (Attribute attr : attributes) {
            super.populateMetricsFromEntity(metricDetails, attr);
        }
    }

    private List<Attribute> processAttributeValue(String attributeName, Map<String, Object> attributesValue) {
        List<Attribute> finalAttributesList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : attributesValue.entrySet()) {
            StringBuilder sb = new StringBuilder(attributeName);
            Object finalAttributeValue = null;
            do {
                sb.append(PERIOD).append(entry.getKey());
                finalAttributeValue = entry.getValue(); // {england=10}
            } while (finalAttributeValue instanceof Map);
            if (!(finalAttributeValue instanceof String)) {
                logger.debug("Found type {} which is Not supported", finalAttributeValue.getClass());
            } else {
                Attribute attribute = new Attribute(sb.toString(), finalAttributeValue.toString());
                finalAttributesList.add(attribute);
            }
        }
        return finalAttributesList;
    }

//    private void processAttributeValue(MetricDetails metricDetails, String attributeName, Map<String, ?> attributesFound) {
//        for (Object metricNameKey : attributesFound.keySet()) {
//            String key = attributeName + PERIOD + metricNameKey.toString();
//            Object attributeValue = attributesFound.get(metricNameKey);
//            Attribute mapMetric = new Attribute(key, attributeValue);
//            if (attributeValue instanceof Map) {
//                populateMetricsFromEntity(metricDetails, mapMetric);
//            } else {
//                super.populateMetricsFromEntity(metricDetails, mapMetric);
//            }
//        }
//    }
//
//    private void processAttributeValue1(MetricDetails metricDetails, String attributeName, Map<String, ?> attributesFound) {
//        for (Object metricNameKey : attributesFound.keySet()) {
//            Attribute mapMetricAttribute = getAttribute(attributeName, attributesFound, metricNameKey);
//            if (mapMetricAttribute.getValue() instanceof Map) {
//                populateMetricsFromEntity(metricDetails, mapMetricAttribute);
//            } else {
//                super.populateMetricsFromEntity(metricDetails, mapMetricAttribute);
//            }
//        }
//    }
//
//    private Attribute getAttribute(String attributeName, Map<String, ?> attributesFound, Object metricNameKey) {
//        String key = attributeName + PERIOD + metricNameKey.toString();
//        Object attributeValue = attributesFound.get(metricNameKey);
//        return new Attribute(key, attributeValue);
//    }

}
