package com.hao.ai;

import com.hao.udv.FiveChessView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class AI implements Runnable {
    //chessboard info
    private int[][] chessArray;
    //bot role(black as default)
    private int aiChess = FiveChessView.BLACK_CHESS;
    //all points has no chess
    private List<Point> pointList;
    //ai finish callback
    private AICallBack callBack;
    //panelLength
    private int panelLength;
    /**
     * priority
     * FIVE at least five (win)
     * LIVE_X X continous chess and open both sides
     * DEAD_X continous chess and open one side
     * DEAD
     */
    private final static int FIVE = 10000;
    private final static int LIVE_FOUR = 4500;
    private final static int DEAD_FOUR = 2000;
    private final static int LIVE_THREE = 900;
    private final static int DEAD_THREE = 400;
    private final static int LIVE_TWO = 150;
    private final static int DEAD_TWO = 70;
    private final static int LIVE_ONE = 30;
    private final static int DEAD_ONE = 15;
    private final static int DEAD = 1;

    public AI(int[][] chessArray, AICallBack callBack) {
        pointList = new ArrayList<>();
        this.chessArray = chessArray;
        this.callBack = callBack;
        this.panelLength = chessArray.length;
    }

    //ai start
    public void aiBout() {
        new Thread(this).start();
    }

    //determine the priority
    private void checkPriority(Point p) {
        int aiPriority = checkSelf(p.getX(), p.getY());
        int userPriority = checkUser(p.getX(), p.getY());
        p.setPriority(aiPriority >= userPriority ? aiPriority : userPriority);
    }

    //get temporary and check self
    private int checkSelf(int x, int y) {
        return getHorizontalPriority(x, y, aiChess)
                + getVerticalPriority(x, y, aiChess)
                + getLeftSlashPriority(x, y, aiChess)
                + getRightSlashPriority(x, y, aiChess);
    }

    //get temporary and check player
    private int checkUser(int x, int y) {
        int userChess;
        if (aiChess == FiveChessView.WHITE_CHESS) {
            userChess = FiveChessView.BLACK_CHESS;
        } else {
            userChess = FiveChessView.WHITE_CHESS;
        }
        return getHorizontalPriority(x, y, userChess)
                + getVerticalPriority(x, y, userChess)
                + getLeftSlashPriority(x, y, userChess)
                + getRightSlashPriority(x, y, userChess);
    }

    //find best
    @Override
    public void run() {
        //clean pointList
        pointList.clear();
        int blankCount = 0;
        for (int i = 0; i < panelLength; i++)
            for (int j = 0; j < panelLength; j++) {
                if (chessArray[i][j] == FiveChessView.NO_CHESS) {
                    Point p = new Point(i, j);
                    checkPriority(p);
                    pointList.add(p);
                    blankCount++;
                }
            }
        //go through pointList，find the Point with highest priority
        Point max = pointList.get(0);
        for (Point point : pointList) {
            if (max.getPriority() < point.getPriority()) {
                max = point;
            }
        }
        //AI or player first play
        if (blankCount >= panelLength * panelLength - 1) {
            max = getStartPoint();
        }
        //sleep for 2sec
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //play adn callback
        chessArray[max.getX()][max.getY()] = aiChess;
        callBack.aiAtTheBell();
    }

    public void setAiChess(int aiChess) {
        this.aiChess = aiChess;
    }


    private Point getStartPoint() {
        //is can be use
        boolean isUse = true;
        //select random position
        Random random = new Random();
        int x = random.nextInt(5) + 5;
        int y = random.nextInt(5) + 5;
        //make sure no chess around
        for (int i = x - 1; i <= x + 1; i++)
            for (int j = y - 1; j <= y + 1; j++) {
                if (chessArray[i][j] != FiveChessView.NO_CHESS) {
                    isUse = false;
                }
            }
        if (isUse) {
            return new Point(x, y);
        } else {
            return getStartPoint();
        }
    }

    /**
     * determine chessArray[x][y] priority horizontal
     *
     * @param x
     * @param y
     * @param chess color
     * @return priority
     */
    private int getHorizontalPriority(int x, int y, int chess) {
        //connected count
        int connectCount = 1;
        //if left is open
        boolean isStartStem = false;
        //if right is open
        boolean isEndStem = false;

        //calculate left first
        //if y = 0, the left is closed because its on edge
        if (y == 0) {
            isStartStem = true;
        } else {
            //go through left
            for (int i = y - 1; i >= 0; i--) {
                //if not self
                if (chessArray[x][i] != chess) {
                    //if not self
                    isStartStem = chessArray[x][i] != FiveChessView.NO_CHESS;
                    break;
                } else {
                    connectCount++;
                    if (i == 0) {
                        //on edge
                        isStartStem = true;
                    }
                }
            }
        }

        //right
        //same
        if (y == panelLength - 1) {
            isEndStem = true;
        } else {
            //S
            for (int i = y + 1; i < panelLength; i++) {
                //S
                if (chessArray[x][i] != chess) {
                    //S
                    isEndStem = chessArray[x][i] != FiveChessView.NO_CHESS;
                    break;
                } else {
                    connectCount++;
                    if (i == panelLength - 1) {
                        //S
                        isEndStem = true;
                    }
                }
            }
        }
        //calculate priority
        return calcPriority(connectCount, isStartStem, isEndStem);
    }

    /**
     * determine chessArray[x][y] priority vertical
     */
    private int getVerticalPriority(int x, int y, int chess) {
        //S
        int connectCount = 1;
        //S
        boolean isStartStem = false;
        //S
        boolean isEndStem = false;

        //S
        if (x == 0) {
            isStartStem = true;
        } else {
            //S
            for (int i = x - 1; i >= 0; i--) {
                //S
                if (chessArray[i][y] != chess) {
                    //S
                    isStartStem = chessArray[i][y] != FiveChessView.NO_CHESS;
                    break;
                } else {
                    connectCount++;
                    if (i == 0) {
                        //S
                        isStartStem = true;
                    }
                }
            }
        }

        //S
        if (x == panelLength - 1) {
            isEndStem = true;
        } else {
            //S
            for (int i = x + 1; i < panelLength; i++) {
                //S
                if (chessArray[i][y] != chess) {
                    //S
                    isEndStem = chessArray[i][y] != FiveChessView.NO_CHESS;
                    break;
                } else {
                    connectCount++;
                    if (i == panelLength - 1) {
                        //S
                        isEndStem = true;
                    }
                }
            }
        }
        //S
        return calcPriority(connectCount, isStartStem, isEndStem);
    }

    /**
     * determiine chessArray[x][y] priority top-left to bot-right
     */
    private int getLeftSlashPriority(int x, int y, int chess) {
        //S
        int connectCount = 1;
        //S
        boolean isStartStem = false;
        //S
        boolean isEndStem = false;

        //S
        if (x == 0 || y == 0) {
            isStartStem = true;
        } else {
            //S
            for (int i = x - 1, j = y - 1; i >= 0 && j >= 0; i--, j--) {
                //S
                if (chessArray[i][j] != chess) {
                    //S
                    isStartStem = chessArray[i][j] != FiveChessView.NO_CHESS;
                    break;
                } else {
                    connectCount++;
                    if (i == 0 || j == 0) {
                        //S
                        isStartStem = true;
                    }
                }
            }
        }

        //S
        if (x == panelLength - 1 || y == panelLength - 1) {
            isEndStem = true;
        } else {
            //S
            for (int i = x + 1, j = y + 1; i < panelLength && j < panelLength; i++, j++) {
                //S
                if (chessArray[i][j] != chess) {
                    //S
                    isEndStem = chessArray[i][j] != FiveChessView.NO_CHESS;
                    break;
                } else {
                    connectCount++;
                    if (i == panelLength - 1 || j == panelLength - 1) {
                        //S
                        isEndStem = true;
                    }
                }
            }
        }
        //S
        return calcPriority(connectCount, isStartStem, isEndStem);
    }

    /**
     * determiine chessArray[x][y] priority top-right to bot-left
     */
    private int getRightSlashPriority(int x, int y, int chess) {
        //S
        int connectCount = 1;
        //S
        boolean isStartStem = false;
        //S
        boolean isEndStem = false;

        //S
        if (x == panelLength - 1 || y == 0) {
            isStartStem = true;
        } else {
            //S
            for (int i = x + 1, j = y - 1; i < panelLength && j >= 0; i++, j--) {
                //S
                if (chessArray[i][j] != chess) {
                    //S
                    isStartStem = chessArray[i][j] != FiveChessView.NO_CHESS;
                    break;
                } else {
                    connectCount++;
                    if (i == panelLength - 1 || j == 0) {
                        //S
                        isStartStem = true;
                    }
                }
            }
        }

        //S
        if (x == 0 || y == panelLength - 1) {
            isEndStem = true;
        } else {
            //S
            for (int i = x - 1, j = y + 1; i >= 0 && j < panelLength; i--, j++) {
                //S
                if (chessArray[i][j] != chess) {
                    //S
                    isEndStem = chessArray[i][j] != FiveChessView.NO_CHESS;
                    break;
                } else {
                    connectCount++;
                    if (i == 0 || j == panelLength - 1) {
                        //S
                        isEndStem = true;
                    }
                }
            }
        }
        //S
        return calcPriority(connectCount, isStartStem, isEndStem);
    }


    /**
     * calculate the priority based on the num of connected and if is closed
     *
     * @param connectCount num of connected
     * @param isStartStem  status of start
     * @param isEndStem    status of end
     * @return priority
     */
    private int calcPriority(int connectCount, boolean isStartStem, boolean isEndStem) {
        //priority
        int priority = 0;
        if (connectCount >= 5) {
            //can win
            priority = FIVE;
        } else {
            //cannot win
            if (isStartStem && isEndStem) {
                //dead
                priority = DEAD;
            } else if (isStartStem == isEndStem) {
                //open both
                if (connectCount == 4) {
                    priority = LIVE_FOUR;
                } else if (connectCount == 3) {
                    priority = LIVE_THREE;
                } else if (connectCount == 2) {
                    priority = LIVE_TWO;
                } else if (connectCount == 1) {
                    priority = LIVE_ONE;
                }
            } else {
                //open one side
                if (connectCount == 4) {
                    priority = DEAD_FOUR;
                } else if (connectCount == 3) {
                    priority = DEAD_THREE;
                } else if (connectCount == 2) {
                    priority = DEAD_TWO;
                } else if (connectCount == 1) {
                    priority = DEAD_ONE;
                }
            }
        }
        return priority;
    }

}
