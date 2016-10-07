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
import java.net.URI;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.project.AbstractCuteNode;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaException;

/**
 * Sound Action. <br>
 * This {@link Action} plays sound. <br>
 * Supports ".wav", ".mp3" and ".ogg" files <br>
 * TODO: some ".wav" files can't be played as they have different format. Find a solution.
 */
@SuppressWarnings("unchecked")
public class SoundAction extends AbstractCuteNode implements Action {

	static final Logger logger = LoggerFactory.getLogger(SoundAction.class);

	/** The acceptable extensions of sound files. */
	@JsonIgnore
	public final static ObservableList<String> allowedExtensions = FXCollections
			.observableArrayList("*.wav", "*.mp3", "*.ogg");

	/** The path of sound file. */
	@JsonProperty
	protected StringProperty soundPath = new SimpleStringProperty("");

	/** The sound that will be played. */
	protected AudioClip soundToPlay;

	/** The volume of sound from 0 to 1. */
	@JsonProperty
	protected DoubleProperty volume = new SimpleDoubleProperty(1.0);

	public SoundAction() {
	}

	/**
	 * Instantiates a new SoundAction with volume 1.
	 *
	 * @param soundFilePath
	 *            the path of sound file
	 */
	public SoundAction(String soundFilePath) {
		this(soundFilePath, 1); // second argument is default volume
	}

	/**
	 * Instantiates a new SoundAction.
	 *
	 * @param soundFilePath
	 *            the path of sound file
	 * @param volume
	 *            the volume playback from 0 to 1
	 */
	@JsonCreator
	public SoundAction(@JsonProperty("soundPath") String soundFilePath,
			@JsonProperty("volume") double volume) {
		this.volume.set(volume);
		this.soundPath.set(soundFilePath);
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			elementInfo.setBooleanResult(play());
		}
	}

	public String getSoundPath() {
		return soundPath.get();
	}

	public double getVolume() {
		return volume.get();
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (volume.get() < 0 || volume.get() > 1)
			elementInfo.setAsBroken("Volume must be in range from 0.0 to 1.0");
		if (soundPath.get().isEmpty()) {
			elementInfo.setAsBroken("Sound path is not defined");
			return;
		}
		if (!Util.checkSingleFileExistanceAndExtension(soundPath.get(),
				allowedExtensions.toArray(new String[0]))) {
			elementInfo.setAsBroken("Can't find or read sound file " + soundPath.get());
			return;
		}
		soundToPlay = initializeSound(soundPath.get());
	}

	/**
	 * Initializes a sound at given path, produces an AudioClip.
	 *
	 * @param soundPath
	 *            the path of sound
	 * @return the AudioClip
	 */
	protected AudioClip initializeSound(String soundPath) {
		String URISoundPath = new File(soundPath).toURI().toString();
		AudioClip result = null;
		try {
			result = new AudioClip(URISoundPath);
		} catch (MediaException e) {
			elementInfo.setAsSick(
					"Compressed WAVE sound file detected.\nSuch files can't be played:\n"
							+ soundPath);
		} catch (Exception e) {
			elementInfo.setAsBroken("Can't initialize sound file " + soundPath);
		}
		return result;
	}

	/**
	 * Play sound.
	 */
	protected boolean play() {
		double globalVolume = CuteConfig.getDouble(CuteConfig.CUTE, "GlobalVolume");
		if (soundToPlay != null) {
			soundToPlay.setVolume(volume.get() * globalVolume);
			soundToPlay.play();
			logger.info(String.format("Playing(%.2f): %s", soundToPlay.getVolume(),
					Paths.get(URI.create(soundToPlay.getSource()))));
			return true;
		} else {
			logger.error("Can't play the sound. AudioClip is not defined.");
			return false;
		}
	}

	public void setSoundPath(String soundPath) {
		this.soundPath.set(soundPath);
	}

	public void setVolume(double volume) {
		this.volume.set(volume);;
	}
	
	public StringProperty soundPathProperty() {
		return soundPath;
	}
	
	public DoubleProperty volumeProperty() {
		return volume;
	}
	
}
