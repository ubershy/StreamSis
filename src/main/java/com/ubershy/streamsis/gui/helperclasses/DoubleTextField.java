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

import java.text.DecimalFormat;
import java.text.ParsePosition;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * DoubleTextField. The TextField that is modified to allow only double numbers to input.
 * Also provides convenient {@link #numberProperty()} for binding.
 * <p>
 * Note: when DoubleTextField's new input is empty String or minus sign, {@link #numberProperty()}
 * remain unchanged. 
 */
public class DoubleTextField extends TextField {
	
	/** The actual double number. Linked to {@link #textProperty()}. */
	private DoubleProperty numberProperty = new SimpleDoubleProperty();
	
	/** The flag that only exist to prevent infinite loop that could happen because 
	 * {@link #textProperty()} and {@link #numberProperty()} are linked together via
	 *  listeners.
	 */
	private boolean changeInitiatedFromInputFlag = false;
	
	/** The TextField listener. On TextField changes this listener changes {@link #numberProperty()}
	 *  too. The {@link #numberProperty()} does not change if new text in TextField is empty.
	 */
	private ChangeListener<String> textFieldListener = (observableValue, oldString, newString) -> {
		double newDouble;
		try {
			newDouble = Double.parseDouble(newString); 
		} catch (NumberFormatException ex) {
			return;
		}
		changeInitiatedFromInputFlag = true; // to prevent infinite loop
		this.numberProperty.set(newDouble);
	};
	
	/**
	 * The numberProperty listener. When numberProperty changes this listener changes
	 * {@link #textProperty()} too.
	 */
	private ChangeListener<? super Number> numberPropertyListener = (observableValue, oldDouble,
			newDouble) -> {
		if (changeInitiatedFromInputFlag) {
			changeInitiatedFromInputFlag = false;
		} else {
			this.textProperty().set(String.valueOf(newDouble));
		}
	};

	/**
	 * Instantiates a new DoubleTextField.
	 *
	 * @param maxAllowedValue
	 *            The maximum allowed value to input. If input value is more than that, the input
	 *            will not occur.
	 * @param allowNegativeValues
	 *            Allow negative values or not.
	 */
	public DoubleTextField(double maxAllowedValue, boolean allowNegativeValues) {
		setDoubleFilter(maxAllowedValue, allowNegativeValues);
		textProperty().addListener(textFieldListener);
		numberProperty().addListener(numberPropertyListener);
	}

	/**
	 * Instantiates a new DoubleTextField.
	 *
	 * @param text
	 *            The text to set initially.
	 * @param maxAllowedValue
	 *            The maximum allowed value to input. If input value is more than that, the input
	 *            will not occur.
	 * @param allowNegativeValues
	 *            Allow negative values or not.
	 */
	public DoubleTextField(String text, double maxAllowedValue, boolean allowNegativeValues) {
		super(text);
		setDoubleFilter(maxAllowedValue, allowNegativeValues);
		textProperty().addListener(textFieldListener);
		numberProperty().addListener(numberPropertyListener);
	}

	/**
	 * Modify TextField so only double/integer numbers will be allowed to be input in it.
	 *
	 * @param maxAllowedValue
	 *            The maximum allowed value to input. If input value is more than that, the input
	 *            will not occur.
	 * @param allowNegativeValues
	 *            Allow negative values or not.
	 */
	private void setDoubleFilter(double maxAllowedValue, boolean allowNegativeValues) {
		DecimalFormat format = new DecimalFormat();
		this.setTextFormatter(new TextFormatter<>(c -> {
			String newText = c.getControlNewText();
			if (newText.isEmpty())
				return c;
			// The user might start typing the number with minus sign, though it could not be 
			if (allowNegativeValues) {
				if (newText.equals("-"))
					return c;
			}
			ParsePosition parsePosition = new ParsePosition(0);
			Number number = format.parse(newText, parsePosition);
			if (number == null) // Means unable to parse
				return null;
			if (parsePosition.getIndex() < newText.length()) // Means extra symbols exist
				return null;
			double numberConvertedToDouble = number.doubleValue();
			if (numberConvertedToDouble > maxAllowedValue)
				return null;
			if (!allowNegativeValues) {
				if (numberConvertedToDouble < 0)
					return null;
			}
			return c;
		}));
	}
	
	/**
	 * {@link #numberProperty()} which changes on user input, except for the time when input is an
	 * empty String.
	 *
	 * @return The {@link #numberProperty()}.
	 */
	public DoubleProperty numberProperty() {
		return numberProperty;
	}

}
