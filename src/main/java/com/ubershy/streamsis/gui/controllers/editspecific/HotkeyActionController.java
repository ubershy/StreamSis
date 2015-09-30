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

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.actions.HotkeyAction;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.CuteElement;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.ModifierValue;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

public class HotkeyActionController extends AbstractCuteController {

	@FXML
	protected TextField keyTextField;

	@FXML
	protected TextField modifiersTextField;

	@FXML
	protected GridPane root;

	protected HotkeyAction hkAction;

	protected StringProperty keysProperty = new SimpleStringProperty("");

	protected ValidationSupport validationSupport;

	protected ObjectProperty<KeyCodeCombination> keyCombinationProperty = new SimpleObjectProperty<KeyCodeCombination>();

	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement element) {
		hkAction = (HotkeyAction) element;
		keysProperty.bind(hkAction.keysProperty());
		keyCombinationProperty.set(parseKeyCodeCombinationFromString(keysProperty.get()));
		keysProperty.addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
			keyCombinationProperty.set(parseKeyCodeCombinationFromString(newValue));
		});
		StringBinding readableKey = Bindings.createStringBinding(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return extractKeyFromKeyCombination(keyCombinationProperty.get());
			}
		}, keyCombinationProperty);
		StringBinding readableModifiers = Bindings.createStringBinding(new Callable<String>() {
			@Override
			public String call() throws Exception {
				String newValue = extractModifiersFromKeyCombination(keyCombinationProperty.get());
				KeyCodeCombination originalKB = parseKeyCodeCombinationFromString(
						keysProperty.get());
				GUIUtil.reportToButtonStateManager(extractModifiersFromKeyCombination(originalKB),
						newValue, modifiersTextField, null, buttonStateManager);
				return newValue;
			}
		}, keyCombinationProperty);
		keyTextField.textProperty().bind(readableKey);
		modifiersTextField.textProperty().bind(readableModifiers);
		keyTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				// if (keycode.isLetterKey() || keycode.isDigitKey() || keycode.isFunctionKey()) {
				if (!t.getCode().isModifierKey()) {
					setNewKeyCode(t.getCode());
				}
				t.consume();
			}
		});
		modifiersTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ESCAPE) {
					// When root's parent is focused, second hit to Escape key will close panel
					root.getParent().requestFocus();
				}
				if (t.getCode().isModifierKey()) {
					ArrayList<ModifierValue> modifiers = HotkeyAction.generateDefaultModifiers();
					boolean anyModifiersPressed = false;
					if (t.isShiftDown()) {
						modifiers.set(0, ModifierValue.DOWN);
						anyModifiersPressed = true;
					}
					if (t.isAltDown()) {
						modifiers.set(2, ModifierValue.DOWN);
						anyModifiersPressed = true;
					}
					// control on Windows and meta (command key) on Mac
					if (t.isShortcutDown()) {
						modifiers.set(4, ModifierValue.DOWN);
						anyModifiersPressed = true;
					}
					if (anyModifiersPressed) {
						setNewModifiers(modifiers);
					}
				} else {
					if (t.getCode() == KeyCode.BACK_SPACE || t.getCode() == KeyCode.DELETE) {
						setNewModifiers(HotkeyAction.generateDefaultModifiers());
					}
				}
				t.consume();
			}
		});
	}

	protected void setNewModifiers(ArrayList<ModifierValue> modifiers) {
		keyCombinationProperty.set(new KeyCodeCombination(getCurrentKeyCode(), modifiers.get(0),
				modifiers.get(1), modifiers.get(2), modifiers.get(3), modifiers.get(4)));
	}

	protected void setNewKeyCode(KeyCode keycode) {
		ArrayList<ModifierValue> modifiers = getCurrentModifiers();
		keyCombinationProperty.set(new KeyCodeCombination(keycode, modifiers.get(0),
				modifiers.get(1), modifiers.get(2), modifiers.get(3), modifiers.get(4)));
	}

	protected ArrayList<ModifierValue> getCurrentModifiers() {
		ArrayList<ModifierValue> modifiers = HotkeyAction.generateDefaultModifiers();
		modifiers.set(0, keyCombinationProperty.get().getShift());
		modifiers.set(2, keyCombinationProperty.get().getAlt());
		// control on Windows and meta (command key) on Mac
		modifiers.set(4, keyCombinationProperty.get().getShortcut());
		return modifiers;
	}

	protected KeyCode getCurrentKeyCode() {
		return keyCombinationProperty.get().getCode();
	}

	protected KeyCodeCombination parseKeyCodeCombinationFromString(String stringToParse) {
		KeyCodeCombination kb = null;
		if (stringToParse.isEmpty()) {

		} else {
			kb = (KeyCodeCombination) KeyCombination.keyCombination(stringToParse);
		}
		return kb;
	}

	@Override
	/*
	 * @inheritDoc
	 */
	public Node getView() {
		return root;
	}

	protected boolean validatekeyEmptiness(Control c, String newValue) {
		if (newValue.isEmpty())
			return true;
		return false;
	}

	@Override
	/*
	 * @inheritDoc
	 */
	public void reset() {
		keyCombinationProperty.set(parseKeyCodeCombinationFromString(keysProperty.get()));
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void apply() {
		hkAction.keysProperty().set(keyCombinationProperty.get().getName());
		hkAction.init();
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void setValidationSupport(ValidationSupport validationSupport) {
		this.validationSupport = validationSupport;
		Validator<String> keyTextFieldValidator = (c, newValue) -> {
			// ValidationResult alreadyExistanceResult = ValidationResult.fromErrorIf(c,
			// "The name is already taken. Please choose another one",
			// validateNameAlreadyExistence(c, newValue));
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please choose a keyboard key", validatekeyEmptiness(c, newValue));
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult);
			KeyCodeCombination kb = parseKeyCodeCombinationFromString(keysProperty.get());
			GUIUtil.reportToButtonStateManager(extractKeyFromKeyCombination(kb), newValue, c,
					finalResult, buttonStateManager);
			return finalResult;
		};
		this.validationSupport.registerValidator(keyTextField, keyTextFieldValidator);
	}

	protected String extractFullHotkeyFromKeyCombination(KeyCodeCombination kb) {
		return kb.getDisplayText();
	}

	protected String extractKeyFromKeyCombination(KeyCodeCombination kb) {
		if (kb.getCode().equals(KeyCode.CLEAR)) {
			// in HotkeyAction KeyCode.Clear is used as empty KeyCode
			return "";
		}
		return kb.getCode().getName();
	}

	protected String extractModifiersFromKeyCombination(KeyCodeCombination kb) {
		ModifierValue downMod = ModifierValue.DOWN;
		// If any modifier exist
		if (kb.getAlt() == downMod || kb.getShortcut() == downMod || kb.getShift() == downMod) {
			String FullHotkey = extractFullHotkeyFromKeyCombination(kb);
			int lastPlusSignIndex = FullHotkey.lastIndexOf("+");
			return FullHotkey.substring(0, lastPlusSignIndex);
		}
		return "";
	}

}
