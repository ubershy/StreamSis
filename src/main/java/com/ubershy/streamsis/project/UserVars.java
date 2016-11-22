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
package com.ubershy.streamsis.project;

import java.util.Map;
import java.util.TreeMap;

import com.ubershy.streamsis.actions.VariableSetterAction;
import com.ubershy.streamsis.actions.VariableSwitchAction;
import com.ubershy.streamsis.checkers.VariableChecker;

import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.collections.FXCollections;

/**
 * This class is for creating and using string variables the user wants to use in his
 * {@link CuteProject}.
 * <p>
 * Combined with {@link VariableSetterAction}, {@link VariableSwitchAction} and
 * {@link VariableChecker} variables can affect how user's CuteProject behaves.
 * <p>
 * <b>Example:</b> <br>
 * The user is playing an online game. After winning or loosing a match the game is always showing
 * main menu. <br>
 * When StreamSis detects the user has won, {@link VariableSetterAction} can execute to set a new
 * variable "WinOrLose" with value <b>"Win"</b>.<br>
 * When main menu finally starts showing and one of the Checkers detects it,
 * {@link VariableSwitchAction} can execute to change Streaming Program layout to something
 * <b>happy</b>. <br>
 * However, If user has lost his match and "WinOrLose" variable is set to <b>"Lose"</b>,
 * {@link VariableSwitchAction} can change Streaming Program layout to something <b>sad</b>.<br>
 */
public final class UserVars {

	/**
	 * The map in format variable(key):value where the user can store data that defines
	 * {@link CuteProject}'s behavior. The comparison of keys is case-insensitive.
	 */
	private static ReadOnlyMapWrapper<String, String> variables = new ReadOnlyMapWrapper<>(
			FXCollections
					.observableMap(new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)));
	
	/**
	 * Gets the value of the variable stored in {@link UserVars}.
	 *
	 * @param key
	 *            the name of variable <br>
	 *            Can't be null or empty
	 * @return the value of variable <br>
	 *         null, if variable is not found in {@link UserVars}
	 * @throws IllegalArgumentException
	 */
	public static String get(String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Argument key is null or empty");
		}
		return variables.get(key);
	}

	/**
	 * Sets the value for the variable stored in {@link UserVars}. If the variable with specified
	 * name does not exist, it will be created.
	 *
	 * @param key
	 *            the name of variable which value we want to set <br>
	 *            Can't be null or empty.
	 * @param value
	 *            the new value <br>
	 *            Can't be null, but can be empty
	 * @throws IllegalArgumentException
	 */
	public static void put(String key, String value) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Provided key is null or empty");
		}
		if (value == null) {
			throw new IllegalArgumentException("Provided value is null");
		}
		variables.put(key, value);
	}

	/**
	 * Clears all variables with values out.
	 */
	public static void clear() {
		variables.clear();
	}
	
	/**
	 * Removes the specified variable and associated value with it.
	 * @param key
	 *            The name of variable which we want to remove. <br>
	 *            Can't be null or empty.
	 * @throws IllegalArgumentException
	 */
	public static void remove(String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Provided key is null or empty");
		}
		variables.remove(key);
	}

	/**
	 * Clears up {@link #variables} and sets it to the provided map contents.
	 * 
	 * @param map
	 *            The map which contents to set to {@link #variables}.
	 */
	public static void setAll(Map<String, String> map) {
		variables.clear();
		variables.putAll(map);
	}
	
	/**
	 * Gets a read-only map of {@link #variables}.
	 * 
	 * @return The read-only map of {@link #variables}.
	 */
	public static ReadOnlyMapProperty<String, String> getUserVarsMap() {
		return variables.getReadOnlyProperty();
	}

}