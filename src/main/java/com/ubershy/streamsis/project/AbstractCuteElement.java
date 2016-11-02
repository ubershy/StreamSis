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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.ConstsAndVars;

/**
 * The abstract implementation of {@link CuteElement}.
 * <p>
 * Subtypes must override {@link #init()} method with super.init().
 */
public abstract class AbstractCuteElement implements CuteElement {
	
	static final Logger abstractLogger = LoggerFactory.getLogger(AbstractCuteElement.class);

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
	@JsonIgnore
	@Override
	public ObservableList<? extends CuteElement> getChildren() {
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
	 * @inheritDoc
	 */
	@JsonIgnore
	@Override
	public MinAddableChildrenCount getMinAddableChildrenCount() {
		return MinAddableChildrenCount.UNDEFINEDORZERO; // No information how many children need be
		                                                // added inside by default.
	}
	
	/*
	 * Subtypes must override {@link #init()} method with super.init(). <br> There can be
	 * exceptions, of course. <br> This init() sets CuteElement as healthy, reports to
	 * ProjectManager, inits CuteElement's children.
	 */
	@JsonIgnore
	@Override
	public void init() {
		initWithoutChildrenStuff();
		if (getAddableChildrenTypeInfo() != null) { // Null means no children can be added by user.
			int childrenCount = getChildren().size();
			String childrenTypeName = null;
			if (getChildContainerCreationParams() != null) {
				childrenTypeName = getChildContainerCreationParams().containerFakeTypeName;
			} else {
				childrenTypeName = getAddableChildrenTypeInfo().getType().getSimpleName();
			}
			switch (getMinAddableChildrenCount()) {
			case ONE:
				if (childrenCount < 1) {
					getAddableChildrenTypeInfo().getType().getSimpleName();
					elementInfo.setAsBroken(
							"At least one " + childrenTypeName + " needs be added inside");
				}
				break;
			case UNDEFINEDORZERO:
				break;
			default:
				break;
			}
			switch (getMaxAddableChildrenCount()) {
			case INFINITY:
				break;
			case ONE:
				if (childrenCount > 1) {
					throw new RuntimeException(
							"How this thingy ended up having more than one child?");
				}
				break;
			case UNDEFINEDORZERO:
				break;
			default:
				break;
			}
		}
		if (getChildren() != null) {
			for (CuteElement child : getChildren()) {
				child.init();
				ElementInfo childInfo = child.getElementInfo();
				if (childInfo.isBroken()) {
					String childTypeName = null;
					if (child instanceof CuteElementContainer<?>) {
						childTypeName = ((CuteElementContainer<?>) child).containerFakeTypeName;
					} else {
						childTypeName = child.getClass().getSimpleName();
					}
					this.elementInfo.setAsBroken("The contained " + childTypeName + " '"
							+ childInfo.getName() + "' is broken");
				}
			}
		}
	}

	protected void initWithoutChildrenStuff() {
		if (ConstsAndVars.slowDownInitForMs > 0) {
			try {
				Thread.sleep(ConstsAndVars.slowDownInitForMs);
			} catch (InterruptedException e) {
				abstractLogger.debug("Sleeping during init() was interrupted."); 
			}
		}
		elementInfo.setAsReadyAndHealthy();
		if (ProjectManager.getProject() != null)
			ProjectManager.getProject().incrementInitNumberOfElements();
	}
}
