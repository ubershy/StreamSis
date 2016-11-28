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
package com.ubershy.streamsis.elements.counters;

import com.ubershy.streamsis.elements.CuteElement;

/**
 * Counter is any class that can {@link #count()} something. <br>
 * <p>
 * For example, Counter may count how many ponies are located on the user's screen or how much damage they have caused to the user's life. <br>
 * In a game with ponies. If such game exists...
 */
public interface Counter extends CuteElement {

	/**
	 * Counts something.
	 *
	 * @return the number returned by counting operation
	 */
	public int count();
}
