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

import synthesijer.lib.led.*;
import synthesijer.rt.*;

public class I2CIface
{
  public final FreeRunCounter32Iface counter = new FreeRunCounter32Iface();
  public final I2CWrapper i2c = new I2CWrapper();

  // COUNT = 50,000,000 Hz / 100,000 Hz / 4 = 125
  private int count;

  public void i2cInit(int divider)
  {
    count = divider;
    counter.reset();
    i2c.data_o = 0x03; // sda_o:1 scl_o:1
  }

  public int getCycle()
  {
    return counter.get();
  }

  public void cycleWait(int cycles)
  {
    counter.cycleWait(cycles);
  }

  public void i2cWait()
  {
    counter.cycleWait(count);
  }

  public void i2cStart()
  {
    i2c.data_o = 0x03; // sda_o:1 scl_o:1
    i2cWait();
    i2cWait();
    i2c.data_o = 0x01; // sda_o:0 scl_o:1
    i2cWait();
    i2c.data_o = 0x00; // sda_o:0 scl_o:0
    i2cWait();
  }

  private void i2cTxBit(int data)
  {
    int sda = data << 1;
    i2c.data_o = sda | 0; // sda_o:sda scl_o:0
    i2cWait();
    i2c.data_o = sda | 1; // sda_o:sda scl_o:1
    i2cWait();
    i2cWait();
    while ((i2c.data_i & 2) == 0)
    {
      // clock stretching
    }
    i2c.data_o = sda | 0; // sda_o:sda scl_o:0
    i2cWait();
  }

  private int i2cRxBit()
  {
    i2c.data_o = 0x02; // sda_o:1 scl_o:0
    i2cWait();
    i2c.data_o = 0x03; // sda_o:1 scl_o:1
    i2cWait();
    while ((i2c.data_i & 2) == 0)
    {
      // clock stretching
    }
    int data = i2c.data_i & 1;
    i2cWait();
    i2c.data_o = 0x02; // sda_o:1 scl_o:0
    i2cWait();
    return data;
  }

  public boolean i2cTx(int data)
  {
    boolean ack;

    for (int i = 0; i < 8; i++)
    {
      int sda = (data & 0x80) >>> 7;
      i2cTxBit(sda);
      data <<= 1;
    }

    if (i2cRxBit() == 0)
    {
      ack = true;
    }
    else
    {
      ack = false;
    }

    return ack;
  }

  public int i2cRx(boolean isAck)
  {
    int data = 0;

    for (int i = 0; i < 8; i++)
    {
      data <<= 1;
      data |= i2cRxBit();
    }

    if (isAck)
    {
      i2cTxBit(0);
    }
    else
    {
      i2cTxBit(1);
    }
    return data;
  }

  public void i2cStop()
  {
    i2c.data_o = 0x01; // sda_o:0 scl_o:1
    i2cWait();
    i2cWait();
    i2c.data_o = 0x03; // sda_o:1 scl_o:1
    i2cWait();
    i2cWait();
  }

  public void i2cWrite(int addr, int reg, int data)
  {
    i2cStart();
    i2cTx(addr << 1);
    i2cTx(reg);
    i2cTx(data);
    i2cStop();
  }

  public int i2cRead(int addr, int reg)
  {
    i2cStart();
    i2cTx(addr << 1);
    i2cTx(reg);
    i2cStart();
    i2cTx((addr << 1) | 1);
    int data = i2cRx(false);
    i2cStop();
    return data;
  }
}
