/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.jmx;

import javax.management.Attribute;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 2/26/18.
 */
public class JMXUtil {

    public static final String REPLACE = "replace";
    public static final String REPLACE_WITH = "replaceWith";

    public static String convertToString(final Object field, final String defaultStr) {
        if (field == null) {
            return defaultStr;
        }
        return field.toString();
    }

    public static boolean isCompositeObject(String objectName) {
        return (objectName.indexOf('.') != -1);
    }

    public static String getMetricNameFromCompositeObject(String objectName) {
        return objectName.split("\\.")[0];
    }

    public static boolean isCurrentObjectComposite(Attribute attribute) {
     return attribute.getValue() instanceof CompositeData;
//        return attribute.getValue().getClass().equals(CompositeDataSupport.class);
    }

    public static boolean isCurrentObjectMap(Object attribute) {
        return attribute.getClass().equals(Map.class) || attribute.getClass().equals(HashMap.class);
    }

    public static boolean isCurrentAttributeMap(Attribute attribute) {
        return attribute.getValue().getClass().equals(Map.class) || attribute.getValue().getClass().equals(HashMap.class);
    }

    public static boolean isCurrentObjectList(Object attribute) {
        return attribute.getClass().equals(List.class) || attribute.getClass().equals(ArrayList.class) || attribute.getClass().equals(Array.class);
    }

    public static boolean isCurrentAttributeList(Attribute attribute) {
        return attribute.getValue().getClass().equals(List.class) || attribute.getValue().getClass().equals(Array.class) || attribute.getValue().getClass().equals(ArrayList.class);
    }

    public static String getMetricAfterCharacterReplacement(String replaceTextHere, List<Map<String, String>> metricReplacer) {

        for (Map chars : metricReplacer) {
            String replace = (String) chars.get(REPLACE);
            String replaceWith = (String) chars.get(REPLACE_WITH);

            if (replaceTextHere.contains(replace)) {
                replaceTextHere = replaceTextHere.replaceAll(replace, replaceWith);
            }
        }
        return replaceTextHere;
    }



}
