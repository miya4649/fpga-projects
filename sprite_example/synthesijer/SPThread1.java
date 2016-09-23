/*
  Copyright (c) 2016, miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import synthesijer.rt.*;

public class SPThread1 extends Thread
{
  private static final int SCREEN_SIZE_BITS = 6;
  private static final int SCREEN_SIZE_BITS_X2 = (SCREEN_SIZE_BITS << 1);
  private static final int SCREEN_SIZE = (1 << SCREEN_SIZE_BITS);
  private static final int SCREEN_SIZE_M1 = (SCREEN_SIZE - 1);
  private static final int SCREEN_SIZE2 = (1 << SCREEN_SIZE_BITS_X2);
  private static final int SNAKES = 4;
  private static final int TAIL_LENGTH_BITS = 4;
  private static final int TAIL_LENGTH = (1 << TAIL_LENGTH_BITS);
  private static final int TAIL_LENGTH_M1 = (TAIL_LENGTH - 1);
  private static final int TAIL_SIZE = (SNAKES * TAIL_LENGTH);
  private int tailX[] = new int[TAIL_SIZE];
  private int tailY[] = new int[TAIL_SIZE];
  private final Sprite sprite = new Sprite();

  private int random = -59634649;
  private int frame;

  private int x[] = new int[SNAKES];
  private int y[] = new int[SNAKES];
  private int dir[] = new int[SNAKES];
  private int count;
  private int dx[] = new int[4];
  private int dy[] = new int[4];

  private int rand()
  {
    int r = random;
    r = r ^ (r << 13);
    r = r ^ (r >>> 17);
    r = r ^ (r << 5);
    random = r;
    return r;
  }

  public void init()
  {
    sprite.x = 0;
    sprite.y = 0;
    sprite.scale = 4;
    count = 0;
    dx[0] = 0;
    dx[1] = 1;
    dx[2] = 0;
    dx[3] = -1;
    dy[0] = -1;
    dy[1] = 0;
    dy[2] = 1;
    dy[3] = 0;
    for (int i = 0; i < SNAKES; i++)
    {
      x[i] = 8;
      y[i] = 8;
      dir[i] = 0;
    }
    for (int i = 0; i < TAIL_SIZE; i++)
    {
      tailX[i] = 0;
      tailX[i] = 0;
    }
    for (int i = 0; i < SCREEN_SIZE2; i++)
    {
      sprite.bitmap[i] = (byte)0;
    }
  }

  private int get_index(int x, int y)
  {
    return (x & SCREEN_SIZE_M1) + ((y & SCREEN_SIZE_M1) << SCREEN_SIZE_BITS);
  }

  public void setParam(int x, int y, int offset, int scale, int f)
  {
    //sprite.x = x - offset;
    //sprite.y = y - offset;
    //sprite.scale = scale;
    frame = f;
  }

  private void drawSnake()
  {
    for (int i = 0; i < SNAKES; i++)
    {
      if (rand() > 1610612736)
      {
        dir[i] = rand() & 3;
      }

      x[i] += dx[dir[i]];
      y[i] += dy[dir[i]];
      if ((x[i] < 0) || (x[i] > 39) || (y[i] < 0) || (y[i] > 29))
      {
        dir[i] += 2;
        dir[i] = dir[i] & 3;
        x[i] += dx[dir[i]] << 1;
        y[i] += dy[dir[i]] << 1;
      }
      int c1 = (i << TAIL_LENGTH_BITS) + (count & TAIL_LENGTH_M1);
      tailX[c1] = x[i];
      tailY[c1] = y[i];
      sprite.bitmap[get_index(tailX[c1], tailY[c1])] = (byte)((count + (i << 6)) & 255);
      c1 = (i << TAIL_LENGTH_BITS) + ((count + 1) & TAIL_LENGTH_M1);
      sprite.bitmap[get_index(tailX[c1], tailY[c1])] = (byte)0;
    }
    count++;
  }

  public void run()
  {
    init();
    int prev = 0;
    while (true)
    {
      if (frame != prev)
      {
        prev = frame;
        drawSnake();
      }
    }
  }
}
