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

import org.controlsfx.validation.ValidationSupport;

import com.ubershy.streamsis.gui.helperclasses.ApplyAndOkButtonsStateManager;
import com.ubershy.streamsis.project.CuteElement;

import javafx.scene.Node;

/**
 * The Interface CuteController represents the view with controls to edit specific
 * {@link CuteElement} in Element Editor panel.
 */
public interface CuteController {

	/**
	 * Sets the {@link CuteElement} to work with and binds view's controls to CuteElement's
	 * properties.
	 *
	 * @param element
	 *            the CuteElement
	 */
	public void bindToCuteElement(CuteElement element);

	/**
	 * Gets the View's Root Node corresponding to this controller.
	 *
	 * @return the view
	 */
	public Node getView();

	/**
	 * Resets all view's controls to original values of {@link CuteElement}'s properties.
	 */
	public void reset();

	/**
	 * Sets the ApplyAndOkButtonsStateManager - a thing where to report about validation errors in
	 * controls.
	 *
	 * @param buttonStateManager
	 *            the ApplyAndOkButtonsStateManager
	 */
	public void setApplyAndOkButtonsStateManager(ApplyAndOkButtonsStateManager buttonStateManager);

	/**
	 * Apply changes made in controls to {@link CuteElement}.
	 */
	public void apply();

	/**
	 * Sets the {@link ValidationSupport} to this controller. <br>
	 * This will allow to validate nicely values in controls, by registering controls in
	 * ValidationSupport.
	 *
	 * @param validationSupport
	 *            the ValidationSupport to use.
	 */
	public void setValidationSupport(ValidationSupport validationSupport);
}
