package com.mikea.gae;

import com.google.appengine.api.utils.SystemProperty;

public class GaeUtil {
    public static boolean isDevMode() {
        return SystemProperty.environment.value().equals(SystemProperty.Environment.Value.Development);
    }
}