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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigUtil;

import javafx.stage.FileChooser;

/**
 * This class performs saving {@link StreamSis}'s variables to configuration file and reading variables from configuration file.
 * <p>
 * Internally it is working with single {@link Config} object which can be saved to file or read from file when necessary.
 * 
 * @see: {@link ConstsAndVars} for managing runtime variables.
 */
public final class CuteConfig {

	static final Logger logger = LoggerFactory.getLogger(CuteConfig.class);

	/** The options for saving configuration file */
	private static ConfigRenderOptions opts = ConfigRenderOptions.defaults().setJson(false).setOriginComments(false);

	/** {@link Config} object to work with */
	private static Config conf;

	/** True if {@link Config} is changed and needs to be saved */
	private static boolean needsSave;

	/** Path of fallback configuration file */
	private final static String fallbackConfigPath = "config/StreamSisFallback.conf";

	/** Path of configuration file */
	private static String configPath;

	/** The name of the root of all keys in {@link Config} */
	private final static String MAINKEY = "Root";

	/** The name of the main settings section in {@link Config} for {@link StreamSis}'s */
	public final static String CUTE = "StreamSis";

	/** The name of the GUI settings section in {@link Config} for {@link StreamSis}'s */
	public final static String GUI = "UserGui";

	/**
	 * The name of the utility GUI settings section in {@link Config} for {@link StreamSis}'s <br>
	 * Such as last window size, last opened directory for {@link FileChooser} etc
	 */
	public final static String UTILGUI = "UtilGUI";

	static {
		conf = loadConfig();
	}

	/**
	 * Loads {@link Config} from user data location. If not possible, uses fallback configuration.
	 *
	 * @return the {@link Config}
	 */
	public static Config loadConfig() {
		Config result = null;
		configPath = LowLevel.getAppDataPath() + "StreamSis.conf";
		logger.info("Loading configuration file: " + configPath + " ...");
		try {
			Config fallbackConfig = ConfigFactory.parseResources(fallbackConfigPath);
			if (fallbackConfig == null) {
				throw new RuntimeException("Fallback configuration file cannot be loaded. Probably problem with Maven");
			}
			result = ConfigFactory.parseFile(new File(configPath));
			if (result.isEmpty()) {
				logger.error("Can't load configuration file. Using fallback configuration");
			} else {
				logger.info("Configuration file successfully loaded");
			}
			result = result.withFallback(fallbackConfig);
		} catch (NullPointerException e) {
			logger.error("gosh");
		}
		return result;
	}

	/**
	 * Gets a variable from {@link Config} as string.
	 *
	 * @param key
	 *            the name of settings section
	 * @param subKey
	 *            the name of needed variable
	 * @return the string value of variable
	 */
	public static String getString(String key, String subKey) {
		try {
			return conf.getString(MAINKEY + "." + key + "." + subKey);
		} catch (ConfigException e) {
			e.printStackTrace();
			logger.error("Missing parameter in configuration: " + key + "." + subKey);
			return "";
		}
	}

	/**
	 * Gets a variable from {@link Config} as boolean value.
	 *
	 * @param key
	 *            the name of settings section
	 * @param subKey
	 *            the name of needed variable
	 * @return the boolean value of variable
	 */
	public static boolean getBoolean(String key, String subKey) {
		try {
			return conf.getBoolean(MAINKEY + "." + key + "." + subKey);
		} catch (ConfigException e) {
			e.printStackTrace();
			logger.error("Missing parameter in configuration: " + key + "." + subKey);
			return false;
		}
	}

	/**
	 * Gets a variable from {@link Config} as double value.
	 *
	 * @param key
	 *            the name of settings section
	 * @param subKey
	 *            the name of needed variable
	 * @return the double value of variable
	 */
	public static double getDouble(String key, String subKey) {
		try {
			return conf.getDouble(MAINKEY + "." + key + "." + subKey);
		} catch (ConfigException e) {
			e.printStackTrace();
			logger.error("Missing parameter in configuration: " + key + "." + subKey);
			return -1;
		}
	}

	/**
	 * Save {@link Config} to file.
	 */
	public static void saveConfig() {
		if (!needsSave) {
			needsSave = false;
			return;
		}
		Util.createFileAndDirectoriesAtPath(configPath);
		File file = new File(configPath);
		try (BufferedWriter br = new BufferedWriter(new FileWriter(file))) {
			br.write(conf.root().withOnlyKey(MAINKEY).render(opts));
			logger.info("Updated configuration file: " + configPath);
		} catch (IOException e) {
			logger.error("Can't rewrite configuration file for some reason: " + configPath);
			e.printStackTrace();
		}
	}

	/**
	 * Sets the double value for {@link Config} variable.
	 *
	 * @param key
	 *            the name of settings section
	 * @param subKey
	 *            the name of needed variable
	 * @param value
	 *            the value to set
	 */
	public static void setDoubleValue(String key, String subKey, double value) {
		setStringValue(key, subKey, String.valueOf(value));
	}

	/**
	 * Sets the boolean value for {@link Config} variable.
	 *
	 * @param key
	 *            the name of settings section
	 * @param subKey
	 *            the name of needed variable
	 * @param value
	 *            the value to set
	 */
	public static void setBooleanValue(String key, String subKey, boolean value) {
		setStringValue(key, subKey, String.valueOf(value));
	}

	/**
	 * Sets the string value for {@link Config} variable.
	 *
	 * @param key
	 *            the name of settings section
	 * @param subKey
	 *            the name of needed variable
	 * @param value
	 *            the value to set
	 */
	public static void setStringValue(String key, String subKey, String newValue) {
		String fullPath = MAINKEY + "." + key + "." + subKey;
		if (getString(key, subKey).equals(newValue)) {
			logger.debug(String.format("Configuration value unchanged. Key: %s.%s Value: %s", key, subKey, newValue));
			return;
		}
		String processedNewValue = ConfigUtil.quoteString(newValue);
		// Lets make new Config object with just a single variable
		// Also lets preserve the comments from original Config
		ConfigOrigin or = conf.getValue(fullPath).origin();
		StringBuilder toParse = new StringBuilder();
		for (String comment : or.comments()) {
			toParse.append("#").append(comment).append("\n");
		}
		toParse.append(fullPath).append("=").append(processedNewValue);
		Config newLittleConfig = ConfigFactory.parseString(toParse.toString());
		// Now we have our little Config with the single variable and old comments
		// Let's merge it with old Config
		conf = newLittleConfig.withFallback(conf);
		logger.info(String.format("Configuration update. Key: %s.%s Value: %s", key, subKey, newValue));
		needsSave = true;
	}

}
