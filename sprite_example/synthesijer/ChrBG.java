/*
  Copyright (c) 2016, miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.util.EnumSet;

import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class ChrBG extends HDLModule
{
  public byte[] chr;
  public int x;
  public int y;
  public int scale;
  public int palette0;
  public int palette1;
  public int palette2;
  public int palette3;

  public ChrBG(String... args)
  {
    super("chr_bg", "clk", "reset");

    newParameter("CHR_SIZE_BITS", 6);

    newPort("chr_length", DIR.OUT, HDLPrimitiveType.genSignedType(32));
    newPort("chr_address",DIR.IN, HDLPrimitiveType.genSignedType(32));
    newPort("chr_din",    DIR.IN, HDLPrimitiveType.genSignedType(8));
    newPort("chr_dout",   DIR.OUT, HDLPrimitiveType.genSignedType(8));
    newPort("chr_we",     DIR.IN, HDLPrimitiveType.genBitType());
    newPort("chr_oe",     DIR.IN, HDLPrimitiveType.genBitType());

    newPort("x", DIR.IN, HDLPrimitiveType.genSignedType(32));
    newPort("y", DIR.IN, HDLPrimitiveType.genSignedType(32));
    newPort("scale", DIR.IN, HDLPrimitiveType.genSignedType(32));
    newPort("palette0", DIR.IN, HDLPrimitiveType.genSignedType(32));
    newPort("palette1", DIR.IN, HDLPrimitiveType.genSignedType(32));
    newPort("palette2", DIR.IN, HDLPrimitiveType.genSignedType(32));
    newPort("palette3", DIR.IN, HDLPrimitiveType.genSignedType(32));

    newPort("ext_clkv", DIR.IN, HDLPrimitiveType.genBitType(), EnumSet.of(HDLPort.OPTION.EXPORT));
    newPort("ext_resetv", DIR.IN, HDLPrimitiveType.genBitType(), EnumSet.of(HDLPort.OPTION.EXPORT));

    newPort("ext_color", DIR.OUT, HDLPrimitiveType.genSignedType(32), EnumSet.of(HDLPort.OPTION.EXPORT));
    newPort("ext_count_h", DIR.IN, HDLPrimitiveType.genSignedType(32), EnumSet.of(HDLPort.OPTION.EXPORT));
    newPort("ext_count_v", DIR.IN, HDLPrimitiveType.genSignedType(32), EnumSet.of(HDLPort.OPTION.EXPORT));
  }
}
