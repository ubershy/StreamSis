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
package com.ubershy.streamsis.actors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class was replaced by ActorCheckerService. It was intended to inform GUI about sleeping progress. And GUI was drawing cool
 * {@link javafx.scene.control.ProgressIndicator ProgressIndicators} instead of hearts. But they all were making unacceptable impact on CPU.
 */
@Deprecated
public class ProgressIndicatorActorCheckerService extends javafx.concurrent.Service<Void> {

	static final Logger logger = LoggerFactory.getLogger(ProgressIndicatorActorCheckerService.class);

	private Actor actor;

	public ProgressIndicatorActorCheckerService(Actor actor) {
		super();
		this.actor = actor;
	}

	@Override
	protected javafx.concurrent.Task<Void> createTask() {
		ProgressTask ct = new ProgressTask();
		return ct;
	}

	public class ProgressTask extends javafx.concurrent.Task<Void> {

		@Override
		public Void call() {
			updateProgress(0, 1);
			while (true) {
				long start = System.currentTimeMillis();
				int max = actor.getCheckInterval(); // We can update checkInterval during Actor working.
				actor.checkAndAct();
				int updateInterval = Math.max(max / 30, 10);
				try {
					while (true) {
						if (isCancelled()) {
							updateProgress(0, 1);
							return null;
						}
						long delta = System.currentTimeMillis() - start;
						if (delta > max) {
							updateProgress(1, 1);
							break;
						}
						updateProgress(delta, max);
						Thread.sleep(updateInterval);
					}
				} catch (InterruptedException e) {
					if (isCancelled()) {
						updateProgress(0, 1);
						break;
					} else {
						e.printStackTrace();
					}
				}
			}
			return null;
		}
	}
}