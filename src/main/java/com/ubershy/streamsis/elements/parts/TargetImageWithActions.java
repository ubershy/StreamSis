/** 
 * StreamSis
 * Copyright (C) 2017 Eva Balycheva
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
package com.ubershy.streamsis.elements.parts;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.elements.AbstractCuteElement;
import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.actions.Action;
import com.ubershy.streamsis.elements.actions.RegionSwitchAction;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * TargetImageWithActions is a part of {@link RegionSwitchAction}.<br>
 * It contains Target image and a list of Actions associated with that image.
 */
public class TargetImageWithActions extends AbstractCuteElement implements PartElement {
	
	static final Logger logger = LoggerFactory.getLogger(TargetImageWithActions.class);
	
	/** The description of this CuteElement type. */
	public final static String description = TargetImageWithActions.class.getSimpleName()
			+ " is a part of " + RegionSwitchAction.class.getSimpleName()
			+ ". It represents of a target image associated with a list of Actions.\n" + "About "
			+ RegionSwitchAction.class.getSimpleName() + ":\n" + RegionSwitchAction.description;

	/** The list of {@link Action Actions} associated with {@link TargetImageWithActions}. */
	@JsonProperty("actions")
	protected ObservableList<Action> actions = FXCollections.observableArrayList();
	public ObservableList<Action> getActions() {
		return actions;
	}
	
	/**
	 * The file path of target image associated with {@link TargetImageWithActions}'s {@link #actions}.
	 */
	@JsonProperty
	protected StringProperty targetImagePath = new SimpleStringProperty("");
	public StringProperty targetImagePathProperty() {return targetImagePath;}
	public String getTargetImagePath() {return targetImagePath.get();}
	public void setTargetImagePath(String targetImagePath) {
		this.targetImagePath.set(targetImagePath);
	}

	/**
	 * The acceptable extensions of image files.
	 * <p>
	 * Even though *.jpg and *.jpeg images are also supported by Sikuli library, let's restrict
	 * image files to only *.png files as it is a lossless format. This will allow to avoid
	 * complains from the users like: <br>
	 * <i> "The exact image is on my screen and similarity is 100%, but StreamSis can't find this
	 * image! =(" </i> <br>
	 * while this user provided very compressed *.jpg image as Target.
	 */
	@JsonIgnore
	public final static ObservableList<String> allowedExtensions = FXCollections
			.observableArrayList("*.png");
	
	public TargetImageWithActions() {
	}

	/**
	 * Instantiates a new TargetImageWithActions.
	 * 
	 * @param targetImagePath
	 *            The file path of the target image associated with this {@link TargetImageWithActions}.
	 * @param actions
	 *            The list of {@link Action Actions} to execute by parent when target image is
	 *            found.
	 */
	@JsonCreator
	public TargetImageWithActions(@JsonProperty("targetImagePath") String targetImagePath,
			@JsonProperty("actions") ArrayList<Action> actions) {
		this.actions.setAll(actions);
		this.targetImagePath.set(targetImagePath);
	}
	
	@Override
	public void init() {
		super.init();
		if (targetImagePath.get().isEmpty()) {
			elementInfo.setAsBroken("Target image file is not defined");
			return;
		}
		if (!Util.checkSingleFileExistanceAndExtension(targetImagePath.get(),
				allowedExtensions.toArray(new String[0]))) {
			elementInfo
					.setAsBroken("Can't find or read Target image file: " + targetImagePath.get());
			return;
		}
	}

	@Override
	public AddableChildrenTypeInfo getAddableChildrenTypeInfo() {
		return AddableChildrenTypeInfo.ACTION;
	}

	@Override
	public MaxAddableChildrenCount getMaxAddableChildrenCount() {
		return MaxAddableChildrenCount.INFINITY;
	}

	@Override
	public MinAddableChildrenCount getMinAddableChildrenCount() {
		// Allow having no Actions inside.
		return MinAddableChildrenCount.UNDEFINEDORZERO;
	}
	
	@Override
	public ObservableList<? extends CuteElement> getChildren() {
		return actions;
	}

}
