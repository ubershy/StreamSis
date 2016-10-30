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
 * This container is useful for GUI rendering when an {@link CuteElement} has more than one list of
 * children. Sometimes it can be used as part of the model.
 * 
 * @see {@link AbstractActor} as example. <br>
 *      It has 3 ObservableLists of children: checker, onActions, offActions inside corresponding
 *      CuteElementContainers. It can't return all these three lists in getChildren() method.
 *      Instead it returns three CuteElementContainers each one wrapping corresponding
 *      ObservableList.
 */
public class CuteElementContainer<T extends CuteElement> extends AbstractCuteElement {

	/** The children with {@link CuteElement}s. */
	@JsonProperty("children")
	protected ObservableList<T> children = FXCollections.observableArrayList();

	/** The allowed type for children. */
	@JsonProperty("childrenType")
	protected AddableChildrenTypeInfo childrenType;
	
	/** The allowed type for children. */
	@JsonProperty("containerFakeTypeName")
	protected String containerFakeTypeName;
	
	/** The allowed max quantity of children. */
	@JsonProperty("maxAddableChildrenCount")
	protected MaxAddableChildrenCount maxAddableChildrenCount;
	
	/** The allowed min quantity of children. */
	@JsonProperty("minAddableChildrenCount")
	protected MinAddableChildrenCount minAddableChildrenCount;
	
	public CuteElementContainer() {
		// CuteElementContainers more often used as not part of the model. So let's set them as
		// not editable.
		elementInfo.setEditable(false);
	}

	/**
	 * Instantiates a new CuteElementContainer normally. It's children list is same as passed
	 * children list argument.
	 *
	 * @param children
	 *            The children to store inside CuteElementContainer.
	 * @param name
	 *            The name of this CuteElementContainer.
	 * @param childrenType
	 *            The allowed type of children.
	 * @param maxMaxAddableChildrenCount
	 *            The allowed max quantity of children.
	 * @param minAddableChildrenCount
	 *            The allowed min quantity of children.
	 * @param containerFakeTypeName
	 *            How to call this container. For example, "Adorable Actions".
	 */
	public CuteElementContainer(ObservableList<T> children, String name,
			AddableChildrenTypeInfo childrenType, MaxAddableChildrenCount maxAddableChildrenCount,
			MinAddableChildrenCount minAddableChildrenCount, String containerFakeTypeName) {
		this();
		this.children = children;
		this.elementInfo.setName(name);
		this.childrenType = childrenType;
		this.maxAddableChildrenCount = maxAddableChildrenCount;
		this.minAddableChildrenCount = minAddableChildrenCount;
		this.containerFakeTypeName = containerFakeTypeName;
	}
	
	/**
	 * Instantiates a new CuteElementContainer. Used for deserialization. It's list children is just
	 * a non-deep copy of passed children list argument.
	 *
	 * @param children
	 *            The children to store inside CuteElementContainer.
	 * @param name
	 *            The name of this CuteElementContainer.
	 * @param childrenType
	 *            The allowed type of children.
	 * @param maxChildren
	 *            The allowed max quantity of children.
	 * @param minChildren
	 *            The allowed min quantity of children.
	 * @param containerFakeTypeName
	 *            How to call this container. For example, "Adorable Actions".
	 */
	public CuteElementContainer(@JsonProperty("children") ArrayList<T> children,
			@JsonProperty("name") String name,
			@JsonProperty("childrenType") AddableChildrenTypeInfo childrenType,
			@JsonProperty("maxAddableChildrenCount") MaxAddableChildrenCount maxChildren,
			@JsonProperty("minAddableChildrenCount") MinAddableChildrenCount minChildren,
			@JsonProperty("containerFakeTypeName") String containerFakeTypeName) {
		this();
		this.children.setAll(children);
		this.elementInfo.setName(name);
		this.childrenType = childrenType;
		this.maxAddableChildrenCount = maxChildren;
		this.minAddableChildrenCount = minChildren;
		this.containerFakeTypeName = containerFakeTypeName;
	}

	@JsonIgnore
	@Override
	public ObservableList<? extends CuteElement> getChildren() {
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
		return maxAddableChildrenCount;
	}
	
	@JsonIgnore
	@Override
	public MinAddableChildrenCount getMinAddableChildrenCount() {
		return minAddableChildrenCount;
	}

	@Override
	public void init() {
		super.init();
	}
	
	/**
	 * Instantiates a new CuteElementContainer, but doesn't require programmer to provide an empty
	 * list and specify it's generic subtype, because passed childrenType parameter already contains
	 * information about subtype.
	 *
	 * @param name
	 *            The name of this CuteElementContainer.
	 * @param childrenType
	 *            The allowed type of children.
	 * @param maxAddableChildrenCount
	 *            The max allowed quantity of children.
	 * @param minAddableChildrenCount
	 *            The max allowed quantity of children.
	 */
	public static CuteElementContainer<?> createEmptyCuteElementContainer(String name,
			AddableChildrenTypeInfo childrenType,
			MaxAddableChildrenCount maxAddableChildrenCount,
			MinAddableChildrenCount minAddableChildrenCount, String ContainerFakeTypeName) {
		CuteElementContainer<?> container = null;
		switch (childrenType) {
		case ACTION:
			container = new CuteElementContainer<Action>(FXCollections.observableArrayList(), name,
					childrenType, maxAddableChildrenCount, minAddableChildrenCount,
					ContainerFakeTypeName);
			break;
		case CHECKER:
			container = new CuteElementContainer<Checker>(FXCollections.observableArrayList(), name,
					childrenType, maxAddableChildrenCount, minAddableChildrenCount,
					ContainerFakeTypeName);
			break;
		case CONTAINER:
			throw new RuntimeException("Container can't have containers as children.");
		case COUNTER:
			container = new CuteElementContainer<Counter>(FXCollections.observableArrayList(), name,
					childrenType, maxAddableChildrenCount, minAddableChildrenCount,
					ContainerFakeTypeName);
			break;
		default:
			break;
		}
		return container;
	}

}
