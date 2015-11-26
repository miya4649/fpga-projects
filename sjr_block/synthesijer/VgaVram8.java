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

import java.util.EnumSet;

import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class VgaVram8 extends HDLModule
{
  // フィールド定義。下のポート仕様で同じ名前のポートを記述するとJava側から読み書きできるレジスタになる。
  byte[] data;
  boolean vsync;
  int offset_h;
  int offset_v;

  public VgaVram8(String... args)
  {
    // "vga_vram_8"は実体のHDLのモジュール名
    super("vga_vram_8", "clk", "reset");

    // 配列のポート仕様記述には以下のようなポートが必要
    // length: 配列のサイズ取得用
    newPort("data_length", DIR.OUT, HDLPrimitiveType.genSignedType(32));
    // address: アドレス（配列の添字）
    newPort("data_address",DIR.IN, HDLPrimitiveType.genSignedType(32));
    // din: 入力データ。byte配列なのでサイズが8。int配列なら32にする。
    newPort("data_din",    DIR.IN, HDLPrimitiveType.genSignedType(8));
    // dout: 出力データ
    newPort("data_dout",   DIR.OUT, HDLPrimitiveType.genSignedType(8));
    // we, oe: 読み書き時に使われるフラグ
    newPort("data_we",     DIR.IN, HDLPrimitiveType.genBitType());
    newPort("data_oe",     DIR.IN, HDLPrimitiveType.genBitType());

    newPort("vsync", DIR.OUT, HDLPrimitiveType.genBitType());
    newPort("offset_h", DIR.IN, HDLPrimitiveType.genSignedType(32));
    newPort("offset_v", DIR.IN, HDLPrimitiveType.genSignedType(32));

    // HDLPort.OPTION.EXPORT を指定したポートはトップモジュールまで遡って接続される。ピンに接続したい時などに使う。
    newPort("ext_clkv", DIR.IN, HDLPrimitiveType.genBitType(), EnumSet.of(HDLPort.OPTION.EXPORT));
    newPort("ext_resetv", DIR.IN, HDLPrimitiveType.genBitType(), EnumSet.of(HDLPort.OPTION.EXPORT));
    newPort("ext_vga_hs", DIR.OUT, HDLPrimitiveType.genBitType(), EnumSet.of(HDLPort.OPTION.EXPORT));
    newPort("ext_vga_vs", DIR.OUT, HDLPrimitiveType.genBitType(), EnumSet.of(HDLPort.OPTION.EXPORT));
    newPort("ext_vga_de", DIR.OUT, HDLPrimitiveType.genBitType(), EnumSet.of(HDLPort.OPTION.EXPORT));
    newPort("ext_vga_r", DIR.OUT, HDLPrimitiveType.genSignedType(8), EnumSet.of(HDLPort.OPTION.EXPORT));
    newPort("ext_vga_g", DIR.OUT, HDLPrimitiveType.genSignedType(8), EnumSet.of(HDLPort.OPTION.EXPORT));
    newPort("ext_vga_b", DIR.OUT, HDLPrimitiveType.genSignedType(8), EnumSet.of(HDLPort.OPTION.EXPORT));
  }
}
