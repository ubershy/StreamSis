/** 
 * StreamSis
 * Copyright (C) 2017 Eva Balycheva
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
package com.ubershy.streamsis.elements.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.SuperSoundManager;
import com.ubershy.streamsis.elements.AbstractCuteElement;

/**
 * Sounds Stop Action. <br>
 * This {@link Action} stops all currently playing sounds. <br>
 */
public class SoundsStopAction extends AbstractCuteElement implements Action {

	static final Logger logger = LoggerFactory.getLogger(SoundsStopAction.class);
	
	/** The description of this CuteElement type. */
	public final static String description = SoundsStopAction.class.getSimpleName()
			+ " on execution stops all currently playing sounds.";

	public SoundsStopAction() {
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			elementInfo.setBooleanResult(SuperSoundManager.stopAllSounds());
		}
	}

	@Override
	public void init() {
		super.init();
		// Do nothing.
	}

}
