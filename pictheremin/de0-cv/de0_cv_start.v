/*
  Copyright (c) 2017, miya
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
   output [3:0] VGA_B,
   inout [35:0] GPIO_1
   );

  localparam PLANES = 4;

  wire          clk_video;
  wire          clk_audio;
  wire          pll_locked;

  // truncate RGB data
  wire [7:0]    VGA_R_in;
  wire [7:0]    VGA_G_in;
  wire [7:0]    VGA_B_in;
  assign VGA_R = VGA_R_in[3:0];
  assign VGA_G = VGA_G_in[3:0];
  assign VGA_B = VGA_B_in[3:0];

  // audio output port
  wire          audio_r;
  wire          audio_l;
  assign GPIO_1[33] = audio_r;
  assign GPIO_1[35] = audio_l;

  // unused GPIO
  assign GPIO_1[32:12] = 36'hzzzzzzzzz;
  assign GPIO_1[3:0] = 4'hz;
  assign GPIO_1[5] = 1'bz;
  assign GPIO_1[7] = 1'bz;
  assign GPIO_1[9] = 1'bz;
  assign GPIO_1[11] = 1'bz;
  assign GPIO_1[34] = 1'bz;

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

  // I2C pin
  wire scl_i;
  wire scl_o;
  wire sda_i;
  wire sda_o;
  wire [31:0] i2c_in;
  wire [31:0] i2c_out;

  assign GPIO_1[4] = scl_o ? 1'bz : 1'b0;
  assign GPIO_1[6] = sda_o ? 1'bz : 1'b0;

  // synchronize input signal
  shift_register_vector
    #(
      .WIDTH (1),
      .DEPTH (3)
      )
  shift_register_vector_0
    (
     .clk (CLOCK_50),
     .data_in (GPIO_1[4]),
     .data_out (scl_i)
     );

  shift_register_vector
    #(
      .WIDTH (1),
      .DEPTH (3)
      )
  shift_register_vector_1
    (
     .clk (CLOCK_50),
     .data_in (GPIO_1[6]),
     .data_out (sda_i)
     );

  // key
  wire [7:0]  key;
  assign key[7:2] = 6'd0;
  stabilizer
    #(
      .SYNC_DEPTH (3),
      .LENGTH_IN_BITS (4)
      )
  stabilizer_0
    (
     .clk (CLOCK_50),
     .reset (reset),
     .in (~GPIO_1[8]),
     .out (key[0])
     );

  stabilizer
    #(
      .SYNC_DEPTH (3),
      .LENGTH_IN_BITS (4)
      )
  stabilizer_1
    (
     .clk (CLOCK_50),
     .reset (reset),
     .in (~GPIO_1[10]),
     .out (key[1])
     );

  av_pll av_pll_0
    (
     .refclk (CLOCK_50),
     .rst (resetpll),
     .outclk_0 (clk_video),
     .outclk_1 (clk_audio),
     .locked (pll_locked)
     );

  // connector
  wire signed [32-1:0] count_h;
  wire signed [32-1:0] count_v;
  wire signed [32-1:0] color_all;
  wire signed [32-1:0] color [0:PLANES-1];
  reg signed [32-1:0]  layer [0:PLANES-1];
  integer              i;
  // no delay
  always @*
    begin
      layer[0] = (color[0] == 0) ? 0 : color[0];
      for (i = 0; i < PLANES-1; i = i + 1)
        begin
          layer[i+1] = (color[i+1] == 0) ? layer[i] : color[i+1];
        end
    end
  assign color_all = layer[PLANES-1];

  Pictheremin Pictheremin_0
    (
     .clk (CLOCK_50),
     .reset (reset),
     .i2c_i2c_ext_scl_i_exp (scl_i),
     .i2c_i2c_ext_scl_o_exp (scl_o),
     .i2c_i2c_ext_sda_i_exp (sda_i),
     .i2c_i2c_ext_sda_o_exp (sda_o),
     .key_din_exp (key),

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

     .video_sp_cursor_ext_clkv_exp (clk_video),
     .video_sp_cursor_ext_resetv_exp (resetv),
     .video_sp_cursor_ext_color_exp (color[3]),
     .video_sp_cursor_ext_count_h_exp (count_h),
     .video_sp_cursor_ext_count_v_exp (count_v),

     .video_sp_paint_ext_clkv_exp (clk_video),
     .video_sp_paint_ext_resetv_exp (resetv),
     .video_sp_paint_ext_color_exp (color[0]),
     .video_sp_paint_ext_count_h_exp (count_h),
     .video_sp_paint_ext_count_v_exp (count_v),

     .video_sp_particle_ext_clkv_exp (clk_video),
     .video_sp_particle_ext_resetv_exp (resetv),
     .video_sp_particle_ext_color_exp (color[1]),
     .video_sp_particle_ext_count_h_exp (count_h),
     .video_sp_particle_ext_count_v_exp (count_v),

     .video_bg0_ext_clkv_exp (clk_video),
     .video_bg0_ext_resetv_exp (resetv),
     .video_bg0_ext_color_exp (color[2]),
     .video_bg0_ext_count_h_exp (count_h),
     .video_bg0_ext_count_v_exp (count_v),

     .synth_audio_ext_audio_clk_exp (clk_audio),
     .synth_audio_ext_audio_reset_exp (reseta),
     .synth_audio_ext_audio_r_exp (audio_r),
     .synth_audio_ext_audio_l_exp (audio_l)
     );

endmodule
