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


public class Synthesizer extends Thread
{
  private static final int INT_BITS = 32;
  private static final int FIXED_BITS = 15;
  private static final int FIXED_BITS_ENV = 8;
  private static final int FIXED_BITS_OCT = 24;
  private static final int FIXED_SCALE = (1 << FIXED_BITS);
  private static final int SCALE_SIZE = 12;
  private static final int SCALE_DATA_SIZE = 16;
  private static final int FINE_SCALE_DATA_SIZE = 128;

  private static final int WAVE_BUFFER_BITS = 11;
  private static final int WAVE_BUFFER_SIZE = (1 << WAVE_BUFFER_BITS);
  private static final int ECHO_BUFFER_SIZE_L = 0x4000;
  private static final int ECHO_BUFFER_SIZE_R = 0x4000;
  private static final int ECHO_BUFFER_SIZE_L_M1 = (ECHO_BUFFER_SIZE_L - 1);
  private static final int ECHO_BUFFER_SIZE_R_M1 = (ECHO_BUFFER_SIZE_R - 1);
  private static final int TUNE_A = ((WAVE_BUFFER_BITS - 1) * SCALE_SIZE * FINE_SCALE_DATA_SIZE);
  private static final int OSCS_BITS = 2;
  private static final int OSCS = (1 << OSCS_BITS);
  private static final int CONST01 = ((1 << FIXED_BITS_OCT) / SCALE_SIZE / FINE_SCALE_DATA_SIZE);
  private static final int CONST02 = (SCALE_SIZE * FINE_SCALE_DATA_SIZE);
  private static final int CONST03 = (1 << FIXED_BITS << FIXED_BITS_ENV);
  private static final int CONST04 = (FIXED_SCALE / 2);
  private static final int CONST05 = (CONST04 / 2);
  private static final int PATCH_BITS = 1;
  private static final int PATCH_REAL_SIZE = 2;
  private static final int PATCH_DATA_SIZE = (1 << (PATCH_BITS + OSCS_BITS));

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

  private final int[] echoBufferL = new int[ECHO_BUFFER_SIZE_L];
  private final int[] echoBufferR = new int[ECHO_BUFFER_SIZE_R];
  private final int[] pitch = new int[OSCS];

  private final AudioOutputWrapper audio = new AudioOutputWrapper();
  private final Oscillator osc0 = new Oscillator();
  private final Oscillator osc1 = new Oscillator();
  private final Oscillator osc2 = new Oscillator();
  private final Oscillator osc3 = new Oscillator();

  private final ROMscaledata scaledata = new ROMscaledata();
  private final ROMfinescaledata finescaledata = new ROMfinescaledata();

  private final FreeRunCounter32Iface timer = new FreeRunCounter32Iface();
  public final int[] debug = new int[16];

  private int mixL;
  private int mixR;
  private int mixRevL;
  private int mixRevR;

  public int sample;
  public int SynthNote;
  public int SynthFine;
  public int SynthOct;
  public int SynthVol;
  public boolean SynthNoteOn;

  public void setParam(int osc, int name, int value)
  {
    switch (osc)
    {
      case 0: osc0.setParam(name, value); break;
      case 1: osc1.setParam(name, value); break;
      case 2: osc2.setParam(name, value); break;
      case 3: osc3.setParam(name, value); break;
    }
  }

  public void run()
  {
    boolean validToggle = false;
    int echoAddrL = 0;
    int echoAddrR = 0;
    boolean goToggle = false;

    // (18MHz / 48000Hz) - 1 = 374
    audio.clock_divider = 374;

    // tone parameter
    for (int i = 0; i < OSCS; i++)
    {
      setParam(i, PARAM_NAME_envelopeLevelA, CONST03);
      setParam(i, PARAM_NAME_envelopeLevelS, CONST03);
      setParam(i, PARAM_NAME_envelopeDiffA, CONST03 >> 9);
      setParam(i, PARAM_NAME_envelopeDiffD, (0 - CONST03) >> 16);
      setParam(i, PARAM_NAME_envelopeDiffR, (0 - CONST03) >> 12);
      setParam(i, PARAM_NAME_levelL, CONST05);
      setParam(i, PARAM_NAME_levelR, CONST05);
      setParam(i, PARAM_NAME_levelRev, CONST04);
      setParam(i, PARAM_NAME_mixOut, i & 1);
      setParam(i, PARAM_NAME_modLevel0, 0);
      pitch[i] = 0;
    }

    osc0.start();
    osc1.start();
    osc2.start();
    osc3.start();

    while (true)
    {
      timer.reset();

      osc0.goToggle = goToggle;
      osc1.goToggle = goToggle;
      osc2.goToggle = goToggle;
      osc3.goToggle = goToggle;

      while (true)
      {
        if ((osc0.outDoneToggle == goToggle) &&
            (osc1.outDoneToggle == goToggle) &&
            (osc2.outDoneToggle == goToggle) &&
            (osc3.outDoneToggle == goToggle))
        {
          break;
        }
      }

      mixL = osc0.outWaveL + osc1.outWaveL + osc2.outWaveL + osc3.outWaveL;
      mixR = osc0.outWaveR + osc1.outWaveR + osc2.outWaveR + osc3.outWaveR;
      mixRevL = osc0.outRevL + osc1.outRevL + osc2.outRevL + osc3.outRevL;
      mixRevR = osc0.outRevR + osc1.outRevR + osc2.outRevR + osc3.outRevR;
      osc0.pitch = pitch[0];
      osc1.pitch = pitch[1];
      osc2.pitch = pitch[2];
      osc3.pitch = pitch[3];
      osc0.velocity = SynthVol;
      osc1.velocity = SynthVol;
      osc2.velocity = SynthVol;
      osc3.velocity = SynthVol;
      osc0.noteOn = SynthNoteOn;
      osc1.noteOn = SynthNoteOn;
      osc2.noteOn = SynthNoteOn;
      osc3.noteOn = SynthNoteOn;

      // Connect Patch
      osc0.modPatch0 = 0;
      osc0.modPatch1 = 0;
      osc1.modPatch0 = 0;
      osc1.modPatch1 = osc0.outData;
      osc2.modPatch0 = 0;
      osc2.modPatch1 = 0;
      osc3.modPatch0 = 0;
      osc3.modPatch1 = osc2.outData;

      goToggle = !goToggle;

      for (int i = 0; i < OSCS; i++)
      {
        pitch[i] = (((scaledata.data[SynthNote] * finescaledata.data[SynthFine]) >>> FIXED_BITS) << (SynthOct + (i & 1))) + 234567 * i;
      }

      echoAddrL++;
      if (echoAddrL > 0x3fff)
      {
        echoAddrL = 0;
      }
      echoAddrR++;
      if (echoAddrR > 0x2e51)
      {
        echoAddrR = 0;
      }
      int current_rev_L = echoBufferL[echoAddrL];
      int current_rev_R = echoBufferR[echoAddrR];
      echoBufferL[echoAddrL] = mixRevR + (current_rev_R >> 1);
      echoBufferR[echoAddrR] = mixRevL + (current_rev_L >> 1);
      int waveL = (mixL + current_rev_L + 0x8000) & 0xffff;
      int waveR = (mixR + current_rev_R + 0x8000) & 0xffff;
      int data = (waveR << 16) | waveL;

      debug[0] = sample;
      debug[1] = timer.get();

      while (audio.full == true)
      {
        //yield();
      }
      validToggle = !validToggle;
      audio.data = data;

      audio.valid_toggle = validToggle;
      sample++;
    }
  }
}
