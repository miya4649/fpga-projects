/*
  Copyright (c) 2018, miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

//`define USE_150M_CLK
//`define USE_V2

module top
  (
   input        CLOCK_50,
   input        CLOCK2_50,
   input        RESET_N,
   input [0:0]  SW,
   output [6:0] HEX0,
   output [6:0] HEX1,
   output [6:0] HEX2,
   output [6:0] HEX3,
   output [6:0] HEX4,
   output [6:0] HEX5,
   output       VGA_HS,
   output       VGA_VS,
   output [3:0] VGA_R,
   output [3:0] VGA_G,
   output [3:0] VGA_B
   );

  wire          pll_locked;

  // generate reset signal (push button 1)
  reg           reset;
  reg           reset1;
  reg           resetpll;
  reg           resetpll1;

  always @(posedge CLOCK_50)
    begin
      resetpll1 <= ~RESET_N;
      resetpll <= resetpll1;
    end

  always @(posedge CLOCK_50)
    begin
      reset1 <= ~pll_locked;
      reset <= reset1;
    end

  av_pll av_pll_0
    (
     .refclk (CLOCK_50),
     .rst (resetpll),
     .outclk_0 (clkv),
     .outclk_1 (),
     .locked (pll_locked)
     );

  // line clk, reset
  wire          clk_line;
  wire          reset_line;

`ifdef USE_150M_CLK
  localparam    COUNT_PERIOD = 150000000;
  wire          pll_locked_150m;
  reg           reset1_150m;
  reg           reset_150m;

  reg           resetpll_150m;
  reg           resetpll1_150m;

  always @(posedge CLOCK2_50)
    begin
      resetpll1_150m <= ~RESET_N;
      resetpll_150m <= resetpll1_150m;
    end

  always @(posedge CLOCK2_50)
    begin
      reset1_150m <= ~pll_locked_150m;
      reset_150m <= reset1_150m;
    end

  pll_150m pll_150m_0
    (
     .refclk (CLOCK2_50),
     .rst (resetpll),
     .outclk_0 (clk_line),
     .locked (pll_locked_150m)
     );
  assign reset_line = reset_150m;
`else
  localparam    COUNT_PERIOD = 50000000;
  assign clk_line = CLOCK_50;
  assign reset_line = reset;
`endif

  // vga clk, reset
  wire          clkv;
  reg           resetv;
  reg           resetv1;
  // truncate RGB data
  wire [7:0]    VGA_R_in;
  wire [7:0]    VGA_G_in;
  wire [7:0]    VGA_B_in;
  assign VGA_R = VGA_R_in[3:0];
  assign VGA_G = VGA_G_in[3:0];
  assign VGA_B = VGA_B_in[3:0];
  // sync
  wire          VGA_HS_in;
  wire          VGA_VS_in;
  assign VGA_HS = VGA_HS_in;
  assign VGA_VS = VGA_VS_in;

  always @(posedge clkv)
    begin
      resetv1 <= ~pll_locked;
      resetv <= resetv1;
    end

  wire [23:0]    hex_led;
  assign HEX5 = get_hex(hex_led[23:20]);
  assign HEX4 = get_hex(hex_led[19:16]);
  assign HEX3 = get_hex(hex_led[15:12]);
  assign HEX2 = get_hex(hex_led[11:8]);
  assign HEX1 = get_hex(hex_led[7:4]);
  assign HEX0 = get_hex(hex_led[3:0]);

  function [6:0] get_hex
    (
     input [3:0] count
     );
    begin
      case (count)
        4'h0: get_hex = 7'b1000000;
        4'h1: get_hex = 7'b1111001;
        4'h2: get_hex = 7'b0100100;
        4'h3: get_hex = 7'b0110000;
        4'h4: get_hex = 7'b0011001;
        4'h5: get_hex = 7'b0010010;
        4'h6: get_hex = 7'b0000010;
        4'h7: get_hex = 7'b1011000;
        4'h8: get_hex = 7'b0000000;
        4'h9: get_hex = 7'b0010000;
        4'ha: get_hex = 7'b0001000;
        4'hb: get_hex = 7'b0000011;
        4'hc: get_hex = 7'b1000110;
        4'hd: get_hex = 7'b0100001;
        4'he: get_hex = 7'b0000110;
        4'hf: get_hex = 7'b0001110;
        default: get_hex = 7'bx;
      endcase
    end
  endfunction

  // mode switch
  reg mode;
  reg mode1;
  always @(posedge clk_line)
    begin
      mode <= mode1;
      mode1 <= SW[0];
    end

  line_bench
    #(
     .COUNT_PERIOD (COUNT_PERIOD)
     )
  line_bench_0
    (
     .clk (clk_line),
     .reset (reset_line),
     .clkv (clkv),
     .resetv (resetv),
     .mode (mode),
     .count (hex_led),
     .vga_hs (VGA_HS_in),
     .vga_vs (VGA_VS_in),
     .vga_r (VGA_R_in),
     .vga_g (VGA_G_in),
     .vga_b (VGA_B_in)
     );

endmodule
