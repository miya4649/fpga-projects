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
  localparam STEP_CLK_VIDEO = 39; // 39 ns: 25.x MHz
  localparam TICKS = 1000000;

  wire VGA_HS;
  wire VGA_VS;
  wire [7:0] VGA_R_in;
  wire [7:0] VGA_G_in;
  wire [7:0] VGA_B_in;

  reg        clk;
  reg        clk_video;
  reg        reset;
  reg        resetv;
  reg        reset_d1;
  reg        resetv_d1;
  reg        reset_sw;
  reg [31:0] count_clk;

  initial
    begin
      $dumpfile("wave.vcd");
      $dumpvars(5, testbench);
    end

  // generate clock signal
  initial
    begin
      clk = 1'b1;
      clk_video = 1'b1;
    end

  always
    begin
      #(STEP_CLK / 2) clk = ~clk;
    end

  always
    begin
      #(STEP_CLK_VIDEO / 2) clk_video = ~clk_video;
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

  always @(posedge clk_video)
    begin
      resetv_d1 <= reset_sw;
      resetv <= resetv_d1;
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

  // connector
  localparam PLANES = 9;
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
     .clk (clk),
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
