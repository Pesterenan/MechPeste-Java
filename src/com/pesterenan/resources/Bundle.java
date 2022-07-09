package com.pesterenan.resources;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Bundle {
	public static final String BUNDLE_NAME = Bundle.class.getPackage().getName() + ".MechPesteBundle"; //$NON-NLS-1$

	public static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Bundle() {
		System.out.println(BUNDLE_NAME);
		System.out.println(RESOURCE_BUNDLE);
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
