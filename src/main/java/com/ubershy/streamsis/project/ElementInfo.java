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
package com.ubershy.streamsis.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * This class is a container for some common variables in {@link CuteElement}. <br>
 * It is used as field encapsulated in these objects. <br>
 * It contains CuteElement's name, the reason why CuteElement is unhealthy etc.
 * <p>
 * <b> Example: </b>
 * 
 * <pre>
 * {@code
 * Class MyClass {
 * 
 *     private ElementInfo elementInfo = new ElementInfo(this);
 * 
 *     public MyClass(String name) {
 *         this.elementInfo.setName(name);
 *     }
 * 
 *     public ElementInfo getInfo() {
 *         return this.elementInfo;
 *     }
 * 
 * }
 * </pre>
 */
public class ElementInfo {

	/**
	 * The Enum with information about {@link CuteElement}'s health.
	 */
	public enum ElementHealth {

		/**
		 * Indicates that the {@link CuteElement} is out of order, can't function at all. Broken.
		 * <p>
		 * For example, the CuteElement that plays sound by path might be broken, if no sound file
		 * can be found by the path.
		 */
		BROKEN("Element is broken"),

		/**
		 * Indicates that the {@link CuteElement} is healthy, fully functional, ready to work.
		 */
		HEALTHY("Element is OK"),

		/**
		 * Indicates that the {@link CuteElement} can work, but with some minor problems.
		 * <p>
		 * For example, the CuteElement can have a parameter set to not very optimal value. <br>
		 * Or something that is not good constantly affects the CuteElement while it's working.
		 */
		SICK("Element is not feeling well");

		/** The message that describes encapsulating {@link CuteElement}'s health. */
		private final String message;

		/**
		 * Instantiates a new ElementHealth.
		 *
		 * @param message
		 *            the status {@link #message}
		 */
		private ElementHealth(String message) {
			this.message = message;
		}

		@Override
		public String toString() {
			return message;
		}
	}

	/**
	 * The Enum with information about the {@link CuteElement}'s state.
	 */
	public enum ElementState {

		/**
		 * Indicates that the {@link CuteElement} need to be initialized by
		 * {@link CuteElement#init()} before it can work.
		 */
		NEEDINIT("Element needs initialization"),

		/**
		 * Indicates that the {@link CuteElement} is ready to work.
		 */
		READY("Element is ready"),

		/**
		 * Indicates that the {@link CuteElement} has finished it's operation.
		 */
		FINISHED("Element has finished working"),

		/**
		 * Indicates that the {@link CuteElement} is currently working.
		 */
		WORKING("Element is working");

		private String value;

		private ElementState(String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}
	};

	/**
	 * The Enum with information about the {@link CuteElement}'s last result of working.
	 */
	public enum Result {
		/**
		 * Indicates that after the {@link CuteElement} has finished working, it got
		 * unsatisfying/zero result. <br>
		 */
		FAIL("Last result: FAIL"),

		/**
		 * Indicates that after the {@link CuteElement} has finished working, it got
		 * satisfying/non-zero result.
		 */
		SUCCESS("Last result: SUCCESS"),
		
		/**
		 * Indicates that {@link CuteElement} don't have the result yet or it's still unknown.
		 */
		UNKNOWN("Last result: UNKNOWN");
		private String value;

		private Result(String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}
	};

	static final Logger logger = LoggerFactory.getLogger(ElementInfo.class);

	/**
	 * Tells if the {@link CuteElement} is turned on or off by user. <br>
	 * If it's not enabled, the CuteElement will not execute when asked.
	 */
	@JsonIgnore
	private BooleanProperty isEnabledProperty = new SimpleBooleanProperty(true);

	/** The name assigned to {@link CuteElement}. */
	@JsonIgnore
	private StringProperty nameProperty = new SimpleStringProperty("");

	/** The class simple name of {@link CuteElement}. */
	@JsonProperty
	private String objectSimpleName;

	/** The property describing {@link CuteElement}'s state. */
	@JsonIgnore
	private ObjectProperty<ElementState> elementStateProperty = new SimpleObjectProperty<>(
			ElementState.NEEDINIT);

	/** The property describing {@link CuteElement}'s health. */
	@JsonIgnore
	private ObjectProperty<ElementHealth> elementHealthProperty = new SimpleObjectProperty<>(
			ElementHealth.HEALTHY);

	/** The property describing {@link CuteElement}'s last result of working. */
	@JsonIgnore
	private ObjectProperty<Result> lastResultProperty = new SimpleObjectProperty<>(Result.UNKNOWN);

	/** The reason why the {@link CuteElement} is unhealthy and can't work properly. */
	@JsonIgnore
	private StringProperty whyUnhealthyProperty = new SimpleStringProperty("");

	/**
	 * Instantiates a new ElementInfo normally.
	 *
	 * @param cuteElement
	 *            the {@link CuteElement} which will encapsulate this ElementInfo as field
	 */
	public ElementInfo(CuteElement cuteElement) {
		this.objectSimpleName = cuteElement.getClass().getSimpleName();
	}

	/**
	 * Instantiates a new ElementInfo. Used by deserializator.
	 *
	 * @param objectSimpleName
	 *            the class name of {@link CuteElement} which encapsulates this ElementInfo.
	 * @param name
	 *            the name of the CuteElement
	 * @param isEnabled
	 *            is the CuteElement enabled?
	 */
	@JsonCreator
	private ElementInfo(@JsonProperty("objectSimpleName") String objectSimpleName,
			@JsonProperty("name") String name, @JsonProperty("isEnabled") boolean isEnabled) {
		this.objectSimpleName = objectSimpleName;
		this.nameProperty.set(name);
		this.isEnabledProperty.set(isEnabled);
	}

	/**
	 * Checks if the {@link CuteElement} is able to work.
	 * <p>
	 * It is not able to work if any of these expressions are true:
	 * <ul>
	 * <li>CuteElement's health is {@link ElementHealth#BROKEN}</li>
	 * <li>CuteElement's state is {@link ElementState#NEEDINIT}</li>
	 * <li>CuteElement's state is {@link ElementState#WORKING}</li>
	 * <li>CuteElement is not enabled (disabled by user)</li>
	 * </ul>
	 * 
	 * @return true, if the object can work
	 */
	public boolean canWork() {
		if (elementStateProperty.get().equals(ElementState.NEEDINIT)) {
			return false;
		}
		if (elementHealthProperty.get().equals(ElementHealth.BROKEN)) {
			return false;
		}
		if (!isEnabledProperty.get()) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the name of the {@link CuteElement}.
	 *
	 * @return the name
	 */
	@JsonProperty("name")
	public String getName() {
		return nameProperty.get();
	}

	/**
	 * Gets the message with reason why the {@link CuteElement} is unhealthy.
	 *
	 * @return the message why the CuteElement is unhealthy
	 */
	@JsonIgnore
	public String getUnhealthyMessage() {
		return whyUnhealthyProperty.get();
	}

	/**
	 * Checks if the {@link CuteElement} is broken. Broken CuteElement can't execute.
	 *
	 * @return true, if the CuteElement is broken
	 */
	@JsonIgnore
	public boolean isBroken() {
		return elementHealthProperty.get().equals(ElementHealth.BROKEN);
	}

	/**
	 * Checks if the {@link CuteElement} is enabled, i.e. not turned off by user. <br>
	 * Not enabled CuteElement will not execute when it's time comes.
	 *
	 * @return true, if the CuteElement is enabled
	 */
	@JsonProperty("isEnabled")
	public boolean isEnabled() {
		return isEnabledProperty.get();
	}

	/**
	 * Tells if the {@link CuteElement} is turned on or off by user. <br>
	 * If it's not enabled, the CuteElement is not executing when it's time for it.
	 *
	 * @return the boolean property
	 */
	public BooleanProperty isEnabledProperty() {
		return isEnabledProperty;
	}

	/**
	 * The name assigned to {@link CuteElement}.
	 *
	 * @return the string property
	 */
	public StringProperty nameProperty() {
		return nameProperty;
	}

	/**
	 * Sets the "broken message" for the {@link CuteElement}, <br>
	 * Sets the CuteElement's health as {@link ElementHealth#BROKEN}, <br>
	 * Sets the CuteElement's state as {@link ElementState#NEEDINIT}.
	 * <p>
	 * The "broken message" contains the reason why the CuteElement is broken and can't work. <br>
	 * The message might later show in GUI, telling the user a hint on how to fix the CuteElement.
	 * <br>
	 * <i>
	 * "The Coder's eyes are bleeding and he can't work, because of the bad JavaDoc. Please fix the JavaDoc"
	 * </i> - a good example of "broken message" for the "Coder" CuteElement. <br>
	 *
	 * @param whyUnhealthy
	 *            a string telling a story why this CuteElement is broken
	 */
	public void setAsBroken(String whyUnhealthy) {
		elementHealthProperty.set(ElementHealth.BROKEN);
		elementStateProperty.set(ElementState.NEEDINIT);
		whyUnhealthyProperty.set(whyUnhealthy);
		String name = (nameProperty.get().isEmpty()) ? "NONAME" : nameProperty.get();
		logger.info("Name: " + name + "' of type: '" + objectSimpleName + "' is broken: "
				+ whyUnhealthy);
	}

	/**
	 * Sets the CuteElement's last result as {@link Result#FAIL}.
	 */
	public void setFailedResult() {
		lastResultProperty.set(Result.FAIL);
		setAsFinished();
	}

	/**
	 * Resets the message why the {@link CuteElement} is unhealthy<br>
	 * . Sets the CuteElement's health as {@link ElementHealth#HEALTHY} <br>
	 * . Sets the CuteElement's state as {@link ElementState#READY}.
	 */
	public void setAsReadyAndHealthy() {
		elementStateProperty.set(ElementState.READY);
		elementHealthProperty.set(ElementHealth.HEALTHY);
		whyUnhealthyProperty.set("");
	}

	/**
	 * Sets the CuteElement's state as {@link ElementState#READY}.
	 */
	public void setAsReady() {
		elementStateProperty.set(ElementState.READY);
	}

	/**
	 * Sets the "sick message" for the {@link CuteElement} and sets the CuteElement as not feeling
	 * well. <br>
	 * The "sick message" contains the reason why the CuteElement is sick, but still can work. <br>
	 * The message might later show in GUI, telling the user a hint on how to fix the CuteElement.
	 * <br>
	 * <i> "The old Dog is barely creeping, because she was kicked. You may unkick her" </i> - a
	 * good example of "sick message" for the "old Dog" CuteElement. <br>
	 *
	 * @param whyUnhealthy
	 *            a string telling a story why this CuteElement is sick
	 */
	public void setAsSick(String whyUnhealthy) {
		elementHealthProperty.set(ElementHealth.SICK);
		whyUnhealthyProperty.set(whyUnhealthy);
		String name = (nameProperty.get().isEmpty()) ? "NONAME" : nameProperty.get();
		logger.info(
				"Name: " + name + "' of type: '" + objectSimpleName + "' is sick: " + whyUnhealthy);
	}

	/**
	 * Sets the CuteElement's last result as {@link Result#SUCCESS}.
	 */
	public void setSuccessfulResult() {
		lastResultProperty.set(Result.SUCCESS);
		setAsFinished();
	}

	/**
	 * Sets the CuteElement's last result as {@link Result#UNKNOWN}.
	 */
	public void setUnknownResult() {
		lastResultProperty.set(Result.UNKNOWN);
		setAsFinished();
	}

	/**
	 * Sets the CuteElement's state as {@link ElementState#WORKING}.
	 */
	public void setAsWorking() {
		elementStateProperty.set(ElementState.WORKING);
		setUnknownResult();
	}

	/**
	 * Sets the CuteElement's state as {@link ElementState#FINISHED}.
	 */
	private void setAsFinished() {
		elementStateProperty.set(ElementState.FINISHED);
	}

	/**
	 * Sets the {@link CuteElement} as enabled or not enabled.
	 *
	 * @param isEnabled
	 *            true or false
	 */
	@JsonProperty("isEnabled")
	public void setEnabled(boolean isEnabled) {
		isEnabledProperty.set(isEnabled);
	}

	/**
	 * Sets the name for the {@link CuteElement}.
	 *
	 * @param name
	 *            the CuteElement's name
	 */
	@JsonProperty("name")
	public void setName(String name) {
		this.nameProperty.set(name);
	}

	/**
	 * The current {@link ElementState} of the {@link CuteElement}.
	 *
	 * @return the object property
	 */
	public ObjectProperty<ElementState> elementStateProperty() {
		return elementStateProperty;
	}

	/**
	 * The last {@link Result} of {@link CuteElement}'s work.
	 *
	 * @return the object property
	 */
	public ObjectProperty<Result> lastResultProperty() {
		return lastResultProperty;
	}

	public String toString() {
		return objectSimpleName + ": " + nameProperty.get();
	}

	/**
	 * The reason why the {@link CuteElement} is unhealthy and can't work properly.
	 *
	 * @return the string property
	 */
	public StringProperty whyUnhealthyProperty() {
		return whyUnhealthyProperty;
	}

	/**
	 * The current {@link ElementHealth} of the {@link CuteElement}.
	 *
	 * @return the object property
	 */
	public ObjectProperty<ElementHealth> elementHealthProperty() {
		return elementHealthProperty;
	}
}
