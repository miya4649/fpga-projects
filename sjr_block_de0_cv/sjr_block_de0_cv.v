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

module sjr_block_de0_cv
  (
   input        CLOCK_50,
   input        RESET_N,
   output       VGA_HS,
   output       VGA_VS,
   output [3:0] VGA_R,
   output [3:0] VGA_G,
   output [3:0] VGA_B,
   output [6:0] HEX0,
   output [6:0] HEX1,
   output [6:0] HEX2,
   output [6:0] HEX3,
   output [6:0] HEX4,
   output [6:0] HEX5,
   output [9:0] LEDR,
   input [3:0]  KEY,
   input [9:0]  SW,
   inout [35:0] GPIO_1
   );

  // unused GPIO
  assign GPIO_1[32:0] = 33'hzzzzzzzzz;
  assign GPIO_1[34] = 1'bz;

  wire          clk_video;
  wire          clk_audio;
  wire          pll_locked;

  // main
  wire          main_busy;
  wire          main_req;
  assign main_req = 1'b1;

  // toggle switch
  wire [15:0]   SW_out;
  assign SW_out = SW;

  // key
  wire [7:0]    KEY_out;
  assign KEY_out = KEY;

  // red leds
  wire [15:0]   LEDR_in;
  assign LEDR = LEDR_in[9:0];

  // 7seg leds
  wire [31:0]    HEX_in;
  assign HEX5 = get_hex(HEX_in[23:20]);
  assign HEX4 = get_hex(HEX_in[19:16]);
  assign HEX3 = get_hex(HEX_in[15:12]);
  assign HEX2 = get_hex(HEX_in[11:8]);
  assign HEX1 = get_hex(HEX_in[7:4]);
  assign HEX0 = get_hex(HEX_in[3:0]);

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

  // audio output port
  wire          audio_r;
  wire          audio_l;
  assign GPIO_1[33] = audio_r;
  assign GPIO_1[35] = audio_l;

  // truncate RGB data
  wire [7:0] VGA_R_in;
  wire [7:0] VGA_G_in;
  wire [7:0] VGA_B_in;
  assign VGA_R = VGA_R_in[3:0];
  assign VGA_G = VGA_G_in[3:0];
  assign VGA_B = VGA_B_in[3:0];

  // generate reset signal
  reg           reset;
  reg           reset1;
  reg           resetv;
  reg           resetv1;
  reg           reseta;
  reg           reseta1;
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

  always @(posedge clk_video)
    begin
      resetv1 <= ~pll_locked;
      resetv <= resetv1;
    end

  always @(posedge clk_audio)
    begin
      reseta1 <= ~pll_locked;
      reseta <= reseta1;
    end


  av_pll av_pll_0
    (
     .refclk (CLOCK_50),
     .rst (resetpll),
     .outclk_0 (clk_video),
     .outclk_1 (clk_audio),
     .locked (pll_locked)
     );

  Sjr_Top sjr_top0
    (
     .clk (CLOCK_50),
     .reset (reset),
     .class_obj_0000_class_vram_0013_ext_clkv_exp_exp (clk_video),
     .class_obj_0000_class_vram_0013_ext_resetv_exp_exp (resetv),
     .class_obj_0000_class_vram_0013_ext_vga_hs_exp_exp (VGA_HS),
     .class_obj_0000_class_vram_0013_ext_vga_vs_exp_exp (VGA_VS),
     .class_obj_0000_class_vram_0013_ext_vga_de_exp_exp (),
     .class_obj_0000_class_vram_0013_ext_vga_r_exp_exp (VGA_R_in),
     .class_obj_0000_class_vram_0013_ext_vga_g_exp_exp (VGA_G_in),
     .class_obj_0000_class_vram_0013_ext_vga_b_exp_exp (VGA_B_in),
     .class_obj_0000_class_psg_0015_class_audio_0003_ext_clka_exp_exp_exp (clk_audio),
     .class_obj_0000_class_psg_0015_class_audio_0003_ext_reseta_exp_exp_exp (reseta),
     .class_obj_0000_class_psg_0015_class_audio_0003_ext_audio_r_exp_exp_exp (audio_r),
     .class_obj_0000_class_psg_0015_class_audio_0003_ext_audio_l_exp_exp_exp (audio_l),
     .class_obj_0000_class_hexLED_0017_ext_hex_led_exp_exp (HEX_in),
     .class_obj_0000_class_redLED_0019_ext_red_led_exp_exp (LEDR_in),
     .class_obj_0000_class_boardSW_0021_ext_board_switch_exp_exp (SW_out),
     .class_obj_0000_class_boardKey_0023_ext_board_key_exp_exp (KEY_out)
     // .main_busy (main_busy),
     // .main_req (main_req)
     );

endmodule
