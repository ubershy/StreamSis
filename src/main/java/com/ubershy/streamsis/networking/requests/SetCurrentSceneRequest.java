package com.ubershy.streamsis.networking.requests;

public class SetCurrentSceneRequest extends Request {
	
	public SetCurrentSceneRequest(String scene) {
		super("SetCurrentScene");
		this.data.put("Scene", scene);
	}
}