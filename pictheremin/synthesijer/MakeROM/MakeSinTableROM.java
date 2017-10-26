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

public class MakeSinTableROM extends MakeROM
{
  private static final int WAVE_BUFFER_BITS = 11;
  private static final int WAVE_BUFFER_SIZE = (1 << WAVE_BUFFER_BITS);
  private static final int FIXED_BITS = 15;
  private static final int FIXED_SCALE = (1 << FIXED_BITS);
  private final int[] data = new int[WAVE_BUFFER_SIZE];

  public void make()
  {
    for (int i = 0; i < WAVE_BUFFER_SIZE; i++)
    {
      data[i] = (int)(Math.sin((double)i / (double)WAVE_BUFFER_SIZE * 6.28318530716) * (double)FIXED_SCALE);
    }
    writeVerilog("sintable", data, 32);
    writeJava("sintable", "int", data, 32);
  }
}
