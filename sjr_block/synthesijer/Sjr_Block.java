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

import synthesijer.rt.*;

public class Sjr_Block
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
  private final HexLED hexLED = new HexLED();
  private final RedLED redLED = new RedLED();
  private final BoardSwitch boardSW = new BoardSwitch();
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
  // Sequencer
  private final int[] seqTime = new int[64];
  private final int[] seqDuration = new int[64];
  private final int[] seqChannel = new int[64];
  private final int[] seqNote = new int[64];
  private final int[] seqOctave = new int[64];
  private int seqStartSample;
  private int seqPointer = 0;
  private int seqMusicLength;


  private int rand()
  {
    int r = random;
    r = r ^ (r << 13);
    r = r ^ (r >>> 17);
    r = r ^ (r << 5);
    random = r;
    return r;
  }

  private void resetMusic(int pointer, int length)
  {
    seqStartSample = psg.sample;
    seqPointer = pointer;
    seqMusicLength = length;
  }

  private boolean sequencerRun()
  {
    int seqSample = psg.sample - seqStartSample;

    if (seqSample > seqMusicLength)
    {
      return true;
    }
    else if (seqSample > seqTime[seqPointer])
    {
      // play a note
      psg.setParameter(seqChannel[seqPointer], seqNote[seqPointer], seqOctave[seqPointer], 0, 6144, 6144, seqDuration[seqPointer], 1000);
      psg.play(seqChannel[seqPointer]);
      seqPointer++;
    }
    return false;
  }

  private void setSeqData(int id, int time, int duration, int channel, int note, int octave)
  {
    seqTime[id] = time;
    seqDuration[id] = duration;
    seqChannel[id] = channel;
    seqNote[id] = note;
    seqOctave[id] = octave;
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

  private void init()
  {
    psg.start();
    vram.offset_h = 0;
    vram.offset_v = 0;

    // music data
    // メインBGM
    setSeqData( 0,      0,  9000, 0,  0, 4);
    setSeqData( 1,  10000,  9000, 0,  2, 4);
    setSeqData( 2,  20000,  9000, 0,  4, 4);
    setSeqData( 3,  30000,  9000, 0,  5, 4);
    setSeqData( 4,  40000, 19000, 0,  7, 4);
    setSeqData( 5,  60000, 19000, 0,  7, 4);
    setSeqData( 6,  80000,  9000, 0,  7, 4);
    setSeqData( 7,  90000,  9000, 0,  5, 4);
    setSeqData( 8, 100000,  9000, 0,  4, 4);
    setSeqData( 9, 110000,  9000, 0,  2, 4);
    setSeqData(10, 120000, 29000, 0,  4, 4);
    setSeqData(11, 160000,  9000, 0,  0, 4);
    setSeqData(12, 170000,  9000, 0,  2, 4);
    setSeqData(13, 180000,  9000, 0,  4, 4);
    setSeqData(14, 190000,  9000, 0,  5, 4);
    setSeqData(15, 200000, 19000, 0,  7, 4);
    setSeqData(16, 220000, 19000, 0,  7, 4);
    setSeqData(17, 240000,  9000, 0,  7, 4);
    setSeqData(18, 250000,  9000, 0,  5, 4);
    setSeqData(19, 260000,  9000, 0,  4, 4);
    setSeqData(20, 270000,  9000, 0,  2, 4);
    setSeqData(21, 280000, 29000, 0,  0, 4);
    setSeqData(22, 320000,  9000, 0,  0, 5);
    setSeqData(23, 330000,  9000, 0, 11, 4);
    setSeqData(24, 340000,  9000, 0,  9, 4);
    setSeqData(25, 350000,  9000, 0,  7, 4);
    setSeqData(26, 360000,  9000, 0,  0, 5);
    setSeqData(27, 370000,  9000, 0, 11, 4);
    setSeqData(28, 380000,  9000, 0,  9, 4);
    setSeqData(29, 390000,  9000, 0,  7, 4);
    setSeqData(30, 400000,  9000, 0,  5, 4);
    setSeqData(31, 410000,  9000, 0,  4, 4);
    setSeqData(32, 420000,  9000, 0,  2, 4);
    setSeqData(33, 430000,  9000, 0,  0, 4);
    setSeqData(34, 440000, 29000, 0,  7, 4);
    setSeqData(35, 480000,  9000, 0,  0, 5);
    setSeqData(36, 490000,  9000, 0, 11, 4);
    setSeqData(37, 500000,  9000, 0,  9, 4);
    setSeqData(38, 510000,  9000, 0,  7, 4);
    setSeqData(39, 520000,  9000, 0,  0, 5);
    setSeqData(40, 530000,  9000, 0, 11, 4);
    setSeqData(41, 540000,  9000, 0,  9, 4);
    setSeqData(42, 550000,  9000, 0,  7, 4);
    setSeqData(43, 560000,  9000, 0,  5, 4);
    setSeqData(44, 570000,  9000, 0,  4, 4);
    setSeqData(45, 580000,  9000, 0,  2, 4);
    setSeqData(46, 590000,  9000, 0,  7, 4);
    setSeqData(47, 600000, 29000, 0,  0, 4);
    setSeqData(48, 999999,     0, 0,  0, 4);
    // クリアファンファーレ
    setSeqData(49,      0,     0, 0,  0, 4);
    setSeqData(50,  40000,  9000, 0,  0, 4);
    setSeqData(51,  50000,  9000, 0,  2, 4);
    setSeqData(52,  60000,  9000, 0,  4, 4);
    setSeqData(53,  70000,  9000, 0,  5, 4);
    setSeqData(54,  80000,  9000, 0,  7, 4);
    setSeqData(55,  90000,  9000, 0,  9, 4);
    setSeqData(56, 100000,  9000, 0, 11, 4);
    setSeqData(57, 110000,  9000, 0,  7, 4);
    setSeqData(58, 120000, 19000, 0,  0, 5);
    setSeqData(59, 140000, 19000, 0,  0, 5);
    setSeqData(60, 160000, 19000, 0,  0, 5);
    setSeqData(61, 999999,     0, 0,  0, 5);
    // sound effect settings
    psg.setParameter(1, 0, 6, -8000, 4096, 8192, 100010, 100000);
    psg.setParameter(2, 0, 2, 600, 6144, 6144, 30010, 30000);
    psg.setParameter(3, 0, 4, -100, 8192, 4096, 96000, 10000);

    // clear screen
    fillRect(0, 0, VRAM_WIDTH, VRAM_HEIGHT, WALL_COLOR);
  }

  private void gameStart()
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

    // init sequencer
    resetMusic(0, 640000);

    stageState = STATE_PLAYING;
  }

  private void stageClear()
  {
    stageScore++;
    // draw stage score
    if (stageScore <= 30)
    {
      fillRect(32, 30 - stageScore, 8, 1, (byte)(stageScore + 225));
    }
    resetMusic(49, 240000);
    while (!sequencerRun())
    {
    }
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

  @auto
  public void main()
  {
    init();

    while (true)
    {
      // vsync wait
      while (vram.vsync == true)
      {
      }
      while (vram.vsync == false)
      {
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
          fillRect(32, 0, 8, VRAM_HEIGHT, (byte)0);
          gameStart();
        }
      }
      else if (stageState == STATE_PLAYING)
      {
        // シーケンサを実行して曲が終わったら曲の最初からリピートする
        if (sequencerRun())
        {
          resetMusic(0, 640000);
        }

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

      redLED.data = boardSW.data;
      hexLED.data = stageScore;
      sleepCount++;
      if (sleepCount == gameSpeed)
      {
        sleepCount = 0;
      }
    }
  }
}
