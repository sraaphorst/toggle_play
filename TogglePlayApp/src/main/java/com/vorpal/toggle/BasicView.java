package com.vorpal.toggle;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.vorpal.toggle.board.Board;
import com.vorpal.toggle.board.BoardType;
import com.vorpal.toggle.dice.DefaultDiceSets;
import com.vorpal.toggle.trie.LinkedTrie;
import com.vorpal.toggle.trie.Trie;
import com.vorpal.utils.BigMath;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.math.BigInteger;

public class BasicView extends View {

    public BasicView() {
        final Board board = new Board(BoardType.TORUS,
                DefaultDiceSets.DEFAULT_16_DICE_SET,
                BigMath.unrankPermutationAsList(16, BigInteger.valueOf(9223372036854775807L)),
                BigMath.unrankDiceFacesAsList(16, BigInteger.valueOf(839283)),
                Trie.createDefaultTrie(), 4);
        final BoardWidget boardWidget = new BoardWidget(board);

        VBox controls = new VBox(15.0, boardWidget);
        controls.setAlignment(Pos.CENTER);
        setCenter(controls);
    }

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> System.out.println("Menu")));
        appBar.setTitleText("Basic View");
        appBar.getActionItems().add(MaterialDesignIcon.SEARCH.button(e -> System.out.println("Search")));
    }
    
}
