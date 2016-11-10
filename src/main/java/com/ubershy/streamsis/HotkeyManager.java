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
package com.ubershy.streamsis;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.project.ProjectManager;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.ModifierValue;

/**
 * Listens to global Hotkeys if asked and runs the associated actions when catches one of them.
 */
public final class HotkeyManager {
	
	/**
	 * The Enum with all possible Hotkeys in StreamSis.
	 */
	public enum Hotkey implements Runnable {

		START("Start Project", "Shift+F1", () -> {
			ProjectManager.getProject().startProject();
		}),

		STOP("Stop Project", "Shift+F2", () -> {
			ProjectManager.getProject().stopProject();
		}),

		SELECTREGION("Select Region on screen (when available)", "Shift+F3", () -> {
			if (currentSelectRegionRunnable != null) {
				currentSelectRegionRunnable.run();
			} else {
				logger.info("Selection of Region on screen is currently unavailable.");
			}
		}),

		SELECTIMAGE("Select Target Image (and sometimes Region) on screen (when available)",
				"Shift+F4", () -> {
					if (currentSelectImageRunnable != null) {
						currentSelectImageRunnable.run();
					} else {
						logger.info("Selection of Image on screen is currently unavailable.");
					}
				});

		/** The Hotkey's user-friendly short description. */
		private final String description;
		
		/** The action this Hotkey does. */
		private final Runnable action;
		
		/** The default Keys for this Hotkey, for example, "Alt+F4". */
		private final String defaultKeys;

		/**
		 * Instantiates a new Hotkey.
		 *
		 * @param description
		 *            The Hotkey's user-friendly short description.
		 * @param defaultKeys
		 *            The default Keys for this Hotkey, for example, "Alt+F4".
		 * @param action
		 *            The action this Hotkey does.       
		 */
		private Hotkey(String description, String defaultKeys, Runnable action) {
			this.description = description;
			this.defaultKeys = defaultKeys;
			this.action = action;
		}

		@Override
		public String toString() {
			return description;
		}
		
		public void run() {
			action.run();
		}
		
		/** Gets {@link #defaultKeys} for this Hotkey. */
		public String getDefaultKeys() {
			return defaultKeys;
		}
	}
	
	public static class NKListener implements NativeKeyListener {
		
		@SuppressWarnings("deprecation")
		@Override
		public void nativeKeyPressed(NativeKeyEvent e) {
			if (hotkeyIsPressed) {
				return; // Do nothing if Hotkey is already pressed.
			}
			int modifiers = e.getModifiers();
		    if (modifiers == 0)
		    	return; // Do nothing when no modifiers are pressed, because it's not a hotkey.
		    boolean isAltPressed = (modifiers & NativeKeyEvent.ALT_MASK) != 0;
		    boolean isShiftPressed = (modifiers & NativeKeyEvent.SHIFT_MASK) != 0;
		    // Shortcut modifier is Control modifier in Windows/Linux system and Meta (Command) in
		    // Mac systems.
			boolean isShortcutPressed = (modifiers & NativeKeyEvent.CTRL_MASK)
					+ (modifiers & NativeKeyEvent.META_MASK) != 0;
			for (Entry<Hotkey, ObjectProperty<KeyCodeCombination>> entry : registeredHotkeys
					.entrySet()) {
				Hotkey hotkey = entry.getKey();
				KeyCodeCombination kcc = entry.getValue().get();
				// FIXME: provide compatibility with Java 9 (pretty easy).
				if (e.getRawCode() != kcc.getCode().impl_getCode())
					continue; // Key code does not match Hotkey.
				if (isAltPressed != (kcc.getAlt() == ModifierValue.DOWN))
					continue; // Alt modifier doesn't match Hotkey.
				if (isShiftPressed != (kcc.getShift() == ModifierValue.DOWN))
					continue; // Shift modifier doesn't match Hotkey.
				if (isShortcutPressed != (kcc.getShortcut() == ModifierValue.DOWN))
					continue; // Shortcut modifier doesn't match Hotkey.
				if (hotkeyIsRunning) {
					logger.error("Previously pressed Hotkey is still running.");
					return; // Do nothing if Hotkey is still running.
				}
				// At this point the pressed key and modifiers match one of the registered Hotkeys.
				// Let's run the associated runnable with this Hotkey.
				logger.info("Catched the Hotkey: \"" + hotkey.name()
						+ "\". Running the associated action.");
				hotkeyIsPressed = true;
				hotkeyIsRunning = true;
				Task<Void> task = new Task<Void>() {
					@Override
					protected Void call() {
						hotkey.run();
						return null;
					}
				};
				task.setOnSucceeded(succededEvent -> {
					logger.info("Finished running the action associated with the Hotkey: \""
							+ hotkey.name() + "\".");
					hotkeyIsRunning = false;
				});
				task.run();
				new Thread(task).run();
				break;
			}
			
		}

