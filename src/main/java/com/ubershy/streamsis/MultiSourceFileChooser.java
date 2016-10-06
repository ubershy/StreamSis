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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.project.CuteElement;
import com.ubershy.streamsis.project.ElementInfo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * MultiSourceFileChooser can help to choose next time file for action either randomly or
 * sequentially from a predefined list of files or from a directory containing files.
 */
public class MultiSourceFileChooser {

	/** Defines if to choose each next file randomly or sequentially. */
	@JsonIgnore
	protected BooleanProperty chooseFilesRandomly = new SimpleBooleanProperty(true);

	/**
	 * The index of current file in {@link #temporarySourceFileList}.
	 * 
	 * @note Should not be serialized.
	 */
	@JsonIgnore
	protected IntegerProperty currentFileIndex = new SimpleIntegerProperty(0);

	/**
	 * Defines if this {@link Action} should: <br>
	 * Use {@link #srcFilePath} directory to find source files automatically. <br>
	 * <b>OR</b> <br>
	 * Use {@link #persistentSourceFileList} as list of source files.
	 */
	@JsonIgnore
	protected BooleanProperty findingSourcesInSrcPath = new SimpleBooleanProperty(true);

	/**
	 * The persistent list of source files to use when {@link #findingSourcesInSrcPath} is false.
	 */
	@JsonProperty
	protected ObjectProperty<ObservableList<File>> persistentSourceFileList = new SimpleObjectProperty<>(
			FXCollections.observableArrayList());

	/** The acceptable extensions of source files. Each in "*.extension" format. */
	@JsonIgnore
	protected ListProperty<String> acceptableExtensions = new SimpleListProperty<String>(
			FXCollections.observableArrayList());

	/** The random index generator. */
	protected NonRepeatingRandom random = new NonRepeatingRandom();

	/** The path where to search files when {@link #findingSourcesInSrcPath} is true. */
	@JsonIgnore
	protected StringProperty srcPath = new SimpleStringProperty("");

	/**
	 * The actual list of files from where to get the next file. <br>
	 * Can be generated either from {@link #srcFilePath} or {@link #persistentSourceFileList}
	 * depending on {@link #findingSourcesInSrcPath} boolean value.
	 * 
	 * @note Should not be serialized.
	 */
	@JsonIgnore
	protected ReadOnlyListWrapper<File> temporarySourceFileList = new ReadOnlyListWrapper<File>(
			this, "temporarySourceFileList", FXCollections.observableArrayList());

	public MultiSourceFileChooser() {

	}

	/**
	 * Instantiates a new MultiSourceFileChooser. Used by deserializator.
	 *
	 * @param chooseFileRandomly
	 *            {@link #choseFilesRandomly}
	 * @param findSourcesInSrcPath
	 *            {@link #findingSourcesInSrcPath}
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
		this.chooseFilesRandomly.set(chooseFileRandomly);
		this.findingSourcesInSrcPath.set(findSourcesInSrcPath);
		this.persistentSourceFileList.get().setAll(persistentSourceFileList);
		this.srcPath.set(srcPath);
	}

	/**
	 * Compute the index of next file in Source directory.
	 */
	public void computeNextFileIndex() {
		if (chooseFilesRandomly.get()) {
			currentFileIndex.set(random.nextInt(temporarySourceFileList.getSize()));
		} else {
			if (currentFileIndex.get() < temporarySourceFileList.getSize()) {
				currentFileIndex.set(currentFileIndex.get() + 1);
			} else {
				currentFileIndex.set(0);
			}
		}
	}

