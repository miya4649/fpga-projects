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
  private static final int I_HALT = 0x00;
  private static final int I_LD   = 0x01;
  private static final int I_ST   = 0x02;
  private static final int I_BC   = 0x03;
  private static final int I_BR   = 0x04;
  private static final int I_BA   = 0x05;
  // 1 cycle instructions
  private static final int I_NOP  = 0x40;
  private static final int I_ADD  = 0x41;
  private static final int I_SUB  = 0x42;
  private static final int I_AND  = 0x43;
  private static final int I_OR   = 0x44;
  private static final int I_XOR  = 0x45;
  private static final int I_NOT  = 0x46;
  private static final int I_MV   = 0x47;
  private static final int I_MVI  = 0x48;
  private static final int I_MVIH = 0x49;
  private static final int I_SR   = 0x4a;
  private static final int I_SL   = 0x4b;
  private static final int I_SRA  = 0x4c;
  private static final int I_CEQ  = 0x4d;
  private static final int I_CGT  = 0x4e;
  private static final int I_IN   = 0x4f;
  private static final int I_OUT  = 0x50;
  private static final int I_MUL  = 0x51;


  public void do_asm()
  {
    address = 0;

    // プログラム
    // カウントアップしてその値をI/Oポートに出力
    as_mvi(0, 0);
    as_mvi(1, 1);
    as_add(0, 0, 1);
    as_out(0);
    as_br(2, -2);
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
    // reg_file[reg_d] = mem_d[reg_file[reg_a] + offset]
    print_binary((reg_d << 26) | (reg_a << 20) | ((offset & 0x1fff) << 7) | I_LD);
  }

  private void as_st(int reg_d, int reg_a, int offset)
  {
    // mem_d[reg_file[reg_a] + offset] = reg_file[reg_d]
    print_binary((reg_d << 26) | (reg_a << 20) | ((offset & 0x1fff) << 7) | I_ST);
  }

  private void as_mv(int reg_d, int reg_a, int reg_b)
  {
    print_binary((reg_d << 26) | (reg_a << 20) | (reg_b << 14) | I_MV);
  }

  private void as_mvi(int reg_d, int value)
  {
    print_binary((reg_d << 26) | ((value & 0xffff) << 7) | I_MVI);
  }

  private void as_mvih(int reg_d, int value)
  {
    print_binary((reg_d << 26) | ((value & 0xffff) << 7) | I_MVIH);
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

  private void as_br(int reg_d, int offset)
  {
    print_binary((reg_d << 26) | ((offset & 0x7ffff) << 7) | I_BR);
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
