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

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.actors.AbstractActor;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.counters.Counter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This container is useful for GUI rendering when an {@link CuteNode} has more than one list of
 * children. Sometimes it can be used as part of the model.
 * 
 * @see {@link AbstractActor} as example. <br>
 *      It has 3 ObservableLists of children: checker, onActions, offActions inside corresponding
 *      CuteNodeContainers. It can't return all these three lists in getChildren() method. Instead
 *      it returns three CuteNodeContainers each one wrapping corresponding ObservableList.
 */
public class CuteNodeContainer<T extends CuteNode> extends AbstractCuteNode {

	/** The children with {@link CuteNode}s. */
	@JsonProperty("children")
	protected ObservableList<T> children = FXCollections.observableArrayList();

	/** The allowed type for children. */
	@JsonProperty("childrenType")
	protected AddableChildrenTypeInfo childrenType;
	
	/** The allowed quantity of children. */
	@JsonProperty("maxMaxAddableChildrenCount")
	protected MaxAddableChildrenCount maxMaxAddableChildrenCount;
	
	public CuteNodeContainer() {
		// CuteNodeContainers more often used as not part of the model. So let's set them as
		// not editable.
		elementInfo.setEditable(false);
	}

	/**
	 * Instantiates a new CuteNodeContainer normally. It's children list is same as passed
	 * children list argument.
	 *
	 * @param children
	 *            The children to store inside CuteNodeContainer.
	 * @param name
	 *            The name of this CuteNodeContainer.
	 * @param childrenType
	 *            The allowed type of children.
	 * @param maxMaxAddableChildrenCount
	 *            The allowed quantity of children.
	 */
	public CuteNodeContainer(ObservableList<T> children, String name,
			AddableChildrenTypeInfo childrenType,
			MaxAddableChildrenCount maxMaxAddableChildrenCount) {
		this();
		this.children = children;
		this.elementInfo.setName(name);
		this.childrenType = childrenType;
		this.maxMaxAddableChildrenCount = maxMaxAddableChildrenCount;
	}
	
	/**
	 * Instantiates a new CuteNodeContainer. Used for deserialization. It's list children is just a
	 * non-deep copy of passed children list argument.
	 *
	 * @param children
	 *            The children to store inside CuteNodeContainer.
	 * @param name
	 *            The name of this CuteNodeContainer.
	 * @param childrenType
	 *            The allowed type of children.
	 * @param maxChildren
	 *            The allowed quantity of children.
	 */
	public CuteNodeContainer(@JsonProperty("children") ArrayList<T> children,
			@JsonProperty("name") String name,
			@JsonProperty("childrenType") AddableChildrenTypeInfo childrenType,
			@JsonProperty("maxMaxAddableChildrenCount") MaxAddableChildrenCount maxChildren) {
		this();
		this.children.setAll(children);
		this.elementInfo.setName(name);
		this.childrenType = childrenType;
		this.maxMaxAddableChildrenCount = maxChildren;
	}

	@JsonIgnore
	@Override
	public ObservableList<? extends CuteNode> getChildren() {
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
		// Let init() of contained children happen in CuteNodes where this CuteNodeContainer is
		// stored.
	}
	
	/**
	 * Instantiates a new CuteNodeContainer, but doesn't require programmer to provide an empty list
	 * and specify it's generic subtype, because passed childrenType parameter already contains
	 * information about subtype.
	 *
	 * @param name
	 *            The name of this CuteNodeContainer.
	 * @param childrenType
	 *            The allowed type of children.
	 * @param maxMaxAddableChildrenCount
	 *            The allowed quantity of children.
	 */
	public static CuteNodeContainer<?> createEmptyCuteNodeContainer(String name,
			AddableChildrenTypeInfo childrenType,
			MaxAddableChildrenCount maxMaxAddableChildrenCount) {
		CuteNodeContainer<?> container = null;
		switch (childrenType) {
		case ACTION:
			container =  new CuteNodeContainer<Action>(FXCollections.observableArrayList(), name,
					childrenType, maxMaxAddableChildrenCount);
			break;
		case CHECKER:
			container =  new CuteNodeContainer<Checker>(FXCollections.observableArrayList(), name,
					childrenType, maxMaxAddableChildrenCount);
			break;
		case CONTAINER:
			throw new RuntimeException("Container can't have containers as children.");
		case COUNTER:
			container = new CuteNodeContainer<Counter>(FXCollections.observableArrayList(), name,
					childrenType, maxMaxAddableChildrenCount);
			break;
		default:
			break;
		}
		return container;
	}

}
