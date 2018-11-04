// BoardWidget.java
//
// By Sebastian Raaphorst, 2018.

package com.vorpal.toggle;

import com.vorpal.toggle.board.AxisAlignment;
import com.vorpal.toggle.board.Board;
import com.vorpal.toggle.board.BoardType;
import com.vorpal.toggle.dice.Die;
import com.vorpal.utils.Coordinates;
import com.vorpal.utils.Dimensions;

import javafx.event.EventHandler;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Set;
import java.util.Stack;

public final class BoardWidget extends GridPane {
    private final static Font DEFAULT_FONT = Font.font(70);

    // A subclass of a ToggleButton that holds a die.
    private class DieButton extends ToggleButton {
        private final Die die;

        // The position of the button in the grid: it could be out of bounds.
        private final int x;
        private final int y;

        // The position of the button as a valid die in the grid.
        private final int adjustedX;
        private final int adjustedY;

        DieButton(final int x, final int y, final int row, final int column) {
            super(BoardWidget.this.inBounds(x, y) ? board.getValueAt(x, y) : board.getOutOfBoundsValueAt(x, y));
            this.x = x;
            this.y = y;
            final Coordinates c = board.getBoardType().convert(board.getSize().first,
                    board.getSize().second, x, y)
                    .orElseThrow(() -> new IllegalArgumentException("Illegal coordinates:" + new Coordinates(x, y)));

            adjustedX = c.first;
            adjustedY = c.second;
            die = board.getDieAt(adjustedX, adjustedY);

            // Formatting.
            setFont(DEFAULT_FONT);

            if (!inBounds(x, y)) {
                setDisable(true);
                onMouseClickedProperty().set((o) -> processButtonToggleAttempt(this));
            } else {
                setDisable(false);
                selectedProperty().addListener((odb, o, n) -> processButtonToggleAttempt(this));
            }

            // Add it to the grid.
            GridPane.setRowIndex(this, row);
            GridPane.setColumnIndex(this, column);
            BoardWidget.this.getChildren().add(this);
        }

        Die getDie() {
            return die;
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        Coordinates getPosition() {
            return new Coordinates(x, y);
        }

        int getAdjustedX() {
            return adjustedX;
        }

        int getAdjustedY() {
            return adjustedY;
        }

        Coordinates getAdjustedCoordinates() {
            return new Coordinates(adjustedX, adjustedY);
        }
    }

    // A reference to the current board.
    private final Board board;

    // A stack of the buttons that have been clicked. The must be consecutive.
    private final Stack<DieButton> buttonsClicked;

    BoardWidget(final Board board) {
        super();

        this.board = board;
        buttonsClicked = new Stack<>();

        // Create the toggle buttons. We don't create a toggle group because we want the buttons to be selectable
        // without deselecting others.
        final Dimensions dim = board.getSize();
        final int dimx = dim.first;
        final int dimy = dim.second;

        final BoardType boardType = board.getBoardType();
        final boolean xrows = boardType.getXAxisAlignment() != AxisAlignment.NONE;
        final boolean yrows = boardType.getYAxisAlignment() != AxisAlignment.NONE;

        final int basex = xrows ? 1 : 0;
        final int basey = yrows ? 1 : 0;

        // Make the diagonal corners if necessary.
        if (xrows && yrows) {
            new DieButton(-1, -1, 0, 0);
            new DieButton(-1, dimy, 0, dimy + 1);
            new DieButton(dimx, -1, dimx + 1, 0);
            new DieButton(dimx, dimy, dimx + 1, dimy + 1);
        }

        // Make the x rows if necessary.
        if (xrows) {
            for (int column = 0; column < dimy; ++column) {
                new DieButton(-1, column, 0, column + basey);
                new DieButton(dimx, column, dimx + 1, column + basey);
            }
        }

        // Make the y rows if necessary.
        if (yrows) {
            for (int row = 0; row < dimy; ++row) {
                new DieButton(row, -1, row + basex, 0);
                new DieButton(row, dimy, row + basex, dimy + 1);
            }
        }

        for (int x = 0; x < dimx; ++x)
            for (int y = 0; y < dimy; ++y) {
                new DieButton(x, y, x + basex, y + basey);
            }
    }

    private boolean inBounds(final int x, final int y) {
        return (x >= 0 && x < board.getSize().first && y >= 0 && y < board.getSize().second);
    }

    /**
     * This handles the case when a button is clicked.
     * @param db the button
     */
    private void processButtonToggleAttempt(final DieButton db) {
        System.out.println("HERE");

        // Three cases:
        // 1. The last button clicked was clicked: remove it.
        if (!buttonsClicked.isEmpty() && buttonsClicked.peek().equals(db))
            buttonsClicked.pop();

        // 2. This button was formerly unclicked and is adjacent to the last button clicked: add it.
        else if (!buttonsClicked.isEmpty() &&
                !buttonsClicked.contains(db) &&
                board.getAdjacencies(buttonsClicked.peek().getAdjustedCoordinates()).contains(db.getAdjustedCoordinates()))
                buttonsClicked.push(db);

        // 3. Not adjacent to the previous button clicked, or already clicked and not previous button clicked:
        //    Start over.
        else {
            while (!buttonsClicked.isEmpty()) {
                final DieButton b = buttonsClicked.pop();
                b.setSelected(false);
            }
            db.setSelected(true);
            buttonsClicked.push(db);
        }

    }
}
