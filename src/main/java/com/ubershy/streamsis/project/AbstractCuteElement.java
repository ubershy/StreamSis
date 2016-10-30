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

import javafx.collections.ObservableList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The abstract implementation of {@link CuteElement}.
 * <p>
 * Subtypes must override {@link #init()} method with super.init().
 */
public abstract class AbstractCuteElement implements CuteElement {

	/** Object's {@link ElementInfo}. */
	@JsonProperty
	protected ElementInfo elementInfo = new ElementInfo(this);

	/*
	 * @inheritDoc
	 */
	@Override
	public ElementInfo getElementInfo() {
		return elementInfo;
	}

	/*
	 * @inheritDoc
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@JsonIgnore
	@Override
	public ObservableList getChildren() {
		return null; // Childfree by default.
	}

	/*
	 * @inheritDoc
	 */
	@JsonIgnore
	@Override
	public AddableChildrenTypeInfo getAddableChildrenTypeInfo() {
		return null; // No information what children can be added inside by default.
	}

	/*
	 * @inheritDoc
	 */
	@JsonIgnore
	@Override
	public MaxAddableChildrenCount getMaxAddableChildrenCount() {
		return MaxAddableChildrenCount.UNDEFINEDORZERO; // No information how many children can be
														// added inside by default.
	}
	
	/*
	 * @inheritDoc
	 */
	@JsonIgnore
	@Override
	public ContainerCreationParams getChildContainerCreationParams(){
		return null; // No container parameters, because it can't have containers as children by
		             // by default.
	}
	
	/*
	 * Subtypes must override {@link #init()} method with super.init().
	 */
	@JsonIgnore
	@Override
	public void init() {
		ProjectManager.incrementInitNumberOfElements();
	}
}
