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
import com.ubershy.streamsis.project.AbstractCuteElement;
import com.ubershy.streamsis.project.UserVars;

import impl.org.controlsfx.i18n.SimpleLocalizedStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Variable Checker. <br>
 * This {@link Checker} checks if the specified variable in {@link UserVars} equals to specified
 * expected value. <br>
 * If variable is not found in UserVars, this Checker returns false;
 */
@SuppressWarnings("unchecked")
public class VariableChecker extends AbstractCuteElement implements Checker {

	static final Logger logger = LoggerFactory.getLogger(VariableChecker.class);

	/** The value to expect under the variable in {@link UserVars}. */
	@JsonProperty
	private StringProperty expectedValue = new SimpleLocalizedStringProperty("");
	public StringProperty expectedValueProperty() {return expectedValue;}
	public String getExpectedValue() {return expectedValue.get();}
	public void setExpectedValue(String expectedValue) {this.expectedValue.set(expectedValue);;}

	/** The variable to check in {@link UserVars}. */
	@JsonProperty
	private StringProperty key = new SimpleLocalizedStringProperty("");
	public StringProperty keyProperty() {return key;}
	public String getKey() {return key.get();}
	public void setKey(String key) {this.key.set(key);}

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
		this.key.set(key);
		this.expectedValue.set(expectedValue);
	}

	@Override
	public boolean check() {
		boolean result = false;
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			String currentValue = UserVars.get(key.get());
			if (expectedValue.equals(currentValue)) {
				if (!previousResult) {
					logger.info("Variable: '" + key.get() + "' EQUALS expected Value: '"
							+ expectedValue.get() + "'");
				}
				result = true;
			} else {
				if (previousResult) {
					logger.info("Variable: '" + key.get() + "' NOT EQUALS expected Value: '"
							+ expectedValue.get() + "'. Current Value: '" + currentValue + "'");
				}
			}
			elementInfo.setBooleanResult(result);
		}
		previousResult = result;
		return result;
	}

	@Override
	public void init() {
		super.init();
		elementInfo.setAsReadyAndHealthy();
		if (key.get().isEmpty())
			elementInfo.setAsBroken("Variable name is empty");
		if (expectedValue == null)
			throw new RuntimeException("Expected value can't be set to null.");
	}

}
