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

public class Sjr_VGA_Test extends Thread
{
  private final VgaVram8 vram = new VgaVram8();

  private int calc_page;
  private int draw_page;
  private int color;

  private byte cell[] = new byte[8192 /*all_page_size*/];

  private void pset(int x, int y, int color)
  {
    vram.data[(y << 6) + x] = (byte)color;
  }

  public void init()
  {
    for (int i = 0; i < 8192 /*all_page_size*/; i++)
    {
      cell[i] = (byte)0;
    }
    set_cell(34, 29, 0, (byte)1);
    set_cell(32, 30, 0, (byte)1);
    set_cell(34, 30, 0, (byte)1);
    set_cell(35, 30, 0, (byte)1);
    set_cell(32, 31, 0, (byte)1);
    set_cell(34, 31, 0, (byte)1);
    set_cell(32, 32, 0, (byte)1);
    set_cell(30, 33, 0, (byte)1);
    set_cell(28, 34, 0, (byte)1);
    set_cell(30, 34, 0, (byte)1);
    calc_page = 1;
    draw_page = 0;
    clear_screen();
    draw_cells();
    calc_page = 0;
    draw_page = 1;
  }

  private int get_index(int x, int y, int page)
  {
    return (x & 63 /*cell_size_minus_1*/) + ((y & 63 /*cell_size_minus_1*/) << 6 /*cell_size_bits*/) + (page << 12 /*page_bits*/);
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
      get_cell(x-1,y,p)+                    get_cell(x+1,y,p)+
      get_cell(x-1,y+1,p)+get_cell(x,y+1,p)+get_cell(x+1,y+1,p);
    return neighbor;
  }

  private void calc_cells()
  {
    for (int y = 0; y < 64 /*cell_size*/; y++)
    {
      for (int x = 0; x < 64 /*cell_size*/; x++)
      {
        byte me = get_cell(x, y, calc_page);
        set_cell(x, y, draw_page, me);
        int neighbor = get_neighbor(x, y , calc_page);
        if (me == (byte)0)
        {
          if (neighbor == 3)
          {
            set_cell(x, y, draw_page, (byte)1);
          }
        }
        else
        {
          if ((neighbor < 2) || (neighbor > 3))
          {
            set_cell(x, y, draw_page, (byte)0);
          }
        }
      }
    }
  }

  private void clear_screen()
  {
    for (int i = 0; i < 4096; i++)
    {
      vram.data[i] = (byte)0;
    }
  }

  private void draw_cells()
  {
    for (int y = 0; y < 30 /*screen_height*/; y++)
    {
      for (int x = 0; x < 40 /*screen_width*/; x++)
      {
        int col;
        if (get_cell(x + 12, y + 16, draw_page) == (byte)1)
        {
          col = color;
        }
        else
        {
          col = 0;
        }
        pset(x, y, col);
      }
    }
  }

  public void run()
  {
    init();
    for (int i = 0; i < 512; i++)
    {
      // scroll
      vram.offset_h = i - 256;
      vram.offset_v = i - 256;
      // color shift
      color = i;
      calc_cells();
      // vsync wait
      while (vram.vsync == true)
      {
        yield();
      }
      draw_cells();
      int p = calc_page;
      calc_page = draw_page;
      draw_page = p;
    }
  }
}
