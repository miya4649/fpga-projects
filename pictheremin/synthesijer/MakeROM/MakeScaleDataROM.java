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

public class MakeScaleDataROM extends MakeROM
{
  private static final int FIXED_BITS = 15;
  private static final int FIXED_SCALE = (1 << FIXED_BITS);
  private static final int SCALE_SIZE = 12;
  private static final int SCALE_DATA_SIZE = 16;
  private static final int WAVE_BUFFER_BITS = 11;
  private static final int WAVE_BUFFER_SIZE = (1 << WAVE_BUFFER_BITS);
  private static final int FINE_SCALE_DATA_SIZE = 128;
  private static final int DIVISIONS_SIZE = 64;
  private static final int WIDTH = 320;
  private static final int SAMPLING_RATE = 48000;
  private static final double TUNE = 440.0;

  private final int[] scaleData = new int[SCALE_DATA_SIZE];
  private final int[] fineScaleData = new int[FINE_SCALE_DATA_SIZE];
  private final int[] divisionsData = new int[DIVISIONS_SIZE];

  public void make()
  {
    for (int i = 0; i < SCALE_DATA_SIZE; i++)
    {
      scaleData[i] = (int)Math.round(Math.pow(2.0, (double)i / (double)SCALE_SIZE) * TUNE * ((double)0x100000000L / (double)SAMPLING_RATE / (double)WAVE_BUFFER_SIZE));
    }

    double scale = Math.pow(2.0, 1.0 / (double)SCALE_SIZE);
    for (int i = 0; i < FINE_SCALE_DATA_SIZE; i++)
    {
      fineScaleData[i] = (int)Math.round(Math.pow(scale, (double)i / (double)FINE_SCALE_DATA_SIZE) * (double)FIXED_SCALE);
    }

    for (int i = 0; i < WIDTH; i += 8)
    {
      int oct = ((i + 72) / 96);
      int color = 0;
      int chr = 0;
      int note = ((i + 72) / 8) % 12;
      if ((note == 1) ||
          (note == 3) ||
          (note == 6) ||
          (note == 8) ||
          (note == 10))
      {
        chr = 6;
      }
      else
      {
        chr = 5;
      }

      if ((oct & 1) == 1)
      {
        color = 1;
      }
      else
      {
        color = 0;
      }

      int cursor = i >>> 3;
      divisionsData[cursor] = (byte)((color << 6) | chr);
    }

    writeVerilog("scaledata", scaleData, 32);
    writeJava("scaledata", "int", scaleData, 32);
    writeVerilog("finescaledata", fineScaleData, 32);
    writeJava("finescaledata", "int", fineScaleData, 32);
    writeVerilog("divisionsdata", divisionsData, 8);
    writeJava("divisionsdata", "byte", divisionsData, 8);
  }
}
