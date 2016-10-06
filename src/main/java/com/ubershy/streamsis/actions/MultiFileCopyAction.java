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
import com.ubershy.streamsis.MultiSourceFileChooser;
import com.ubershy.streamsis.Util;

/**
 * Multi File Copy Action. <br>
 * This {@link Action} can copy Destination file with one of the files from Source file directory or
 * from predefined list of files. <br>
 * It can choose a particular file either <b>randomly</b> or <b>sequentially</b>.
 */
public class MultiFileCopyAction extends FileCopyAction {

	static final Logger logger = LoggerFactory.getLogger(MultiFileCopyAction.class);
	
	/** The file chooser that helps to choose each next source file. */
	@JsonProperty
	private MultiSourceFileChooser fileChooser = new MultiSourceFileChooser();

	public MultiFileCopyAction() {
		dstFilePath.addListener((o, oldVal, newVal) -> {
			fileChooser.setAcceptableExtensions(generateExtensionsForChooser(newVal));
		});
	}

	/**
	 * Generates list with allowed extensions for chooser. The allowed extensions list will consist
	 * only of one extension - destination file's extension.
	 *
	 * @param dstPath
	 *            The destination file path.
	 * @return The list with single allowed extension to pass to {@link #fileChooser}.
	 */
	private List<String> generateExtensionsForChooser(String dstPath) {
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
		fileChooser.setAcceptableExtensions(generateExtensionsForChooser(dstFilePath));
		this.dstFilePath.set(dstFilePath);
		fileChooser.setFindingSourcesInSrcPath(true);
		fileChooser.setSrcPath(srcDirectoryPath);
		fileChooser.setChooseFilesRandomly(chooseFileRandomly);
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
		fileChooser.setAcceptableExtensions(generateExtensionsForChooser(dstFilePath));
		this.dstFilePath.set(dstFilePath);
		fileChooser.setFindingSourcesInSrcPath(false);
		fileChooser.getPersistentSourceFileList().setAll(persistentSourceFileList);
		fileChooser.setChooseFilesRandomly(chooseFileRandomly);
	}

	/**
	 * Instantiates a new Multi File Copy Action FULLY. Used by deserializator.
	 *
	 * @param dstFilePath
	 *            the Destination <b>file</b>'s path
	 * @param fileChooser
	 *            the {@link MultiSourceFileChooser}
	 */
	@JsonCreator
	public MultiFileCopyAction(@JsonProperty("dstFilePath") String dstFilePath,
			@JsonProperty("fileChooser") MultiSourceFileChooser fileChooser) {
		this();
		fileChooser.setAcceptableExtensions(generateExtensionsForChooser(dstFilePath));
		this.fileChooser = fileChooser;
		this.dstFilePath.set(dstFilePath);
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			File fileToCopy = fileChooser.getTemporarySourceFileList()
					.get(fileChooser.getCurrentFileIndex());
			logger.info(
					"Copying Source directory file # " + (fileChooser.getCurrentFileIndex() + 1));
			File fileToReplace = new File(dstFilePath.get());
			fileChooser.computeNextFileIndex();
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
		if (fileChooser.getAcceptableExtensions().size() != 0) {
			fileChooser.initTemporaryFileList(elementInfo, "Source files", dstFilePath.get());
		} else {
			elementInfo.setAsBroken("Destination file has no extension: " + dstFilePath.get()
					+ "\nPlease choose another file");
			return;
		}
		if (elementInfo.isBroken()) {
			// already broken by fileChooser.initTemporaryFileList() or null extension
			return;
		}
		if (fileChooser.isChoosingFilesRandomly()) {
			fileChooser.computeNextFileIndex();
		}
	}
	
	/**
	 * Gets the {@link #fileChooser}.
	 *
	 * @return The {@link #fileChooser}.
	 */
	public MultiSourceFileChooser getFileChooser() {
		return fileChooser;
	}
	
	/**
	 * Sets the {@link #fileChooser}. In most cases it's not needed to set fileChooser. This setter
	 * exists only for automatic copying of attributes.
	 *
	 * @param fileChooser
	 *            The new {@link #fileChooser}.
	 */
	public void setFileChooser(MultiSourceFileChooser fileChooser) {
		fileChooser.setAcceptableExtensions(generateExtensionsForChooser(dstFilePath.get()));
		this.fileChooser = fileChooser;
	}

}
