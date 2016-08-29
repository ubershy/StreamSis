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
package com.ubershy.streamsis.actions;

import com.ubershy.streamsis.StreamSis;
import com.ubershy.streamsis.actors.Actor;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.counters.Counter;
import com.ubershy.streamsis.project.CuteNode;

/**
 * Actions are {@link Actor}'s hands. <br>
 * Action can {@link #execute()} when asked.
 * <p>
 * It does something that can be useful for the {@link StreamSis} user. <br>
 * For example, plays a sound or switches scene in Streaming Program.
 * <p>
 * Actions are usually contained inside {@link Actor}. <br>
 * Actions can be complex and contain inside other {@link CuteNode CuteNodes}: {@link Checker Checkers}, {@link Action Actions}, {@link Counter Counters}.
 * <p>
 * Action extends {@link CuteNode} interface.
 */
public interface Action extends CuteNode {

	/**
	 * It can do anything that can be useful for the {@link StreamSis} user.
	 */
	public void execute();
}
