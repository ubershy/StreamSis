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

import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsController implements Initializable {

    @FXML
    private VBox root;
    @FXML
    private CheckBox projectAutoLoadCheckBox;
    @FXML
    private CheckBox projectAutoStartCheckBox;
    @FXML
    private ButtonBar buttonBar;

	public VBox getView() {
		return root;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO: implement Hotkeys tab
		root.sceneProperty().addListener((InvalidationListener) o -> {
			if (root.getScene() != null) {
				String url = SettingsController.class.getResource("/css/streamsis.css")
						.toExternalForm();
				root.getScene().getStylesheets().add(url);
			}
		});
		
		projectAutoLoadCheckBox.selectedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				projectAutoStartCheckBox.setDisable(true);
				projectAutoStartCheckBox.setSelected(false);
			} else {
				projectAutoStartCheckBox.setDisable(false);
			}
		});

		setInitialValuesOfControls();
		
		Button okButton = new Button("OK");
		ButtonBar.setButtonData(okButton, ButtonData.OK_DONE);
		Button cancelButton = new Button("Cancel");
		ButtonBar.setButtonData(cancelButton, ButtonData.CANCEL_CLOSE);
		buttonBar.getButtons().addAll(okButton, cancelButton);
		okButton.addEventFilter(ActionEvent.ACTION, (event) -> {
			applySettings();
			((Stage) okButton.getScene().getWindow()).close();
		});
		cancelButton.addEventFilter(ActionEvent.ACTION, (event) -> {
			((Stage) okButton.getScene().getWindow()).close();
		});
	}

	/**
	 * Sets the initial values of controls.
	 */
	private void setInitialValuesOfControls() {
		boolean projectAutoLoad = CuteConfig.getBoolean(CuteConfig.CUTE, "ProjectAutoLoad");
		projectAutoLoadCheckBox.setSelected(projectAutoLoad);
		if (projectAutoLoad) {
			projectAutoStartCheckBox
					.setSelected(CuteConfig.getBoolean(CuteConfig.CUTE, "ProjectAutoStart"));
		}
	}

	/**
	 * Applies new settings.
	 */
	private void applySettings() {
		CuteConfig.setBooleanValue(CuteConfig.CUTE, "ProjectAutoLoad",
				projectAutoLoadCheckBox.isSelected());
		CuteConfig.setBooleanValue(CuteConfig.CUTE, "ProjectAutoStart",
				projectAutoStartCheckBox.isSelected());
	}
}
