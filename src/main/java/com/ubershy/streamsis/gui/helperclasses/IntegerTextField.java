/** 
 * StreamSis
 * Copyright (C) 2016 Eva Balycheva
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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * IntegerTextField. The TextField that is modified to allow only integer numbers to input.
 * Also provides convenient {@link #numberProperty()} for binding.
 * <p>
 * Note: when IntegerTextField's new input is empty String or minus sign, {@link #numberProperty()}
 * remain unchanged. 
 */
public class IntegerTextField extends TextField {
	
	/** The actual integer number. Linked to {@link #textProperty()}. */
	private IntegerProperty numberProperty = new SimpleIntegerProperty();
	
	/** The flag that only exist to prevent infinite loop that could happen because 
	 * {@link #textProperty()} and {@link #numberProperty()} are linked together via
	 *  listeners.
	 */
	private boolean changeInitiatedFromInputFlag = false;
	
	/** The TextField listener. On TextField changes this listener changes {@link #numberProperty()}
	 *  too. The {@link #numberProperty()} does not change if new text in TextField is empty.
	 */
	private ChangeListener<String> textFieldListener = (observableValue, oldString, newString) -> {
		int newInteger;
		try {
			newInteger = Integer.parseInt(newString); 
		} catch (NumberFormatException ex) {
			return;
		}
		changeInitiatedFromInputFlag = true; // to prevent infinite loop
		this.numberProperty.set(newInteger);
	};
	
	/** The numberProperty listener. When numberProperty changes this listener changes
	 *  {@link #textProperty()} too.
	 */
	private ChangeListener<? super Number> numberPropertyListener = (observableValue, oldInt, newInt) -> {
		if (changeInitiatedFromInputFlag) {
			changeInitiatedFromInputFlag = false;
		}
		else {
			this.textProperty().set(String.valueOf(newInt));
		}
	};

	/**
	 * Instantiates a new IntegerTextField.
	 *
	 * @param maxAllowedValue
	 *            The maximum allowed value to input. If input value is more than that, the input
	 *            will not occur.
	 * @param allowNegativeValues
	 *            Allow negative values or not.
	 */
	public IntegerTextField(int maxAllowedValue, boolean allowNegativeValues) {
		setIntegerFilter(maxAllowedValue, allowNegativeValues);
		textProperty().addListener(textFieldListener);
		numberProperty().addListener(numberPropertyListener);
	}

	/**
	 * Instantiates a new IntegerTextField.
	 *
	 * @param text
	 *            The text to set initially.
	 * @param maxAllowedValue
	 *            The maximum allowed value to input. If input value is more than that, the input
	 *            will not occur.
	 * @param allowNegativeValues
	 *            Allow negative values or not.
	 */
	public IntegerTextField(String text, int maxAllowedValue, boolean allowNegativeValues) {
		super(text);
		setIntegerFilter(maxAllowedValue, allowNegativeValues);
		textProperty().addListener(textFieldListener);
		numberProperty().addListener(numberPropertyListener);
	}

	/**
	 * Modify TextField so only integer numbers will be allowed to be input in it.
	 *
	 * @param maxAllowedValue
	 *            The maximum allowed value to input. If input value is more than that, the input
	 *            will not occur.
	 * @param allowNegativeValues
	 *            Allow negative values or not.
	 */
	private void setIntegerFilter(int maxAllowedValue, boolean allowNegativeValues) {
		this.setTextFormatter(new TextFormatter<>(c -> {
			String regex = "\\d*";
			if (allowNegativeValues) {
				if (c.getControlNewText().equals("-"))
					return c;
				regex = "-?\\d*";
			}
			if (c.getControlNewText().isEmpty())
				return c;
			if (c.getControlNewText().startsWith("0")) {
				if (c.getControlNewText().length() > 1) {
					return null;
				}
			}
			if (c.getControlNewText().matches(regex)) {
				int parsedInt;
				try {
					parsedInt = Integer.parseInt(c.getControlNewText());
				} catch (NumberFormatException ex) {
					return null;
				}
				if (parsedInt > maxAllowedValue)
					return null;
				if (!allowNegativeValues) {
					if (parsedInt < 0)
						return null;
				}
				return c;
			}
			return null;
		}));
	}
	
	/**
	 * {@link #numberProperty()} which changes on user input, except for the time when input is an
	 * empty String.
	 *
	 * @return The {@link #numberProperty()}.
	 */
	public IntegerProperty numberProperty() {
		return numberProperty;
	}

}