	/**
	 * Get new Array for {@link #temporarySourceFileList}. <br>
	 * Depending on the value {@link #findingSourcesInSrcPath}, it gets Array either from
	 * {@link #srcFilePath} or from {@link #persistentSourceFileList}.
	 *
	 * @param elementInfo
	 *            The instance of {@link ElementInfo} to make it broken in case something is wrong.
	 * @param sourceDirectorySymbolicName
	 *            How to call directory with source files when generating broken messages.
	 * @param destinationPath
	 *            This parameter might be empty. Specify it if you want to prevent choosing the same
	 *            file as the file on this destination.
	 * @return The Array with source files, <br>
	 *         Null if something is wrong.
	 */
	protected File[] getTemporarySourceFileList(ElementInfo elementInfo,
			String sourceDirectorySymbolicName, String destinationPath) {
		File[] result;
		if (findingSourcesInSrcPath.get()) {
			if (srcPath.get().isEmpty()) {
				elementInfo.setAsBroken("The path with source files can't be an empty string unless"
						+ " choosing files manually");
				return null;
			}
			if (Util.checkIfAbsolutePathSeemsValid(destinationPath)) {
				File dstDirFile = new File(destinationPath).getParentFile();
				File srcDirFile = new File(srcPath.get());
				try {
					if (dstDirFile.getCanonicalPath().equals(srcDirFile.getCanonicalPath())) {
						elementInfo.setAsBroken(
								"You can't choose the same source path as destination file's path");
						return null;
					}
				} catch (IOException e) {
					elementInfo
							.setAsBroken("Unable to read directories to check if they are valid.");
					return null;
				}
			}
			// Find source files in the directory.
			result = Util.findFilesInDirectory(srcPath.get(),
					acceptableExtensions.toArray(new String[0]));
			if (result != null) {
				if (result.length == 0) {
					elementInfo.setAsBroken("Can't find files with extensions: '"
							+ acceptableExtensions + "') in the " + sourceDirectorySymbolicName
							+ " directory " + srcPath.get());
					return null;
				}
			} else {
				elementInfo.setAsBroken("Something wrong with the " + sourceDirectorySymbolicName
						+ " directory " + srcPath.get() + "\nPlease go and check it");
				return null;
			}
		} else {
			// Use source files from the list.
			result = persistentSourceFileList.get().toArray(new File[0]);
			if (result != null) {
				if (result.length == 0) {
					elementInfo.setAsBroken("No files are chosen");
					return null;
				}
			}
			for (File file : result) {
				if (file.getAbsolutePath().equals(destinationPath)) {
					elementInfo.setAsBroken("File: '" + file.getName() + "' is located on the same "
							+ "path as destination file.");
				}
				if (!Util.checkFileExtension(file.getAbsolutePath(),
						acceptableExtensions.toArray(new String[0]))) {
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
	 * Initializes {@link MultiSourceFileChooser}, if something is wrong, sets the
	 * {@link CuteElement} associated with {@link ElementInfo} as broken.
	 * <p>
	 * NOTE: Before running this method, you must set allowed extensions via
	 * {@link #setAcceptableExtensions(List)}.
	 *
	 * @param elementInfo
	 *            The instance of {@link ElementInfo} to make it broken in case something is wrong.
	 * @param sourceDirectorySymbolicName
	 *            How to call directory with source files when generating broken messages.
	 * @param destinationPath
	 *            This parameter might be empty. Specify it if you want to prevent choosing the same
	 *            file as the file on this destination.
	 * 
	 * @throws IllegalArgumentException
	 *             If {@link #acceptableExtensions} contains no extensions. It should be set by
	 *             {@link #setAcceptableExtensions(List)} method.
	 */
	public void initTemporaryFileList(ElementInfo elementInfo, String sourceDirectorySymbolicName,
			String destinationPath) {
		if (elementInfo == null) {
			throw new NullPointerException();
		}
		if (acceptableExtensions.size() != 0) {
			// getTemporarySourceFileList can set the ElementInfo as broken. 
			File[] tempArray = getTemporarySourceFileList(elementInfo, sourceDirectorySymbolicName,
					destinationPath);
			if (tempArray == null)
				tempArray = new File[0];
			temporarySourceFileList.setAll(tempArray);
		} else {
			throw new IllegalArgumentException(
					"The list of allowed extensions should have at least one extension before"
					+ " running this method. Use setAcceptableExtensions() method.");
		}
	}

	/**
	 * @return The value of {@link #srcPath}.
	 */
	@JsonProperty("srcPath")
	public String getSrcPath() {
		return srcPath.get();
	}

	/**
	 * @param srcPath
	 *            The {@link #srcPath}'s value to set.
	 */
	@JsonProperty("srcPath")
	public void setSrcPath(String srcPath) {
		this.srcPath.set(srcPath);
	}

	/**
	 * @return The {@link #srcPath} property.
	 */
	public StringProperty srcPathProperty() {
		return srcPath;
	}
	
	/**
	 * @return The value of {@link #chooseFilesRandomly}.
	 */
	@JsonProperty("chooseFilesRandomly")
	public boolean isChoosingFilesRandomly() {
		return chooseFilesRandomly.get();
	}

	/**
	 * @param chooseRandomly
	 *            The {@link #chooseFilesRandomly}'s value to set.
	 */
	@JsonProperty("chooseFilesRandomly")
	public void setChooseFilesRandomly(boolean chooseRandomly) {
		this.chooseFilesRandomly.set(chooseRandomly);
	}
	
	/**
	 * @return The {@link #chooseFilesRandomly} property.
	 */
	public BooleanProperty chooseFilesRandomlyProperty() {
		return chooseFilesRandomly;
	}
	
	/**
	 * @return The value of {@link #findingSourcesInSrcPath}.
	 */
	@JsonProperty("findingSourcesInSrcPath")
	public boolean isFindingSourcesInSrcPath() {
		return findingSourcesInSrcPath.get();
	}

	/**
	 * @param findingSourcesInSrcPath
	 *            The {@link #findingSourcesInSrcPath}'s value to set.
	 */
	@JsonProperty("findingSourcesInSrcPath")
	public void setFindingSourcesInSrcPath(boolean findingSourcesInSrcPath) {
		this.findingSourcesInSrcPath.set(findingSourcesInSrcPath);
	}
	
	/**
	 * @return The {@link #findingSourcesInSrcPath} property.
	 */
	public BooleanProperty findingSourcesInSrcPathProperty() {
		return findingSourcesInSrcPath;
	}

	/**
	 * @return The {@link #persistentSourceFileList} property..
	 */
	public ObjectProperty<ObservableList<File>> persistentSourceFileListProperty() {
		return persistentSourceFileList;
	}
	
	/**
	 * @return The {@link #persistentSourceFileList}'s current list.
	 */
	public ObservableList<File> getPersistentSourceFileList() {
		return persistentSourceFileList.get();
	}

	/**
	 * @param persistentSourceFileList The {@link #persistentSourceFileList}'s value to set.
	 */
	public void setPersistentSourceFileList(ObservableList<File> persistentSourceFileList) {
		this.persistentSourceFileList.set(persistentSourceFileList);;
	}
	
	/**
	 * @return The value of {@link #currentFileIndex}.
	 */
	public int getCurrentFileIndex() {
		return currentFileIndex.get();
	}

	/**
	 * @param currentFileIndex
	 *            The {@link #currentFileIndex}'s value to set.
	 */
	public void setCurrentFileIndex(int currentFileIndex) {
		this.currentFileIndex.set(currentFileIndex);
	}
	
	/**
	 * @return The {@link #currentFileIndex} property.
	 */
	public IntegerProperty currentFileIndexProperty() {
		return currentFileIndex;
	}
	
	/**
	 * @return The {@link #acceptableExtensions} property.
	 */
	@JsonIgnore
	public ListProperty<String> acceptableExtensionsProperty() {
		return acceptableExtensions;
	}
	
	/**
	 * @param acceptableExtensions
	 *            The {@link #acceptableExtensions}'s value to set.
	 */
	@JsonIgnore
	public void setAcceptableExtensions(List<String> acceptableExtensions) {
		this.acceptableExtensions.get().setAll(acceptableExtensions);
	}
	
	/**
	 * @return The {@link #acceptableExtensions} value.
	 */
	@JsonIgnore
	public ObservableList<String> getAcceptableExtensions() {
		return this.acceptableExtensions.get();
	}
	
	/**
	 * @return The {@link #temporarySourceFileList} read-only property.
	 */
	public ReadOnlyListProperty<File> getTemporarySourceFileList() {
		return temporarySourceFileList.getReadOnlyProperty();
	}
	
}
