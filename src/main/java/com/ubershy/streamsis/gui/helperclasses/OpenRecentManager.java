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
package com.ubershy.streamsis.gui.helperclasses;

import java.util.LinkedList;

import com.ubershy.streamsis.CuteConfig;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This class manages "Open Recent Project" Menu in GUI of StreamSis <br>
 * It automatically saves the list of recent Projects to configuration file when it's needed.
 * <p>
 * Usage: <br>
 * Pass file path of opened Project using {@link #setRecentProject(String)} <br>
 * And take fresh sorted list of Project file paths using {@link #getRecentProjectPathsList()} to generate menu from it. <br>
 * <i>That's it!</i>
 */
public final class OpenRecentManager {
	/**
	 * The internal list of file paths to work with. <br>
	 * Sometimes we synchronize it to external list which can be bound to GUI.
	 */
	static ObservableList<String> internalList = FXCollections.observableList(new LinkedList<String>());

	/**
	 * The external list of file paths. <br>
	 * Can be bound to GUI to generate menu. <br>
	 * Updates only when it's necessary.
	 */
	static ObservableList<String> externalList = FXCollections.observableList(new LinkedList<String>());

	/**
	 * Fills lists of file paths by the values from config.
	 */
	private static void getValuesFromConfig() {
		internalList.clear();
		for (int i = 0; i < 10; i++) {
			internalList.add(CuteConfig.getString(CuteConfig.UTILGUI, "OpenRecent" + i));
		}
		externalList.setAll(internalList);
	}

	/**
	 * Sets the values to config from internal list of file paths.
	 */
	private static void setValuesToConfig() {
		int sizeOfList = internalList.size();
		String valueToSet;
		for (int i = 0; i < 10; i++) {
			if (i < sizeOfList) {
				valueToSet = internalList.get(i);
			} else {
				valueToSet = "";
			}
			CuteConfig.setStringValue(CuteConfig.UTILGUI, "OpenRecent" + i, valueToSet);
		}
		CuteConfig.saveConfig();
	}

	/**
	 * Gets the recent list of Project file paths.
	 *
	 * @return the recent item paths list
	 */
	public static ObservableList<String> getRecentProjectPathsList() {
		if (externalList.isEmpty())
			getValuesFromConfig();
		return externalList;
	}

	/**
	 * Considers the recently opened Project. And based on it rebuilds his list of Project file paths.
	 *
	 * @param path
	 *            the path of recently opened Project
	 */
	public static void setRecentProject(String path) {
		int existingIndex = internalList.indexOf(path);
		if (existingIndex != -1) {
			if (existingIndex != 0) {
				internalList.remove(existingIndex);
				internalList.add(path); // add element to the end
				FXCollections.rotate(internalList, 1); // now element is first
			} else {
				return; // Nothing changed
			}
		} else {
			internalList.add(path); // add element to the end
			FXCollections.rotate(internalList, 1); // now element is first
		}
		if (internalList.size() > 10) {
			internalList.remove(10);
		}
		setValuesToConfig();
		externalList.setAll(internalList);
	}

	/**
	 * Clears Manager's data and values in configuration file.
	 */
	public static void clear() {
		internalList.clear();
		externalList.clear();
		setValuesToConfig();
	}
}
