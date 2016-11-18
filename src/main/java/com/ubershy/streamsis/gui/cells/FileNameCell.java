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
package com.ubershy.streamsis.gui.cells;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.gui.contextmenu.PossibleMoves;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;

/**
 * FileNameCell. Contains name of {@link File}. <br>
 */
public class FileNameCell extends TableCell<File, String> {

	static final Logger logger = LoggerFactory.getLogger(FileNameCell.class);
	
	private final Tooltip tooltip = new Tooltip();
	
	private PossibleMoves possibleMoves;

	/**
	 * Instantiates a new FileNameCell Cell. Sets the default values.
	 */
	public FileNameCell() {
		setMaxHeight(getHeight());
		setGraphicTextGap(6.0);
		setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setText(null);
			setGraphic(null);
			setContextMenu(null);
			setTooltip(null);
		} else {
			if (isEditing()) {
				// do not implement
			} else {
				setText(item);
				if (getTooltip() == null) {
					setTooltip(tooltip);
				}
				tooltip.setText(item);
				possibleMoves = PossibleMoves.UPORDOWN;
				int sizeOfList = getTableView().getItems().size();
				if (sizeOfList != 1) {
					if (getIndex() == 0)
						possibleMoves = PossibleMoves.ONLYDOWN;
					if (getIndex() == sizeOfList - 1)
						possibleMoves = PossibleMoves.ONLYUP;
				} else {
					possibleMoves = PossibleMoves.NOWHERE;
				}
			}
		}
	}

	public PossibleMoves getPossibleMoves() {
		return possibleMoves;
	}

}
