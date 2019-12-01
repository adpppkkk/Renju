package com.hao.udv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.hao.gamefivechess.R;



public class FiveChessView extends View implements View.OnTouchListener {

    //paint
    private Paint paint;
    //chess array
    private int[][] chessArray;
    //play order
    private boolean isWhite = true;
    //if over
    private boolean isGameOver = false;

    //bitmap
    private Bitmap whiteChess;
    private Bitmap blackChess;
    //Rect
    private Rect rect;
    //length
    private float len;
    //grid num
    private int GRID_NUMBER = 15;
    //cell width
    private float preWidth;
    //offset
    private float offset;
    //callback
    private GameCallBack callBack;
    //wins
    private int whiteChessCount, blackChessCount;
    //if player's term
    private boolean isUserBout = true;
    //player's color
    private int userChess = WHITE_CHESS;
    //num of wins
    private int userScore = 0, aiScore = 0;
    /**
     * variables
     */
    //white chess
    public static final int WHITE_CHESS = 1;
    //black chess
    public static final int BLACK_CHESS = 2;
    //
    public static final int NO_CHESS = 0;
    //white win
    public static final int WHITE_WIN = 101;
    //black win
    public static final int BLACK_WIN = 102;
    //draw
    public static final int NO_WIN = 103;

    public FiveChessView(Context context) {
        this(context, null);
    }

