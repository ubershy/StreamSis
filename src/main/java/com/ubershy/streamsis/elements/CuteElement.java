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
package com.ubershy.streamsis.elements;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ubershy.streamsis.elements.actions.Action;
import com.ubershy.streamsis.elements.checkers.Checker;
import com.ubershy.streamsis.elements.counters.Counter;
import com.ubershy.streamsis.gui.helperclasses.RecursiveParent;
import com.ubershy.streamsis.project.ProjectManager;

/**
 * CuteElement is a {@link CuteElement} and {@link RecursiveParent} simultaneously with additional
 * methods useful in Hierarchical structures.
 * <p>
 * Very useful for {@link com.ubershy.streamsis.StreamSis StreamSis} GUI rendering. <br>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface CuteElement extends RecursiveParent<CuteElement> {
	
	/**
	 * Gets the {@link ElementInfo}. <br>
	 * ElementInfo allows to see {@link CuteElement}'s name, state.
	 *
	 * @return the elementInfo of CuteElement
	 */
	public ElementInfo getElementInfo();

	/**
	 * Initializes the {@link CuteElement} so it's ready to work.
	 * If this CuteElement is a {@link CuteElement}, initializes it's children.
	 * <p>
	 * Optional things it can do: <br>
	 * 1. Decide if CuteElement is fully functional and not {@link ElementInfo#isBroken() broken}.
	 * <i>(used very often)</i> <br>
	 * 2. {@link #init()} CuteElement children. if some children are {@link ElementInfo#isBroken()
	 * broken}, CuteElement may be considered as {@link ElementInfo#isBroken() broken} too. <br>
	 * 3. Check important class fields. <br>
	 * 4. Fill important class fields with values. <br>
	 * 5. Resolve required resources, e.g. images or sounds, and validate them. <br>
	 * 6. Do {@link ProjectManager#incrementInitNumberOfElements()}.
	 *
	 */
	public void init();

	/**
	 * Gets this {@link CuteElement}'s {@link AddableChildrenTypeInfo}.
	 *
	 * @return the AddableChildrenTypeInfo,<br>
	 *         null, if no information provided, so the user will be unable to add or remove
	 *         children to this CuteElement
	 */
	public AddableChildrenTypeInfo getAddableChildrenTypeInfo();

	/**
	 * Gets this {@link CuteElement}'s {@link MaxAddableChildrenCount}.
	 *
	 * @return the MaxAddableChildrenCount <br>
	 */
	public MaxAddableChildrenCount getMaxAddableChildrenCount();
	
	/**
	 * Gets this {@link CuteElement}'s {@link MinAddableChildrenCount}.
	 *
	 * @return the MinAddableChildrenCount
	 */
	public MinAddableChildrenCount getMinAddableChildrenCount();
	
	/**
	 * Gets {@link ContainerCreationParams} of this {@link CuteElement} if it can have
	 * CuteElementContainers as children.
	 *
	 * @return {@link ContainerCreationParams} of this {@link CuteElement},<br>
	 *         null if this CuteElement can't have CuteElementContainers as children.
	 */
	public ContainerCreationParams getChildContainerCreationParams();
	
	/**
	 * ContainerParams has all information required for creating {@link CuteElementContainer} as
	 * CuteElement's child by request of the user. This class is only useful when CuteElement can
	 * have CuteElementContainers created by the user - see
	 * {@link CuteElement#getAddableChildrenTypeInfo()}.
	 */
	public class ContainerCreationParams {
		public final AddableChildrenTypeInfo childrenType;
		public final MaxAddableChildrenCount childrenMaxCount;
		public final MinAddableChildrenCount childrenMinCount;
		public final String containerFakeTypeName;
		public final String creationBaseName;
		public final String GUIDescription;
		public final boolean editable;
		public final boolean emptyNameAllowed;
		
		public ContainerCreationParams(AddableChildrenTypeInfo childrenType,
				MaxAddableChildrenCount childrenMaxCount, MinAddableChildrenCount childrenMinCount,
				boolean editable, boolean emptyNameAllowed, String containerFakeTypeName,
				String creationBaseName, String GUIDescription) {
			this.childrenType = childrenType;
			this.childrenMaxCount = childrenMaxCount;
			this.childrenMinCount = childrenMinCount;
			this.editable = editable;
			this.containerFakeTypeName = containerFakeTypeName;
			this.creationBaseName = creationBaseName;
			this.GUIDescription = GUIDescription;
			this.emptyNameAllowed = emptyNameAllowed;
		}
	}

	/**
	 * MaxAddableChildrenCount contains information describing how many children can be added to
	 * this {@link CuteElement}. <br>
	 * So the GUI(if it currently exists) will know how to manage this CuteElement. <br>
	 */
	public enum MaxAddableChildrenCount {
		/** Means children can't be added. */
		UNDEFINEDORZERO,
		/** Means can add one child. */
		ONE,
		/** Means can add infinity children. */
		INFINITY
	}
	
	/**
	 * MinChildrenCount contains information describing the minimum amount of children this
	 * {@link CuteElement} needs to have. <br>
	 */
	public enum MinAddableChildrenCount {
		/** Means can have any number of children. */
		UNDEFINEDORZERO,
		/** Means should have at least one child. */
		ONE
	}

	/**
	 * AddableChildrenTypeInfo contains information describing type of the {@link CuteElement} that
	 * can be added by user to this CuteElement's list of children. <br>
	 * So the GUI(if it currently exists) will know how to manage this CuteElement.
	 */
	public enum AddableChildrenTypeInfo {
		/**
		 * Indicates that a {@link Action} can be added by user to this {@link CuteElement}'s
		 * children.
		 */
		ACTION(Action.class),

		/**
		 * Indicates that a {@link Checker} can be added by user to this {@link CuteElement}'s
		 * children.
		 */
		CHECKER(Checker.class),

		/**
		 * Indicates that a {@link Counter} can be added by user to this {@link CuteElement}'s
		 * children.
		 */
		COUNTER(Counter.class),

		/**
		 * Indicates that a {@link CuteElementContainer} can be added by user to this
		 * {@link CuteElement} 's children.
		 */
		CONTAINER(CuteElementContainer.class);

		/** The type. */
		private final Class<? extends CuteElement> type;

		/**
		 * Instantiates a new addable children type info.
		 *
		 * @param type
		 *            the type
		 */
		private AddableChildrenTypeInfo(final Class<? extends CuteElement> type) {
			this.type = type;
		}

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public Class<? extends CuteElement> getType() {
			return type;
		}
	}
}
