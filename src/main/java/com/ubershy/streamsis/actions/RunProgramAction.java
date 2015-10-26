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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.project.AbstractCuteNode;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Run Program Action. <br>
 * This {@link Action} can execute a program or script by using path, arguments and working
 * directory provided by the user.
 */
@SuppressWarnings("unchecked")
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "actnType")
public class RunProgramAction extends AbstractCuteNode implements Action {

	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(RunProgramAction.class);

	/**
	 * The arguments which will be passed to the program.
	 */
	@JsonIgnore
	protected StringProperty argumentsProperty = new SimpleStringProperty("");

	// /** The environment path. */
	// private String environmentPATH = System.getenv("PATH");

	/**
	 * Determines if the program must be killed by StreamSis if it's still running from previous
	 * {@link #execute()}, so it can be started again.
	 */
	@JsonIgnore
	protected BooleanProperty killIfStillRunningProperty = new SimpleBooleanProperty(false);

	/**
	 * The path of the program/script to execute.
	 */
	@JsonIgnore
	protected StringProperty pathProperty = new SimpleStringProperty("");

	/** The process. */
	private Process process;

	/**
	 * The working directory path of program/script execution.
	 */
	@JsonIgnore
	protected StringProperty workingDirProperty = new SimpleStringProperty("");

	/**
	 * Instantiates a new {@link RunProgramAction}.
	 */
	public RunProgramAction() {
	}

	/**
	 * Instantiates a new {@link RunProgramAction} by {@link #pathProperty path} and
	 * {@link #argumentsProperty arguments}.
	 *
	 * @param path
	 *            the path of the program/script to execute
	 * @param arguments
	 *            the arguments to be passed to the program
	 * @param workingDir
	 *            the working directory path of program execution
	 * @param shouldKill
	 *            should the program be killed on #execute() if it's still running from previous
	 *            {@link #execute()}
	 */
	@JsonCreator
	public RunProgramAction(@JsonProperty("path") String path,
			@JsonProperty("arguments") String arguments,
			@JsonProperty("workingDir") String workingDir,
			@JsonProperty("killIfStillRunning") boolean shouldKill) {
		this.pathProperty.set(path);
		this.argumentsProperty.set(arguments);
		this.workingDirProperty.set(workingDir);
		this.killIfStillRunningProperty.set(shouldKill);
	}

	/**
	 * Returns {@link #argumentsProperty}.
	 *
	 * @return the string property
	 */
	public StringProperty argumentsProperty() {
		return argumentsProperty;
	}