		@Override
		public void nativeKeyReleased(NativeKeyEvent e) {
			 hotkeyIsPressed = false;
		}

		@Override
		public void nativeKeyTyped(NativeKeyEvent e) {
			// Do nothing.
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(HotkeyManager.class);
	
	/** Tells if {@link HotkeyManager} is currently listening to Hotkeys. */
	private static volatile boolean isListening = false;
	
	/** Tells if one of the {@link #registeredHotkeys} are currently pressed on keyboard. */
	private static volatile boolean hotkeyIsPressed = false;
	
	/**
	 * Tells if the action associated with one of the {@link #registeredHotkeys} is currently
	 * running. During this time, no other Hotkey actions can be run.
	 */
	private static volatile boolean hotkeyIsRunning = false;
	
	/** The map with {@link Hotkey}s and currently associated KeyCodeCombinations with them. */
	private static final Map<Hotkey, ObjectProperty<KeyCodeCombination>> registeredHotkeys;
	
	/** The listener of key presses. */
	private static final NKListener nkListener = new NKListener();

	/**
	 * The action to execute when {@link HotkeyManager} catches {@link Hotkey#SELECTREGION} Hotkey.
	 */
	private static Runnable currentSelectRegionRunnable;

	/**
	 * The action to execute when {@link HotkeyManager} catches {@link Hotkey#SELECTIMAGE} Hotkey.
	 */
	private static Runnable currentSelectImageRunnable;
	
	static {
		// Disable logging of jnativehook
		// TODO: enable logging of jnativehook with "warning" level and bridge it to sl4j.
		java.util.logging.Logger jnativehookLogger = java.util.logging.Logger
				.getLogger(GlobalScreen.class.getPackage().getName());
		jnativehookLogger.setLevel(java.util.logging.Level.OFF);
		jnativehookLogger.setUseParentHandlers(false);
		// Let's fill registeredHotkeys map.
		registeredHotkeys = new EnumMap<>(Hotkey.class);
		for (Hotkey hk : Hotkey.values()) {
			// These are stringified keyboard keys. "Alt+F4" for example.
			String keys = CuteConfig.getString(CuteConfig.HOTKEYS, hk.name());
			KeyCodeCombination kcc = (KeyCodeCombination) KeyCodeCombination.valueOf(keys);
			ObjectProperty<KeyCodeCombination> opkcc = new SimpleObjectProperty<KeyCodeCombination>(
					kcc);
			registeredHotkeys.put(hk, opkcc);
		}
	}
	
	/** Makes {@link HotkeyManager} start listening to Hotkeys. */
	public static void startListeningToHotkeys() throws NativeHookException {
		if (!isListening) {
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(nkListener);
			isListening = true;
		}
	}

	/** Makes {@link HotkeyManager} stop listening to Hotkeys. */
	public static void stopListeningToHotkeys() throws NativeHookException {
		if (isListening) {
			GlobalScreen.unregisterNativeHook();
			GlobalScreen.removeNativeKeyListener(nkListener);
			isListening = false;
		}
	}

	/**
	 * Sets action to execute when {@link HotkeyManager} catches {@link Hotkey#SELECTREGION} Hotkey.
	 *
	 * @param r
	 *            The runnable to run when {@link HotkeyManager} catches {@link Hotkey#SELECTREGION}
	 *            Hotkey.
	 */
	public static void setSelectRegionRunnable(Runnable r) {
		currentSelectRegionRunnable = r;
	}

	/**
	 * Sets action to execute when {@link HotkeyManager} catches {@link Hotkey#SELECTIMAGE} Hotkey.
	 * 
	 * @param r
	 *            The runnable to run when {@link HotkeyManager} catches {@link Hotkey#SELECTIMAGE}
	 *            Hotkey.
	 */
	public static void setSelectImageRunnable(Runnable r) {
		currentSelectImageRunnable = r;
	}

	/**
	 * Returns true if {@link HotkeyManager} is currently active and listening to Hotkeys.
	 * 
	 * @return True, if {@link HotkeyManager} is currently active and listening to Hotkeys. False -
	 *         otherwise.
	 */
	public static boolean isListening() {
		return isListening;
	}

	/**
	 * Gets {@link #registeredHotkeys} of {@link HotkeyManager}.
	 * 
	 * @return The {@link #registeredHotkeys} of {@link HotkeyManager}.
	 */
	public static Map<Hotkey, ObjectProperty<KeyCodeCombination>> getRegisteredHotkeys() {
		return registeredHotkeys;
	}

	/**
	 * Gets the {@link KeyCodeCombination} property associated with {@link Hotkey}.
	 *
	 * @param hk The Hotkey which KeyCodeCombination to return.
	 * @return The KeyCodeCombination.
	 */
	public static ObjectProperty<KeyCodeCombination> getKeyCodeCombinationPropertyOfHotkey(
			Hotkey hk) {
		return registeredHotkeys.get(hk);
	}

}
