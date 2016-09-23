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

public class VideoController extends Thread
{
  private final VgaIface vga = new VgaIface();
  private final Sprite sprite0 = new Sprite();
  private final Sprite sprite1 = new Sprite();
  private final Sprite sprite2 = new Sprite();
  private final Sprite sprite3 = new Sprite();
  private final Sprite sprite4 = new Sprite();
  private final Sprite sprite5 = new Sprite();
  private final Sprite sprite6 = new Sprite();
  private final SPThread1 spt0 = new SPThread1();
  private final BGThread bgt0 = new BGThread();

  private static final int LAYERS = 9;

  private final int[] x = new int[LAYERS];
  private final int[] y = new int[LAYERS];
  private final int[] x1 = new int[LAYERS];
  private final int[] y1 = new int[LAYERS];
  private final int[] dx = new int[LAYERS];
  private final int[] dy = new int[LAYERS];
  private final int[] scale = new int[LAYERS];
  private final int[] scale_d = new int[LAYERS];
  private final int[] scale_time = new int[LAYERS];
  private final int[] scale_max = new int[LAYERS];
  private final int[] offset = new int[LAYERS];

  private int random = -59634649;

  private int rand()
  {
    int r = random;
    r = r ^ (r << 13);
    r = r ^ (r >>> 17);
    r = r ^ (r << 5);
    random = r;
    return r;
  }

  public void run()
  {
    spt0.start();
    bgt0.start();

    for (int i = 0; i < 64 * 64; i++)
    {
      sprite0.bitmap[i] = (byte)0;
      sprite1.bitmap[i] = (byte)0;
      sprite2.bitmap[i] = (byte)0;
      sprite3.bitmap[i] = (byte)0;
      sprite4.bitmap[i] = (byte)0;
      sprite5.bitmap[i] = (byte)0;
      sprite6.bitmap[i] = (byte)0;
    }

    for (int i = 0; i < 64; i++)
    {
      int p0 = (i << 6) + i;
      int p1 = (i << 6) + (63 - i);
      sprite0.bitmap[p0] = (byte)(255 - i);
      sprite0.bitmap[p1] = (byte)(255 - i);
      sprite1.bitmap[p0] = (byte)(245 - i);
      sprite1.bitmap[p1] = (byte)(245 - i);
      sprite2.bitmap[p0] = (byte)(235 - i);
      sprite2.bitmap[p1] = (byte)(235 - i);
      sprite3.bitmap[p0] = (byte)(225 - i);
      sprite3.bitmap[p1] = (byte)(225 - i);
      sprite4.bitmap[p0] = (byte)(215 - i);
      sprite4.bitmap[p1] = (byte)(215 - i);
      sprite5.bitmap[p0] = (byte)(205 - i);
      sprite5.bitmap[p1] = (byte)(205 - i);
      sprite6.bitmap[p0] = (byte)(195 - i);
      sprite6.bitmap[p1] = (byte)(195 - i);
    }

    for (int i = 0; i < LAYERS; i++)
    {
      x[i] = rand() & 1023;
      y[i] = rand() & 1023;
      dx[i] = i + 4;
      dy[i] = i + 4;
      scale[i] = 4;
      scale_time[i] = 0;
      scale_max[i] = (LAYERS - i) << 2;
      scale_d[i] = 1;
    }

    int j = 0;
    int frame = 0;

    while (true)
    {
      // vsync wait
      while (vga.vsync == true)
      {
      }
      while (vga.vsync == false)
      {
      }

      sprite0.x = x[2] - offset[2];
      sprite0.y = y[2] - offset[2];
      sprite0.scale = scale[2];
      sprite1.x = x[3] - offset[3];
      sprite1.y = y[3] - offset[3];
      sprite1.scale = scale[3];
      sprite2.x = x[4] - offset[4];
      sprite2.y = y[4] - offset[4];
      sprite2.scale = scale[4];
      sprite3.x = x[5] - offset[5];
      sprite3.y = y[5] - offset[5];
      sprite3.scale = scale[5];
      sprite4.x = x[6] - offset[6];
      sprite4.y = y[6] - offset[6];
      sprite4.scale = scale[6];
      sprite5.x = x[7] - offset[7];
      sprite5.y = y[7] - offset[7];
      sprite5.scale = scale[7];
      sprite6.x = x[8] - offset[8];
      sprite6.y = y[8] - offset[8];
      sprite6.scale = scale[8];

      frame++;
      bgt0.setParam(x[0], y[0], offset[0], scale[0], frame);
      spt0.setParam(x[1], y[1], offset[1], scale[1], frame);

      for (int i = 0; i < LAYERS; i++)
      {
        x1[i] += dx[i];
        if ((x1[i] < 0) || (x1[i] > 2560))
        {
          dx[i] = -dx[i];
          x1[i] += dx[i];
        }
        y1[i] += dy[i];
        if ((y1[i] < 0) || (y1[i] > 1920))
        {
          dy[i] = -dy[i];
          y1[i] += dy[i];
        }
        x[i] = x1[i] >> 2;
        y[i] = y1[i] >> 2;

        scale_time[i]++;
        if (scale_time[i] > scale_max[i])
        {
          scale_time[i] = 0;
          if (scale[i] < 4)
          {
            scale_d[i] = 1;
          }
          if (scale[i] > 7)
          {
            scale_d[i] = -1;
          }
          scale[i] += scale_d[i];
        }

        offset[i] = (64 << 7) >>> scale[i];
      }
    }
  }
}
