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
package com.ubershy.streamsis.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.project.AbstractCuteNode;
import com.ubershy.streamsis.project.UserVars;

/**
 * Variable Setter Action. <br>
 * This {@link Action} can create a new user variable(key):value pair in {@link UserVars} <br>
 * If user variable(key) already exists in {@link UserVars}, it's value will be overwritten. <br>
 */
@SuppressWarnings("unchecked")
public class VariableSetterAction extends AbstractCuteNode implements Action {

	static final Logger logger = LoggerFactory.getLogger(VariableSetterAction.class);

	/** The name of user variable(key). */
	@JsonProperty
	protected String key = "";

	/** The value of user variable. */
	@JsonProperty
	protected String value = "";

	public VariableSetterAction() {
	}

	/**
	 * Instantiates a new VariableSetterAction. <br>
	 * On execution it will create a new user variable(key):value pair in {@link UserVars}. <br>
	 * If user variable(key) already exists in {@link UserVars}, it's value will be overwritten. <br>
	 *
	 * @param key
	 *            the name of the user variable(key) to set in {@link UserVars}
	 * @param value
	 *            the value to set to the user variable(key) in {@link UserVars}
	 */
	@JsonCreator
	public VariableSetterAction(@JsonProperty("key") String key, @JsonProperty("value") String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			logger.info("Set Variable(key): '" + key + "' to Value: '" + value + "'");
			elementInfo.setAsWorking();
			if (UserVars.get(key).equals(value)) {
				// Such key already has this value, set false result
				elementInfo.setBooleanResult(false);
			} else {
				UserVars.set(key, value);
				elementInfo.setBooleanResult(true);
			}
		}
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (key.isEmpty()) {
			elementInfo.setAsBroken("Variable name is empty");
			return;
		}
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
