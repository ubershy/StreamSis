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
package com.ubershy.streamsis.gui.controllers.settings;

import java.util.ArrayList;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.HotkeyManager.Hotkey;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.Util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.ModifierValue;
import javafx.scene.input.KeyEvent;

public class HotkeyRow {
	
	private ObjectProperty<KeyCodeCombination> keyCombinationProperty = 
			new SimpleObjectProperty<KeyCodeCombination>(null);

	private TextField keyTextField = new TextField();

	private TextField modifiersTextField = new TextField();
	
	private Label descriptionLabel = new Label();
	
	private Button restoreDefaultButton = new Button("Restore Default");
	
	private Hotkey hotkey;

	private ValidationSupport validationSupport;

	private KeyCodeCombination defaultKCC;

	public HotkeyRow (Hotkey hotkey) {
		this.hotkey = hotkey;
		descriptionLabel.setText(hotkey.toString());
		restoreDefaultButton.setMaxWidth(Double.MAX_VALUE);
		defaultKCC = (KeyCodeCombination) KeyCodeCombination
				.keyCombination(hotkey.getDefaultKeys());
		setFieldPrompts(defaultKCC);
		initializeBindings();
		initializeInputHandlers();
	}

	private void setFieldPrompts(KeyCodeCombination defkcc) {
		keyTextField.setPromptText("e.g. " + defkcc.getCode().getName());
		modifiersTextField.setPromptText("e.g. " + modifiersTextFromKeyCombination(defkcc));
	}

	private void initializeBindings() {
		StringBinding readableKey = Bindings.createStringBinding(() -> {
			KeyCodeCombination kcc = keyCombinationProperty.get();
			if (kcc == null) {
				disableModifiersTextFieldValidation();
				return "";
			} else {
				enableModifiersTextFieldValidation();
				return kcc.getCode().getName();
			}
		}, keyCombinationProperty);
		StringBinding readableModifiers = Bindings.createStringBinding(() -> {
			return modifiersTextFromKeyCombination(keyCombinationProperty.get());
		}, keyCombinationProperty);
		modifiersTextField.textProperty().bind(readableModifiers);
		keyTextField.textProperty().bind(readableKey);
		// Disable Modifiers field if Key is not set.
		modifiersTextField.disableProperty().bind(keyTextField.textProperty().isEmpty());
	}

	private void initializeInputHandlers() {
		keyTextField.setOnKeyPressed(e -> {
			// if (keycode.isLetterKey() || keycode.isDigitKey() || keycode.isFunctionKey()) {
			if (!e.getCode().isModifierKey()) {
				if (e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.DELETE) {
					// Clean up key and modifiers
					keyCombinationProperty.set(null);
				} else {
					setNewKeyCodeFromInput(e.getCode());
				}
			}
			e.consume();
		});
		modifiersTextField.addEventFilter(KeyEvent.ANY, e -> {
			if (e.getEventType() != KeyEvent.KEY_PRESSED) {
				e.consume();
				return;
			}
			// This is a list with default modifiers - ModifierValue.ANY.
			ArrayList<ModifierValue> modifiers = Util.generateDefaultModifiersForKeyCombination();
			if (e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.DELETE) {
				// Clean up modifiers.
				setNewModifiersFromInput(modifiers);
			}
			if (e.getCode().isModifierKey()) {
				boolean anyModifiersPressed = false;
				if (e.isShiftDown()) {
					modifiers.set(0, ModifierValue.DOWN);
					anyModifiersPressed = true;
				}
				if (e.isAltDown()) {
					modifiers.set(2, ModifierValue.DOWN);
					anyModifiersPressed = true;
				}
				// control on Windows and meta (command key) on Mac
				if (e.isShortcutDown()) {
					modifiers.set(4, ModifierValue.DOWN);
					anyModifiersPressed = true;
				}
				if (anyModifiersPressed) {
					setNewModifiersFromInput(modifiers);
				}
			}
			e.consume();
		});
		restoreDefaultButton.setOnAction((e) -> {
			restoreDefaultValueOfKeyCodeCombination();
		});
	}

