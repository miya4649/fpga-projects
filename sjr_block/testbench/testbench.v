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

`timescale 1ns / 100ps

module testbench;
  localparam STEP_CLK = 20; // 20 ns: 50MHz
  localparam STEP_CLKV = 39; // 39 ns: 25.x MHz
  localparam STEP_CLKA = 55; // 55 ns: 18.x MHz
  localparam TICKS = 1000000;

  wire vga_hs;
  wire vga_vs;
  wire [7:0] vga_r;
  wire [7:0] vga_g;
  wire [7:0] vga_b;
  wire audio_r;
  wire audio_l;
  wire [31:0] hex_led;
  wire [15:0] red_led;
  wire [15:0] board_switch;
  wire [7:0] board_key;

  reg        clk;
  reg        clkv;
  reg        clka;
  reg        reset;
  reg        resetv;
  reg        reseta;
  reg        reset_d1;
  reg        resetv_d1;
  reg        reseta_d1;
  reg        reset_sw;
  reg [31:0] count_clk;

  assign board_switch = 16'd0;
  assign board_key = 8'd0;

  initial
    begin
      $dumpfile("wave.vcd");
      $dumpvars(5, testbench);
    end

  // generate clock signal
  initial
    begin
      clk = 1'b1;
      clkv = 1'b1;
      clka = 1'b1;
    end

  always
    begin
      #(STEP_CLK / 2) clk = ~clk;
    end

  always
    begin
      #(STEP_CLKV / 2) clkv = ~clkv;
    end

  always
    begin
      #(STEP_CLKA / 2) clka = ~clka;
    end

  // generate reset signal
  initial
    begin
      reset_sw = 1'b0;
      #(STEP_CLK * 5 + 3) reset_sw = 1'b1;
      #(STEP_CLK * 20 + 3) reset_sw = 1'b0;
    end

  always @(posedge clk)
    begin
      reset_d1 <= reset_sw;
      reset <= reset_d1;
    end

  always @(posedge clkv)
    begin
      resetv_d1 <= reset_sw;
      resetv <= resetv_d1;
    end

  always @(posedge clka)
    begin
      reseta_d1 <= reset_sw;
      reseta <= reseta_d1;
    end

  // stop simulation after TICKS
  always @(posedge clk)
    begin
      if (reset == 1'b1)
        begin
          count_clk <= 1'd0;
        end
      else
        begin
          count_clk = count_clk + 1;
          if (count_clk > TICKS)
            begin
              $finish;
            end
        end
    end

  Sjr_Block Sjr_Block_0
    (
     .clk (clk),
     .reset (reset),
     .vram_ext_clkv_exp (clkv),
     .vram_ext_resetv_exp (resetv),
     .vram_ext_vga_hs_exp (vga_hs),
     .vram_ext_vga_vs_exp (vga_vs),
     .vram_ext_vga_de_exp (),
     .vram_ext_vga_r_exp (vga_r),
     .vram_ext_vga_g_exp (vga_g),
     .vram_ext_vga_b_exp (vga_b),
     .psg_audio_ext_clka_exp (clka),
     .psg_audio_ext_reseta_exp (reseta),
     .psg_audio_ext_audio_r_exp (audio_r),
     .psg_audio_ext_audio_l_exp (audio_l),
     .hexLED_ext_hex_led_exp (hex_led),
     .redLED_ext_red_led_exp (red_led),
     .boardSW_ext_board_switch_exp (board_switch),
     .boardKey_ext_board_key_exp (board_key)
     );

endmodule
