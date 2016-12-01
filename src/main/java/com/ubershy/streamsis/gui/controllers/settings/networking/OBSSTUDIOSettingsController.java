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
package com.ubershy.streamsis.gui.controllers.settings.networking;

import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.control.textfield.CustomPasswordField;
import com.ubershy.streamsis.CuteConfig;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class OBSSTUDIOSettingsController implements SpecificNetworkSettingsController {
	
	private static final String PASSCONFIGSUBKEY = "OBSSTUDIOPASS";

    @FXML
    private GridPane root;
    @FXML
    private CustomPasswordField passwordField;
    
	public GridPane getView() {
		return root;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		passwordField.setText(CuteConfig.getString(CuteConfig.NETWORKING, PASSCONFIGSUBKEY));
	}
	
	public void applySettings() {
		CuteConfig.setSecretString(CuteConfig.NETWORKING, PASSCONFIGSUBKEY,
				passwordField.getText());
	}

}
