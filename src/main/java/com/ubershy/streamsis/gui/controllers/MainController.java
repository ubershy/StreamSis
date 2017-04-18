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

import java.net.URL;
import java.util.ResourceBundle;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainController implements Initializable {
	@FXML
	private VBox root;
	@FXML
	private AnchorPane contentPane;

	private Node fullModeView;
	private Node compactModeView;

	private Stage window;
	
	private boolean currentModeIsFull = false;

	public Node getView() {
		return root;
	}

	public void setPrimaryStageAndViews(Stage primaryStage, Node fullView, Node compactView) {
		this.window = primaryStage;
		this.fullModeView = fullView;
		this.compactModeView = compactView;
	}

	public void useLastMode() {
		contentPane.getChildren().clear();
		String lastMode = CuteConfig.getString(CuteConfig.UTILGUI, "LastMode");
		switch (lastMode) {
		case "Compact":
			useCompactMode();
			break;
		case "Full":
			useFullMode();
			break;
		default:
			break;
		}
	}

	public void useFullMode() {
		currentModeIsFull = true;
		String lastMode = "Full";
		String configModeSubKey = lastMode + "Mode";
		// Firstly we will save the state of the window of the previous mode
		saveCurrentModeCoordinates();
		CuteConfig.setString(CuteConfig.UTILGUI, "LastMode", lastMode);
		window.setMaxWidth(1500);
		window.setMaxHeight(1000);
		window.setMinWidth(415);
		window.setMinHeight(600);

		GUIUtil.positionWindowBasedOnConfigCoordinates(configModeSubKey, window);

		Node view = this.fullModeView;
		contentPane.getChildren().clear();
		contentPane.getChildren().add(view);
		AnchorPane.setTopAnchor(view, 0.0);
		AnchorPane.setRightAnchor(view, 0.0);
		AnchorPane.setLeftAnchor(view, 0.0);
		AnchorPane.setBottomAnchor(view, 0.0);
	}

	public void useCompactMode() {
		currentModeIsFull = false;
		String lastMode = "Compact";
		String configModeSubKey = lastMode + "Mode";
		// Firstly we will save the state of the window of the previous mode
		saveCurrentModeCoordinates();
		CuteConfig.setString(CuteConfig.UTILGUI, "LastMode", "Compact");

		window.setMinHeight(200);
		window.setMinWidth(100);
		window.setMaxHeight(500);
		window.setMaxWidth(500);

		GUIUtil.positionWindowBasedOnConfigCoordinates(configModeSubKey, window);

		Node view = this.compactModeView;
		contentPane.getChildren().clear();
		contentPane.getChildren().add(view);
		AnchorPane.setTopAnchor(view, 0.0);
		AnchorPane.setRightAnchor(view, 0.0);
		AnchorPane.setLeftAnchor(view, 0.0);
		AnchorPane.setBottomAnchor(view, 0.0);
	}
	
	/**
	 * Tells if application is running in Full Mode. If it's false, the application is probably
	 * running in Compact Mode.
	 *
	 * @return true, if current mode if Full Mode.
	 */
	public boolean isCurrentModeFull() {
		return currentModeIsFull;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		GUIManager.manageWindowStyleOfRootNode(root);
	}

	/**
	 * Saves the coordinates of MainController's window.
	 */
	public void saveCurrentModeCoordinates() {
		if (window.isShowing()) {
			String currentMode = CuteConfig.getString(CuteConfig.UTILGUI, "LastMode") + "Mode";
			GUIUtil.saveWindowCoordinates(currentMode, window);
		}
	}
}
