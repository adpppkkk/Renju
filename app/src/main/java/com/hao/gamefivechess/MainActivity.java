package com.hao.gamefivechess;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.hao.ai.AI;
import com.hao.ai.AICallBack;
import com.hao.udv.FiveChessView;
import com.hao.udv.GameCallBack;


public class MainActivity extends AppCompatActivity implements GameCallBack, AICallBack, View.OnClickListener {

    //chess UI
    private FiveChessView fiveChessView;
    //score
    private TextView userScoreTv, aiScoreTv;
    //player's color
    private ImageView userChessIv, aiChessIv;
    //who's term
    private ImageView userTimeIv, aiTimeIv;
    //ai
    private AI ai;
    //PopUpWindow(choose color
    private PopupWindow chooseChess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize
        initViews();
        //initialize ai
        ai = new AI(fiveChessView.getChessArray(), this);
        //load view
        fiveChessView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //initialize PopupWindow
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                initPop(wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight());
            }
        });
    }

    private void initViews() {
        //chess UI
        fiveChessView = (FiveChessView) findViewById(R.id.five_chess_view);
        fiveChessView.setCallBack(this);
        //show the socre
        userScoreTv = (TextView) findViewById(R.id.user_score_tv);
        aiScoreTv = (TextView) findViewById(R.id.ai_score_tv);
        //player's color
        userChessIv = (ImageView) findViewById(R.id.user_chess_iv);
        aiChessIv = (ImageView) findViewById(R.id.ai_chess_iv);
        //who's term
        userTimeIv = (ImageView) findViewById(R.id.user_think_iv);
        aiTimeIv = (ImageView) findViewById(R.id.ai_think_iv);
        //restart game
        findViewById(R.id.restart_game).setOnClickListener(this);
    }


    //initialize PopupWindow
    private void initPop(int width, int height) {
        if (chooseChess == null) {
            View view = View.inflate(this, R.layout.pop_choose_chess, null);
            ImageButton white = (ImageButton) view.findViewById(R.id.choose_white);
            ImageButton black = (ImageButton) view.findViewById(R.id.choose_black);
            white.setOnClickListener(this);
            black.setOnClickListener(this);
            chooseChess = new PopupWindow(view, width, height);
            chooseChess.setOutsideTouchable(false);
            chooseChess.showAtLocation(fiveChessView, Gravity.CENTER, 0, 0);
        }
    }

    @Override
    public void GameOver(int winner) {
        //update wins
        updateWinInfo();
        switch (winner) {
            case FiveChessView.BLACK_WIN:
                showToast("Black Player Win！");
                break;
            case FiveChessView.NO_WIN:
                showToast("Draw！");
                break;
            case FiveChessView.WHITE_WIN:
                showToast("White Player Win！");
                break;
        }
    }

    //update win info
    private void updateWinInfo() {
        userScoreTv.setText(fiveChessView.getUserScore() + " ");
        aiScoreTv.setText(fiveChessView.getAiScore() + " ");
    }

    @Override
    public void ChangeGamer(boolean isWhite) {
        //ai term
        ai.aiBout();
        //play
        aiTimeIv.setVisibility(View.VISIBLE);
        userTimeIv.setVisibility(View.GONE);
    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void aiAtTheBell() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //update UI
                fiveChessView.postInvalidate();
                //check if is end
                fiveChessView.checkAiGameOver();
                //player term
                fiveChessView.setUserBout(true);
                //change play
                aiTimeIv.setVisibility(View.GONE);
                userTimeIv.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restart_game:
                //show PopupWindow
                chooseChess.showAtLocation(fiveChessView, Gravity.CENTER, 0, 0);
                //restart
                fiveChessView.resetGame();
                break;
            case R.id.choose_black:
                changeUI(false);
                chooseChess.dismiss();
                break;
            case R.id.choose_white:
                changeUI(true);
                chooseChess.dismiss();
                break;
        }
    }

    //update ui according to player's choice
    private void changeUI(boolean isUserWhite) {
        if (isUserWhite) {
            //choose white
            fiveChessView.setUserChess(FiveChessView.WHITE_CHESS);
            ai.setAiChess(FiveChessView.BLACK_CHESS);
            //player first
            fiveChessView.setUserBout(true);

            userChessIv.setBackgroundResource(R.drawable.white_chess);
            aiChessIv.setBackgroundResource(R.drawable.black_chess);
            aiTimeIv.setVisibility(View.GONE);
            userTimeIv.setVisibility(View.VISIBLE);
        } else {
            //choose black
            fiveChessView.setUserChess(FiveChessView.BLACK_CHESS);
            fiveChessView.setUserBout(false);
            //ai first
            ai.setAiChess(FiveChessView.WHITE_CHESS);
            ai.aiBout();

            userChessIv.setBackgroundResource(R.drawable.black_chess);
            aiChessIv.setBackgroundResource(R.drawable.white_chess);
            aiTimeIv.setVisibility(View.VISIBLE);
            userTimeIv.setVisibility(View.GONE);
        }
    }
}