	public boolean checkIfPathIsAbsoluteAndFileExists(String path) {
		if ((new File(path).isAbsolute())) { // the path provided is absolute
			if (Util.checkifSingleFileExists(path)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			String toExecute = getPath() + " " + getArguments();
			boolean skipSettingStatus = false;
			boolean success = true;
			if (process != null) {
				logger.info("Checking if the program is still running: " + toExecute);
				if (process.isAlive()) {
					if (getkillIfStillRunning()) {
						logger.info("Terminating the program, because it's still running");
						process.destroy();
						logger.info("The program was violently terminated");
					} else {
						elementInfo
								.setAsSick("Can't run the program again while it's still running. "
										+ "If you think it's bad and the program is not responding"
										+ ", use task manager to terminate it");
						success = false;
					}
				}
			}
			if (success != false) {
				try {
					logger.info("Running the program: " + toExecute);
					process = Runtime.getRuntime().exec(toExecute, null, new File(getWorkingDir()));
					int exitCode = process.waitFor();
					if (exitCode == 1) {
						BufferedReader br = new BufferedReader(
								new InputStreamReader(process.getErrorStream()));
						String stringToAdd;
						StringBuilder errorLines = new StringBuilder("");
						try {
							while ((stringToAdd = br.readLine()) != null) {
								errorLines.append(stringToAdd).append("\n");
							}
						} catch (IOException e) {
							logger.error("Problem reading error buffer", e);
						}
						// Better to break the RunProgramAction in this case.
						// So the project will stop.
						// Better be safe than sorry.
						elementInfo.setAsBroken(
								"Run of the program with specified path and parameters "
										+ "ended with an error: \n" + errorLines);
						success = false;
					}
				} catch (IOException e) {
					elementInfo.setAsBroken("Something is wrong "
							+ "Is it really a program(not an ordinary file) located on the "
							+ "chosen path?");
					success = false;
				} catch (InterruptedException e) {
					skipSettingStatus = true;
				}
			}
			if (!skipSettingStatus) {
				if (success) {
					elementInfo.setSuccessfulResult();
				} else {
					elementInfo.setFailedResult();
				}
			}
		}
	}

	/**
	 * Gets the {@link #argumentsProperty()} value.
	 *
	 * @return the arguments
	 */
	@JsonProperty("arguments")
	public String getArguments() {
		return argumentsProperty.get();
	}

	/**
	 * Gets the {@link #killIfStillRunningProperty} value.
	 */
	@JsonProperty("killIfStillRunning")
	public boolean getkillIfStillRunning() {
		return this.killIfStillRunningProperty.get();
	}

	/**
	 * Gets the {@link #pathProperty()} value.
	 *
	 * @return the path
	 */
	@JsonProperty("path")
	public String getPath() {
		return pathProperty.get();
	}

	/**
	 * Gets the {@link #workingDirProperty} value.
	 */
	@JsonProperty("workingDir")
	public String getWorkingDir() {
		return this.workingDirProperty.get();
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (getPath().isEmpty()) {
			elementInfo.setAsBroken("The path to the program is empty");
			return;
		}
		// TODO: improve algorithms below. I was in hurry. =/
		String absolutePath = null;
		if (checkIfPathIsAbsoluteAndFileExists(getPath())) { // the path provided is absolute
			absolutePath = getPath();
		} 
		// Too hard to implement ability to support non-absolute paths of executables.
		// TODO: implement someday. Now I'm in hurry. =/
		// else { // the path provided is not absolute
		// if ("cmd.exe".equals(getPath())) {
		// absolutePath = "C:\\Windows\\system32\\" + getPath();
		// return;
		// }
		// // lets search in environment variable "PATH"
		// String[] envDirs = environmentPATH.split(";");
		// for (String envDir : envDirs) {
		// File possibleFile = new File(envDir, getPath());
		// String possibleAbsolutePath = possibleFile.getAbsolutePath();
		// if (Util.checkifSingleFileExists(possibleAbsolutePath)) {
		// absolutePath = possibleAbsolutePath;
		// return;
		// }
		// }
		// }
		if (absolutePath == null) {
			elementInfo.setAsBroken("The path to the program seems invalid. "
					+ "Please check if the program really exists on this path");
			return;
		}
		File pathFile = new File(absolutePath);
		try {
			if (!pathFile.canExecute()) {
				elementInfo.setAsBroken("The path to the program seems invalid. "
						+ "Please check if the program really exists on this path");
				return;
			}
		} catch (SecurityException e) {
			elementInfo.setAsBroken("You don't have enough rights to run this program");
			return;
		}

		if (getWorkingDir().isEmpty()) { // if it's empty, lets initialize workingDir by the
											// program directory
			File parentToPathFile = pathFile.getAbsoluteFile().getParentFile();
			if (parentToPathFile != null) {
				setWorkingDir(parentToPathFile.getAbsolutePath());
			} else {
				throw new RuntimeException("Can't find parent directory of the program");
			}
		} else { // Means the user has specified the working directory
			if (!Util.checkDirectory(getWorkingDir())) {
				elementInfo.setAsBroken("The working directory you have specified does not exist");
			}
		}
	}

	/**
	 * Returns {@link #killIfStillRunningProperty}.
	 *
	 * @return the boolean property
	 */
	public BooleanProperty killIfStillRunningProperty() {
		return killIfStillRunningProperty;
	}

	/**
	 * Returns {@link #pathProperty}.
	 *
	 * @return the string property
	 */
	public StringProperty pathProperty() {
		return pathProperty;
	}

	/**
	 * Sets the {@link #argumentsProperty()} value.
	 *
	 * @param arguments
	 *            the new arguments
	 */
	@JsonProperty("arguments")
	public void setArguments(String arguments) {
		this.argumentsProperty.set(arguments);
	}

	/**
	 * Sets the {@link #killIfStillRunningProperty} value.
	 *
	 * @param shouldKill
	 *            if the program must be killed if it's still running
	 */
	@JsonProperty("killIfStillRunning")
	public void setkillIfStillRunning(boolean shouldKill) {
		this.killIfStillRunningProperty.set(shouldKill);
	}

	/**
	 * Sets the {@link #pathProperty()} value.
	 *
	 * @param path
	 *            the new path
	 */
	@JsonProperty("path")
	public void setPath(String path) {
		this.pathProperty.set(path);
		setWorkingDir(""); // lets reset working dir
	}

	/**
	 * Sets the {@link #workingDirProperty()} value.
	 *
	 * @param workingDir
	 *            the working directory of execution
	 */
	@JsonProperty("workingDir")
	public void setWorkingDir(String workingDir) {
		this.workingDirProperty.set(workingDir);
	}

	/**
	 * Returns {@link #workingDirProperty}.
	 *
	 * @return the string property
	 */
	public StringProperty workingDirProperty() {
		return workingDirProperty;
	}
	
}
