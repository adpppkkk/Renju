package com.hao.udv;

/**
 *Callback
 */

public interface GameCallBack {
    //game over callback
    void GameOver(int winner);
    //change player callback
    void ChangeGamer(boolean isWhite);
}
