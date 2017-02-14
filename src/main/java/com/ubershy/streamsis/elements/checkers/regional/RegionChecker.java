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
package com.ubershy.streamsis.elements.checkers.regional;

import org.sikuli.script.Image;
import org.sikuli.script.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.elements.AbstractCuteElement;
import com.ubershy.streamsis.elements.checkers.Checker;
import com.ubershy.streamsis.elements.parts.Coordinates;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Region Checker. <br>
 * This {@link Checker} can scan a region(represented by {@link Coordinates}) on the display screen for the presence of the targetPattern image. <br>
 * Returns true on {@link #check()} when the targetPattern image is contained within the region. <br>
 * The region can be bigger than the targetPattern image we search inside it. <br>
 * <p>
 * The tolerance of how different the searched image can look in region is specified by {@link #similarity} parameter from 0 to 1.0, e.g.: <br>
 * "1.0" - exact image. <br>
 * "0.9" - almost exact image. <br>
 * "0.5" - everything that slightly reminds the targetPattern image. <br>
 * "0" - everything.
 */
public class RegionChecker extends AbstractCuteElement implements Checker {

	static final Logger logger = LoggerFactory.getLogger(RegionChecker.class);
	
	/** The description of this CuteElement type. */
	public final static String description = RegionChecker.class.getSimpleName()
			+ " on check performs 2D image recognition in the specified region on the screen.\n"
			+ "If it's able to recognize the specified image in this region, returns True."
			+ " False otherwise.";

	/** 
	 * The {@link Coordinates} of region where to search the image. 
	 */
	@JsonProperty
	protected Coordinates coords = new Coordinates(0, 0, 100, 100);
	public Coordinates getCoords() {return coords;}
	public void setCoords(Coordinates coords) {this.coords = coords;}
	
	/**
	 * The similarity. <br>
	 * A float number from 0 to 1.00 specifying the tolerance of how different the searched image
	 * can look in region. <br>
	 * E.g. "1.00f" - exact image. <br>
	 */
	@JsonProperty
	protected FloatProperty similarity = new SimpleFloatProperty(0.95f);
	public FloatProperty similarityProperty() {return similarity;};
	public float getSimilarity() {return similarity.get();}
	public void setSimilarity(float similarity) {this.similarity.set(similarity);}

	/** 
	 * The file path of target image which this Checker will try to find on screen.
	 */
	@JsonProperty
	protected StringProperty targetImagePath = new SimpleStringProperty("");
	public StringProperty targetImagePathProperty() {return targetImagePath;}
	public String getTargetImagePath() {return targetImagePath.get();}
	public void setTargetImagePath(String targetImagePath) {
		this.targetImagePath.set(targetImagePath);
	}

	/** The target {@link org.sikuli.script.Pattern Pattern} to internally work with. */
	protected Pattern targetPattern;
	
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

	public RegionChecker() {
	}

	/**
	 * Instantiates a new RegionChecker.
	 *
	 * @param coords
	 *            the {@link Coordinates} of region where to search the image.
	 * @param targetImagePath
	 *            the file path of the target image
	 * @param similarity
	 *            the image {@link #similarity} from 0 to 1.0
	 */
	@JsonCreator
	public RegionChecker(@JsonProperty("coords") Coordinates coords,
			@JsonProperty("targetImagePath") String targetImagePath,
			@JsonProperty("similarity") float similarity) {
		this.targetImagePath.set(targetImagePath);
		this.similarity.set(similarity);
		this.coords = coords;
	}

	@Override
	public boolean check() {
		boolean result = false;
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			// TODO: debug mode variable in config
			// if (Util.debugMode) {
			// coords.highlightRegion();
			// }
			result = (coords.getRegion().exists(targetPattern, 0) != null);
			elementInfo.setBooleanResult(result);
		}
		return result;
	}

	@Override
	public void init() {
		super.init();
		targetPattern = null;
		coords.initRegion(elementInfo);
		if (elementInfo.isBroken()) {
			// already broken by coords.initRegion();
			return;
		}
		// Let's not accept zero value even though SikuliX library accepts such values. Why?
		// 1. It's pointless to find image with zero similarity.
		// 2. For some reason SikuliX library's Region.exists() method stops working correctly after
		// the first try to find image with zero similarity.
		if (similarity.get() > 1 || similarity.get() <= 0) {
			elementInfo.setAsBroken("Similarity parameter must be from 0 to 1.00");
			return;
		}
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
		Image.unCacheBundledImage(targetImagePath.get());
		targetPattern = new Pattern(targetImagePath.get()).similar(similarity.get());
		Image image = targetPattern.getImage();
		if (image.getSize().getWidth() > coords.getW()) {
			elementInfo.setAsBroken(
					"Target image's width should be smaller than width of specified Region");
			return;
		}
		if (image.getSize().getHeight() > coords.getH()) {
			elementInfo.setAsBroken(
					"Target image's height should be smaller than height of specified Region");
			return;
		}
	}

}
