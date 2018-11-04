// BoardWidget.java
//
// By Sebastian Raaphorst, 2018.

package com.vorpal.toggle;

import com.vorpal.toggle.board.AxisAlignment;
import com.vorpal.toggle.board.BoardType;
import com.vorpal.utils.Dimensions;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import com.vorpal.toggle.board.Board;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public final class BoardWidget extends GridPane {
    BoardWidget(final Board board) {
        super();

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
            final ToggleButton tbnw = makeTB(board.getOutOfBoundsValueAt(-1, -1), 0, 0);
            tbnw.setDisable(true);
            final ToggleButton tbne = makeTB(board.getOutOfBoundsValueAt(-1, dimy), 0, dimy + 1);
            tbne.setDisable(true);
            final ToggleButton tbsw = makeTB(board.getOutOfBoundsValueAt(dimx, -1), dimx + 1, 0);
            tbsw.setDisable(true);
            final ToggleButton tbse = makeTB(board.getOutOfBoundsValueAt(dimx, dimy), dimx + 1, dimy + 1);
            tbse.setDisable(true);
        }

        // Make the x rows if necessary.
        if (xrows) {
            for (int column = 0; column < dimy; ++column) {
                final ToggleButton tbn = makeTB(board.getOutOfBoundsValueAt(-1, column),
                        0, column + basey);
                tbn.setDisable(true);

                final ToggleButton tbs = makeTB(board.getOutOfBoundsValueAt(dimx, column),
                        dimx + 1, column + basey);
                tbs.setDisable(true);
            }
        }

        // Make the y rows if necessary.
        if (yrows) {
            for (int row = 0; row < dimy; ++row) {
                final ToggleButton tbw = makeTB(board.getOutOfBoundsValueAt(row, -1),
                        row + basex, 0);
                tbw.setDisable(true);

                final ToggleButton tbe = makeTB(board.getOutOfBoundsValueAt(row, dimy),
                        row + basex, dimy + 1);
                tbe.setDisable(true);
            }
        }

        for (int x = 0; x < dimx; ++x)
            for (int y = 0; y < dimy; ++y) {
//                final Text text = new Text(board.getValueAt(x, y));
//                text.setFont(Font.font(60));
//                final Rectangle rect = new Rectangle();
//                rect.setArcWidth(20);
//                rect.setArcHeight(20);
//                rect.setFill(new Color(0, 1, 0, 1));
//                StackPane stack = new StackPane();
//                stack.getChildren().addAll(rect, text);
//
//                GridPane.setRowIndex(stack, x);
//                GridPane.setColumnIndex(stack, y);
//                getChildren().add(stack);

                final ToggleButton tb = makeTB(board.getValueAt(x, y), x + basex, y + basey);
            }
    }

    private ToggleButton makeTB(final String s, int row, int column) {
        final ToggleButton tb = new ToggleButton(s);
        tb.setFont(Font.font(70));
        GridPane.setRowIndex(tb, row);
        GridPane.setColumnIndex(tb, column);
        getChildren().add(tb);
        return tb;

    }
}
