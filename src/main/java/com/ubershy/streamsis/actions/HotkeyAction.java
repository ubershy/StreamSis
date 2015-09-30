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

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ubershy.streamsis.LowLevel;
import com.ubershy.streamsis.LowLevel.OS;
import com.ubershy.streamsis.project.AbstractCuteNode;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.ModifierValue;

/**
 * Hotkey Action. <br>
 * This {@link Action} simulates sending keyboard key combination to OS(current focused element in
 * OS), i.e. hotkey. <br>
 * <p>
 * 
 * @see {@link OBSHotkeyAction}
 */
@SuppressWarnings("unchecked")
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "actnType")
public class HotkeyAction extends AbstractCuteNode implements Action {

	static final Logger logger = LoggerFactory.getLogger(HotkeyAction.class);

	/**
	 * Generate list for {@link KeyCodeCombination} with {@link ModifierValue#ANY} values.
	 *
	 * @return the list
	 */
	public static ArrayList<ModifierValue> generateDefaultModifiers() {
		ModifierValue defMod = ModifierValue.ANY;
		ArrayList<ModifierValue> modifiers = new ArrayList<ModifierValue>();
		// There are only five modifiers in C
		for (int i = 0; i < 5; i++) {
			modifiers.add(defMod);
		}
		return modifiers;
	}

	/**
	 * Lets define this {@link KeyCode} as empty main key in {@link KeyCodeCombination}. <br>
	 * We need it, because we can't create KeyCodeCombination with NULL KeyCode. <br>
	 * It's not located on keyboard, so we can safely use it.
	 */
	@JsonIgnore
	protected KeyCode emptyCode = KeyCode.CLEAR;

	/**
	 * The keys to simulate, e.g. new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN). <br>
	 * Extracted at runtime from {@link #keysProperty}.
	 */
	@JsonIgnore
	protected KeyCodeCombination keyCombination;

	/**
	 * The keys to simulate (String representation of {@link KeyCodeCombination}, can be
	 * serialized);
	 */
	@JsonIgnore
	protected StringProperty keysProperty = new SimpleStringProperty("");

	/**
	 * The sleep time between pressing modifiers is important as many programs catch hotkeys using
	 * strange algorithms.
	 */
	@JsonIgnore
	protected long modifiersPressSleepTime = 10;

	/** The {@link Robot} to use to simulate keys. */
	@JsonIgnore
	protected Robot robot;

	/**
	 * The actual {@link KeyEvent} to simulate when the {@link KeyCombination.getShortcut()} key is
	 * set to be pressed.
	 * <p>
	 * This variable must be defined at runtime using {@link #defineShortcutKeyEvent()}. <br>
	 */
	@JsonIgnore
	protected int shortcutKeyEvent = -1;

	/**
	 * Instantiates a new HotkeyAction with stub {@link KeyCodeCombination} inside.
	 */
	public HotkeyAction() {
		shortcutKeyEvent = defineShortcutKeyEvent();
		keysProperty.set(generateStubKeyCombinationString());
	}

	/**
	 * Instantiates a new HotkeyAction using string representation of {@link KeyCodeCombination}.
	 * <br>
	 * Please use {@link KeyCodeCombination#getName()}.
	 *
	 * @param keys
	 *            the string representation of {@link KeyCodeCombination}
	 */
	@JsonCreator
	public HotkeyAction(@JsonProperty("keys") String keys) {
		this.keysProperty.set(keys);
		try {
			robot = new Robot();
		} catch (AWTException e) {
			// :o
		}
		shortcutKeyEvent = defineShortcutKeyEvent();
	}

