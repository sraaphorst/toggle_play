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

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;

public final class BoardWidget extends GridPane {
    // Logging
    private final static Logger LOG = Logger.getLogger(BoardWidget.class.getName());

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

            // If it is not in bounds, we disable the button, wrap it in another component to receive mouse events,
            // and then add that component to the grid.
            final Node node;
            if (!inBounds(x, y)) {
                setDisable(true);
                final Pane p = new Pane();
                p.getChildren().add(this);
                node = p;
                p.setOnMouseClicked((o) -> processButtonToggleAttempt(this));
            } else {
                node = this;
                setOnAction(v -> processButtonToggleAttempt(this));
            }

            // Add it to the grid.
            GridPane.setRowIndex(node, row);
            GridPane.setColumnIndex(node, column);
            BoardWidget.this.getChildren().add(node);
        }

        Die getDie() {
            return die;
        }

        int getUnadjustedX() {
            return x;
        }

        int getUnadjustedY() {
            return y;
        }

        Coordinates getUnadjustedCoordinates() {
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

    // A list of the middle DieButtons.
    private final ArrayList<ArrayList<DieButton>> dieButtons;

    // A reference to the current board.
    private final Board board;

    // A stack of the buttons that have been clicked. The must be consecutive.
    private final Stack<DieButton> dieButtonStack;

    BoardWidget(final Board board) {
        super();
        setAlignment(Pos.CENTER);

        this.board = board;
        dieButtonStack = new Stack<>();

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

        // Now we store the rest of the DieButtons, so that when an outer button is clicked, we can
        // get the DieButton in the interior to which it is linked.
        dieButtons = new ArrayList<>();
        for (int x = 0; x < dimx; ++x) {
            final ArrayList<DieButton> row = new ArrayList<>();
            for (int y = 0; y < dimy; ++y)
                row.add(new DieButton(x, y, x + basex, y + basey));
            dieButtons.add(row);
        }
    }

    private boolean inBounds(final int x, final int y) {
        return (x >= 0 && x < board.getSize().first && y >= 0 && y < board.getSize().second);
    }

    /**
     * This handles the case when a button is clicked.
     * Note that this could be a button outside of bounds, used to indicate adjacencies. What we actually
     * want is the button in the interior to which is corresponds: hence, we consider this button "unadjusted"
     * and work with its "adjusted" version.
     * @param unadjustedDb the unadjusted button
     */
    private void processButtonToggleAttempt(final DieButton unadjustedDb) {
        LOG.info(String.format("Processing button: unadjusted=%s, adjusted=%s",
                unadjustedDb.getUnadjustedCoordinates(),
                unadjustedDb.getAdjustedCoordinates()));

        // Get the adjusted die button, i.e. the DieButton in the interior to which this one corresponds.
        final DieButton db = dieButtons.get(unadjustedDb.getAdjustedX()).get(unadjustedDb.getAdjustedY());
        assert(db.getDie() == unadjustedDb.getDie());

        // Three cases:
        if (!dieButtonStack.isEmpty() && dieButtonStack.peek().equals(db)) {
            // 1. The last button clicked was clicked: remove it.
            db.setSelected(false);
            dieButtonStack.pop();
        }
        else if (!dieButtonStack.isEmpty() &&
                !dieButtonStack.contains(db) &&
                board.getAdjacencies(dieButtonStack.peek().getAdjustedCoordinates()).contains(db.getAdjustedCoordinates())) {
            // 2. This button was formerly unclicked and is adjacent to the last button clicked: add it.
            db.setSelected(true);
            dieButtonStack.push(db);
        }
        else {
            // 3. Not adjacent to the previous button clicked, or already clicked and not previous button clicked:
            //    Start over.
            while (!dieButtonStack.isEmpty()) {
                final DieButton b = dieButtonStack.pop();
                b.setSelected(false);
            }
            db.setSelected(true);
            dieButtonStack.push(db);
        }
    }
}
