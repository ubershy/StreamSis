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
package com.ubershy.streamsis.gui.controllers;

import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.scene.Node;

import javafx.scene.control.TextArea;

import javafx.scene.layout.AnchorPane;

import javafx.scene.layout.VBox;

import org.controlsfx.control.NotificationPane;

//Under construction. Not working. Please unsee.
public class FullModeNotificationPaneController {

	@FXML
	TextArea ta;

	@FXML
	VBox vbox;

	NotificationPane notificationPane;

	public void initialize() {

		ObservableList<Node> list = vbox.getChildren();

		Node n = list.iterator().next();

		AnchorPane nn = (AnchorPane) n;

		notificationPane = new NotificationPane(nn);

		notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);

		notificationPane.setText("New data available for review.");

		notificationPane.setShowFromTop(false);

		vbox.getChildren().clear();

		vbox.getChildren().add(notificationPane);

	}

	@FXML
	private void okClickHandler() {

		notificationPane.show();

	}

}