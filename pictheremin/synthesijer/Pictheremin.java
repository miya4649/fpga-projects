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

import synthesijer.lib.timer.*;
import synthesijer.lib.led.*;
import synthesijer.rt.*;

public class Pictheremin
{
  // COUNT = 50,000,000 Hz / 100,000 Hz / 4 = 125
  private static final int COUNT = 125;
  private static final int GYRO_ADDR = 0x6b;
  private static final int COMMAND = 0x00;
  private static final int DATA = 0x40;
  private static final int WIDTH = 640;
  private static final int HEIGHT = 480;
  private static final int SCREEN_SHIFT_BITS = 11;
  private static final int MAX_Z = (640 << SCREEN_SHIFT_BITS);
  private static final int MAX_X = (480 << SCREEN_SHIFT_BITS);

  private static final int FIXED_BITS = 15;
  private static final int FIXED_BITS_OCT = 24;
  private static final int FIXED_BITS_ENV = 8;
  private static final int SCALE_SIZE = 12;
  private static final int SCALE_DATA_SIZE = 16;
  private static final int FINE_SCALE_DATA_SIZE = 128;
  private static final int WAVE_BUFFER_BITS = 11;
  private static final int TUNE_A = ((WAVE_BUFFER_BITS - 1) * SCALE_SIZE * FINE_SCALE_DATA_SIZE);
  private static final int CONST01 = ((1 << FIXED_BITS_OCT) / SCALE_SIZE / FINE_SCALE_DATA_SIZE);
  private static final int CONST02 = (SCALE_SIZE * FINE_SCALE_DATA_SIZE);

  private final I2CIface i2c = new I2CIface();
  private final KeyInput key = new KeyInput();
  private final VideoController video = new VideoController();
  private final Synthesizer synth = new Synthesizer();

  private int dx = 0;
  private int dy = 0;
  private int dz = 0;

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

  private void resetSensor()
  {
    int i = 0;
    short ax, ay, az;
    short sx = 0;
    short sy = 0;
    short sz = 0;
    while (i < 256)
    {
      if ((i2c.i2cRead(GYRO_ADDR, 0x2f) & 0x20) != 0x20)
      {
        ax = (short)i2c.i2cRead(GYRO_ADDR, 0x28);
        ax |= (short)(i2c.i2cRead(GYRO_ADDR, 0x29) << 8);
        ay = (short)i2c.i2cRead(GYRO_ADDR, 0x2a);
        ay |= (short)(i2c.i2cRead(GYRO_ADDR, 0x2b) << 8);
        az = (short)i2c.i2cRead(GYRO_ADDR, 0x2c);
        az |= (short)(i2c.i2cRead(GYRO_ADDR, 0x2d) << 8);
        sx += ax;
        sy += ay;
        sz += az;
        i2c.cycleWait(200000);
        i++;
      }
    }
    dx = sx >> 8;
    dy = sy >> 8;
    dz = sz >> 8;
  }

  private void init()
  {
    video.start();
    synth.start();
    i2c.i2cInit(COUNT);
    i2c.cycleWait(50000000);

    // GYRO ON
    i2c.i2cWrite(GYRO_ADDR, 0x20, 0x3f); // enable xyz axis, maximum bandwidth
    i2c.i2cWrite(GYRO_ADDR, 0x2e, 0x40); // FIFO mode: stream
    i2c.i2cWrite(GYRO_ADDR, 0x24, 0x40); // FIFO enable
  }

  @auto
  public void main()
  {
    short ax = 0;
    short ay = 0;
    short az = 0;
    int x = 0;
    int y = 0;
    int z = 0;
    byte nkey = (byte)0;

    int debugCounter = 0;
    int debugRate = 95;
    int debug0 = 0;
    int debug1 = 0;

    init();
    resetSensor();

    while (true)
    {
      nkey = key.value;
      if ((nkey & 1) == 1)
      {
        synth.SynthNoteOn = true;
      }
      else
      {
        synth.SynthNoteOn = false;
      }

      if ((nkey & 2) == 2)
      {
      }

      if ((i2c.i2cRead(GYRO_ADDR, 0x2f) & 0x20) != 0x20)
      {
        ax = (short)i2c.i2cRead(GYRO_ADDR, 0x28);
        ax |= (short)(i2c.i2cRead(GYRO_ADDR, 0x29) << 8);
        ay = (short)i2c.i2cRead(GYRO_ADDR, 0x2a);
        ay |= (short)(i2c.i2cRead(GYRO_ADDR, 0x2b) << 8);
        az = (short)i2c.i2cRead(GYRO_ADDR, 0x2c);
        az |= (short)(i2c.i2cRead(GYRO_ADDR, 0x2d) << 8);
        x = x + (int)(ax - dx);
        y = y + (int)(ay - dy);
        z = z - (int)(az - dz);

        z = max(z, 0);
        z = min(z, MAX_Z);
        x = max(x, 0);
        x = min(x, MAX_X);
        int cursor_x = z >> SCREEN_SHIFT_BITS;
        int cursor_y = x >> SCREEN_SHIFT_BITS;
        int f1 = (z >>> 8) + TUNE_A;
        int oct = (f1 * CONST01) >>> FIXED_BITS_OCT;
        int f2 = f1 - (oct * CONST02);
        int note = f2 >>> 7;
        int fine = f2 & 0x7f;
        int vol = (MAX_X - x) >> 4;

        synth.SynthNote = note;
        synth.SynthFine = fine;
        synth.SynthOct = oct - 1;
        synth.SynthVol = vol;

        video.setParam(cursor_x, cursor_y, nkey);

        debugCounter++;
        if (debugCounter == debugRate)
        {
          debugCounter = 0;
          debug0 = az;
          debug1 = ax;
        }

        video.debug[0] = cursor_x;
        video.debug[1] = cursor_y;
        video.debug[2] = dz;
        video.debug[3] = dx;
      }

      i2c.cycleWait(100000);
    }
  }
}
