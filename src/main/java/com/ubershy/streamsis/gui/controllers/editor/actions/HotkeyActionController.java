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
package com.ubershy.streamsis.gui.controllers.editor.actions;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.actions.HotkeyAction;
import com.ubershy.streamsis.gui.controllers.editor.AbstractCuteController;
import com.ubershy.streamsis.gui.controllers.editor.CuteElementController;
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

public class HotkeyActionController extends AbstractCuteController
		implements CuteElementController {

	/** The {@link HotkeyAction} to edit. */
	protected HotkeyAction hkAction;
    
	/** The original {@link HotkeyAction} to compare values with {@link #hkAction}. */
	protected HotkeyAction origHkAction;

	protected ObjectProperty<KeyCodeCombination> keyCombinationProperty = 
			new SimpleObjectProperty<KeyCodeCombination>();

	@FXML
	protected TextField keyTextField;

	@FXML
	protected TextField modifiersTextField;

	@FXML
	protected GridPane root;

	protected ValidationSupport validationSupport;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeBindings();
		initializeHandlers();
		
	}
	
	private void initializeHandlers() {
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
			// This is a list with default modifiers - ModifierValue.ANY.
			ArrayList<ModifierValue> modifiers = Util.generateDefaultModifiersForKeyCombination();
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
			} else {
				if (e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.DELETE) {
					setNewModifiersFromInput(modifiers);
				}
			}
			e.consume();
		});
	}

	private void initializeBindings() {
		StringBinding readableKey = Bindings.createStringBinding(() -> {
			KeyCodeCombination kcc = keyCombinationProperty.get();
			if (kcc == null || kcc.getCode() == KeyCode.CLEAR) {
				return "";
			} else {
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
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		hkAction = (HotkeyAction) editableCopyOfCE;
		origHkAction = (HotkeyAction) origCE;
		String keys = hkAction.getHotkey();
		keyCombinationProperty.set(parseKeyCodeCombinationFromString(keys));
		StringBinding applyChangesToCuteElement = Bindings.createStringBinding(
				() -> keyCombinationProperty.get().getName(), keyCombinationProperty);
		hkAction.keysProperty().bind(applyChangesToCuteElement);
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void unbindFromCuteElement() {
		hkAction.keysProperty().unbind();
		keyTextField.textProperty().unbind();
	}
	
	private String keyTextFromKeyCombination(KeyCodeCombination kcc) {
		if (kcc.getCode().equals(KeyCode.CLEAR)) {
			// in HotkeyAction KeyCode.Clear is used as empty KeyCode
			return "";
		}
		return kcc.getCode().getName();
	}

	private String modifiersTextFromKeyCombination(KeyCodeCombination kcc) {
		if (kcc == null) {
			return "";
		}
		ModifierValue downMod = ModifierValue.DOWN;
		// If any modifier exist
		if (kcc.getAlt() == downMod || kcc.getShortcut() == downMod || kcc.getShift() == downMod) {
			String FullHotkeyText = kcc.getDisplayText();
			int lastPlusSignIndex = FullHotkeyText.lastIndexOf("+");
			return FullHotkeyText.substring(0, lastPlusSignIndex);
		}
		return "";
	}

	private ArrayList<ModifierValue> getCurrentModifiers() {
		ArrayList<ModifierValue> modifiers = Util.generateDefaultModifiersForKeyCombination();
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

	private KeyCodeCombination parseKeyCodeCombinationFromString(String stringToParse) {
		KeyCodeCombination kb = null;
		if (!stringToParse.isEmpty()) {
			kb = (KeyCodeCombination) KeyCombination.keyCombination(stringToParse);
		}
		return kb;
	}

	private void setNewKeyCodeFromInput(KeyCode keycode) {
		ArrayList<ModifierValue> modifiers = getCurrentModifiers();
		keyCombinationProperty.set(new KeyCodeCombination(keycode, modifiers.get(0),
				modifiers.get(1), modifiers.get(2), modifiers.get(3), modifiers.get(4)));
	}

	private void setNewModifiersFromInput(ArrayList<ModifierValue> modifiers) {
		keyCombinationProperty.set(
				new KeyCodeCombination(keyCombinationProperty.get().getCode(), modifiers.get(0),
						modifiers.get(1), modifiers.get(2), modifiers.get(3), modifiers.get(4)));
		String newModifiers = modifiersTextFromKeyCombination(keyCombinationProperty.get());
		KeyCodeCombination originalKB = parseKeyCodeCombinationFromString(origHkAction.getHotkey());
		buttonStateManager.reportNewValueOfControl(modifiersTextFromKeyCombination(originalKB),
				newModifiers, modifiersTextField, null);
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
			KeyCodeCombination OrigKK = parseKeyCodeCombinationFromString(origHkAction.getHotkey());
			buttonStateManager.reportNewValueOfControl(keyTextFromKeyCombination(OrigKK), newValue,
					c, finalResult);
			return finalResult;
		};
		this.validationSupport.registerValidator(keyTextField, keyTextFieldValidator);
	}

}
