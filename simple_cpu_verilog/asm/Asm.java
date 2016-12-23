/*
  Copyright (c) 2015-2016, miya
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
  private static final int ROM_SIZE = 0x100;
  private static final int LABEL_SIZE = 0x100;
  private int p_address;
  private final int[] rom = new int[ROM_SIZE];
  private final int[] label_value = new int[LABEL_SIZE];
  // opcode
  private static final int I_HALT = 0x00;
  private static final int I_LD   = 0x01;
  private static final int I_ST   = 0x02;
  private static final int I_BC   = 0x03;
  private static final int I_BL   = 0x04;
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
  private static final int I_CGTA = 0x4f;
  private static final int I_IN   = 0x50;
  private static final int I_OUT  = 0x51;
  private static final int I_MUL  = 0x52;

  private static final String header = "module rom\n"
    + "  (\n"
    + "   input             clk,\n"
    + "   input [7:0]       addr,\n"
    + "   output reg [31:0] data_out\n"
    + "   );\n"
    + "\n"
    + "  always @(posedge clk)\n"
    + "    begin\n"
    + "      case (addr)\n";

  private static final String footer = "      endcase\n"
    + "    end\n"
    + "\n"
    + "endmodule\n";


  public void program()
  {
    // program: must be implemented in sub-classes
  }

  // label (hold the current program counter)
  public void label(int id)
  {
    label_value[id] = p_address;
  }

  // return the absolute address of the label
  public int addr_abs(int id)
  {
    return label_value[id];
  }

  // return the relative address between the current line and the label
  public int addr_rel(int id)
  {
    return label_value[id] - p_address;
  }

  public void do_asm()
  {
    for (int i = 0; i < LABEL_SIZE; i++)
    {
      label_value[i] = 0;
    }

    for (int i = 0; i < ROM_SIZE; i++)
    {
      rom[i] = 0;
    }

    System.out.printf(header);

    p_address = 0;
    program(); // pass 1
    p_address = 0;
    program(); // pass 2

    for (int i = 0; i < ROM_SIZE; i++)
    {
      print_binary(i);
    }

    System.out.printf(footer);
  }

  private void print_binary(int i)
  {
    System.out.printf("        8'h%02x: data_out <= 32'h%08x;\n", i, rom[i]);
  }

  private void store_inst(int inst)
  {
    rom[p_address] = inst;
    p_address++;
  }

  private int cut_bits(int bits, int value)
  {
    return (value & ((1 << bits) - 1));
  }

  private int set_field(int shift, int bits, int value)
  {
    return (cut_bits(bits, value) << shift);
  }

  private void set_inst_normal(int reg_d, int reg_a, int reg_b, int op)
  {
    int inst = 0;
    inst |= set_field(26, 6, reg_d);
    inst |= set_field(20, 6, reg_a);
    inst |= set_field(14, 6, reg_b);
    inst |= set_field(0, 7, op);
    store_inst(inst);
  }

  private void set_inst_im_ldst(int reg_d, int reg_a, int im, int op)
  {
    int inst = 0;
    inst |= set_field(26, 6, reg_d);
    inst |= set_field(20, 6, reg_a);
    inst |= set_field(7, 13, im);
    inst |= set_field(0, 7, op);
    store_inst(inst);
  }

  private void set_inst_im_mvi(int reg_d, int im, int op)
  {
    int inst = 0;
    inst |= set_field(26, 6, reg_d);
    inst |= set_field(7, 16, im);
    inst |= set_field(0, 7, op);
    store_inst(inst);
  }

  private void set_inst_im_bcbl(int reg_d, int im, int op)
  {
    int inst = 0;
    inst |= set_field(26, 6, reg_d);
    inst |= set_field(7, 19, im);
    inst |= set_field(0, 7, op);
    store_inst(inst);
  }

  // assembly

  public void as_halt()
  {
    set_inst_normal(0, 0, 0, I_HALT);
  }
  
  public void as_add(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_ADD);
  }

  public void as_sub(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_SUB);
  }

  public void as_and(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_AND);
  }

  public void as_or(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_OR);
  }

  public void as_xor(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_XOR);
  }

  public void as_not(int reg_d, int reg_a)
  {
    set_inst_normal(reg_d, reg_a, 0, I_NOT);
  }

  public void as_ld(int reg_d, int reg_a, int offset)
  {
    set_inst_im_ldst(reg_d, reg_a, offset, I_LD);
  }

  public void as_st(int reg_d, int reg_a, int offset)
  {
    set_inst_im_ldst(reg_d, reg_a, offset, I_ST);
  }

  public void as_mv(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_MV);
  }

  public void as_mvi(int reg_d, int value)
  {
    set_inst_im_mvi(reg_d, value, I_MVI);
  }

  public void as_mvih(int reg_d, int value)
  {
    set_inst_im_mvi(reg_d, value, I_MVIH);
  }

  public void as_sr(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_SR);
  }

  public void as_sl(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_SL);
  }

  public void as_sra(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_SRA);
  }

  public void as_ceq(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_CEQ);
  }

  public void as_cgt(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_CGT);
  }

  public void as_cgta(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_CGTA);
  }

  public void as_bc(int reg_d, int offset)
  {
    set_inst_im_bcbl(reg_d, offset, I_BC);
  }

  public void as_bl(int reg_d, int offset)
  {
    set_inst_im_bcbl(reg_d, offset, I_BL);
  }

  public void as_ba(int reg_a)
  {
    set_inst_normal(0, reg_a, 0, I_BA);
  }

  public void as_nop()
  {
    set_inst_normal(0, 0, 0, I_NOP);
    print_binary(I_NOP);
  }

  public void as_in(int reg_d)
  {
    set_inst_normal(reg_d, 0, 0, I_IN);
  }

  public void as_out(int reg_a)
  {
    set_inst_normal(0, reg_a, 0, I_OUT);
  }

  public void as_mul(int reg_d, int reg_a, int reg_b)
  {
    set_inst_normal(reg_d, reg_a, reg_b, I_MUL);
  }
}
