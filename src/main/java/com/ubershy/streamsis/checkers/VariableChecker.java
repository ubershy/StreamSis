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
package com.ubershy.streamsis.checkers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ubershy.streamsis.project.AbstractCuteNode;
import com.ubershy.streamsis.project.UserVars;

/**
 * Value Checker. <br>
 * This {@link Checker} checks if the specified variable in {@link UserVars} equals to specified
 * expected value. <br>
 * If variable is not found in UserVars, this Checker returns false;
 */
@SuppressWarnings("unchecked")
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "chkrType")
public class VariableChecker extends AbstractCuteNode implements Checker {

	static final Logger logger = LoggerFactory.getLogger(VariableChecker.class);

	/** The value to expect under the variable in {@link UserVars}. */
	@JsonProperty
	private String expectedValue = "";

	/** The variable to check in {@link UserVars}. */
	@JsonProperty
	private String key = "";

	/** The variable for storing previous result. Helps to prevent spamming to log. */
	@JsonIgnore
	private boolean previousResult = false;

	public VariableChecker() {
	}

	/**
	 * Instantiates a new {@link VariableChecker}.
	 *
	 * @param key
	 *            the variable to check in {@link UserVars}
	 * @param expectedValue
	 *            the value to expect under the variable in {@link UserVars}
	 */
	@JsonCreator
	public VariableChecker(@JsonProperty("key") String key,
			@JsonProperty("expectedValue") String expectedValue) {
		this.key = key;
		this.expectedValue = expectedValue;
	}

	@Override
	public boolean check() {
		boolean result = false;
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			String currentValue = UserVars.get(key);
			if (expectedValue.equals(currentValue)) {
				if (!previousResult) {
					logger.info("Variable: '" + key + "' EQUALS expected Value: '" + expectedValue
							+ "'");
				}
				result = true;
			} else {
				if (previousResult) {
					logger.info("Variable: '" + key + "' NOT EQUALS expected Value: '"
							+ expectedValue + "'. Current Value: '" + currentValue + "'");
				}
			}
			if (result)
				elementInfo.setSuccessfulResult();
			else
				elementInfo.setFailedResult();
		}
		previousResult = result;
		return result;
	}

	public String getExpectedValue() {
		return expectedValue;
	}

	public String getKey() {
		return key;
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (key.isEmpty())
			elementInfo.setAsBroken("Variable name is empty");
	}

	public void setExpectedValue(String expectedValue) {
		this.expectedValue = expectedValue;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
