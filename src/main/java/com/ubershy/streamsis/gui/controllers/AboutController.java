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
package com.ubershy.streamsis.gui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import com.ubershy.streamsis.LowLevel;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;

import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 * The controller for the view inside "About" window.
 */
public class AboutController implements Initializable {
	@FXML
    private VBox root;
    @FXML
    private Label applicationNameLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private TextArea mainLicenseTextArea;
    @FXML
    private TextArea thirdPartyLicensesTextArea;

	public VBox getView() {
		return root;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		root.sceneProperty().addListener((InvalidationListener) o -> {
			if (root.getScene() != null) {
				String url = MainController.class.getResource("/css/streamsis.css")
						.toExternalForm();
				root.getScene().getStylesheets().add(url);
			}
		});
		applicationNameLabel.setText(LowLevel.getApplicationName());
		versionLabel.setText(LowLevel.getApplicationVersion());
		setFileTextForTextArea(mainLicenseTextArea, "/LICENSE.txt");
		setFileTextForTextArea(thirdPartyLicensesTextArea, "/THIRD-PARTY-LICENSES.txt");
	}
	
	private void setFileTextForTextArea(TextArea textArea, String path) {
		try {
			String text = Util.readTextFromResourceFile(path);
			textArea.setText(text);
		} catch (IOException e) {
			throw new RuntimeException("Can't get " + path + " text.");
		}
	}

	@FXML
    void openContributorsPage(ActionEvent event) {
		GUIUtil.openWebPage("https://github.com/ubershy/StreamSis/graphs/contributors");
    }

}
