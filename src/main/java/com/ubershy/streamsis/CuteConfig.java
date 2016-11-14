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
 * {@link StreamSis} configuration manager.
 * <p>
 * Internally it is working with a single {@link Config} object which can be saved to file or read
 * from a file when necessary. Can provide default value for each configuration key.
 * 
 * @note: If adding new configuration key, default value for the key should be added to
 *        {@link #fallbackConfigResourcePath} file.
 * @see: {@link ConstsAndVars} for managing runtime variables.
 */
public final class CuteConfig {

	static final Logger logger = LoggerFactory.getLogger(CuteConfig.class);

	/** The options for saving configuration file. */
	private static ConfigRenderOptions opts = ConfigRenderOptions.defaults().setJson(false)
			.setOriginComments(false);

	/**
	 * The {@link Config} object in RAM to work with. Has {@link #fallbackConf} as fallback Config.
	 */
	private static Config conf;

	/** The fallback {@link Config} object to use as storage of default configuration values. */
	private static Config fallbackConf;

	/** True if {@link Config} is changed and needs to be saved. */
	private static boolean needsSave;

	/** Path of fallback configuration file {@link #fallbackConf}. */
	private final static String fallbackConfigResourcePath = "config/StreamSisFallback.conf";

	/** Path of configuration file {@link #conf}. */
	private static String configPath;

	/** The name of the root of all keys in {@link Config}. */
	private final static String MAINKEY = "Root";

	/** The name of the main settings section in {@link Config} for {@link StreamSis}. */
	public final static String CUTE = "StreamSis";

	/** The name of the GUI settings section in {@link Config} for {@link StreamSis}. */
	public final static String USERGUI = "UserGUI";

	/**
	 * The name of the utility GUI settings section in {@link Config} for {@link StreamSis}. <br>
	 * Such as last window size, last opened directory for {@link FileChooser}, etc.
	 */
	public final static String UTILGUI = "UtilGUI";

	/** The name of the Hotkey settings section in {@link Config} for {@link StreamSis}. */
	public final static String HOTKEYS = "Hotkeys";

	static {
		// Initialize conf and fallbackConf.
		configPath = LowLevel.getAppDataPath() + "StreamSis.conf";
		logger.info("Loading configuration file: " + configPath + " ...");
		try {
			fallbackConf = ConfigFactory.parseResources(fallbackConfigResourcePath);
			if (fallbackConf == null) {
				throw new RuntimeException("Fallback configuration file cannot be loaded."
						+ " Probably problem with Maven setup.");
			}
			conf = ConfigFactory.parseFile(new File(configPath));
			if (conf.isEmpty()) {
				logger.error(
						"Can't find or load configuration file. Using fallback configuration.");
			} else {
				logger.info("Configuration file successfully loaded to RAM.");
			}
			conf = conf.withFallback(fallbackConf);
		} catch (NullPointerException e) {
			logger.error("gosh");
		}
	}
	
	/**
	 * Gets a variable's value from configuration as String.
	 *
	 * @param key
	 *            The name of settings section.
	 * @param subKey
	 *            The name of needed variable.
	 * @return The string value of variable.
	 */
	private static String getStringFromConfig(Config config, String key, String subKey) {
		try {
			return config.getString(MAINKEY + "." + key + "." + subKey);
		} catch (ConfigException e) {
			throw new RuntimeException("Missing parameter in configuration: " + key + "." + subKey);
		}
	}

	/**
	 * Gets a variable's value from current configuration as String.
	 *
	 * @param key
	 *            The name of settings section.
	 * @param subKey
	 *            The name of needed variable.
	 * @return The string value of variable.
	 */
	public static String getString(String key, String subKey) {
		return getStringFromConfig(conf, key, subKey);
	}

	/**
	 * Gets a variable's value from configuration as boolean value.
	 *
	 * @param key
	 *            The name of settings section.
	 * @param subKey
	 *            The name of needed variable.
	 * @return The boolean value of variable.
	 */
	private static boolean getBooleanFromConfig(Config config, String key, String subKey) {
		try {
			return config.getBoolean(MAINKEY + "." + key + "." + subKey);
		} catch (ConfigException e) {
			throw new RuntimeException("Missing parameter in configuration: " + key + "." + subKey);
		}
	}

	/**
	 * Gets the default variable's value from fallback configuration as String.
	 *
	 * @param key
	 *            The name of settings section.
	 * @param subKey
	 *            The name of needed variable.
	 * @return The string value of variable.
	 */
	public static String getStringDefault(String key, String subKey) {
		return getStringFromConfig(fallbackConf, key, subKey);
	}

	/**
	 * Gets a variable's value from current configuration as boolean value.
	 *
	 * @param key
	 *            The name of settings section.
	 * @param subKey
	 *            The name of needed variable.
	 * @return The boolean value of variable.
	 */
	public static boolean getBoolean(String key, String subKey) {
		return getBooleanFromConfig(conf, key, subKey);
	}
	
	/**
	 * Gets the default variable's value from fallback configuration as boolean value.
	 *
	 * @param key
	 *            The name of settings section.
	 * @param subKey
	 *            The name of needed variable.
	 * @return The boolean value of variable.
	 */
	public static boolean getBooleanDefault(String key, String subKey) {
		return getBooleanFromConfig(fallbackConf, key, subKey);
	}

	/**
	 * Gets a variable's value from configuration as double value.
	 *
	 * @param key
	 *            The name of settings section.
	 * @param subKey
	 *            The name of needed variable.
	 * @return The double value of variable.
	 */
	public static double getDoubleFromConfig(Config config, String key, String subKey) {
		try {
			return config.getDouble(MAINKEY + "." + key + "." + subKey);
		} catch (ConfigException e) {
			throw new RuntimeException("Missing parameter in configuration: " + key + "." + subKey);
		}
	}

	/**
	 * Gets a variable's value from current configuration as double value.
	 *
	 * @param key
	 *            The name of settings section.
	 * @param subKey
	 *            The name of needed variable.
	 * @return The double value of variable.
	 */
	public static double getDouble(String key, String subKey) {
		return getDoubleFromConfig(conf, key, subKey);
	}

	/**
	 * Gets the default variable's value from fallback configuration as double value.
	 *
	 * @param key
	 *            The name of settings section.
	 * @param subKey
	 *            The name of needed variable.
	 * @return The double value of variable.
	 */
	public static double getDoubleDefault(String key, String subKey) {
		return getDoubleFromConfig(fallbackConf, key, subKey);
	}

	/**
	 * Saves current configuration {@link #conf} to file at {@link #configPath} path.
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
			logger.error("Can't rewrite configuration file for some reason: " + configPath, e);
		}
	}

	/**
	 * Sets the double value for current configuration's variable.
	 *
	 * @param key
	 *            The name of settings section.
	 * @param subKey
	 *            The name of needed variable.
	 * @param value
	 *            The value to set.
	 */
	public static void setDouble(String key, String subKey, double value) {
		setString(key, subKey, String.valueOf(value));
	}

	/**
	 * Sets the boolean value for current configuration's variable.
	 *
	 * @param key
	 *            The name of settings section.
	 * @param subKey
	 *            The name of needed variable.
	 * @param value
	 *            The value to set.
	 */
	public static void setBoolean(String key, String subKey, boolean value) {
		setString(key, subKey, String.valueOf(value));
	}

	/**
	 * Sets the String value for current configuration's variable.
	 *
	 * @param key
	 *            The name of settings section.
	 * @param subKey
	 *            The name of needed variable.
	 * @param value
	 *            The value to set.
	 */
	public static void setString(String key, String subKey, String newValue) {
		String fullPath = MAINKEY + "." + key + "." + subKey;
		if (getString(key, subKey).equals(newValue)) {
			logger.debug(String.format("Configuration value unchanged. Key: %s.%s Value: %s", key,
					subKey, newValue));
			return;
		}
		String processedNewValue = ConfigUtil.quoteString(newValue);
		// Lets make new Config object with just a single variable.
		// Also lets preserve the comments from the original Config.
		ConfigOrigin or = conf.getValue(fullPath).origin();
		StringBuilder toParse = new StringBuilder();
		for (String comment : or.comments()) {
			toParse.append("#").append(comment).append("\n");
		}
		toParse.append(fullPath).append("=").append(processedNewValue);
		Config newLittleConfig = ConfigFactory.parseString(toParse.toString());
		// Now we have our little Config with the single variable and old comments.
		// Let's merge it with the old Config.
		conf = newLittleConfig.withFallback(conf);
		logger.info(String.format("Configuration update in RAM. Key: %s.%s Value: %s", key, subKey,
				newValue));
		needsSave = true;
	}

}
