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
package com.ubershy.streamsis.gui.controllers;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainController {
	@FXML
	private VBox root;
	@FXML
	private AnchorPane contentPane;

	private Node fullModeView;
	private Node compactModeView;

	/** Default Compact Mode window height. */
	private final double defaultCompactModeHeight = 250;

	/** Default Compact Mode window width. */
	private final double defaultCompactModeWidth = 360;

	/** Default Full Mode window height. */
	private final double defaultFullModeHeight = 562;

	/** Default Full Mode window width. */
	private final double defaultFullModeWidth = 1000;

	private Stage window;

	public Node getView() {
		return root;
	}

	public void init(Stage primaryStage, Node fullView, Node compactView) {
		this.window = primaryStage;
		this.fullModeView = fullView;
		this.compactModeView = compactView;
	}

	public void showLastMode() {
		contentPane.getChildren().clear();
		String lastMode = CuteConfig.getString(CuteConfig.UTILGUI, "LastMode");
		switch (lastMode) {
		case "Compact":
			GUIUtil.positionWindowBasedOnCurrentMode(defaultFullModeWidth, defaultFullModeHeight);
			showCompactMode();
			break;
		case "Full":
		default:
			GUIUtil.positionWindowBasedOnCurrentMode(defaultCompactModeWidth,
					defaultCompactModeHeight);
			showFullMode();
			break;
		}
	}

	public void showFullMode() {
		// Firstly we will save the state of the window of the previous mode
		GUIUtil.saveCurrentModeWindowStateAndEverything();
		CuteConfig.setStringValue(CuteConfig.UTILGUI, "LastMode", "Full");
		window.setMaxWidth(1500);
		window.setMaxHeight(1000);
		window.setMinWidth(415);
		window.setMinHeight(500);

		GUIUtil.positionWindowBasedOnCurrentMode(defaultFullModeWidth, defaultFullModeHeight);

		Node view = this.fullModeView;
		contentPane.getChildren().clear();
		contentPane.getChildren().add(view);
		AnchorPane.setTopAnchor(view, 0.0);
		AnchorPane.setRightAnchor(view, 0.0);
		AnchorPane.setLeftAnchor(view, 0.0);
		AnchorPane.setBottomAnchor(view, 0.0);
	}

	public void showCompactMode() {
		// Firstly we will save the state of the window of the previous mode
		GUIUtil.saveCurrentModeWindowStateAndEverything();
		CuteConfig.setStringValue(CuteConfig.UTILGUI, "LastMode", "Compact");

		window.setMinHeight(200);
		window.setMinWidth(100);
		window.setMaxHeight(500);
		window.setMaxWidth(500);

		GUIUtil.positionWindowBasedOnCurrentMode(defaultCompactModeWidth, defaultCompactModeHeight);

		Node view = this.compactModeView;
		contentPane.getChildren().clear();
		contentPane.getChildren().add(view);
		AnchorPane.setTopAnchor(view, 0.0);
		AnchorPane.setRightAnchor(view, 0.0);
		AnchorPane.setLeftAnchor(view, 0.0);
		AnchorPane.setBottomAnchor(view, 0.0);
	}
}
