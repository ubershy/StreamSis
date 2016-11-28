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
package com.ubershy.streamsis;

import com.ubershy.streamsis.elements.actors.Actor;

/**
 * This class stores some constants and manages some variables used in {@link StreamSis}. <br>
 * GUI constants are also here. Variables in this class exist only during runtime.
 *
 * @see {@link UserVars} for managing user-defined CuteProject runtime variables
 * @see: {@link CuteConfig} for managing persisting variables.
 */
public final class ConstsAndVars {

	/** Defines if Actor will be really acting when asked to do so. */
	public static boolean performActing = true;

	/** Defines if Actor will be really checking when asked to do so. */
	public static boolean performChecking = true;

	/** If greater than 0, slows down initialization of each CuteElement. */
	public static int slowDownInitForMs = 0;

	/**
	 * minimumCheckInterval is a minimum allowed length of time to sleep in milliseconds between
	 * {@link Actor}'s checks.
	 */
	public final static int minimumCheckInterval = 50;

}
