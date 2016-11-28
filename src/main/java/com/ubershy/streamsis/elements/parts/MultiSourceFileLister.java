/** 
 * StreamSis
 * Copyright (C) 2016 Eva Balycheva
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.ElementInfo;
import com.ubershy.streamsis.elements.actions.Action;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


/**
 * MultiSourceFileLister generates usable list of files {@link #getTemporarySourceFileList()} from:
 * <br>
 * 1. Predefined list of files: {@link #persistentSourceFileList}. <br>
 * 2. Or list of files from the chosen directory: {@link #srcPath}. <br>
 * And ability to quickly switch between them via {@link #setFindingSourcesInSrcPath(boolean)}.
 * 
 * @note before using this class, {@link #initTemporaryFileList(ElementInfo, String, String)} must
 * be invoked first with desired parameters.
 */
public class MultiSourceFileLister {
	
	// Properties
	
	/**
	 * Defines if this {@link Action} should: <br>
	 * Use {@link #srcFilePath} directory to find source files automatically. <br>
	 * <b>OR</b> <br>
	 * Use {@link #persistentSourceFileList} as list of source files.
	 */
	@JsonProperty("findingSourcesInSrcPath")
	protected BooleanProperty findingSourcesInSrcPath = new SimpleBooleanProperty(true);
	public BooleanProperty findingSourcesInSrcPathProperty() {return findingSourcesInSrcPath;}
	public boolean isFindingSourcesInSrcPath() {return findingSourcesInSrcPath.get();}
	public void setFindingSourcesInSrcPath(boolean findingSourcesInSrcPath) {
		this.findingSourcesInSrcPath.set(findingSourcesInSrcPath);
	}

	/**
	 * The persistent list of source files to use when {@link #findingSourcesInSrcPath} is false.
	 */
	@JsonProperty("persistentSourceFileList")
	protected ObjectProperty<ObservableList<File>> persistentSourceFileList = new SimpleObjectProperty<>(
			FXCollections.observableArrayList());
	public ObjectProperty<ObservableList<File>> persistentSourceFileListProperty() {
		return persistentSourceFileList;
	}
	public ObservableList<File> getPersistentSourceFileList() {
		return persistentSourceFileList.get();
	}
	public void setPersistentSourceFileList(ObservableList<File> persistentSourceFileList) {
		this.persistentSourceFileList.set(persistentSourceFileList);;
	}

	/** The path where to search files when {@link #findingSourcesInSrcPath} is true. */
	@JsonProperty("srcPath")
	protected StringProperty srcPath = new SimpleStringProperty("");
	public StringProperty srcPathProperty() {return srcPath;}
	public String getSrcPath() {return srcPath.get();}
	public void setSrcPath(String srcPath) {this.srcPath.set(srcPath);}

	// Runtime variables
	
	/** The acceptable extensions of source files. Each in "*.extension" format. */
	@JsonIgnore
	protected ListProperty<String> acceptableExtensions = new SimpleListProperty<String>(
			FXCollections.observableArrayList());
	public ListProperty<String> acceptableExtensionsProperty() {return acceptableExtensions;}
	public ObservableList<String> getAcceptableExtensions() {
		return this.acceptableExtensions.get();
	}
	public void setAcceptableExtensions(List<String> acceptableExtensions) {
		this.acceptableExtensions.get().setAll(acceptableExtensions);
	}
	
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
	/**
	 * @return The {@link #temporarySourceFileList} read-only property.
	 */
	@JsonIgnore
	public ReadOnlyListProperty<File> getTemporarySourceFileList() {
		return temporarySourceFileList.getReadOnlyProperty();
	}

	public MultiSourceFileLister() {

	}

	/**
	 * Instantiates a new MultiSourceFileList.
	 *
	 * @param findSourcesInSrcPath
	 *            {@link #findingSourcesInSrcPath}
	 * @param persistentSourceFileList
	 *            the {@link #persistentSourceFileList}
	 * @param srcPath
	 *            the {@link #srcPath}
	 */
	@JsonCreator
	public MultiSourceFileLister(@JsonProperty("findSourcesInSrcPath") boolean findSourcesInSrcPath,
			@JsonProperty("persistentSourceFileList") ArrayList<File> persistentSourceFileList,
			@JsonProperty("srcPath") String srcPath) {
		this.findingSourcesInSrcPath.set(findSourcesInSrcPath);
		this.persistentSourceFileList.get().setAll(persistentSourceFileList);
		this.srcPath.set(srcPath);
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
	 *            This parameter might be empty. Specify it if you want to prevent picking the same
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
	 * Initializes {@link MultiSourceFilePicker}, if something is wrong, sets the
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
	 *            This parameter might be empty. Specify it if you want to prevent picking the same
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
}
