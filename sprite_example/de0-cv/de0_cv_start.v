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

module de0_cv_start
  (
   input        CLOCK_50,
   input        RESET_N,
   output       VGA_HS,
   output       VGA_VS,
   output [3:0] VGA_R,
   output [3:0] VGA_G,
   output [3:0] VGA_B
   );

  localparam PLANES = 9;

  wire          clk_video;
  wire          pll_locked;

  // truncate RGB data
  wire [7:0]    VGA_R_in;
  wire [7:0]    VGA_G_in;
  wire [7:0]    VGA_B_in;
  assign VGA_R = VGA_R_in[3:0];
  assign VGA_G = VGA_G_in[3:0];
  assign VGA_B = VGA_B_in[3:0];

  // generate reset signal
  reg           reset;
  reg           reset1;
  reg           resetv;
  reg           resetv1;
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

  av_pll av_pll_0
    (
     .refclk (CLOCK_50),
     .rst (resetpll),
     .outclk_0 (clk_video),
     .outclk_1 (),
     .locked (pll_locked)
     );

  // connector
  wire signed [32-1:0] count_h;
  wire signed [32-1:0] count_v;
  wire signed [32-1:0] color_all;
  wire signed [32-1:0] color [0:PLANES-1];
  reg signed [32-1:0]  layer [0:PLANES-1];
  integer              i;
  always @*
    begin
      layer[0] = (color[0] == 0) ? 0 : color[0];
      for (i = 0; i < PLANES-1; i = i + 1)
        begin
          layer[i+1] = (color[i+1] == 0) ? layer[i] : color[i+1];
        end
    end
  assign color_all = layer[PLANES-1]; // color_all: delay 8

  SpriteExample SpriteExample_0
    (
     .clk (CLOCK_50),
     .reset (reset),
     .video_vga_ext_clkv_exp (clk_video),
     .video_vga_ext_resetv_exp (resetv),
     .video_vga_ext_color_exp (color_all),
     .video_vga_ext_vga_hs_exp (VGA_HS),
     .video_vga_ext_vga_vs_exp (VGA_VS),
     .video_vga_ext_vga_de_exp (),
     .video_vga_ext_vga_r_exp (VGA_R_in),
     .video_vga_ext_vga_g_exp (VGA_G_in),
     .video_vga_ext_vga_b_exp (VGA_B_in),
     .video_vga_ext_count_h_exp (count_h),
     .video_vga_ext_count_v_exp (count_v),

     .video_sprite0_ext_clkv_exp (clk_video),
     .video_sprite0_ext_resetv_exp (resetv),
     .video_sprite0_ext_color_exp (color[2]),
     .video_sprite0_ext_count_h_exp (count_h),
     .video_sprite0_ext_count_v_exp (count_v),

     .video_sprite1_ext_clkv_exp (clk_video),
     .video_sprite1_ext_resetv_exp (resetv),
     .video_sprite1_ext_color_exp (color[3]),
     .video_sprite1_ext_count_h_exp (count_h),
     .video_sprite1_ext_count_v_exp (count_v),

     .video_sprite2_ext_clkv_exp (clk_video),
     .video_sprite2_ext_resetv_exp (resetv),
     .video_sprite2_ext_color_exp (color[4]),
     .video_sprite2_ext_count_h_exp (count_h),
     .video_sprite2_ext_count_v_exp (count_v),

     .video_sprite3_ext_clkv_exp (clk_video),
     .video_sprite3_ext_resetv_exp (resetv),
     .video_sprite3_ext_color_exp (color[5]),
     .video_sprite3_ext_count_h_exp (count_h),
     .video_sprite3_ext_count_v_exp (count_v),

     .video_sprite4_ext_clkv_exp (clk_video),
     .video_sprite4_ext_resetv_exp (resetv),
     .video_sprite4_ext_color_exp (color[6]),
     .video_sprite4_ext_count_h_exp (count_h),
     .video_sprite4_ext_count_v_exp (count_v),

     .video_sprite5_ext_clkv_exp (clk_video),
     .video_sprite5_ext_resetv_exp (resetv),
     .video_sprite5_ext_color_exp (color[7]),
     .video_sprite5_ext_count_h_exp (count_h),
     .video_sprite5_ext_count_v_exp (count_v),

     .video_sprite6_ext_clkv_exp (clk_video),
     .video_sprite6_ext_resetv_exp (resetv),
     .video_sprite6_ext_color_exp (color[8]),
     .video_sprite6_ext_count_h_exp (count_h),
     .video_sprite6_ext_count_v_exp (count_v),

     .video_spt0_sprite_ext_clkv_exp (clk_video),
     .video_spt0_sprite_ext_resetv_exp (resetv),
     .video_spt0_sprite_ext_color_exp (color[0]),
     .video_spt0_sprite_ext_count_h_exp (count_h),
     .video_spt0_sprite_ext_count_v_exp (count_v),

     .video_bgt0_bg0_ext_clkv_exp (clk_video),
     .video_bgt0_bg0_ext_resetv_exp (resetv),
     .video_bgt0_bg0_ext_color_exp (color[1]),
     .video_bgt0_bg0_ext_count_h_exp (count_h),
     .video_bgt0_bg0_ext_count_v_exp (count_v)
     );

endmodule
