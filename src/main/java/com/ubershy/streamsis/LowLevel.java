/** 
 * StreamSis
 * Copyright (C) 2015 Eva Balycheva
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ubershy.streamsis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class provides platform-specific functionality for different operating systems. Also it
 * parses essential property file.
 */
public final class LowLevel {

	static {
		getOS();
	}

	/**
	 * The enumeration that represents a list with some Operating Systems.
	 */
	public enum OS {
		LINUX("Linux"), MAC("Mac OS"), WINDOWS("Windows");

		private String value;

		private OS(final String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	/** The application data path. */
	private static String applicationDataPath;

	/** The application name. */
	private static String applicationName;

	/** The application version. */
	private static String applicationVersion;

	/** The user's Operating System. */
	static private OS currentOS;

	/**
	 * Gets the path where the application stores it's data. (Cross-platform)
	 *
	 * @return the path with delimiter in the end where the application stores it's data <br>
	 *         Example on Linux: <b>/home/snaily/StreamSis/</b> <br>
	 *         Example on Windows: <b>C:\Users\snaily\AppData\Roaming\StreamSis\</b>
	 */
	public static String getAppDataPath() {
		if (LowLevel.applicationDataPath == null) {
			String dataPath = "";
			switch (getOS()) {
			case LINUX:
				dataPath = System.getProperty("user.home");
				break;
			case WINDOWS:
				dataPath = System.getenv("APPDATA");
				break;
			case MAC:
				dataPath = System.getProperty("user.home") + "/Library/Application Support";
				break;
			}
			LowLevel.applicationDataPath = dataPath + File.separator + LowLevel.getApplicationName()
					+ File.separator;
		}
		return LowLevel.applicationDataPath;
	}

	/**
	 * Gets the Application Name.
	 *
	 * @return the Application Name
	 */
	public static String getApplicationName() {
		if (LowLevel.applicationName == null) {
			try {
				LowLevel.applicationName = getValueFromPropertiesFile("application.name");
			} catch (IOException e) {
				System.out.println("Can't retrieve application name from the properties file.\n"
						+ "Using default 'StreamSis' as Application Name.\n");
				LowLevel.applicationName = "StreamSis";
			}
		}
		return LowLevel.applicationName;
	}

	/**
	 * Gets the Application Version.
	 *
	 * @return the Application Version
	 */
	public static String getApplicationVersion() {
		if (LowLevel.applicationVersion == null) {
			try {
				LowLevel.applicationVersion = getValueFromPropertiesFile("application.version");
			} catch (IOException e) {
				System.out.println("Can't retrieve application version from the properties file.\n"
						+ "Using default '0.0.1-SNAPSHOT' as Application Version.\n");
				LowLevel.applicationVersion = "0.0.1-SNAPSHOT";
			}
		}
		return LowLevel.applicationVersion;
	}

	/**
	 * Gets the current OS.
	 *
	 * @return the instance of LowLevel.OS enum
	 */
	public static OS getOS() {
		if (currentOS == null) {
			String OSName = System.getProperty("os.name").toUpperCase();
			if (OSName.contains("NUX"))
				currentOS = OS.LINUX; // I still haven't tested StreamSis on linux.
			else if (OSName.contains("WIN"))
				currentOS = OS.WINDOWS;
			else if (OSName.contains("MAC")) // I don't have Mac OS to test StreamSis.
				currentOS = OS.MAC;
			else {
				System.out.println("Dear User. Your operating system is truly amazing, but... "
						+ " it is not yet supported.\n" + "Please enjoy application crash.");
				throw new RuntimeException();
			}
		}
		return currentOS;
	}

	/**
	 * Parses the properties file("props/app.properties") to acquire the Value of chosen property.
	 * Explanation: After each successful build the properties file must contain Valid property
	 * values, because the original source properties file is always filtered by Maven. E.g.
	 * "application.name=${pom.name}" can become "application.name=StreamSis".
	 *
	 * @return the property Value parsed from "props/app.properties"
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static String getValueFromPropertiesFile(String propertyName) throws IOException {
		Properties properties = new Properties();
		String propertyPath = "props/app.properties";
		InputStream inputStream = ConstsAndVars.class.getClassLoader()
				.getResourceAsStream(propertyPath);
		if (inputStream != null) {
			properties.load(inputStream);
		} else {
			throw new IOException(
					"No such property (Could be Maven misconfiguration): " + propertyPath);
		}
		String propertyValue = properties.getProperty(propertyName);
		return propertyValue;
	}
}
