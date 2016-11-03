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
package com.ubershy.streamsis;

import java.util.LinkedList;

import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

/**
 * The Class SuperSoundManager. Able to control volume and playback of multiple added
 * {@link MediaPlayer} instances. <br>
 * Use {@link #addSound(MediaPlayer)} to add sound and {@link #removeSound(MediaPlayer)}
 * to remove sound.
 */
public final class SuperSoundManager {
	
	/** All currently controlled sounds. */
	private static final LinkedList<MediaPlayer> allControlledSounds = new LinkedList<>();
	
	/**
	 * Adds the sound to {@link SuperSoundManager}'s list of sounds controlled by
	 * {@link SuperSoundManager}.
	 *
	 * @param player
	 *            The {@link MediaPlayer} instance.
	 */
	public static void addSound(MediaPlayer player) {
		if (allControlledSounds.contains(player)) {
			throw new IllegalArgumentException("Such sound is already added to SuperSoundManager");
		}
		allControlledSounds.add(player);
	}
	
	/**
	 * Removes the sound from the list of sounds controlled by {@link SuperSoundManager}.
	 *
	 * @param player
	 *            The {@link MediaPlayer} instance.
	 */
	public static void removeSound(MediaPlayer player) {
		allControlledSounds.remove(player);
	}
	
	/**
	 * Sets the volume for all sounds controlled by {@link SuperSoundManager}.
	 *
	 * @param volume
	 *            The new volume to set immediately for all controlled sounds.
	 */
	public static void setVolumeForAllSounds(double volume) {
		MediaPlayer[] snapshotOfAllSounds = allControlledSounds.toArray(new MediaPlayer[0]);
		for (MediaPlayer sound : snapshotOfAllSounds) {
			if (sound.getStatus() != Status.DISPOSED)
				sound.setVolume(volume);
		}
	}

	/**
	 * Stops all controlled sounds controlled by {@link SuperSoundManager}.
	 */
	public static void stopAllSounds() {
		MediaPlayer[] snapshotOfAllSounds = allControlledSounds.toArray(new MediaPlayer[0]);
		for (MediaPlayer sound : snapshotOfAllSounds) {
			Status status = sound.getStatus();
			if (status != Status.DISPOSED && status != Status.STOPPED)
				sound.stop();
		}
	}

}