	private String modifiersTextFromKeyCombination(KeyCodeCombination kcc) {
		if (kcc == null) {
			return "";
		}
		ModifierValue downMod = ModifierValue.DOWN;
		// If any modifier exist
		if (kcc.getAlt() == downMod || kcc.getShortcut() == downMod || kcc.getShift() == downMod) {
			String FullHotkey = kcc.getDisplayText();
			int lastPlusSignIndex = FullHotkey.lastIndexOf("+");
			return FullHotkey.substring(0, lastPlusSignIndex);
		}
		return "";
	}

	private ArrayList<ModifierValue> getCurrentModifiers() {
		ArrayList<ModifierValue> modifiers = Util.generateDefaultModifiersForKeyCombination();
		if (keyCombinationProperty.get() != null) {
			modifiers.set(0, keyCombinationProperty.get().getShift());
			modifiers.set(2, keyCombinationProperty.get().getAlt());
			// control on Windows and meta (command key) on Mac
			modifiers.set(4, keyCombinationProperty.get().getShortcut());
		}
		return modifiers;
	}

	private void setNewKeyCodeFromInput(KeyCode keycode) {
		ArrayList<ModifierValue> modifiers = getCurrentModifiers();
		keyCombinationProperty.set(new KeyCodeCombination(keycode, modifiers.get(0),
				modifiers.get(1), modifiers.get(2), modifiers.get(3), modifiers.get(4)));
	}

	private void setNewModifiersFromInput(ArrayList<ModifierValue> modifiers) {
		if (keyCombinationProperty.get() == null) {
			throw new RuntimeException(
					"Modifiers field should be inactive when Key field is empty");
		}
		keyCombinationProperty.set(
				new KeyCodeCombination(keyCombinationProperty.get().getCode(), modifiers.get(0),
						modifiers.get(1), modifiers.get(2), modifiers.get(3), modifiers.get(4)));
	}

	public void setValidationSupport(ValidationSupport validationSupport) {
		// TODO: Fix console CSS warnings related to this method if they exist. If they do not
		// appear anymore, remove this TODO.
		// Example of warning: "WARNING: Could not resolve '-fx-text-background-color' while ..."
		this.validationSupport = validationSupport;
		enableModifiersTextFieldValidation();
	}
	
	private void enableModifiersTextFieldValidation() {
		if (validationSupport != null) {
			Validator<String> modifiersTextFieldValidator = (c, newValue) -> {
				KeyCodeCombination kcc = keyCombinationProperty.get();
				// Allow empty modifiers only if KeyCodeCombination is not set.
				ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
						"Need at least one modifier", kcc != null && newValue.isEmpty());
				ValidationResult finalResult = ValidationResult.fromResults(emptyResult);
				return finalResult;
			};
			validationSupport.registerValidator(modifiersTextField, modifiersTextFieldValidator);
		}
	}
	
	private void disableModifiersTextFieldValidation() {
		if (validationSupport != null) {
			validationSupport.registerValidator(modifiersTextField,
					GUIUtil.createFakeAlwaysSuccessfulValidator());
		}
	}

	public Label getDescriptionLabel() {
		return descriptionLabel;
	}

	public TextField getKeyTextField() {
		return keyTextField;
	}

	public TextField getModifiersTextField() {
		return modifiersTextField;
	}
	
	public Button getRestoreDefaultButton() {
		return restoreDefaultButton;
	}

	public Hotkey getHotkey() {
		return hotkey;
	}
	
	public void restoreDefaultValueOfKeyCodeCombination() {
		keyCombinationProperty.set(defaultKCC);
	}

	public KeyCodeCombination getCurrentKeyCodeCombination() {
		return keyCombinationProperty.get();
	}

	public void setCurrentKeyCodeCombination(KeyCodeCombination kcc) {
		keyCombinationProperty.set(kcc);
	}
}
