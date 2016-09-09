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
package com.ubershy.streamsis.gui.controllers.editspecific;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import com.ubershy.streamsis.actions.HotkeyAction;
import com.ubershy.streamsis.project.CuteElement;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.ModifierValue;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

public class HotkeyActionController extends AbstractCuteController {

	protected HotkeyAction hkAction;

	protected ObjectProperty<KeyCodeCombination> keyCombinationProperty = new SimpleObjectProperty<KeyCodeCombination>();

	protected String origKeys = "";

	@FXML
	protected TextField keyTextField;

	@FXML
	protected TextField modifiersTextField;

	@FXML
	protected GridPane root;

	protected ValidationSupport validationSupport;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		keyTextField.setOnKeyPressed(e -> {
			// if (keycode.isLetterKey() || keycode.isDigitKey() || keycode.isFunctionKey()) {
			if (!e.getCode().isModifierKey()) {
				setNewKeyCodeFromInput(e.getCode());
			}
			e.consume();
		});
		modifiersTextField.addEventFilter(KeyEvent.ANY, e -> {
			if (e.getEventType() != KeyEvent.KEY_PRESSED) {
				e.consume();
				return;
			}
			if (e.getCode() == KeyCode.ESCAPE) {
				// When root's parent is focused, second hit to Escape key will close panel
				root.getParent().requestFocus();
			}
			if (e.getCode().isModifierKey()) {
				ArrayList<ModifierValue> modifiers = HotkeyAction.generateDefaultModifiers();
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
			} else {
				if (e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.DELETE) {
					setNewModifiersFromInput(HotkeyAction.generateDefaultModifiers());
				}
			}
			e.consume();
		});
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement cuteElement) {
		hkAction = (HotkeyAction) cuteElement;
		origKeys = hkAction.keysProperty().get();
		keyCombinationProperty.set(parseKeyCodeCombinationFromString(origKeys));
		StringBinding readableKey = Bindings.createStringBinding(
				() -> keyFromKeyCombination(keyCombinationProperty.get()), keyCombinationProperty);
		String newModifiers = modifiersFromKeyCombination(keyCombinationProperty.get());
		modifiersTextField.setText(newModifiers);
		StringBinding applyChangesToCuteElement = Bindings.createStringBinding(
				() -> keyCombinationProperty.get().getName(), keyCombinationProperty);
		hkAction.keysProperty().bind(applyChangesToCuteElement);
		keyTextField.textProperty().bind(readableKey);
	}

	protected String extractFullHotkeyFromKeyCombination(KeyCodeCombination kb) {
		return kb.getDisplayText();
	}

	protected String keyFromKeyCombination(KeyCodeCombination kb) {
		if (kb.getCode().equals(KeyCode.CLEAR)) {
			// in HotkeyAction KeyCode.Clear is used as empty KeyCode
			return "";
		}
		return kb.getCode().getName();
	}

	protected String modifiersFromKeyCombination(KeyCodeCombination kb) {
		ModifierValue downMod = ModifierValue.DOWN;
		// If any modifier exist
		if (kb.getAlt() == downMod || kb.getShortcut() == downMod || kb.getShift() == downMod) {
			String FullHotkey = extractFullHotkeyFromKeyCombination(kb);
			int lastPlusSignIndex = FullHotkey.lastIndexOf("+");
			return FullHotkey.substring(0, lastPlusSignIndex);
		}
		return "";
	}

	protected KeyCode getCurrentKeyCode() {
		return keyCombinationProperty.get().getCode();
	}

	protected ArrayList<ModifierValue> getCurrentModifiers() {
		ArrayList<ModifierValue> modifiers = HotkeyAction.generateDefaultModifiers();
		modifiers.set(0, keyCombinationProperty.get().getShift());
		modifiers.set(2, keyCombinationProperty.get().getAlt());
		// control on Windows and meta (command key) on Mac
		modifiers.set(4, keyCombinationProperty.get().getShortcut());
		return modifiers;
	}

	@Override
	/*
	 * @inheritDoc
	 */
	public Node getView() {
		return root;
	}

	protected KeyCodeCombination parseKeyCodeCombinationFromString(String stringToParse) {
		KeyCodeCombination kb = null;
		if (!stringToParse.isEmpty()) {
			kb = (KeyCodeCombination) KeyCombination.keyCombination(stringToParse);
		}
		return kb;
	}

	protected void setNewKeyCodeFromInput(KeyCode keycode) {
		ArrayList<ModifierValue> modifiers = getCurrentModifiers();
		keyCombinationProperty.set(new KeyCodeCombination(keycode, modifiers.get(0),
				modifiers.get(1), modifiers.get(2), modifiers.get(3), modifiers.get(4)));
	}

	protected void setNewModifiersFromInput(ArrayList<ModifierValue> modifiers) {
		keyCombinationProperty.set(new KeyCodeCombination(getCurrentKeyCode(), modifiers.get(0),
				modifiers.get(1), modifiers.get(2), modifiers.get(3), modifiers.get(4)));
		String newModifiers = modifiersFromKeyCombination(keyCombinationProperty.get());
		KeyCodeCombination originalKB = parseKeyCodeCombinationFromString(origKeys);
		buttonStateManager.reportNewValueOfControl(modifiersFromKeyCombination(originalKB),
				newModifiers, modifiersTextField, null);
		modifiersTextField.setText(newModifiers);
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void setValidationSupport(ValidationSupport validationSupport) {
		this.validationSupport = validationSupport;
		Validator<String> keyTextFieldValidator = (c, newValue) -> {
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please choose a keyboard key", newValue.isEmpty());
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult);
			KeyCodeCombination OrigKK = parseKeyCodeCombinationFromString(origKeys);
			buttonStateManager.reportNewValueOfControl(keyFromKeyCombination(OrigKK), newValue, c,
					finalResult);
			return finalResult;
		};
		this.validationSupport.registerValidator(keyTextField, keyTextFieldValidator);
	}

}
