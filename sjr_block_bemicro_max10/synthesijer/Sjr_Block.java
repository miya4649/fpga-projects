/*
  Copyright (c) 2015, miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

public class Sjr_Block extends Thread
{
  private static final int LINE_WIDTH = 64;
  private static final int STAGE_WIDTH = 32;
  private static final int VRAM_WIDTH = 40;
  private static final int VRAM_HEIGHT = 30;
  private static final byte BALL_COLOR = (byte)255;
  private static final byte WALL_COLOR = (byte)109;
  private static final byte NONE_COLOR = (byte)0;
  private static final int BALL_NUM = 4;
  private static final int BLOCK_NUM = 30;
  private static final int STATE_GAMEOVER = 0;
  private static final int STATE_PLAYING = 1;
  private static final int STATE_CLEAR = 2;
  private static final int BOARD_KEY0 = 1;
  private static final int BOARD_KEY1 = 2;
  private static final int BOARD_KEY2 = 4;
  private static final int BOARD_KEY3 = 8;
  private static final int DEFAULT_GAMESPEED = 4;
  private static final int DEFAULT_PADDLESIZE = 5;
  private static final int MIN_GAMESPEED = 1;
  private static final int MIN_PADDLESIZE = 3;
  private static final int SHOTBALLTIME = 45;
  private int random = -59634649;
  private final VgaVram8 vram = new VgaVram8();
  private final SoundGenerator psg = new SoundGenerator();
  private final BoardKey boardKey = new BoardKey();
  // Game
  private final byte[] blockColor = new byte[120];
  private final boolean[] blockEnable = new boolean[120];
  private final boolean[] blockWall = new boolean[120];
  private final int[] ballX = new int[4];
  private final int[] ballY = new int[4];
  private final int[] ballDx = new int[4];
  private final int[] ballDy = new int[4];
  private final boolean[] ballEnable = new boolean[4];
  private int paddleX = 16;
  private int paddleY = 28;
  private int paddleSize;
  private int gameSpeed;
  private int ballCount = 4;
  private int stageState = 0;
  private int ballOnStage = 0;
  private int shotBallTimer = 0;
  private int score = 0;
  private int stageScore = 0;
  private int sleepCount = 0;

  private int rand()
  {
    int r = random;
    r = r ^ (r << 13);
    r = r ^ (r >>> 17);
    r = r ^ (r << 5);
    random = r;
    return r;
  }

  private void initBall(int id, int x, int y, int dx, int dy)
  {
    ballX[id] = x;
    ballY[id] = y;
    ballDx[id] = dx;
    ballDy[id] = dy;
    ballEnable[id] = false;
  }

  private void paddleRun()
  {
    int newPaddleX = paddleX;
    if ((boardKey.data & BOARD_KEY0) == BOARD_KEY0)
    {
      newPaddleX = paddleX + 2;
      if (newPaddleX + paddleSize > 27)
      {
        newPaddleX = 27 - paddleSize;
      }
      fillRect(paddleX - paddleSize, paddleY, newPaddleX - paddleX, 1, NONE_COLOR);
    }
    else if ((boardKey.data & BOARD_KEY3) == BOARD_KEY3)
    {
      newPaddleX = paddleX - 2;
      if (newPaddleX - paddleSize < 4)
      {
        newPaddleX = 4 + paddleSize;
      }
      fillRect(newPaddleX + paddleSize + 1, paddleY, paddleX - newPaddleX, 1, NONE_COLOR);
    }

    fillRect(newPaddleX - paddleSize, paddleY, (paddleSize << 1) + 1, 1, (byte)255);
    paddleX = newPaddleX;
  }

  private void shotBall()
  {
    if (shotBallTimer > SHOTBALLTIME)
    {
      shotBallTimer = 0;
      if (ballOnStage < BALL_NUM)
      {
        int dx;
        if ((rand() & 1) == 0)
        {
          dx = 1;
        }
        else
        {
          dx = -1;
        }
        initBall(ballOnStage, paddleX + (rand() & 3) - 2, paddleY - 1, dx, -1);
        ballEnable[ballOnStage] = true;
        ballOnStage++;
      }
    }
    else
    {
      shotBallTimer++;
    }
  }

  private boolean blockCollision(int ballID, boolean xNext, boolean yNext)
  {
    int x;
    int y;
    if (xNext)
    {
      x = ballX[ballID] + ballDx[ballID];
    }
    else
    {
      x = ballX[ballID];
    }

    if (yNext)
    {
      y = ballY[ballID] + ballDy[ballID];
    }
    else
    {
      y = ballY[ballID];
    }

    int blockX = x >> 2;
    int blockY = y >> 1;
    int blockID = blockX | (blockY << 3);
    boolean hit;
    if (blockEnable[blockID])
    {
      hit = true;
      psg.play(1);
      if (blockWall[blockID] == false)
      {
        // 衝突したブロックを消す
        blockEnable[blockID] = false;
        drawBlock(blockID);
        score++;
        if (score == BLOCK_NUM)
        {
          stageState = STATE_CLEAR;
        }
      }
    }
    else
    {
      hit = false;
    }
    return hit;
  }

  private void ballRun()
  {
    int i = 0;
    while (i < BALL_NUM)
    {
      if (ballEnable[i] == true)
      {
        boolean hitx = false;
        boolean hity = false;
        // y方向に進めて衝突判定
        if (blockCollision(i, false, true))
        {
          hity = true;
        }
        // x方向に進めて衝突判定
        if (blockCollision(i, true, false))
        {
          hitx = true;
        }
        // x,y方向に進めて衝突判定
        if ((!hitx) && (!hity) && (blockCollision(i, true, true)))
        {
          hitx = true;
          hity = true;
        }

        if (hitx)
        {
          int dxtmp = 0 - ballDx[i];
          ballDx[i] = dxtmp;
        }

        if (hity)
        {
          int dytmp = 0 - ballDy[i];
          ballDy[i] = dytmp;
        }

        int nx = ballX[i] + ballDx[i];
        int ny = ballY[i] + ballDy[i];
        // paddle collision
        if ((ny == paddleY) && (nx >= paddleX - paddleSize) && (nx <= paddleX + paddleSize))
        {
          psg.play(2);
          int dytmp = 0 - ballDy[i];
          ballDy[i] = dytmp;
          ny = ballY[i] + ballDy[i];
        }

        vram.data[nx + (ny << 6 /*LINE_WIDTH_IN_BITS*/)] = BALL_COLOR;
        vram.data[ballX[i] + (ballY[i] << 6 /*LINE_WIDTH_IN_BITS*/)] = NONE_COLOR;
        ballX[i] = nx;
        ballY[i] = ny;

        // out
        if (ny > paddleY)
        {
          psg.play(3);
          ballEnable[i] = false;
          ballCount--;
          if (ballCount == 0)
          {
            stageState = STATE_GAMEOVER;
          }
        }
      }
      i++;
    }
  }

  private void fillRect(int x, int y, int width, int height, byte color)
  {
    int iy = y << 6 /*LINE_WIDTH_IN_BITS*/;
    int iye = (y + height) << 6 /*LINE_WIDTH_IN_BITS*/;
    int ixe = x + width;
    while (iy < iye)
    {
      int ix = x;
      while (ix < ixe)
      {
        vram.data[ix + iy] = color;
        ix++;
      }
      iy += LINE_WIDTH;
    }
  }

  private void drawBlock(int id)
  {
    byte color;
    if (blockEnable[id] == true)
    {
      color = blockColor[id];
    }
    else
    {
      color = NONE_COLOR;
    }
    int x = id & 7;
    int y = id >>> 3;
    fillRect(x << 2, y << 1, 4, 2, color);
  }

  public void init()
  {
    psg.start();
    vram.offset_h = 0;
    vram.offset_v = 0;

    // sound effect settings
    psg.setParameter(1, 0, 6, -8000, 4096, 8192, 100010, 100000);
    psg.setParameter(2, 0, 2, 600, 6144, 6144, 30010, 30000);
    psg.setParameter(3, 0, 4, -100, 8192, 4096, 96000, 10000);

    // clear screen
    fillRect(0, 0, VRAM_WIDTH, VRAM_HEIGHT, WALL_COLOR);
  }

  public void gameStart()
  {
    // clear stage screen
    fillRect(0, 0, 32, VRAM_HEIGHT, (byte)0);

    // init blocks
    for (int y = 0; y < 15; y++)
    {
      for (int x = 0; x < 8; x++)
      {
        int bid = x | (y << 3);
        if ((x == 0) || (x == 7) || (y == 0))
        {
          blockEnable[bid] = true;
          blockColor[bid] = WALL_COLOR;
          blockWall[bid] = true;
        }
        else
        {
          blockColor[bid] = (byte)(128 + bid);
          blockWall[bid] = false;
          if ((y > 2) && (y < 8))
          {
            blockEnable[bid] = true;
          }
          else
          {
            blockEnable[bid] = false;
          }
        }
        drawBlock(bid);
      }
    }

    // init balls
    for (int i = 0; i < 4; i++)
    {
      initBall(i, 19, 26, -1, -1);
    }

    shotBallTimer = 0;
    ballOnStage = 0;
    ballCount = BALL_NUM;
    score = 0;
    paddleX = 16;
    sleepCount = 0;

    stageState = STATE_PLAYING;
  }

  public void stageClear()
  {
    stageScore++;
    // draw stage score
    paddleSize--;
    if (paddleSize < MIN_PADDLESIZE)
    {
      paddleSize = DEFAULT_PADDLESIZE;
      gameSpeed--;
    }
    if (gameSpeed < MIN_GAMESPEED)
    {
      gameSpeed = MIN_GAMESPEED;
    }
    gameStart();
  }

  public void run()
  {
    init();

    while (true)
    {
      // vsync wait
      while (vram.vsync == true)
      {
        yield();
      }
      while (vram.vsync == false)
      {
        yield();
      }

      if (stageState == STATE_GAMEOVER)
      {
        gameSpeed = DEFAULT_GAMESPEED;
        paddleSize = DEFAULT_PADDLESIZE;
        rand();
        if ((boardKey.data & BOARD_KEY1) == BOARD_KEY1)
        {
          // clear score
          stageScore = 0;
          gameStart();
        }
      }
      else if (stageState == STATE_PLAYING)
      {
        if (sleepCount == 0)
        {
          paddleRun();
          ballRun();
          shotBall();
        }
      }
      else if (stageState == STATE_CLEAR)
      {
        stageClear();
      }

      sleepCount++;
      if (sleepCount == gameSpeed)
      {
        sleepCount = 0;
      }
    }
  }
}
