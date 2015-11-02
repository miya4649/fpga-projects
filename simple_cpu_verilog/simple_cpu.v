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


module simple_cpu
  (
   input             clk,
   input             reset,
   output reg [7:0]  rom_addr,
   input [31:0]      rom_data,
   input [31:0]      port_in,
   output reg [31:0] port_out
   );

  // don't remove
  wire [WIDTH_REG-1:0] mon_reg0;
  assign mon_reg0 = reg_file[0];
  // debug
  /*
  wire [WIDTH_REG-1:0] mon_reg1;
  wire [WIDTH_REG-1:0] mon_reg2;
  wire [WIDTH_REG-1:0] mon_reg3;
  wire [WIDTH_REG-1:0] mon_reg4;
  wire [WIDTH_REG-1:0] mon_reg5;
  assign mon_reg1 = reg_file[1];
  assign mon_reg2 = reg_file[2];
  assign mon_reg3 = reg_file[3];
  assign mon_reg4 = reg_file[4];
  assign mon_reg5 = reg_file[5];
   */

  parameter WIDTH_I = 32;
  parameter WIDTH_D = 32;
  parameter WIDTH_REG = 32;
  parameter DEPTH_I = 8;
  parameter DEPTH_D = 8;
  parameter DEPTH_REG = 4;

  parameter S_0 = 0;
  parameter S_1 = 1;
  parameter S_2 = 2;
  parameter S_3 = 3;
  parameter S_4 = 4;
  parameter S_5 = 5;

  // opcode
  // 3 cycles
  parameter I_HALT = 7'h00;
  parameter I_LD   = 7'h01;
  parameter I_ST   = 7'h02;
  parameter I_BC   = 7'h03;
  parameter I_BL   = 7'h04;
  parameter I_BA   = 7'h05;
  // 1 cycle
  parameter I_NOP  = 7'h40;
  parameter I_ADD  = 7'h41;
  parameter I_SUB  = 7'h42;
  parameter I_AND  = 7'h43;
  parameter I_OR   = 7'h44;
  parameter I_XOR  = 7'h45;
  parameter I_NOT  = 7'h46;
  parameter I_MV   = 7'h47;
  parameter I_MVI  = 7'h48;
  parameter I_MVIH = 7'h49;
  parameter I_SR   = 7'h4a;
  parameter I_SL   = 7'h4b;
  parameter I_SRA  = 7'h4c;
  parameter I_CEQ  = 7'h4d;
  parameter I_CGT  = 7'h4e;
  parameter I_CGTA = 7'h4f;
  parameter I_IN   = 7'h50;
  parameter I_OUT  = 7'h51;
  parameter I_MUL  = 7'h52;

  parameter TRUE = 1'b1;
  parameter FALSE = 1'b0;
  parameter ONE = 1'd1;
  parameter ZERO = 1'd0;
  parameter FFFF = {WIDTH_D{1'b1}};

  wire [WIDTH_I-1:0]   mem_i_o;
  reg [DEPTH_I-1:0]    mem_i_addr;
  reg [WIDTH_I-1:0]    mem_i_i;
  reg                  mem_i_we;

  wire [WIDTH_D-1:0]   mem_d_o;
  reg [DEPTH_D-1:0]    mem_d_addr;
  reg [WIDTH_D-1:0]    mem_d_i;
  reg                  mem_d_we;

  reg                  cpu_en;
  reg                  fetch_valid;
  reg [DEPTH_I-1:0]    pc;
  reg [WIDTH_I-1:0]    inst;
  reg [WIDTH_I-1:0]    inst_next;
  reg [10:0]           stage_init;
  reg [2:0]            stage_cpu;
  reg [2:0]            stage_fetch;
  wire [6:0]           op;
  wire [5:0]           reg_d_addr;
  wire [5:0]           reg_a_addr;
  wire [5:0]           reg_b_addr;
  wire [15:0]          im16;
  wire signed [12:0]   ims13;
  wire signed [18:0]   ims19;
  wire                 is_one_cycle;

  // register file
  reg [WIDTH_REG-1:0]  reg_file[(1 << DEPTH_REG)-1:0];

  // decode
  assign op = inst[6:0];
  assign is_one_cycle = inst[6];
  assign reg_d_addr = inst[31:26];
  assign reg_a_addr = inst[25:20];
  assign reg_b_addr = inst[19:14];
  assign im16 = inst[22:7];
  assign ims13 = inst[19:7];
  assign ims19 = inst[25:7];


  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          stage_init <= ZERO;
          cpu_en <= FALSE;
          mem_i_addr <= ZERO;
          mem_i_we <= FALSE;
          stage_cpu <= ZERO;
          stage_fetch <= ZERO;
          fetch_valid <= FALSE;
          port_out <= ZERO;
        end
      else if (cpu_en == FALSE)
        // init
        begin
          if (stage_init < 11'h400)
            begin
              case (stage_init[1:0])
                // load program from ROM
                2'd0:
                  begin
                    rom_addr <= stage_init[9:2];
                  end
                2'd1:
                  begin
                  end
                2'd2:
                  begin
                    mem_i_addr <= stage_init[9:2];
                    mem_i_i <= rom_data;
                    mem_i_we <= TRUE;
                  end
                2'd3:
                  begin
                    mem_i_we <= FALSE;
                  end
                default: ;
              endcase
              stage_init <= stage_init + ONE;
            end
          else
            begin
              fetch_valid <= FALSE;
              cpu_en <= TRUE;
              mem_i_addr <= ZERO;
            end
        end
      else
        // cpu enable
        begin
          case (stage_cpu)
            S_0:
              begin
                if (fetch_valid == TRUE)
                  begin
                    case (op)
                      I_HALT:
                        begin
                          mem_i_addr <= pc;
                          fetch_valid <= FALSE;
                        end
                      I_LD:
                        begin
                          mem_i_addr <= pc + ONE;
                          mem_d_addr <= reg_file[reg_a_addr] + ims13;
                          fetch_valid <= FALSE;
                          stage_cpu <= S_1;
                        end
                      I_ST:
                        begin
                          mem_i_addr <= pc + ONE;
                          mem_d_i <= reg_file[reg_d_addr];
                          mem_d_addr <= reg_file[reg_a_addr] + ims13;
                          mem_d_we <= TRUE;
                          fetch_valid <= FALSE;
                          stage_cpu <= S_1;
                        end
                      I_BC:
                        begin
                          if (reg_file[reg_d_addr] == ZERO)
                            begin
                              mem_i_addr <= pc + ONE;
                            end
                          else
                            begin
                              mem_i_addr <= pc + ims19;
                            end
                          fetch_valid <= FALSE;
                        end
                      I_BL:
                        begin
                          reg_file[reg_d_addr] <= pc + ONE;
                          mem_i_addr <= pc + ims19;
                          fetch_valid <= FALSE;
                        end
                      I_BA:
                        begin
                          mem_i_addr <= reg_file[reg_a_addr];
                          fetch_valid <= FALSE;
                        end
                      I_NOP:
                        begin
                        end
                      I_ADD:
                        begin
                          reg_file[reg_d_addr] <= reg_file[reg_a_addr] + reg_file[reg_b_addr];
                        end
                      I_SUB:
                        begin
                          reg_file[reg_d_addr] <= reg_file[reg_a_addr] - reg_file[reg_b_addr];
                        end
                      I_AND:
                        begin
                          reg_file[reg_d_addr] <= reg_file[reg_a_addr] & reg_file[reg_b_addr];
                        end
                      I_OR:
                        begin
                          reg_file[reg_d_addr] <= reg_file[reg_a_addr] | reg_file[reg_b_addr];
                        end
                      I_XOR:
                        begin
                          reg_file[reg_d_addr] <= reg_file[reg_a_addr] ^ reg_file[reg_b_addr];
                        end
                      I_NOT:
                        begin
                          reg_file[reg_d_addr] <= ~reg_file[reg_a_addr];
                        end
                      I_MV:
                        begin
                          if (reg_file[reg_b_addr] != ZERO)
                            begin
                              reg_file[reg_d_addr] <= reg_file[reg_a_addr];
                            end
                        end
                      I_MVI:
                        begin
                          reg_file[reg_d_addr] <= im16;
                        end
                      I_MVIH:
                        begin
                          reg_file[reg_d_addr] <= {im16, reg_file[reg_d_addr][15:0]};
                        end
                      I_SR:
                        begin
                          reg_file[reg_d_addr] <= reg_file[reg_a_addr] >> reg_file[reg_b_addr];
                        end
                      I_SL:
                        begin
                          reg_file[reg_d_addr] <= reg_file[reg_a_addr] << reg_file[reg_b_addr];
                        end
                      I_SRA:
                        begin
                          reg_file[reg_d_addr] <= reg_file[reg_a_addr] >>> reg_file[reg_b_addr];
                        end
                      I_CEQ:
                        begin
                          if (reg_file[reg_a_addr] == reg_file[reg_b_addr])
                            begin
                              reg_file[reg_d_addr] <= FFFF;
                            end
                          else
                            begin
                              reg_file[reg_d_addr] <= ZERO;
                            end
                        end
                      I_CGT:
                        begin
                          if (reg_file[reg_a_addr] > reg_file[reg_b_addr])
                            begin
                              reg_file[reg_d_addr] <= FFFF;
                            end
                          else
                            begin
                              reg_file[reg_d_addr] <= ZERO;
                            end
                        end
                      I_CGTA:
                        begin
                          if ($signed(reg_file[reg_a_addr]) > $signed(reg_file[reg_b_addr]))
                            begin
                              reg_file[reg_d_addr] <= FFFF;
                            end
                          else
                            begin
                              reg_file[reg_d_addr] <= ZERO;
                            end
                        end
                      I_IN:
                        begin
                          reg_file[reg_d_addr] <= port_in;
                        end
                      I_OUT:
                        begin
                          port_out <= reg_file[reg_a_addr];
                        end
                      I_MUL:
                        begin
                          reg_file[reg_d_addr] <= $signed(reg_file[reg_a_addr]) * $signed(reg_file[reg_b_addr]);
                        end
                      default: ;
                    endcase

                    // prefetch after 1 cycle instruction
                    if (is_one_cycle == 1'b1)
                      begin
                        inst <= mem_i_o;
                        mem_i_addr <= mem_i_addr + ONE;
                        pc <= pc + ONE;
                      end
                  end
              end
            S_1:
              begin
                case (op)
                  I_LD:
                    begin
                      stage_cpu <= S_2;
                    end
                  I_ST:
                    begin
                      mem_d_we <= FALSE;
                      stage_cpu <= S_0;
                    end
                  default: ;
                endcase
              end
            S_2:
              begin
                case (op)
                  I_LD:
                    begin
                      reg_file[reg_d_addr] <= mem_d_o;
                      stage_cpu <= S_0;
                    end
                  default: ;
                endcase
              end
            default: ;
          endcase

          // prefetch
          if (fetch_valid == FALSE)
            begin
              mem_i_addr <= mem_i_addr + ONE;
              case (stage_fetch)
                S_0:
                  begin
                    pc <= mem_i_addr;
                    stage_fetch <= S_1;
                  end
                S_1:
                  begin
                    inst <= mem_i_o;
                    fetch_valid <= TRUE;
                    stage_fetch <= S_0;
                  end
                default: ;
              endcase
            end
        end
    end


  single_port_ram
    #(
      .DATA_WIDTH (WIDTH_I),
      .ADDR_WIDTH (DEPTH_I)
      )
  mem_i
    (
     .clk (clk),
     .addr (mem_i_addr),
     .data_in (mem_i_i),
     .we (mem_i_we),
     .data_out (mem_i_o)
     );

  single_port_ram
    #(
      .DATA_WIDTH (WIDTH_D),
      .ADDR_WIDTH (DEPTH_D)
      )
  mem_d
    (
     .clk (clk),
     .addr (mem_d_addr),
     .data_in (mem_d_i),
     .we (mem_d_we),
     .data_out (mem_d_o)
     );

endmodule
