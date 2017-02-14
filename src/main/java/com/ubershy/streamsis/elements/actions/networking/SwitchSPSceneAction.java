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
package com.ubershy.streamsis.elements.actions.networking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.elements.AbstractCuteElement;
import com.ubershy.streamsis.elements.actions.Action;
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
	
	/** The description of this CuteElement type. */
	public final static String description = SwitchSPSceneAction.class.getSimpleName()
			+ " on execution sets the specified scene as current in the Streaming Program.\n"
			+ "Note: It requires Streaming Program to be configured to allow it to be controlled"
			+ " over the network.\n" + "Note: Streaming Program should be set in the StreamSis"
					+ " Settings.\n";

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
				reactOnErrorAndGetSick(getSceneNameResponse.getErrorText());
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
				reactOnErrorAndGetSick(setSceneResponse.getErrorText());
				return;
			}
			// Element might got sick on a previous iteration. Time to make it healthy again.
			if (elementInfo.isSick()) {
				elementInfo.setAsHealthy();
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
	
	private void reactOnErrorAndGetSick(String errorText){
		elementInfo.setBooleanResult(false);
		elementInfo.setAsSick("Got error during request ': \"" + errorText + "\"");
	}

}
