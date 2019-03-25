/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.jmx.utils;

// TODO remove unused imports
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.jmx.utils.Constants.REPLACE;
import static com.appdynamics.extensions.jmx.utils.Constants.REPLACE_WITH;

/**
 * Created by bhuvnesh.kumar on 2/26/18.
 */
public class JMXUtil {

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



}
