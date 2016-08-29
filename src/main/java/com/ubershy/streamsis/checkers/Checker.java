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
package com.ubershy.streamsis.checkers;

import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.actors.Actor;
import com.ubershy.streamsis.counters.Counter;
import com.ubershy.streamsis.project.CuteNode;
import com.ubershy.streamsis.project.UserVars;

/**
 * Checker is {@link Actor}'s eyes. <br>
 * Checker can {@link #check()} something when asked and return true or false result.
 * <p>
 * For example, it can check if image pattern is currently showing on user's display. <br>
 * Or if the user variable in {@link UserVars} has the right value. <br>
 * <p>
 * {@link Actor} always has one Checker. <br>
 * Checker can be complex and contain inside other {@link CuteNode CuteNodes}: {@link Checker Checkers}, {@link Action Actions}, {@link Counter Counters}.
 * <p>
 * Checker extends {@link CuteNode} interface.
 */
public interface Checker extends CuteNode {

	/**
	 * Checks something. <br>
	 * If desired state of something is acquired, returns true. <br>
	 * Otherwise, returns false.
	 *
	 * @return true, if desired state of something is acquired
	 */
	public boolean check();

}