	/**
	 * This method finds {@link KeyEvent} representation for "Shortcut" key. In Linux/Windows it's
	 * "control" keyboard key. <br>
	 * In Mac it's "Meta"(Command) keyboard key.
	 *
	 * @return the int (KeyEvent)
	 */
	protected int defineShortcutKeyEvent() {
		int keyEvent = 0;
		OS os = LowLevel.getOS();
		switch (os) {
		case LINUX:
			keyEvent = KeyEvent.VK_CONTROL;
			break;
		case MAC:
			keyEvent = KeyEvent.VK_META;
			break;
		case WINDOWS:
			keyEvent = KeyEvent.VK_CONTROL;
			break;
		}
		if (keyEvent == 0) {
			throw new RuntimeException("Jeez, what system are you using?");
		}
		return keyEvent;
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			logger.info("Hotkey: " + keyCombination.getDisplayText());
			boolean success = true;
			success = success && keysDown(keyCombination);
			success = success && keysUp(keyCombination);
			if (success) {
				elementInfo.setSuccessfulResult();
				
			} else {
				elementInfo.setFailedResult();
			}
			
		}
	}

	private String generateStubKeyCombinationString() {
		ArrayList<ModifierValue> modifiers = generateDefaultModifiers();
		// No way to create KeyCodeCombination with null keycode, using "Clear" that is not located
		// on keyboard;
		KeyCodeCombination kb = new KeyCodeCombination(emptyCode, modifiers.get(0),
				modifiers.get(1), modifiers.get(2), modifiers.get(3), modifiers.get(4));
		return kb.getName();
	}

	@JsonProperty("keys")
	public String getHotkey() {
		return keysProperty.get();
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (keysProperty.get() != null) {
			if (keysProperty.get().isEmpty()) {
				elementInfo.setAsBroken("Hotkey is empty");
				return;
			}
		} else {
			elementInfo.setAsBroken("Hotkey is not defined");
			return;
		}
		keyCombination = (KeyCodeCombination) KeyCodeCombination.keyCombination(keysProperty.get());
		// If no modifiers are pressed, lets consider HotkeyAction as broken, because it's not a
		// hotkey.
		if (keyCombination == null) {
			elementInfo.setAsBroken("Hotkey is invalid");
			return;
		}
		if (keyCombination.getCode().equals(emptyCode)) {
			elementInfo.setAsBroken("Key is not defined");
		}
	}

	/**
	 * Presses the provided {@link KeyCodeCombination}'s keys.
	 *
	 * @param kb            the KeyCodeCombination
	 * @return true, if successful
	 */
	@SuppressWarnings("deprecation")
	protected boolean keysDown(KeyCodeCombination kb) {
		try {
			if (kb.getShortcut() == ModifierValue.DOWN) {
				robot.keyPress(shortcutKeyEvent);
				Thread.sleep(modifiersPressSleepTime);
			}
			if (kb.getAlt() == ModifierValue.DOWN) {
				robot.keyPress(KeyEvent.VK_ALT);
				Thread.sleep(modifiersPressSleepTime);
			}
			if (kb.getShift() == ModifierValue.DOWN) {
				robot.keyPress(KeyEvent.VK_SHIFT);
				Thread.sleep(modifiersPressSleepTime);
			}
			// I know this is a bad practice. Do you have any idea how to avoid this, stranger?
			robot.keyPress(kb.getCode().impl_getCode());
			Thread.sleep(modifiersPressSleepTime);
		} catch (InterruptedException e) {
			logger.debug("Haha, not this time, InterruptedException!");
			return false;
		}
		return true;
	}

	/**
	 * Returns HotkeyAction's {@link #keysProperty} (Keys to simulate).
	 *
	 * @return the string property
	 */
	public StringProperty keysProperty() {
		return keysProperty;
	}

	/**
	 * Releases the provided {@link KeyCodeCombination}'s keys.
	 *
	 * @param kb            the KeyCodeCombination
	 * @return true, if successful
	 */
	@SuppressWarnings("deprecation")
	protected boolean keysUp(KeyCodeCombination kb) {
		try {
			// I know this is a bad practice. Do you have any idea how to avoid this, stranger?
			robot.keyRelease(kb.getCode().impl_getCode());
			Thread.sleep(modifiersPressSleepTime);
			if (kb.getShift() == ModifierValue.DOWN) {
				robot.keyRelease(KeyEvent.VK_SHIFT);
				Thread.sleep(modifiersPressSleepTime);
			}
			if (kb.getAlt() == ModifierValue.DOWN) {
				robot.keyRelease(KeyEvent.VK_ALT);
				Thread.sleep(modifiersPressSleepTime);
			}
			if (kb.getShortcut() == ModifierValue.DOWN) {
				robot.keyRelease(shortcutKeyEvent);
			}
		} catch (InterruptedException e) {
			logger.debug("Haha, not this time, InterruptedException!");
			return false;
		}
		return true;
	}

	@JsonProperty("keys")
	public void setHotkey(String hotkey) {
		this.keysProperty.set(hotkey);
	}

}
