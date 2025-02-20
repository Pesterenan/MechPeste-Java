package com.pesterenan.resources;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Bundle {
    public static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("MechPesteBundle",
            Locale.getDefault());

    private Bundle() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
