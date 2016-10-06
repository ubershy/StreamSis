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

import com.ubershy.streamsis.project.CuteElement;

/**
 * The Interface CuteElementController represents the view with controls to edit specific
 * {@link CuteElement} in Element Editor panel.
 */
public interface CuteElementController extends CuteController {

	/**
	 * Sets the {@link CuteElement} to work with and binds view's controls to CuteElement's
	 * properties.
	 *
	 * @param editableCopyOfCE
	 *            The CuteElement to edit. (Actually a copy of the CuteElement the user wishes to
	 *            edit. The changes made in the copy will be transferred to original CuteElement
	 *            once the user hit "Apply" or "OK" button).
	 * @param origCE
	 *            The Original CuteElement to use as storage of original values of CuteElement's
	 *            attributes. Should not be edited in controllers.
	 */
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE);
	
	/**
	 * Unbinds view's controls from CuteElement's properties.
	 */
	public void unbindFromCuteElement();

}
