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
package com.ubershy.streamsis.gui.contextmenu;

import java.util.List;

public enum PossibleMoves {
	/** Tells that the item(items) can move only up in list (for the last item in list). */
	ONLYUP,
	/** Tells that the item(items) can move only down in list (for the first item in list). */
	ONLYDOWN,
	/** Tells that the item(items) can move up/down in list (for the item in between other items).*/
	UPORDOWN,
	/** Tells that the item(items) can't move in list (for the single item in list). */
	NOWHERE;
	
	public static PossibleMoves generateMovesForSingleItemInList(List<?> list, int indexOfItem) {
		PossibleMoves possibleMoves = PossibleMoves.UPORDOWN;
		int sizeOfList = list.size();
		if (sizeOfList > 1) {
			if (indexOfItem == 0)
				possibleMoves = PossibleMoves.ONLYDOWN;
			if (indexOfItem == sizeOfList - 1)
				possibleMoves = PossibleMoves.ONLYUP;
		} else {
			possibleMoves = PossibleMoves.NOWHERE;
		}
		return possibleMoves;
	}
	
	public static PossibleMoves generateMovesForMultipleItemsInList(List<?> list,
			int indexOfUpperItem, int indexOfLowerItem) {
		PossibleMoves possibleMoves = PossibleMoves.NOWHERE;
		int sizeOfList = list.size();
		if (sizeOfList > 1) {
			boolean upperCanMoveUp = true;
			boolean lowerCanMoveDown = true;
			if (indexOfUpperItem == 0)
				upperCanMoveUp = false;
			if (indexOfLowerItem == sizeOfList - 1)
				lowerCanMoveDown = false;
			if (upperCanMoveUp && lowerCanMoveDown) {
				possibleMoves = PossibleMoves.UPORDOWN;
			} else if (upperCanMoveUp) {
				possibleMoves = PossibleMoves.ONLYUP;
			} else if (lowerCanMoveDown) {
				possibleMoves = PossibleMoves.ONLYDOWN;
			}
		}
		return possibleMoves;
	}
}

