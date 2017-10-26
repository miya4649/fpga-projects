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

import java.lang.Math;

public class MakeTextROM extends MakeROM
{
  private static final int LINE_BITS = 7;
  private static final int DATA_SIZE = 0x100;
  private static final int DATA_INDEX_SIZE = 0x40;
  private static final int[] data = new int[DATA_SIZE];
  private static final int[] dataIndex = new int[DATA_INDEX_SIZE];
  private static final String[] texts = {
    "PITCH OFFSET",
    "MOD LEVEL 1",
    "MOD LEVEL 2",
    "ATTACK LEVEL",
    "SUSTAIN LEVEL",
    "ATTACK SPEED",
    "DECAY SPEED",
    "RELEASE SPEED",
    "MOD PATCH 0",
    "MOD PATCH 1",
    "MOD PATCH 2",
    "LEVEL L",
    "LEVEL R",
    "LEVEL REV",
    "ABS PITCH",
    "OSC0",
    "OSC1",
    "OSC2",
    "OSC3",
    "OUT",
    "ABS",
  };

  private void drawRect(int[] dst, int x, int y, int dx, int dy, int color)
  {
    for (int iy = y; iy < y + dy; iy++)
    {
      for (int ix = x; ix < x + dx; ix++)
      {
        dst[(iy << LINE_BITS) + ix] = color << 6;
      }
    }
  }

  private void drawString(int[] dst, int x, int y, String str, int color)
  {
    for (int ix = 0; ix < str.length(); ix++)
    {
      dst[(y << LINE_BITS) + ix + x] = str.charAt(ix) + (color << 6);
    }
  }

  public void make()
  {
    int cursor = 0;
    for (int j = 0; j < texts.length; j++)
    {
      int length = texts[j].length();
      dataIndex[(j << 1)] = cursor;
      dataIndex[(j << 1) + 1] = length;
      for (int i = 0; i < length; i++)
      {
        data[i + cursor] = texts[j].charAt(i) - 32;
      }
      cursor += length;
    }

    writeVerilog("text", data, 8);
    writeJava("text", "byte", data, 8);
    writeVerilog("text_index", dataIndex, 16);
    writeJava("text_index", "short", dataIndex, 16);
  }
}
