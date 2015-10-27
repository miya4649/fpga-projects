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

public class Asm
{
  private int address;
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
  private static final int I_IN = 21;
  private static final int I_OUT = 22;
  private static final int I_MUL = 23;


  public void do_asm()
  {
    address = 0;

    // プログラム
    // カウントアップしてその値をI/Oポートに出力
    as_mvi(0, 0);
    as_mvi(1, 1);
    as_add(0, 0, 1);
    as_out(0);
    as_br(-2);
  }

  private void print_binary(int binary)
  {
    System.out.printf("    mem_i[0x%04x] = 0x%08x;\n", address, binary);
    address++;
  }

  // アセンブラ
  private void as_halt()
  {
    print_binary(I_HALT);
  }
  
  private void as_add(int reg_d, int reg_a, int reg_b)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (reg_b << 14) | I_ADD);
  }

  private void as_sub(int reg_d, int reg_a, int reg_b)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (reg_b << 14) | I_SUB);
  }

  private void as_and(int reg_d, int reg_a, int reg_b)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (reg_b << 14) | I_AND);
  }

  private void as_or(int reg_d, int reg_a, int reg_b)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (reg_b << 14) | I_OR);
  }

  private void as_xor(int reg_d, int reg_a, int reg_b)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (reg_b << 14) | I_XOR);
  }

  private void as_not(int reg_d, int reg_a)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | I_NOT);
  }

  private void as_ld(int reg_d, int reg_a, int offset)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | ((offset & 0x1fff) << 7) | I_LD);
  }

  private void as_st(int reg_d, int reg_a, int offset)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | ((offset & 0x1fff) << 7) | I_ST);
  }

  private void as_mv(int reg_d, int reg_a, int reg_b)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (reg_b << 14) | I_MV);
  }

  private void as_mvi(int reg_d, int value)
  {
    print_binary((reg_d << 26) | (value << 7) | I_MVI);
  }

  private void as_mvih(int reg_d, int value)
  {
    print_binary((reg_d << 26) | (value << 7) | I_MVIH);
  }

  private void as_sr(int reg_d, int reg_a, int value)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (value << 7) | I_SR);
  }

  private void as_sl(int reg_d, int reg_a, int value)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (value << 7) | I_SL);
  }

  private void as_sra(int reg_d, int reg_a, int value)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (value << 7) | I_SRA);
  }

  private void as_ceq(int reg_d, int reg_a, int reg_b)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (reg_b << 14) | I_CEQ);
  }

  private void as_cgt(int reg_d, int reg_a, int reg_b)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (reg_b << 14) | I_CGT);
  }

  private void as_bc(int reg_d, int offset)
  {
    print_binary((reg_d << 26) | ((offset & 0x7ffff) << 7) | I_BC);
  }

  private void as_br(int offset)
  {
    print_binary(((offset & 0x7ffff) << 7) | I_BR);
  }

  private void as_ba(int reg_a)
  {
    print_binary((reg_a << 20) | I_BA);
  }

  private void as_nop()
  {
    print_binary(I_NOP);
  }

  private void as_in(int reg_d)
  {
    print_binary((reg_d << 26) | I_IN);
  }

  private void as_out(int reg_a)
  {
    print_binary((reg_a << 20) | I_OUT);
  }

  private void as_mul(int reg_d, int reg_a, int reg_b)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (reg_b << 14) | I_MUL);
  }
}
