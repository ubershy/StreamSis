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
package com.ubershy.streamsis.actions;

import java.io.File;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.NonRepeatingRandom;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.project.ElementInfo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MultiSourceFileChooser {

	/** Defines if to choose next file randomly or sequentially. */
	@JsonProperty
	protected boolean chooseFileRandomly = true;

	/** The index of current file in Source directory. */
	protected int currentFileIndex = 0;

	/**
	 * Defines if this {@link Action} should: <br>
	 * Use {@link #srcFilePath} directory to find source files automatically. <br>
	 * <b>OR</b> <br>
	 * Use {@link #persistentSourceFileList} as list of source files.
	 */
	@JsonProperty
	protected boolean findSourcesInSrcPath = true;

	/** The persistent list of files to use when {@link #findSourcesInSrcPath} is false. */
	@JsonProperty
	protected ObservableList<File> persistentSourceFileList = FXCollections.observableArrayList();

	/** The possible extensions for source files. */
	protected ObservableList<String> possibleExtensions = FXCollections.observableArrayList();

	/** The random index generator. */
	protected NonRepeatingRandom random = new NonRepeatingRandom();

	/** The path where to search files when {@link #findSourcesInSrcPath} is true. */
	@JsonProperty
	protected String srcPath = "";

	/**
	 * The actual list of files from where to get the next file. <br>
	 * Can be generated either from {@link #srcFilePath} or {@link #persistentSourceFileList}.
	 */
	protected File[] temporarySourceFileList;

	public MultiSourceFileChooser() {

	}

	/**
	 * Instantiates a new MultiSourceFileChooser. Used by deserializator.
	 *
	 * @param chooseFileRandomly
	 *            {@link #chooseFileRandomly}
	 * @param findSourcesInSrcPath
	 *            {@link #findSourcesInSrcPath}
	 * @param persistentSourceFileList
	 *            the {@link #persistentSourceFileList}
	 * @param srcPath
	 *            the {@link #srcPath}
	 */
	@JsonCreator
	public MultiSourceFileChooser(@JsonProperty("chooseFileRandomly") boolean chooseFileRandomly,
			@JsonProperty("findSourcesInSrcPath") boolean findSourcesInSrcPath,
			@JsonProperty("persistentSourceFileList") ArrayList<File> persistentSourceFileList,
			@JsonProperty("srcPath") String srcPath) {
		this.chooseFileRandomly = chooseFileRandomly;
		this.findSourcesInSrcPath = findSourcesInSrcPath;
		this.persistentSourceFileList.setAll(persistentSourceFileList);
		this.srcPath = srcPath;
	}

	/**
	 * Compute the index of next file in Source directory.
	 */
	protected void computeNextFileIndex() {
		if (chooseFileRandomly) {
			currentFileIndex = random.nextInt(temporarySourceFileList.length);
		} else {
			if (currentFileIndex < temporarySourceFileList.length) {
				currentFileIndex++;
			} else {
				currentFileIndex = 0;
			}
		}
	}

	/**
	 * Get new Array for {@link #temporarySourceFileList}. <br>
	 * Depending on the value {@link #findSourcesInSrcPath}, it gets Array either from
	 * {@link #srcFilePath} or from {@link #persistentSourceFileList}.
	 *
	 * @param elementInfo
	 *            the instance of {@link ElementInfo}
	 * @param sourceDirectorySymbolicName
	 *            how to call directory with source files
	 * @return the Array with source files, <br>
	 *         null if something is wrong
	 */
	protected File[] getTemporarySourceFileList(ElementInfo elementInfo,
			String sourceDirectorySymbolicName) {
		File[] result;
		if (findSourcesInSrcPath) {
			result = Util.findFilesInDirectory(srcPath, possibleExtensions.toArray(new String[0]));
			if (result != null) {
				if (result.length == 0) {
					elementInfo.setAsBroken("Can't find files with extensions: '"
							+ possibleExtensions + "') in the " + sourceDirectorySymbolicName
							+ " directory " + srcPath);
					return null;
				}
			} else {
				elementInfo.setAsBroken("Something wrong with the " + sourceDirectorySymbolicName
						+ " directory " + srcPath + "\nPlease go and check it");
				return null;
			}
		} else {
			result = persistentSourceFileList.toArray(new File[0]);
			if (result != null) {
				if (result.length == 0) {
					elementInfo.setAsBroken("No files are chosen");
					return null;
				}
			}
			for (File file : result) {
				if (!Util.checkFileExtension(file.getAbsolutePath(),
						possibleExtensions.toArray(new String[0]))) {
					elementInfo.setAsBroken("File: '" + file.getName() + "' has wrong extension");
					return null;
				}
				if (!Util.checkifSingleFileExists(file.getAbsolutePath())) {
					elementInfo.setAsBroken("File: '" + file.getName() + "' does not exists");
					return null;
				}
			}
		}
		return result;
	}

	/**
	 * Initializes {@link MultiSourceFileChooser}.
	 *
	 * @param elementInfo
	 *            the instance of {@link ElementInfo}
	 * @param possibleExtensions
	 *            the possible extensions
	 * @param sourceDirectorySymbolicName
	 *            the source directory symbolic name
	 */
	protected void initTemporaryFileList(ElementInfo elementInfo, String[] possibleExtensions,
			String sourceDirectorySymbolicName) {
		if (elementInfo == null) {
			throw new NullPointerException();
		}
		this.possibleExtensions.setAll(possibleExtensions);
		if (possibleExtensions.length != 0) {
			temporarySourceFileList = getTemporarySourceFileList(elementInfo,
					sourceDirectorySymbolicName);
		} else {
			throw new IllegalArgumentException("Possible extensions are undefined");
		}
	}

}
