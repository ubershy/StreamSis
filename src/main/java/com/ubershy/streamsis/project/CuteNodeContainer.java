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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ubershy.streamsis.actors.AbstractActor;

import javafx.collections.ObservableList;

/**
 * This container is useful for GUI rendering when an CuteNode has more than one list of children. <br>
 * 
 * @see {@link AbstractActor} as example. <br>
 *      It has 3 lists of children: checker, onActions, offActions inside corresponding CuteNodeContainers.
 */
@SuppressWarnings("rawtypes")
public class CuteNodeContainer extends AbstractCuteNode {

	/** The children. */
	protected ObservableList<CuteNode> children;

	/** The allowed type for children. */
	protected AddableChildrenTypeInfo childrenType;
	
	/** The allowed quantity of children. */
	protected MaxAddableChildrenCount maxMaxAddableChildrenCount;

	/**
	 * Instantiates a new CuteNodeContainer.
	 *
	 * @param children
	 *            the children to store inside CuteNodeContainer
	 * @param name
	 *            the name of this CuteNodeContainer
	 * @param childrenType
	 *            the allowed type of children
	 */
	@SuppressWarnings("unchecked")
	public CuteNodeContainer(ObservableList children, String name, AddableChildrenTypeInfo childrenType, MaxAddableChildrenCount maxMaxAddableChildrenCount) {
		this.children = children;
		this.elementInfo.setName(name);
		this.childrenType = childrenType;
		this.maxMaxAddableChildrenCount = maxMaxAddableChildrenCount;
	}

	@JsonIgnore
	@Override
	public ObservableList<CuteNode> getChildren() {
		return children;
	}

	@JsonIgnore
	@Override
	public AddableChildrenTypeInfo getAddableChildrenTypeInfo() {
		return childrenType;
	}
	
	@JsonIgnore
	@Override
	public MaxAddableChildrenCount getMaxAddableChildrenCount() {
		return maxMaxAddableChildrenCount;
	}

	@Override
	public void init() {
		// Let init() happen in CuteNodes where CuteNodeContainers are stored.
	}

}
