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


public class SoundGenerator extends Thread
{
  public int sample = 0;
  private static final int CHANNELS = 4;
  private static final int ZERO = 0;
  private final AudioOutput audio = new AudioOutput();
  // 以下のフィールドは親スレッドから書き換えられるので run() 内ではread only
  private final int[] freq = new int[4];
  private final int[] pitchBend = new int[4];
  private final int[] vol_R = new int[4];
  private final int[] vol_L = new int[4];
  private final int[] gateTime = new int[4];
  private final int[] decay = new int[4];
  // 以下のフィールドは run() 内で読み書き可
  private final int[] localFreq = new int[4];
  private final int[] localVol_R = new int[4];
  private final int[] localVol_L = new int[4];
  private final int[] localGateTime = new int[4];
  private final int[] localDecay = new int[4];
  private final int[] localFreqCounter = new int[4];
  private final int[] noteData = new int[12];
  // 両方から読み書きするフィールド
  private final boolean[] playFlag = new boolean[4];
  private final boolean[] stopFlag = new boolean[4];

  public void setParameter(int channel, int note_in, int octave_in, int pitchBend_in, int vol_R_in, int vol_L_in, int gateTime_in, int decay_in)
  {
    freq[channel] = noteData[note_in] >>> (8 - octave_in);
    pitchBend[channel] = pitchBend_in;
    vol_R[channel] = vol_R_in;
    vol_L[channel] = vol_L_in;
    gateTime[channel] = gateTime_in;
    decay[channel] = decay_in;
  }

  public void play(int channel)
  {
    playFlag[channel] = true;
  }

  public void stop(int channel)
  {
    stopFlag[channel] = true;
  }

  public void run()
  {
    // 平均律：pow(2, (x/12)): (x: 0-11)
    // round(pow(2,(note/12.0)) * 440.0 * octave * 0x100000000 / 48000) : (note: 3-14)
    noteData[0] = 374557749;  // C8
    noteData[1] = 396830112;  // C#8
    noteData[2] = 420426858;  // D8
    noteData[3] = 445426740;  // D#8
    noteData[4] = 471913192;  // E8
    noteData[5] = 499974611;  // F8
    noteData[6] = 529704648;  // F#8
    noteData[7] = 561202526;  // G8
    noteData[8] = 594573365;  // G#8
    noteData[9] = 629928537;  // A8
    noteData[10] = 667386037; // A#8
    noteData[11] = 707070876; // B8
    boolean validToggle = false;

    while (true)
    {
      int mixer_R = 0;
      int mixer_L = 0;
      int i = 0;
      while (i < CHANNELS)
      {
        if (playFlag[i])
        {
          playFlag[i] = false;
          localFreq[i] = freq[i];
          localFreqCounter[i] = 0;
          localGateTime[i] = 0;
          localDecay[i] = 0;
          localVol_R[i] = vol_R[i];
          localVol_L[i] = vol_L[i];
        }
        
        if (stopFlag[i])
        {
          stopFlag[i] = false;
          localVol_R[i] = 0;
          localVol_L[i] = 0;
        }

        // localFreqCounterの負号で矩形波を作る(1:+ 0:-)
        // localFreq = f(Hz) * 0x100000000 / 48000
        localFreqCounter[i] += localFreq[i];
        localFreq[i] += pitchBend[i];
        // localDecay:固定小数点 15bit.16bit
        localDecay[i] += decay[i];
        int d = (localDecay[i] >>> 8) >>> 8;
        localDecay[i] = localDecay[i] & 0xffff;
        // ボリュームを減衰させる
        if (localVol_R[i] > d)
        {
          localVol_R[i] -= d;
        }
        else
        {
          localVol_R[i] = 0;
        }

        if (localVol_L[i] > d)
        {
          localVol_L[i] -= d;
        }
        else
        {
          localVol_L[i] = 0;
        }

        // ゲートタイムを超えたらボリュームを0に
        localGateTime[i] = localGateTime[i] + 1;
        if (localGateTime[i] > gateTime[i])
        {
          localVol_R[i] = 0;
          localVol_L[i] = 0;
        }

        // 各チャンネルの出力をmix
        if (localFreqCounter[i] >= ZERO)
        {
          mixer_R += localVol_R[i];
          mixer_L += localVol_L[i];
        }
        else
        {
          mixer_R -= localVol_R[i];
          mixer_L -= localVol_L[i];
        }
        i++;
      }

      // サンプリング値をunsigned 16bitに変換してR+Lを32bitにpack
      int mixer_R2 = (mixer_R + 0x8000) & 0xffff;
      int mixer_L2 = (mixer_L + 0x8000) & 0xffff;
      int data = (mixer_R2 << 16) | mixer_L2;
      while (audio.full == true)
      {
        yield();
      }
      validToggle = !validToggle;
      audio.data = data;
      // dataを書いた後にvalid_toggleフラグをトグル(true<->false)する
      // （次のデータが用意できたことを知らせる）
      audio.valid_toggle = validToggle;
      sample++;
    }
  }
}
