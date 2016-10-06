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

import org.controlsfx.validation.ValidationSupport;

import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;

import javafx.fxml.Initializable;
import javafx.scene.Node;

/**
 * The Interface CuteController represents the view with controls to edit something in Element
 * Editor panel.
 */
public interface CuteController extends Initializable {
	
	/**
	 * Gets the View's Root Node corresponding to this controller.
	 *
	 * @return the view
	 */
	public Node getView();

	/**
	 * Sets the CuteButtonsStatesManager - a thing where to report about validation errors and
	 * changes in controls (user input fields).
	 *
	 * @param buttonStateManager
	 *            the CuteButtonsStatesManager
	 */
	public void setCuteButtonsStatesManager(CuteButtonsStatesManager buttonStateManager);

	/**
	 * Sets the {@link ValidationSupport} to this controller. <br>
	 * This will allow to nicely validate values in controls, by registering controls in
	 * ValidationSupport.
	 *
	 * @param validationSupport
	 *            the ValidationSupport to use.
	 */
	public void setValidationSupport(ValidationSupport validationSupport);

}
