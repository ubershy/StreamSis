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
package com.ubershy.streamsis.elements.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.elements.AbstractCuteElement;
import com.ubershy.streamsis.networking.StreamingProgramManager;
import com.ubershy.streamsis.networking.responses.GetSceneNameResponse;
import com.ubershy.streamsis.networking.responses.Response;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * SwitchSPSceneAction switch scene of Streaming Program to the specified one.
 */
public class SwitchSPSceneAction extends AbstractCuteElement implements Action {

	static final Logger logger = LoggerFactory.getLogger(SwitchSPSceneAction.class);

	/** The name of Streaming Program scene to which to switch. */
	@JsonProperty
	protected StringProperty sceneName = new SimpleStringProperty("");
	public StringProperty sceneNameProperty() {return sceneName;}
	public String getSceneName() {return sceneName.get();}
	public void setSceneName(String sceneName) {this.sceneName.set(sceneName);}
	
	public SwitchSPSceneAction() {
	}

	/**
	 * Instantiates a new SwitchSPSceneAction.
	 *
	 * @param sceneName
	 *            the name of SisScene to which to switch
	 */
	@JsonCreator
	public SwitchSPSceneAction(@JsonProperty("sisSceneName") String sceneName) {
		this.sceneName.set(sceneName);
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			// Lets not change scene without need.
			GetSceneNameResponse getSceneNameResponse = StreamingProgramManager.getSceneName();
			if (getSceneNameResponse.getErrorText() != null) { // Got an error response.
				reactOnError(getSceneNameResponse.getErrorText(), "Set current scene by name");
				return;
			}
			if (this.sceneName.get().equals(getSceneNameResponse.getSceneName())) {
				logger.info("Scene is already active in Streaming Program. Not switching.");
				elementInfo.setBooleanResult(false);
				return;
			}
			// Lets change the scene in Streaming Program.
			Response setSceneResponse = StreamingProgramManager.setSceneName(sceneName.get());
			if (setSceneResponse.getErrorText() != null) { // Got an error response.
				reactOnError(setSceneResponse.getErrorText(), "Set current scene by name");
				return;
			}
			elementInfo.setBooleanResult(true);
		}
	}

	@Override
	public void init() {
		super.init();
		if (sceneName.get().isEmpty()) {
			elementInfo.setAsBroken("Scene name is empty");
		}
	}
	
	private void reactOnError(String errorText, String readableRequestName){
		elementInfo.setBooleanResult(false);
		elementInfo.setAsSick("Got error from Streaming Program during sending a request '"
				+ readableRequestName + "': \"" + errorText + "\"");
	}

}