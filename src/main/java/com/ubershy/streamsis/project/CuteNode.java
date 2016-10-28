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
	 * Gets {@link ContainerCreationParams} of this {@link CuteNode} if it can have
	 * CuteNodeContainers as children.
	 *
	 * @return {@link ContainerCreationParams} of this {@link CuteNode},<br>
	 *         null if this CuteNode can't have CuteNodeContainers as children.
	 */
	public ContainerCreationParams getChildContainerCreationParams();
	
	/**
	 * ContainerParams has all information required for creating {@link CuteNodeContainer} as
	 * CuteNode's child by request of the user. This class is only useful when CuteNode can have
	 * CuteNodeContainers created by the user - see {@link CuteNode#getAddableChildrenTypeInfo()}.
	 */
	public class ContainerCreationParams {
		public final AddableChildrenTypeInfo childrenType;
		public final MaxAddableChildrenCount childrenMaxCount;
		public final String GUIItemMenuName;
		public final String creationBaseName;
		public final String GUIDescription;
		public final boolean editable;
		public final boolean emptyNameAllowed;
		
		public ContainerCreationParams(AddableChildrenTypeInfo childrenType,
				MaxAddableChildrenCount childrenMaxCount, boolean editable,
				boolean emptyNameAllowed, String GUIItemMenuName, String creationBaseName,
				String GUIDescription) {
			this.childrenType = childrenType;
			this.childrenMaxCount = childrenMaxCount;
			this.editable = editable;
			this.GUIItemMenuName = GUIItemMenuName;
			this.creationBaseName = creationBaseName;
			this.GUIDescription = GUIDescription;
			this.emptyNameAllowed = emptyNameAllowed;
		}
	}

	/**
	 * MaxAddableChildrenCount contains information describing how many children can be added to
	 * this {@link CuteNode}. <br>
	 * So the GUI(if it currently exists) will know how to manage this CuteNode. <br>
	 */
	public enum MaxAddableChildrenCount {

		/** The undefinedorzero. */
		UNDEFINEDORZERO,
		/** The one. */
		ONE,
		/** The infinity. */
		INFINITY
	}

	/**
	 * AddableChildrenTypeInfo contains information describing type of the {@link CuteNode} that can
	 * be added by user to this CuteNode's list of children. <br>
	 * So the GUI(if it currently exists) will know how to manage this CuteNode.
	 */
	public enum AddableChildrenTypeInfo {
		/**
		 * Indicates that a {@link Action} can be added by user to this {@link CuteNode}'s children.
		 */
		ACTION(Action.class),

		/**
		 * Indicates that a {@link Checker} can be added by user to this {@link CuteNode}'s
		 * children.
		 */
		CHECKER(Checker.class),

		/**
		 * Indicates that a {@link Counter} can be added by user to this {@link CuteNode}'s
		 * children.
		 */
		COUNTER(Counter.class),

		/**
		 * Indicates that a {@link CuteNodeContainer} can be added by user to this {@link CuteNode}
		 * 's children.
		 */
		CONTAINER(CuteNodeContainer.class);

		/** The type. */
		private final Class<? extends CuteNode> type;
		
		/**
		 * Instantiates a new addable children type info.
		 *
		 * @param type the type
		 */
		private AddableChildrenTypeInfo(final Class<? extends CuteNode> type) {
			this.type = type;
		}

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public Class<? extends CuteNode> getType() {
			return type;
		}
	}
}
