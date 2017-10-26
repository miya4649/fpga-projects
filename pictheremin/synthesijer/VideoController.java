/*
  Copyright (c) 2017, miya
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
  private static final int WIDTH = 640;
  private static final int HEIGHT = 480;
  private static final int SCREEN_SIZE_BITS = 6;
  private static final int SCREEN_SIZE = (1 << SCREEN_SIZE_BITS);
  private static final int SPRITE_SIZE_BITS = 6;
  private static final int SPRITE_SIZE_BITS_X2 = (SPRITE_SIZE_BITS << 1);
  private static final int SPRITE_SIZE = (1 << SPRITE_SIZE_BITS);
  private static final int SPRITE_SIZE_M1 = (SPRITE_SIZE - 1);
  private static final int SPRITE_SIZE2 = (1 << SPRITE_SIZE_BITS_X2);
  private static final int PAINT_SIZE_BITS = 7;
  private static final int PAINT_SIZE_BITS_X2 = (PAINT_SIZE_BITS << 1);
  private static final int PAINT_SIZE2 = (1 << PAINT_SIZE_BITS_X2);
  private static final int PARTICLES_BITS = 5;
  private static final int PARTICLES = (1 << PARTICLES_BITS);

  private final byte HEX_0 = (byte)('0' - 32);
  private final byte HEX_1 = (byte)('1' - 32);
  private final byte HEX_2 = (byte)('2' - 32);
  private final byte HEX_3 = (byte)('3' - 32);
  private final byte HEX_4 = (byte)('4' - 32);
  private final byte HEX_5 = (byte)('5' - 32);
  private final byte HEX_6 = (byte)('6' - 32);
  private final byte HEX_7 = (byte)('7' - 32);
  private final byte HEX_8 = (byte)('8' - 32);
  private final byte HEX_9 = (byte)('9' - 32);
  private final byte HEX_A = (byte)('A' - 32);
  private final byte HEX_B = (byte)('B' - 32);
  private final byte HEX_C = (byte)('C' - 32);
  private final byte HEX_D = (byte)('D' - 32);
  private final byte HEX_E = (byte)('E' - 32);
  private final byte HEX_F = (byte)('F' - 32);

  private final VgaIface vga = new VgaIface();
  private final Sprite sp_cursor = new Sprite("SPRITE_SIZE_BITS", "6");
  private final Sprite sp_paint = new Sprite("SPRITE_SIZE_BITS", "7");
  private final Sprite sp_particle = new Sprite("SPRITE_SIZE_BITS", "7");
  private final ChrBG bg0 = new ChrBG("CHR_SIZE_BITS", "6", "BITMAP_BITS", "1");
  private final ROMdivisionsdata divdata = new ROMdivisionsdata();
  private final ROMchr chr = new ROMchr();

  private final int[] par_x = new int[PARTICLES];
  private final int[] par_y = new int[PARTICLES];
  private final int[] par_dx = new int[PARTICLES];
  private final int[] par_dy = new int[PARTICLES];
  private final int[] par_col = new int[PARTICLES];

  private int cursor_x = 0;
  private int cursor_y = 0;
  private int key = 0;
  private int chr_ptr = 0;
  private int par_time = 0;

  private int random = -59634649;

  public final int[] debug = new int[16];

  private int rand()
  {
    int r = random;
    r = r ^ (r << 13);
    r = r ^ (r >>> 17);
    r = r ^ (r << 5);
    random = r;
    return r;
  }

  private int min(int x, int y)
  {
    if (x > y)
    {
      return y;
    }
    else
    {
      return x;
    }
  }

  private int max(int x, int y)
  {
    if (x > y)
    {
      return x;
    }
    else
    {
      return y;
    }
  }

  private int hexConv(int value, int col)
  {
    byte c = (byte)((value >>> (col << 2)) & 0xf);
    int hex = '0';
    switch (c)
    {
      case 0x0: hex = HEX_0; break;
      case 0x1: hex = HEX_1; break;
      case 0x2: hex = HEX_2; break;
      case 0x3: hex = HEX_3; break;
      case 0x4: hex = HEX_4; break;
      case 0x5: hex = HEX_5; break;
      case 0x6: hex = HEX_6; break;
      case 0x7: hex = HEX_7; break;
      case 0x8: hex = HEX_8; break;
      case 0x9: hex = HEX_9; break;
      case 0xa: hex = HEX_A; break;
      case 0xb: hex = HEX_B; break;
      case 0xc: hex = HEX_C; break;
      case 0xd: hex = HEX_D; break;
      case 0xe: hex = HEX_E; break;
      case 0xf: hex = HEX_F; break;
    }
    return hex;
  }

  private void printValue(int value, int start_digit, int length, int color)
  {
    int i = start_digit;
    int j = start_digit - length;
    while (i > j)
    {
      bg0.chr[chr_ptr] = (byte)(color | hexConv(value, i));
      i--;
      chr_ptr++;
    }
  }

  public int getChrPos(int x, int y)
  {
    return x + (y << SCREEN_SIZE_BITS);
  }

  public int getPaintPos(int x, int y)
  {
    return (x >> 3) + ((y >> 3) << PAINT_SIZE_BITS);
  }

  public void setParam(int x, int y, int k)
  {
    cursor_x = x;
    cursor_y = y;
    key = k;
  }

  private void clear_screen()
  {
    for (int i = 0; i < PAINT_SIZE2; i++)
    {
      sp_paint.bitmap[i] = (byte)0;
    }
  }

  private void init()
  {
    sp_cursor.x = 0;
    sp_cursor.y = 0;
    sp_cursor.scale = 8;
    sp_paint.x = 0;
    sp_paint.y = 0;
    sp_paint.scale = 5;
    sp_particle.x = 0;
    sp_particle.y = 0;
    sp_particle.scale = 5;
    bg0.x = 0;
    bg0.y = 0;
    bg0.scale = 7;
    bg0.palette0 = 0xffff8300;
    bg0.palette1 = 0xffff1300;
    bg0.palette2 = 0xffff4800;
    bg0.palette3 = 0xffff6d00;

    for (int i = 0; i < 4096; i++)
    {
      bg0.bitmap[i] = chr.data[i];
    }

    // draw keybaord
    for (int i = 0; i < SCREEN_SIZE; i++)
    {
      for (int j = 0; j < SCREEN_SIZE; j++)
      {
        bg0.chr[(i << SCREEN_SIZE_BITS) + j] = divdata.data[j];
      }
    }

    for (int i = 0; i < PAINT_SIZE2; i++)
    {
      sp_particle.bitmap[i] = (byte)0;
    }

    for (int i = 0; i < SPRITE_SIZE2; i++)
    {
      sp_cursor.bitmap[i] = (byte)0;
    }

    for (int i = 0; i < SPRITE_SIZE_M1; i++)
    {
      int x = i + (((SPRITE_SIZE >> 1) - 1) << SPRITE_SIZE_BITS);
      int y = ((SPRITE_SIZE >> 1) - 1) + (i << SPRITE_SIZE_BITS);
      sp_cursor.bitmap[x] = (byte)255;
      sp_cursor.bitmap[y] = (byte)255;
    }

    for (int i = 0; i < PARTICLES; i++)
    {
      par_x[i] = 0;
      par_y[i] = 0;
      par_dx[i] = 0;
      par_dy[i] = 0;
      par_col[i] = (byte)0;
    }
  }

  public void run()
  {
    init();
    while (true)
    {
      // vsync wait
      while (vga.vsync == true)
      {
      }
      while (vga.vsync == false)
      {
      }

      // cursor
      sp_cursor.x = cursor_x - 31;
      sp_cursor.y = cursor_y - 31;

      // debug monitor
      chr_ptr = getChrPos(0, 29);
      for (int i = 0; i < 4; i++)
      {
        printValue(debug[i], 7, 8, 192);
        bg0.chr[chr_ptr] = (byte)192;
        chr_ptr++;
      }

      // paint
      if ((key & 1) == 1)
      {
        sp_paint.bitmap[getPaintPos(cursor_x, cursor_y)] = (byte)0x92;
      }

      if ((key & 2) == 2)
      {
        clear_screen();
      }

      // particles
      for (int i = 0; i < PARTICLES; i++)
      {
        int par_nx = par_x[i] + par_dx[i];
        int par_ny = par_y[i] + par_dy[i];
        int colr = (par_col[i] >>> 16) - 8;
        int colg = ((par_col[i] >>> 8) & 0xff) - 8;
        int colb = (par_col[i] & 0xff) - 8;
        colr = max(colr, 0);
        colg = max(colg, 0);
        colb = max(colb, 0);
        int par_ncol = (colr << 16) | (colg << 8) | colb;

        if (par_time == i)
        {
          par_nx = cursor_x;
          par_ny = cursor_y;
          int par_ndx = rand() & 7;
          int par_ndy = rand() & 7;
          int sw_dir = rand() & 3;
          switch (sw_dir)
          {
            case 0: par_ndx = -par_ndx; break;
            case 1: par_ndy = -par_ndy; break;
            case 2: par_ndx = -par_ndx; par_ndy = -par_ndy; break;
            default: break;
          }
          par_dx[i] = par_ndx;
          par_dy[i] = par_ndy;
          switch (rand() & 7)
          {
            case 0: par_ncol = 0x000000ff; break;
            case 1: par_ncol = 0x0000ff00; break;
            case 2: par_ncol = 0x0000ffff; break;
            case 3: par_ncol = 0x00ff0000; break;
            case 4: par_ncol = 0x00ff00ff; break;
            case 5: par_ncol = 0x00ffff00; break;
            case 6: par_ncol = 0x00ffffff; break;
            case 7: par_ncol = 0x00ffffff; break;
            default: break;
          }
        }

        byte par_ncol8 = (byte)(((par_ncol >>> 16) & 0xe0) | ((par_ncol >>> 11) & 0x1c) | ((par_ncol >>> 6) & 3));

        sp_particle.bitmap[getPaintPos(par_nx, par_ny)] = par_ncol8;
        sp_particle.bitmap[getPaintPos(par_x[i], par_y[i])] = (byte)0;
        par_x[i] = par_nx;
        par_y[i] = par_ny;
        par_col[i] = par_ncol;
      }
      par_time++;
      if (par_time > PARTICLES)
      {
        par_time = 0;
      }
    }
  }
}
