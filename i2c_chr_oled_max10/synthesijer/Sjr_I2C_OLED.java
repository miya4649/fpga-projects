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

import synthesijer.rt.*;
import synthesijer.lib.wishbone.*;

public class Sjr_I2C_OLED
{
  private final OpenCoresI2CMasterSimpleIface i2c_if = new OpenCoresI2CMasterSimpleIface();
  private final int SLAVE_ADDR = 0x3c;
  private final int COMMAND = 0x00;
  private final int DATA = 0x40;
  // システムクロック：50MHz　I2Cクロック：100KHz
  // 50MHz / (5 * 100KHz) - 1 = 99
  private final int CLOCK_PRESCALE = 99;

  private void busy_wait(int loop)
  {
    for (int i = 0; i < loop; i++)
    {
    }
  }

  @auto
  public void main()
  {
    busy_wait(10000);

    // クロック設定
    i2c_if.init(CLOCK_PRESCALE);

    // このOLEDモジュールでは2バイトでコマンド or データを受け付ける。
    // 1バイト目が0x00の時、続く1バイトをコマンドとみなす。
    // 0x40の時、続く1バイトをデータとみなす。

    // ディスプレイ・クリア
    i2c_if.i2c_write(SLAVE_ADDR, COMMAND, (byte)0x01);
    // カーソルをホームポジションに戻す
    i2c_if.i2c_write(SLAVE_ADDR, COMMAND, (byte)0x02);
    // 表示On
    i2c_if.i2c_write(SLAVE_ADDR, COMMAND, (byte)0x0f);
    // ディスプレイ・クリア
    i2c_if.i2c_write(SLAVE_ADDR, COMMAND, (byte)0x01);

    // 文字データ書き込み
    i2c_if.i2c_write(SLAVE_ADDR, DATA, (byte)0x48); // H
    i2c_if.i2c_write(SLAVE_ADDR, DATA, (byte)0x65); // e
    i2c_if.i2c_write(SLAVE_ADDR, DATA, (byte)0x6c); // l
    i2c_if.i2c_write(SLAVE_ADDR, DATA, (byte)0x6c); // l
    i2c_if.i2c_write(SLAVE_ADDR, DATA, (byte)0x6f); // o
    i2c_if.i2c_write(SLAVE_ADDR, DATA, (byte)0x2c); // ,
    i2c_if.i2c_write(SLAVE_ADDR, DATA, (byte)0x77); // w
    i2c_if.i2c_write(SLAVE_ADDR, DATA, (byte)0x6f); // o
    i2c_if.i2c_write(SLAVE_ADDR, DATA, (byte)0x72); // r
    i2c_if.i2c_write(SLAVE_ADDR, DATA, (byte)0x6c); // l
    i2c_if.i2c_write(SLAVE_ADDR, DATA, (byte)0x64); // d
    i2c_if.i2c_write(SLAVE_ADDR, DATA, (byte)0x21); // !

    busy_wait(0x6000000);

    i2c_if.i2c_write(SLAVE_ADDR, COMMAND, (byte)0x01);
    i2c_if.i2c_write(SLAVE_ADDR, COMMAND, (byte)0x02);
    // 表示Off
    i2c_if.i2c_write(SLAVE_ADDR, COMMAND, (byte)0x08);
    
    busy_wait(0x6000000);

    // この例では i2c_write() しか使用していませんが、
    // 読み込みは以下のようにして行うことができます。
    // int a = i2c_if.i2c_read(SLAVE_ADDR, register_addr);
  }
}
