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

import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.stage.Window;

/**
 * The WindowCoordinatesManager. <br>
 * Automatically positions window according to coordinates from config, automatically saves
 * coordinates to config on window close requests, etc.
 */
public final class WindowCoordinatesManager {

	static final Logger logger = LoggerFactory.getLogger(WindowCoordinatesManager.class);

	private static HashMap<String, Window> windows = new HashMap<>();

	/**
	 * Starts to manage the specified window. Saves window coordinates on close request, etc.
	 *
	 * @param windowNameInConfig
	 *            The name of the window in config.
	 * @param window
	 *            The window which coordinates to manage.
	 */
	public static void manageWindowCoordinates(String windowNameInConfig, Window window) {
		Window previousWindow = windows.put(windowNameInConfig, window);
		if (previousWindow != null) {
			throw new RuntimeException("Window is already managed");
		}
		window.setOnCloseRequest((e) -> {
			GUIUtil.saveWindowCoordinates(windowNameInConfig, window);
		});
		window.setOnShowing((e) -> {
			GUIUtil.positionWindowBasedOnConfigCoordinates(windowNameInConfig, window);
		});
	}

	public static void saveCoordinatesOfAllManagedWindows() {
		for (Entry<String, Window> entry : windows.entrySet()) {
			Window window = entry.getValue();
			if (window.isShowing()) {
				GUIUtil.saveWindowCoordinates(entry.getKey(), entry.getValue());
			}
		}
	}

}
