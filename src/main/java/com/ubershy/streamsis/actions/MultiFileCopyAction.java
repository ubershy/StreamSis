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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.MultiSourceFilePicker;
import com.ubershy.streamsis.Util;

/**
 * Multi File Copy Action. <br>
 * This {@link Action} can copy Destination file with one of the files from Source file directory or
 * from predefined list of files. <br>
 * It can choose a particular file either <b>randomly</b> or <b>sequentially</b>.
 */
public class MultiFileCopyAction extends FileCopyAction {

	static final Logger logger = LoggerFactory.getLogger(MultiFileCopyAction.class);
	
	/** The file picker that helps to choose each next source file. */
	@JsonProperty
	private MultiSourceFilePicker filePicker = new MultiSourceFilePicker();

	public MultiFileCopyAction() {
		dstFilePath.addListener((o, oldVal, newVal) -> {
			filePicker.setAcceptableExtensions(generateExtensionsForPicker(newVal));
		});
	}

	/**
	 * Generates list with allowed extensions for picker. The allowed extensions list will consist
	 * only of one extension - destination file's extension.
	 *
	 * @param dstPath
	 *            The destination file path.
	 * @return The list with single allowed extension to pass to {@link #filePicker}.
	 */
	private List<String> generateExtensionsForPicker(String dstPath) {
		String singleExtension = null;
		if (dstPath != null && !dstPath.isEmpty()) {
			singleExtension = Util.extractFileExtensionFromPath(dstPath);
		}
		List<String> result = new ArrayList<String>();
		if (singleExtension != null) {
			result.add(singleExtension);
		}
		return result;
	}

	/**
	 * Instantiates a new Multi File Copy Action by path with files.
	 *
	 * @param srcDirectoryPath
	 *            the Source <b>directory</b>'s path
	 * @param dstFilePath
	 *            the Destination <b>file</b>'s path
	 * @param chooseFileRandomly
	 *            choose file randomly
	 */
	public MultiFileCopyAction(String srcDirectoryPath, String dstFilePath,
			boolean chooseFileRandomly) {
		this();
		filePicker.setAcceptableExtensions(generateExtensionsForPicker(dstFilePath));
		this.dstFilePath.set(dstFilePath);
		filePicker.setFindingSourcesInSrcPath(true);
		filePicker.setSrcPath(srcDirectoryPath);
		filePicker.setPickingFilesRandomly(chooseFileRandomly);
	}

	/**
	 * Instantiates a new Multi File Copy Action by list of files.
	 *
	 * @param persistentSourceFileList the persistent source file list
	 * @param dstFilePath            the Destination <b>file</b>'s path
	 * @param chooseFileRandomly            choose file randomly
	 */
	public MultiFileCopyAction(ArrayList<File> persistentSourceFileList, String dstFilePath,
			boolean chooseFileRandomly) {
		this();
		filePicker.setAcceptableExtensions(generateExtensionsForPicker(dstFilePath));
		this.dstFilePath.set(dstFilePath);
		filePicker.setFindingSourcesInSrcPath(false);
		filePicker.getPersistentSourceFileList().setAll(persistentSourceFileList);
		filePicker.setPickingFilesRandomly(chooseFileRandomly);
	}

	/**
	 * Instantiates a new Multi File Copy Action FULLY. Used by deserializator.
	 *
	 * @param dstFilePath
	 *            the Destination <b>file</b>'s path
	 * @param filePicker
	 *            the {@link MultiSourceFilePicker}
	 */
	@JsonCreator
	public MultiFileCopyAction(@JsonProperty("dstFilePath") String dstFilePath,
			@JsonProperty("filePicker") MultiSourceFilePicker filePicker) {
		this();
		filePicker.setAcceptableExtensions(generateExtensionsForPicker(dstFilePath));
		this.filePicker = filePicker;
		this.dstFilePath.set(dstFilePath);
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			File fileToCopy = filePicker.getTemporarySourceFileList()
					.get(filePicker.getCurrentFileIndex());
			logger.info(
					"Copying Source directory file # " + (filePicker.getCurrentFileIndex() + 1));
			File fileToReplace = new File(dstFilePath.get());
			filePicker.computeNextFileIndex();
			try {
				Util.copyFileSynced(fileToCopy, fileToReplace);
			} catch (IOException e) {
				elementInfo.setBooleanResult(false);
				return;
			}
			elementInfo.setBooleanResult(true);
		}
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (dstFilePath.get().isEmpty()) {
			elementInfo.setAsBroken("Destination path is not defined");
			return;
		}
		if (filePicker.getAcceptableExtensions().size() != 0) {
			filePicker.initTemporaryFileList(elementInfo, "Source files", dstFilePath.get());
		} else {
			elementInfo.setAsBroken("Destination file has no extension: " + dstFilePath.get()
					+ "\nPlease choose another file");
			return;
		}
		if (elementInfo.isBroken()) {
			// already broken by filePicker.initTemporaryFileList() or null extension
			return;
		}
		if (filePicker.isPickingFilesRandomly()) {
			filePicker.computeNextFileIndex();
		}
	}
	
	/**
	 * Gets the {@link #filePicker}.
	 *
	 * @return The {@link #filePicker}.
	 */
	public MultiSourceFilePicker getFilePicker() {
		return filePicker;
	}
	
	/**
	 * Sets the {@link #filePicker}. In most cases it's not needed to set filePicker. This setter
	 * exists only for automatic copying of attributes.
	 *
	 * @param filePicker
	 *            The new {@link #filePicker}.
	 */
	public void setFilePicker(MultiSourceFilePicker filePicker) {
		filePicker.setAcceptableExtensions(generateExtensionsForPicker(dstFilePath.get()));
		this.filePicker = filePicker;
	}

}
