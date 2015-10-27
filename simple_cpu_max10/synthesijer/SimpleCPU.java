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
halt
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
オプションの追加命令
in
out
mul

命令エンコード仕様
im: unsigned 即値 3,6,16bit
ims: signed 即値 13,22bit
op: オペコード
reg_d: destination register address
reg_a: register a address
reg_b: register b address
オペランドのビット幅
31-------------------------------------0
add,sub,and,or,xor,mv,ceq,cgt,mul
reg_d:6 reg_a:6 reg_b:6 none:7 op:7

not
reg_d:6 reg_a:6 none:13 op:7

sr,sl,sra
reg_d:6 reg_a:6 none:8 im:5 op:7

ld,st
reg_d:6 reg_a:6 ims:13 op:7

mvi,mvih
reg_d:6 none:3 im:16 op:7

bc
reg_d:6 ims:19 op:7

br
none:6 ims:19 op:7

ba,out
none:6 reg_a:6 none:13 op:7

nop
none:25 op:7

in
reg_d:6 none:19 op:7
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
  // 入出力ポート
  private int port_out;
  private int port_in;

  // オペコード
  private static final int I_HALT = 0;
  private static final int I_ADD = 1;
  private static final int I_SUB = 2;
  private static final int I_AND = 3;
  private static final int I_OR = 4;
  private static final int I_XOR = 5;
  private static final int I_NOT = 6;
  private static final int I_LD = 7;
  private static final int I_ST = 8;
  private static final int I_MV = 9;
  private static final int I_MVI = 10;
  private static final int I_MVIH = 11;
  private static final int I_SR = 12;
  private static final int I_SL = 13;
  private static final int I_SRA = 14;
  private static final int I_CEQ = 15;
  private static final int I_CGT = 16;
  private static final int I_BC = 17;
  private static final int I_BR = 18;
  private static final int I_BA = 19;
  private static final int I_NOP = 20;
  // オプションの追加命令
  private static final int I_IN = 21;
  private static final int I_OUT = 22;
  private static final int I_MUL = 23;


  private void init()
  {
    pc = 0;
    reg_d = 0;

    // プログラム
    // カウントアップしてその値をI/Oポートに出力
    mem_i[0x0000] = 0x0000000a;
    mem_i[0x0001] = 0x0400008a;
    mem_i[0x0002] = 0x00004001;
    mem_i[0x0003] = 0x00000016;
    mem_i[0x0004] = 0x03ffff12;
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
      int op = inst & 0x7f;
      int im = inst >>> 7;
      int im16 = im & 0xffff;
      int im5 = im & 0x1f;
      int ims13 = (inst << 12) >> 19;
      int ims19 = (inst << 6) >> 13;
      int rd_addr = inst >>> 26;
      int ra_addr = (inst >>> 20) & 0x3f;
      int rb_addr = (inst >>> 14) & 0x3f;
      //debug
      ///System.out.printf("pc:%d inst:%x op:%d r0:%d r1:%d r2:%d r3:%d\n", pc, inst, op, reg[0], reg[1], reg[2], reg[3]);
      // 実行
      switch (op)
      {
        case I_HALT:
          //debug
          //System.exit(0);
          break;
        case I_ADD:
          reg_d = reg[ra_addr] + reg[rb_addr];
          reg[rd_addr] = reg_d;
          pc++;
          break;
        case I_SUB:
          reg_d = reg[ra_addr] - reg[rb_addr];
          reg[rd_addr] = reg_d;
          pc++;
          break;
        case I_AND:
          reg_d = reg[ra_addr] & reg[rb_addr];
          reg[rd_addr] = reg_d;
          pc++;
          break;
        case I_OR:
          reg_d = reg[ra_addr] | reg[rb_addr];
          reg[rd_addr] = reg_d;
          pc++;
          break;
        case I_XOR:
          reg_d = reg[ra_addr] ^ reg[rb_addr];
          reg[rd_addr] = reg_d;
          pc++;
          break;
        case I_NOT:
          reg_d = ~reg[ra_addr];
          reg[rd_addr] = reg_d;
          pc++;
          break;
        case I_LD:
          reg_d = mem_d[reg[ra_addr] + ims13];
          reg[rd_addr] = reg_d;
          pc++;
          break;
        case I_ST:
          mem_d[reg[ra_addr] + ims13] = reg[rd_addr];
          pc++;
          break;
        case I_MV:
          if ((reg[rb_addr] & 1) == 1)
          {
            reg_d = reg[ra_addr];
            reg[rd_addr] = reg_d;
          }
          pc++;
          break;
        case I_MVI:
          reg[rd_addr] = im16;
          pc++;
          break;
        case I_MVIH:
          reg_d = reg[rd_addr];
          reg[rd_addr] = (im16 << 16) | reg_d;
          pc++;
          break;
        case I_SR:
          reg_d = reg[ra_addr] >>> im5;
          reg[rd_addr] = reg_d;
          pc++;
          break;
        case I_SL:
          reg_d = reg[ra_addr] << im5;
          reg[rd_addr] = reg_d;
          pc++;
          break;
        case I_SRA:
          reg_d = reg[ra_addr] >> im5;
          reg[rd_addr] = reg_d;
          pc++;
          break;
        case I_CEQ:
          if (reg[ra_addr] == reg[rb_addr])
          {
            reg[rd_addr] = 0xffffffff;
          }
          else
          {
            reg[rd_addr] = 0;
          }
          pc++;
          break;
        case I_CGT:
          if (reg[ra_addr] > reg[rb_addr])
          {
            reg[rd_addr] = 0xffffffff;
          }
          else
          {
            reg[rd_addr] = 0;
          }
          pc++;
          break;
        case I_BC:
          if ((reg[rd_addr] & 1) == 1)
          {
            pc += ims19;
          }
          else
          {
            pc++;
          }
          break;
        case I_BR:
          pc += ims19;
          break;
        case I_BA:
          pc = reg[ra_addr];
          break;
        case I_NOP:
          pc++;
          break;
          // ---オプション命令---
        case I_IN:
          reg[rd_addr] = port_in;
          pc++;
          break;
        case I_OUT:
          port_out = reg[ra_addr];
          pc++;
          break;
        case I_MUL:
          reg_d = reg[ra_addr] * reg[rb_addr];
          reg[rd_addr] = reg_d;
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
