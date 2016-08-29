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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.project.AbstractCuteNode;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.SisScene;
import com.ubershy.streamsis.project.ProjectManager;

/**
 * Switch Cute Scene Action. <br>
 * This {@link Action} can switch current {@link CuteProject CuteProject's} {@link SisScene} to another one.
 */
@SuppressWarnings("unchecked")
public class SwitchSisSceneAction extends AbstractCuteNode implements Action {

	static final Logger logger = LoggerFactory.getLogger(SwitchSisSceneAction.class);

	/** The name of SisScene to which to switch. */
	@JsonProperty
	protected String sisSceneName = "";

	public SwitchSisSceneAction() {
	}

	/**
	 * Instantiates a new SwitchSisSceneAction.
	 *
	 * @param sisSceneName
	 *            the name of SisScene to which to switch
	 */
	@JsonCreator
	public SwitchSisSceneAction(@JsonProperty("sisSceneName") String sisSceneName) {
		this.sisSceneName = sisSceneName;
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			// Lets not change SisScene without need
			if (ProjectManager.getProject().getCurrentSisSceneName().equals(sisSceneName)) {
				elementInfo.setFailedResult();
			} else {
				ProjectManager.getProject().switchSisSceneTo(sisSceneName);
				elementInfo.setSuccessfulResult();
			}
		}
	}

	public String getSisSceneName() {
		return sisSceneName;
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (sisSceneName.isEmpty()) {
			elementInfo.setAsBroken("SisScene name is empty");
		} else {
			SisScene sisScene = ProjectManager.getProject().getSisSceneByName(sisSceneName);
			if (sisScene == null) {
				elementInfo.setAsBroken("SisScene with name '" + sisSceneName + "' not found");
			}
		}
	}

	public void setSisSceneName(String sisSceneName) {
		this.sisSceneName = sisSceneName;
	}

}
