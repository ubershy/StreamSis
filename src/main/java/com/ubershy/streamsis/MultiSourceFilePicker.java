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
package com.ubershy.streamsis;

import java.io.File;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.project.ElementInfo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * MultiSourceFilePicker can help to pick next time file either randomly or sequentially from a
 * predefined list of files or from a directory containing files.
 * 
 * @note before using this class, {@link #initTemporaryFileList(ElementInfo, String, String)} must
 * be invoked first with desired parameters.
 */
public class MultiSourceFilePicker extends MultiSourceFileLister {

	// Properties
	
	/** Defines if to pick each next file randomly or sequentially. */
	@JsonProperty("pickFilesRandomly")
	protected BooleanProperty pickFilesRandomly = new SimpleBooleanProperty(true);
	public BooleanProperty pickFilesRandomlyProperty() {return pickFilesRandomly;}
	public boolean isPickFilesRandomly() {return pickFilesRandomly.get();}
	public void setPickFilesRandomly(boolean pickRandomly) {
		this.pickFilesRandomly.set(pickRandomly);
	}
	
	// Runtime variables
	
	/** The random index generator. */
	@JsonIgnore
	protected NonRepeatingRandom random = new NonRepeatingRandom();

	/**
	 * The index of current file in {@link #temporarySourceFileList}.
	 * 
	 * @note Should not be serialized.
	 */
	@JsonIgnore
	protected IntegerProperty currentFileIndex = new SimpleIntegerProperty(0);
	public IntegerProperty currentFileIndexProperty() {return currentFileIndex;}
	public int getCurrentFileIndex() {return currentFileIndex.get();}
	public void setCurrentFileIndex(int currentFileIndex) {
		this.currentFileIndex.set(currentFileIndex);
	}

	public MultiSourceFilePicker() {

	}

	/**
	 * Instantiates a new MultiSourceFilePicker.
	 *
	 * @param pickFileRandomly
	 *            {@link #pickFilesRandomly}
	 * @param findSourcesInSrcPath
	 *            {@link #findingSourcesInSrcPath}
	 * @param persistentSourceFileList
	 *            the {@link #persistentSourceFileList}
	 * @param srcPath
	 *            the {@link #srcPath}
	 */
	@JsonCreator
	public MultiSourceFilePicker(@JsonProperty("pickFilesRandomly") boolean pickFileRandomly,
			@JsonProperty("findSourcesInSrcPath") boolean findSourcesInSrcPath,
			@JsonProperty("persistentSourceFileList") ArrayList<File> persistentSourceFileList,
			@JsonProperty("srcPath") String srcPath) {
		this.pickFilesRandomly.set(pickFileRandomly);
		this.findingSourcesInSrcPath.set(findSourcesInSrcPath);
		this.persistentSourceFileList.get().setAll(persistentSourceFileList);
		this.srcPath.set(srcPath);
	}

	/**
	 * Compute the index of next file in Source directory.
	 */
	public void computeNextFileIndex() {
		if (pickFilesRandomly.get()) {
			currentFileIndex.set(random.nextInt(temporarySourceFileList.getSize()));
		} else {
			if (currentFileIndex.get() < temporarySourceFileList.getSize() - 1) {
				currentFileIndex.set(currentFileIndex.get() + 1);
			} else {
				currentFileIndex.set(0);
			}
		}
	}

}