    public FiveChessView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FiveChessView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //initialize Paint
        paint = new Paint();
        //AntiAlias
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        //initialize chessArray
        chessArray = new int[GRID_NUMBER][GRID_NUMBER];
        //initialize chess picture bitmap
        whiteChess = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_chess);
        blackChess = BitmapFactory.decodeResource(context.getResources(), R.drawable.black_chess);
        //initialize wins
        whiteChessCount = 0;
        blackChessCount = 0;
        //initialize Rect
        rect = new Rect();
        //set listener
        setOnTouchListener(this);
        //reset chessboard status
        for (int i = 0; i < GRID_NUMBER; i++) {
            for (int j = 0; j < GRID_NUMBER; j++) {
                chessArray[i][j] = 0;
            }
        }
    }

    /**
     * get length' width
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //get
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        //set smaller
        int len = width > height ? height : width;
        //reset
        setMeasuredDimension(len, len);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //set as square
        len = getWidth() > getHeight() ? getHeight() : getWidth();
        preWidth = len / GRID_NUMBER;
        //offset
        offset = preWidth / 2;
        //draw cells
        for (int i = 0; i < GRID_NUMBER; i++) {
            float start = i * preWidth + offset;
            //horizontal
            canvas.drawLine(offset, start, len - offset, start, paint);
            //vertical
            canvas.drawLine(start, offset, start, len - offset, paint);
        }
        //draw chess
        for (int i = 0; i < GRID_NUMBER; i++) {
            for (int j = 0; j < GRID_NUMBER; j++) {
                //rect
                float rectX = offset + i * preWidth;
                float rectY = offset + j * preWidth;
                //set rect
                rect.set((int) (rectX - offset), (int) (rectY - offset),
                        (int) (rectX + offset), (int) (rectY + offset));
                //go through chessArray
                switch (chessArray[i][j]) {
                    case WHITE_CHESS:
                        //draw white
                        canvas.drawBitmap(whiteChess, null, rect, paint);
                        break;
                    case BLACK_CHESS:
                        //draw black
                        canvas.drawBitmap(blackChess, null, rect, paint);
                        break;
                }
            }
        }
    }

    /**
     * ending?
     */
    private void checkGameOver() {
        //get color of playing
        int chess = isWhite ? BLACK_CHESS : WHITE_CHESS;
        //if full
        boolean isFull = true;
        //go through chessArray
        for (int i = 0; i < GRID_NUMBER; i++) {
            for (int j = 0; j < GRID_NUMBER; j++) {
                //is full?
                if (chessArray[i][j] != BLACK_CHESS && chessArray[i][j] != WHITE_CHESS) {
                    isFull = false;
                }
                //if five in a row
                if (chessArray[i][j] == chess) {
                    //*****
                    if (isFiveSame(i, j)) {
                        //***** end
                        isGameOver = true;
                        if (callBack != null) {
                            //who wins
                            if (chess == WHITE_CHESS) {
                                whiteChessCount++;
                            } else {
                                blackChessCount++;
                            }
                            //bot or player win
                            if (userChess == chess) {
                                userScore++;
                            } else {
                                aiScore++;
                            }
                            callBack.GameOver(chess == WHITE_CHESS ? WHITE_WIN : BLACK_WIN);
                        }
                        return;
                    }
                }
            }
        }
        //draw if chessboard is full
        if (isFull) {
            isGameOver = true;
            if (callBack != null) {
                callBack.GameOver(NO_WIN);
            }
        }
    }

    /**
     * reset game
     */
    public void resetGame() {
        isGameOver = false;
        //reset chessboard
        for (int i = 0; i < GRID_NUMBER; i++) {
            for (int j = 0; j < GRID_NUMBER; j++) {
                chessArray[i][j] = 0;
            }
        }
        //update ui
        postInvalidate();
    }

    /**
     * if win
     */
    private boolean isFiveSame(int x, int y) {
        //horizontal
        if (x + 4 < GRID_NUMBER) {
            if (chessArray[x][y] == chessArray[x + 1][y] && chessArray[x][y] == chessArray[x + 2][y]
                    && chessArray[x][y] == chessArray[x + 3][y] && chessArray[x][y] == chessArray[x + 4][y]) {
                return true;
            }
        }
        //vertical
        if (y + 4 < GRID_NUMBER) {
            if (chessArray[x][y] == chessArray[x][y + 1] && chessArray[x][y] == chessArray[x][y + 2]
                    && chessArray[x][y] == chessArray[x][y + 3] && chessArray[x][y] == chessArray[x][y + 4]) {
                return true;
            }
        }
        //top-left - bot-right
        if (y + 4 < GRID_NUMBER && x + 4 < GRID_NUMBER) {
            if (chessArray[x][y] == chessArray[x + 1][y + 1] && chessArray[x][y] == chessArray[x + 2][y + 2]
                    && chessArray[x][y] == chessArray[x + 3][y + 3] && chessArray[x][y] == chessArray[x + 4][y + 4]) {
                return true;
            }
        }
        //top-right - bot-left
        if (y - 4 > 0 && x + 4 < GRID_NUMBER) {
            if (chessArray[x][y] == chessArray[x + 1][y - 1] && chessArray[x][y] == chessArray[x + 2][y - 2]
                    && chessArray[x][y] == chessArray[x + 3][y - 3] && chessArray[x][y] == chessArray[x + 4][y - 4]) {
                return true;
            }
        }
        return false;
    }

    //ai player end
    public void checkAiGameOver() {
        isWhite = userChess == WHITE_CHESS;
        checkGameOver();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isGameOver && isUserBout) {
                    //get position of pressed
                    float downX = event.getX();
                    float downY = event.getY();
                    //press on chessboard?
                    if (downX >= offset / 2 && downX <= len - offset / 2
                            && downY >= offset / 2 && downY <= len - offset / 2) {
                        //get chess position
                        int x = (int) (downX / preWidth);
                        int y = (int) (downY / preWidth);
                        //if a chess exist on that position
                        if (chessArray[x][y] != WHITE_CHESS &&
                                chessArray[x][y] != BLACK_CHESS) {
                            //give num
                            chessArray[x][y] = userChess;
                            //change chess color
                            isWhite = userChess == WHITE_CHESS;
                            //switch to bot
                            isUserBout = false;
                            //update chessboard
                            postInvalidate();
                            //game over?
                            checkGameOver();
                            //callback
                            if (callBack != null) {
                                callBack.ChangeGamer(isWhite);
                            }
                        }
                    }
                } else if (isGameOver) {
                    Toast.makeText(getContext(), "Game is already over, Please restart Game！",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return false;
    }

    public void setCallBack(GameCallBack callBack) {
        this.callBack = callBack;
    }

    public int getWhiteChessCount() {
        return whiteChessCount;
    }

    public int getBlackChessCount() {
        return blackChessCount;
    }

    public int[][] getChessArray() {
        return chessArray;
    }

    public void setUserBout(boolean userBout) {
        isUserBout = userBout;
    }

    public void setUserChess(int userChess) {
        this.userChess = userChess;
    }

    public int getUserScore() {
        return userScore;
    }

    public int getAiScore() {
        return aiScore;
    }
}
