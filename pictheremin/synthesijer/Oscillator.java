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

public class Oscillator extends Thread
{
  private static final int STATE_ATTACK = 0;
  private static final int STATE_DECAY = 1;
  private static final int STATE_SUSTAIN = 2;
  private static final int STATE_RELEASE = 3;
  private static final int STATE_SLEEP = 4;
  private static final int WAVE_BUFFER_BITS = 11;
  private static final int INT_BITS = 32;
  private static final int FIXED_BITS = 15;
  private static final int FIXED_BITS_ENV = 8;
  private static final int WAVE_ADDR_SHIFT = (INT_BITS - WAVE_BUFFER_BITS);
  private static final int WAVE_ADDR_SHIFT_M = (WAVE_ADDR_SHIFT - FIXED_BITS);
  private static final int FIXED_SCALE = (1 << FIXED_BITS);
  private static final int FIXED_SCALE_M1 = (FIXED_SCALE - 1);
  private static final int WAVE_BUFFER_SIZE = (1 << WAVE_BUFFER_BITS);
  private static final int WAVE_BUFFER_SIZE_M1 = (WAVE_BUFFER_SIZE - 1);

  private static final int PARAM_NAME_modLevel0 = 0;
  private static final int PARAM_NAME_mixOut = 1;
  private static final int PARAM_NAME_envelopeLevelA = 2;
  private static final int PARAM_NAME_envelopeLevelS = 3;
  private static final int PARAM_NAME_envelopeDiffA = 4;
  private static final int PARAM_NAME_envelopeDiffD = 5;
  private static final int PARAM_NAME_envelopeDiffR = 6;
  private static final int PARAM_NAME_levelL = 7;
  private static final int PARAM_NAME_levelR = 8;
  private static final int PARAM_NAME_levelRev = 9;

  private int modLevel0;
  private int mixOut;
  private int envelopeLevelA;
  private int envelopeLevelS;
  private int envelopeDiffA;
  private int envelopeDiffD;
  private int envelopeDiffR;
  private int levelL;
  private int levelR;
  private int levelRev;

  private int state;
  private int count;
  private int currentLevel;
  private boolean noteOnSave;

  public boolean noteOn;
  public boolean goToggle;
  public boolean outDoneToggle;
  public int pitch;
  public int velocity;
  public int modPatch0;
  public int modPatch1;
  public int outData;
  public int outWaveL;
  public int outWaveR;
  public int outRevL;
  public int outRevR;

  private final ROMsintable sinTable = new ROMsintable();

  public void setParam(int name, int value)
  {
    switch (name)
    {
      case PARAM_NAME_modLevel0: modLevel0 = value; break;
      case PARAM_NAME_mixOut: mixOut = value; break;
      case PARAM_NAME_envelopeLevelA: envelopeLevelA = value; break;
      case PARAM_NAME_envelopeLevelS: envelopeLevelS = value; break;
      case PARAM_NAME_envelopeDiffA: envelopeDiffA = value; break;
      case PARAM_NAME_envelopeDiffD: envelopeDiffD = value; break;
      case PARAM_NAME_envelopeDiffR: envelopeDiffR = value; break;
      case PARAM_NAME_levelL: levelL = value; break;
      case PARAM_NAME_levelR: levelR = value; break;
      case PARAM_NAME_levelRev: levelRev = value; break;
      default: break;
    }
  }

  public void run()
  {
    state = STATE_SLEEP;

    while (true)
    {
      while (goToggle == outDoneToggle)
      {
        //yield();
      }

      // envelope generator
      if ((noteOn == true) && (noteOnSave != noteOn))
      {
        state = STATE_ATTACK;
      }
      if ((noteOn == false) && (noteOnSave != noteOn))
      {
        state = STATE_RELEASE;
      }
      noteOnSave = noteOn;

      switch (state)
      {
        case STATE_ATTACK:
        {
          currentLevel += envelopeDiffA;
          if (currentLevel > envelopeLevelA)
          {
            currentLevel = envelopeLevelA;
            state = STATE_DECAY;
          }
          break;
        }

        case STATE_DECAY:
        {
          currentLevel += envelopeDiffD;
          if (currentLevel < envelopeLevelS)
          {
            currentLevel = envelopeLevelS;
            state = STATE_SUSTAIN;
          }
          break;
        }

        case STATE_SUSTAIN:
        {
          break;
        }

        case STATE_RELEASE:
        {
          currentLevel += envelopeDiffR;
          if (currentLevel < 0)
          {
            currentLevel = 0;
            state = STATE_SLEEP;
          }
          break;
        }

        // STATE_SLEEP
        default:
        {
          count = 0;
          break;
        }
      }

      int wave_addr = (count +
                       (modPatch0 * modLevel0) +
                       (modPatch1 * velocity)) >>> WAVE_ADDR_SHIFT_M;

      // fetch wave data
      int wave_addr_m = wave_addr & FIXED_SCALE_M1;
      int wave_addr_f = wave_addr >>> FIXED_BITS;
      int wave_addr_r = (wave_addr_f + 1) & WAVE_BUFFER_SIZE_M1;
      int osc_out_f = sinTable.data[wave_addr_f];
      int osc_out_r = sinTable.data[wave_addr_r];
      int osc_out = ((osc_out_f * (FIXED_SCALE - wave_addr_m)) >> FIXED_BITS) +
        ((osc_out_r * wave_addr_m) >> FIXED_BITS);
      outData = (osc_out * (currentLevel >> FIXED_BITS_ENV)) >> FIXED_BITS;
      count += pitch;

      // mix
      if (mixOut == 0)
      {
        outWaveL = 0;
        outWaveR = 0;
        outRevL = 0;
        outRevR = 0;
      }
      else
      {
        outWaveL = (outData * levelL) >> FIXED_BITS;
        outWaveR = (outData * levelR) >> FIXED_BITS;
        outRevL = (outWaveL * levelRev) >> FIXED_BITS;
        outRevR = (outWaveR * levelRev) >> FIXED_BITS;
      }

      outDoneToggle = goToggle;
    }
  }
}
