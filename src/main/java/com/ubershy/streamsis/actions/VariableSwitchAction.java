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

import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.project.AbstractCuteNode;
import com.ubershy.streamsis.project.UserVars;

/**
 * Variable Switch Action. <br>
 * This {@link Action} checks the value of a user variable stored in {@link UserVars}. <br>
 * And based on what the value is, it executes associated {@link Action} stored inside it's
 * {@link #actionsMap}. <br>
 */
@SuppressWarnings("unchecked")
public class VariableSwitchAction extends AbstractCuteNode implements Action {

	static final Logger logger = LoggerFactory.getLogger(VariableSwitchAction.class);

	/** The map with values and associated {@link Action Actions}. */
	@JsonProperty
	TreeMap<String, Action> actionsMap;

	/** The name of user variable(key). */
	@JsonProperty
	String key = "";

	public VariableSwitchAction() {
	}

	/**
	 * Instantiates a new VariableSwitchAction. <br>
	 * On execution it will check the value of a user variable stored in {@link UserVars}. <br>
	 * And based on what the value is, it executes associated {@link Action} stored inside it's
	 * {@link #actionsMap}. <br>
	 *
	 * @param key
	 *            the name of the user variable(key) to set in {@link UserVars}
	 * @param actionsMap
	 *            the treeMap with values and corresponding {@link Action Actions}
	 */
	@JsonCreator
	public VariableSwitchAction(@JsonProperty("key") String key,
			@JsonProperty("actionsMap") TreeMap<String, Action> actionsMap) {
		this.key = key;
		this.actionsMap = actionsMap;
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			String aquiredValue = UserVars.get(this.key);
			Action action = null;
			if (aquiredValue == null) {
				elementInfo.setFailedResult();
				logger.info(
						"VariableSwitchAction: Doing nothing. No string Value is found for Key: "
								+ this.key);
				return;
			}
			action = actionsMap.get(aquiredValue);
			if (action == null) {
				elementInfo.setFailedResult();
				logger.info("VariableSwitchAction: Doing nothing. No Action is found for Key: "
						+ this.key + " Value: " + aquiredValue);
				return;
			}
			logger.info("Action is found for Key: " + this.key + " Value: " + aquiredValue);
			logger.info("Executing '" + action.getElementInfo().getName() + "'("
					+ action.getClass().getSimpleName() + ")");
			action.execute();
			elementInfo.setSuccessfulResult();
		}
	}

	public TreeMap<String, Action> getActionsMap() {
		return actionsMap;
	}

	public String getKey() {
		return key;
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (key.isEmpty()) {
			elementInfo.setAsBroken("Variable name is empty");
			return;
		}
		if (actionsMap != null) {
			if (actionsMap.isEmpty()) {
				elementInfo.setAsBroken("No Actions assigned to Values");
			}
		} else {
			elementInfo.setAsBroken("Map with Actions is not defined");
			return;
		}
		if (actionsMap.containsKey("")) {
			elementInfo.setAsBroken("One or more Variable names are empty");
		}
		for (Action action : actionsMap.values()) {
			action.init();
//			if (action.getElementInfo().isBroken()) {
//				elementInfo.setAsBroken("One or more Actions inside are broken");
//			}
		}
	}

	public void setActionsMap(TreeMap<String, Action> actionsMap) {
		this.actionsMap = actionsMap;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
