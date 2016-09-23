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

public class BGThread extends Thread
{
  private static final int SCREEN_SIZE_BITS = 6;
  private static final int SCREEN_SIZE_BITS_X2 = (SCREEN_SIZE_BITS << 1);
  private static final int SCREEN_SIZE = (1 << SCREEN_SIZE_BITS);
  private static final int SCREEN_SIZE_M1 = (SCREEN_SIZE - 1);
  private static final int SCREEN_SIZE2 = (1 << SCREEN_SIZE_BITS_X2);
  private static final int CELL_SIZE = (SCREEN_SIZE2 << 1);
  private byte cell[] = new byte[CELL_SIZE];
  private final ChrBG bg0 = new ChrBG();

  private int random = -59634649;
  private int frame;

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
    bg0.palette0 = 0xffffe000;
    bg0.palette1 = 0xffff1c00;
    bg0.palette2 = 0xffff0300;
    bg0.palette3 = 0xffffff00;

    for (int i = 0; i < CELL_SIZE; i++)
    {
      cell[i] = (byte)0;
    }
  }

  private int get_index(int x, int y, int page)
  {
    return (x & SCREEN_SIZE_M1) + ((y & SCREEN_SIZE_M1) << SCREEN_SIZE_BITS) + (page << SCREEN_SIZE_BITS_X2);
  }

  private byte get_cell(int x, int y, int page)
  {
    return cell[get_index(x, y, page)];
  }

  private void set_cell(int x, int y, int page, byte value)
  {
    cell[get_index(x, y, page)] = value;
  }

  private int get_neighbor(int x, int y, int p)
  {
    int neighbor =
      get_cell(x-1,y-1,p)+get_cell(x,y-1,p)+get_cell(x+1,y-1,p)+
      get_cell(x-1,y,p)  +                  get_cell(x+1,y,p)+
      get_cell(x-1,y+1,p)+get_cell(x,y+1,p)+get_cell(x+1,y+1,p);
    return neighbor;
  }

  private void calc_cells(int page)
  {
    int back_page = page ^ 1;
    for (int y = 0; y < SCREEN_SIZE; y++)
    {
      for (int x = 0; x < SCREEN_SIZE; x++)
      {
        byte me = get_cell(x, y, back_page);
        set_cell(x, y, page, me);
        int neighbor = get_neighbor(x, y, back_page);
        if (me == (byte)0)
        {
          if (neighbor == 3)
          {
            set_cell(x, y, page, (byte)1);
          }
        }
        else
        {
          if ((neighbor < 2) || (neighbor > 3))
          {
            set_cell(x, y, page, (byte)0);
          }
        }
      }
    }
  }

  private void add_pixels(int page)
  {
    int x1 = (rand() & 63);
    int y1 = (rand() & 63);
    for (int y = 0; y < 5; y++)
    {
      for (int x = 0; x < 5; x++)
      {
        if ((rand() & 1) == 0)
        {
          set_cell(x1 + x, y1 + y, page, (byte)1);
        }
      }
    }
  }

  private void draw_cells(int page)
  {
    int rand_bak = random;
    random = -59634649;
    for (int y = 0; y < SCREEN_SIZE; y++)
    {
      for (int x = 0; x < SCREEN_SIZE; x++)
      {
        byte col = (byte)rand();
        if (get_cell(x, y, page) == (byte)0)
        {
          col = 0;
        }
        bg0.chr[get_index(x, y, 0)] = col;
      }
    }
    random = rand_bak;
  }

  public void setParam(int x, int y, int offset, int scale, int f)
  {
    bg0.x = x - offset;
    bg0.y = y - offset;
    bg0.scale = scale;
    frame = f;
  }

  public void run()
  {
    init();
    int prev = 0;
    int count = 0;
    int page = 0;
    while (true)
    {
      if (frame != prev)
      {
        prev = frame;
        calc_cells(page);
        draw_cells(page);
        count++;
        if (count > 10)
        {
          count = 0;
          add_pixels(page);
        }
        page ^= 1;
      }
    }
  }
}
