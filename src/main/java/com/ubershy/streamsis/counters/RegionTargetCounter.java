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
package com.ubershy.streamsis.counters;

import java.util.Iterator;
import java.util.List;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Image;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.checkers.Coordinates;
import com.ubershy.streamsis.project.AbstractCuteNode;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Region Target Counter. <br>
 * This {@link Counter} finds and counts how many image instances are located inside the specified
 * region on the screen. <br>
 * For example, it can count how many shortcuts the user have on his OS desktop by finding all
 * instances of shortcut arrow image.
 */
@SuppressWarnings("unchecked")
public class RegionTargetCounter extends AbstractCuteNode implements Counter {
	// TODO: fix duplicate code shared with RegionChecker.

	static final Logger logger = LoggerFactory.getLogger(RegionTargetCounter.class);

	/** 
	 * The {@link Coordinates} of region where to search instances of the Target image. 
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
	 * The file path of target image which instances this Checker will try to find on screen.
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
	public final static List<String> allowedExtensions = Util.singleItemAsList("*.png");

	public RegionTargetCounter() {
	}

	/**
	 * Instantiates a new RegionTargetCounter.
	 *
	 * @param coords
	 *            the {@link Coordinates} of region where to search the image.
	 * @param targetImagePath
	 *            the file path of the target image
	 * @param similarity
	 *            the image {@link #similarity} from 0 to 1.0
	 */
	@JsonCreator
	public RegionTargetCounter(@JsonProperty("coords") Coordinates coords,
			@JsonProperty("targetImagePath") String targetImagePath,
			@JsonProperty("similarity") float similarity) {
		this.similarity.set(similarity);
		this.targetImagePath.set(targetImagePath);
		this.coords = coords;
	}

	@Override
	public int count() {
		int result = 0;
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			Iterator<Match> matchResult = null;
			try {
				matchResult = coords.getRegion().findAll(targetPattern);
			} catch (FindFailed notImportant) {
				// Really, it's not that important. =)
			}
			if (matchResult != null)
				for (; matchResult.hasNext(); ++result)
					matchResult.next();
			elementInfo.setNumericResult(result);
		}
		return result;
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		coords.initRegion(elementInfo);
		if (elementInfo.isBroken()) {
			// already broken by coords.initRegion();
			return;
		}
		if (similarity.get() > 1 || similarity.get() <= 0) {
			elementInfo.setAsBroken("Similarity parameter must be from 0.0 to 1.00");
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
