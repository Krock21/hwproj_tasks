package me.hwproj;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board game;

    @BeforeEach
    private void gameInit() {
        game = new Board();
    }


    @Test
    void getStatus() {
    }

    @Test
    void xWinDiagonalGameTest() {
        game.makeMove(0,0);
        game.makeMove(0,1);
        game.makeMove(1,1);
        game.makeMove(0,2);
        game.makeMove(2,2);
        assertEquals(BoardStatus.X_WIN, game.getStatus());
    }


    @Test
    void oWinDiagonalGameTest() {
        game.makeMove(2,1);
        game.makeMove(0,2);
        game.makeMove(0,1);
        game.makeMove(1,1);
        game.makeMove(0,0);
        game.makeMove(2,0);
        assertEquals(BoardStatus.O_WIN, game.getStatus());
    }


    @Test
    void oWinRowGameTest() {
        game.makeMove(2,2);
        game.makeMove(0,0);
        game.makeMove(1,1);
        game.makeMove(0,1);
        game.makeMove(2,1);
        game.makeMove(0,2);
        assertEquals(BoardStatus.O_WIN, game.getStatus());
    }

    @Test
    void xWinColGameTest() {
        game.makeMove(0,2);
        game.makeMove(2,1);
        game.makeMove(0,1);
        game.makeMove(1,1);
        game.makeMove(0,0);
        assertEquals(BoardStatus.X_WIN, game.getStatus());
    }

    @Test
    void drawGameTest() {
        game.makeMove(0,0);
        game.makeMove(1,0);
        game.makeMove(0,1);
        game.makeMove(1,1);
        game.makeMove(0,2);
        game.makeMove(1,2);
        assertEquals(BoardStatus.DRAW, game.getStatus());
    }

    @Test
    void doubleClickGameTest() {
        game.makeMove(0,0);
        game.makeMove(0,0);
        game.makeMove(1,0);
        game.makeMove(0,1);
        game.makeMove(1,1);
        game.makeMove(0,2);
        game.makeMove(1,2);
        assertEquals(BoardStatus.DRAW, game.getStatus());
    }
}