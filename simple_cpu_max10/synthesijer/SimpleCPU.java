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

/*
命令一覧
nop
add
sub
and
or
xor
not
ld: load
st: store
mv: move register
mvi: move immediate
mvih: move immediate high
sr: shift right
sl: shift left
sra: shift right arithmetic
ceq: compare equal
cgt: compare grater than
bc: branch condition
br: branch relative
ba: branch absolute
in
out

命令エンコード仕様
im: 即値(3,6,16bit unsigned, 22bit signed)
op: オペコード
reg_d: destination register address
reg_a: register a address
reg_b: register b address
オペランドのビット幅
31-------------------------------------0
reg_d:6 reg_a:6 reg_b:6 none:4 im:3 op:7
reg_d:6 reg_a:6 none:7 im:6 op:7
reg_d:6 none:3 im:16 op:7
ims:22 im:3 op:7
 */


import synthesijer.rt.*;

public class SimpleCPU
{
  //debug
  private final RedLED led = new RedLED();

  // インストラクション・メモリー
  private final int[] mem_i = new int[256];
  // データ・メモリー
  private final int[] mem_d = new int[256];
  // レジスタファイル
  private final int[] reg = new int[64];
  private int reg_d;
  // プログラム・カウンター
  private int pc;
  // フラグ
  private boolean f_zero;
  private boolean f_negative;
  // 入出力ポート
  private int port_out;
  private int port_in;

  // オペコード
  private final int I_NOP = 0;
  private final int I_BR = 1;
  private final int I_MVI = 2;
  private final int I_ADD = 3;
  private final int I_OUT = 4;


  // アセンブラ（仮）
  private int as_br(int add_pc)
  {
    return (add_pc << 10) | I_BR;
  }

  private int as_mvi(int reg_d, int value)
  {
    return (reg_d << 26) | (value << 7) | I_MVI;
  }

  private int as_add(int reg_d, int reg_a, int reg_b)
  {
    return (reg_d << 26) | (reg_a << 20) | (reg_b << 14) | I_ADD;
  }

  private int as_out(int reg_a)
  {
    return (reg_a << 20) | I_OUT;
  }

  private void init()
  {
    pc = 0;
    f_zero = false;
    f_negative = false;
    reg_d = 0;

    // プログラム
    // カウントアップしてその値をI/Oポートに出力
    mem_i[0] = as_mvi(0, 0);
    mem_i[1] = as_mvi(1, 1);
    mem_i[2] = as_add(0, 0, 1);
    mem_i[3] = as_out(0);
    mem_i[4] = as_br(-2);
  }

  @auto
  public void run()
  {
    init();

    while (true)
    {
      // フェッチ
      int inst = mem_i[pc];
      // デコード
      int op = inst & 0x0f;
      int im = (inst >>> 7);
      int im16 = im & 0xffff;
      int im5 = im & 0x1f;
      int im3 = im & 0x7;
      int ims22 = inst >> 10;
      int rd_addr = inst >>> 26;
      int ra_addr = (inst >>> 20) & 0x3f;
      int rb_addr = (inst >>> 14) & 0x3f;
      //debug
      //System.out.printf("pc:%d inst:%x r0:%d r1:%d r2:%d \n", pc, inst, reg[0], reg[1], reg[2]);
      // 実行
      switch (op)
      {
        case I_NOP:
          pc++;
          break;
        case I_BR:
          pc += ims22;
          break;
        case I_MVI:
          reg[rd_addr] = im16;
          pc++;
          break;
        case I_ADD:
          reg_d = reg[ra_addr] + reg[rb_addr];
          reg[rd_addr] = reg_d;
          pc++;
          break;
        case I_OUT:
          port_out = reg[ra_addr];
          pc++;
          break;
        default:
          break;
      }
      //debug
      led.data = (short)(port_out >>> 16);
    }
  }
}
