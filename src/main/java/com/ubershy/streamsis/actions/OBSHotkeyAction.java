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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.input.KeyCodeCombination;

/**
 * OBS Hotkey Action. <br>
 * This temporary(will be deprecated soon and replaced by WebSockets) {@link Action} simulates
 * sending keyboard key combination to OS(current focused element in OS), i.e. hotkey. <br>
 * Used to control OBS (to switch scenes for examples). <br>
 * The variable {@link HotkeyAction#modifiersPressSleepTime} is increased so it will allow OBS to
 * respond to keystrokes. <br>
 * Something wrong with OBS and it don't respond to simulated hotkeys properly. It needs special
 * treat. <br>
 * 
 * @see {@link HotkeyAction}
 */
@SuppressWarnings("unchecked")
public class OBSHotkeyAction extends HotkeyAction implements Action {

	static final Logger logger = LoggerFactory.getLogger(OBSHotkeyAction.class);

	/**
	 * Instantiates a new HotkeyAction with stub {@link KeyCodeCombination} inside.
	 */
	public OBSHotkeyAction() {
		super();
		// The large sleep time between pressing modifiers is very important for OBS =)
		modifiersPressSleepTime = 50;
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
	public OBSHotkeyAction(@JsonProperty("keys") String keys) {
		super(keys);
		modifiersPressSleepTime = 50;
	}

}
