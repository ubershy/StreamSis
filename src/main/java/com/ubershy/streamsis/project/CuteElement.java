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
package com.ubershy.streamsis.project;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * CuteElement can {@link #init()} itself and {@link #getElementInfo()} about itself.
 * <p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
		    include = JsonTypeInfo.As.PROPERTY,
		    property = "@class")
public interface CuteElement {

	/**
	 * Gets the {@link ElementInfo}. <br>
	 * ElementInfo allows to see {@link CuteElement}'s name, state.
	 *
	 * @return the elementInfo of CuteElement
	 */
	public ElementInfo getElementInfo();

	/**
	 * Initializes the {@link CuteElement} so it's ready to work.
	 * <p>
	 * Optional things it can do: <br>
	 * 1. Decide if CuteElement is fully functional and not {@link ElementInfo#isBroken() broken}.
	 * <i>(used very often)</i> <br>
	 * 2. {@link #init()} CuteElement children. if some children are {@link ElementInfo#isBroken()
	 * broken}, CuteElement may be considered as {@link ElementInfo#isBroken() broken} too. <br>
	 * 3. Check important class fields. <br>
	 * 4. Fill important class fields with values. <br>
	 * 5. Resolve required resources, e.g. images or sounds, and validate them. <br>
	 *
	 */
	public void init();

}
