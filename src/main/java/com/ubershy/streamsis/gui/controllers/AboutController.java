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
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * The controller for the view inside "About" window.
 */
public class AboutController implements Initializable {
    @FXML
    private HBox root;
    @FXML
    private ImageView iconImageView;
    @FXML
    private Label applicationNameLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private TextArea mainLicenseTextArea;
    @FXML
    private TextArea thirdPartyLicensesTextArea;
    
	private Image iconEyesClosed = new Image(
			getClass().getResource("/images/icon/icon_big.png").toExternalForm());

	private Image iconEyesOpen = new Image(
			getClass().getResource("/images/icon/icon_big_eyes_open.png").toExternalForm());

	public HBox getView() {
		return root;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		GUIManager.manageWindowStyleOfRootNode(root);
		applicationNameLabel.setText(LowLevel.getApplicationName());
		versionLabel.setText(LowLevel.getApplicationVersion());
		setFileTextForTextArea(mainLicenseTextArea, "/LICENSE.txt");
		setFileTextForTextArea(thirdPartyLicensesTextArea, "/THIRD-PARTY-LICENSES.txt");
		iconImageView.setOnMousePressed((e) -> {
			toggleEyes();
		});
	}
	
	/**
	 * Closes or opens girl's eyes.
	 */
	private void toggleEyes() {
		Image toSet;
		if (iconEyesClosed.equals(iconImageView.getImage())) {
			toSet = iconEyesOpen;
		} else {
			toSet = iconEyesClosed;
		}
		iconImageView.setImage(toSet);
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
