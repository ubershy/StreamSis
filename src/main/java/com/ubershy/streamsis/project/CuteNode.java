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

import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.counters.Counter;
import com.ubershy.streamsis.gui.helperclasses.RecursiveParent;

/**
 * CuteNode is a {@link CuteElement} and {@link RecursiveParent} simultaneously with additional
 * methods useful in Hierarchical structures.
 * <p>
 * Very useful for {@link com.ubershy.streamsis.StreamSis StreamSis} GUI rendering. <br>
 */
public interface CuteNode extends RecursiveParent<CuteNode>, CuteElement {

	/**
	 * Gets this {@link CuteNode}'s {@link AddableChildrenTypeInfo}.
	 *
	 * @return the AddableChildrenTypeInfo,<br>
	 *         null, if no information provided, so the user will be unable to add or remove
	 *         children to this CuteNode
	 */
	public AddableChildrenTypeInfo getAddableChildrenTypeInfo();

	/**
	 * Gets this {@link CuteNode}'s {@link MaxAddableChildrenCount}.
	 *
	 * @return the MaxAddableChildrenCount, <br>
	 *         null, if no information provided, so the user will be unable to add or remove
	 *         children to this CuteNode
	 */
	public MaxAddableChildrenCount getMaxAddableChildrenCount();

	/**
	 * MaxAddableChildrenCount contains information describing how many children can be added to
	 * this {@link CuteNode}. <br>
	 * So the GUI(if it currently exists) will know how to manage this CuteNode. <br>
	 */
	public enum MaxAddableChildrenCount {
		UNDEFINEDORZERO, ONE, INFINITY
	}

	/**
	 * AddableChildrenTypeInfo contains information describing type of the {@link CuteNode} that can
	 * be added to this CuteNode's list of children. <br>
	 * So the GUI(if it currently exists) will know how to manage this CuteNode.
	 */
	public enum AddableChildrenTypeInfo {
		/** Indicates that a {@link Action} can be added to this {@link CuteNode}'s children */
		ACTION(Action.class),

		/** Indicates that a {@link Checker} can be added to this {@link CuteNode}'s children */
		CHECKER(Checker.class),

		/** Indicates that a {@link Counter} can be added to this {@link CuteNode}'s children */
		COUNTER(Counter.class);

		private final Class<? extends CuteNode> type;

		private AddableChildrenTypeInfo(final Class<? extends CuteNode> type) {
			this.type = type;
		}

		public Class<? extends CuteNode> getType() {
			return type;
		}
	}
}
