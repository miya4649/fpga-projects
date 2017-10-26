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

`timescale 1ns / 1ns

module testbench;
  parameter STEP = 20; // 20 ns: 50MHz
  parameter STEP_V = 40; // 40 ns: 25MHz
  parameter STEP_A = 55; // 55 ns: 18MHz
  parameter TICKS = 200000;

  reg clk;
  reg reset;
  reg clk_video;
  reg resetv;
  reg clk_audio;
  reg reseta;
  wire        I2C_SCL;
  wire        I2C_SDA;
  wire [7:0]    VGA_R_in;
  wire [7:0]    VGA_G_in;
  wire [7:0]    VGA_B_in;
  wire          audio_r;
  wire          audio_l;

  initial
    begin
      $dumpfile("wave.vcd");
      $dumpvars(5, testbench);
    end

  // generate clock signal
  initial
    begin
      clk = 1'b1;
      forever
        begin
          #(STEP / 2) clk = ~clk;
        end
    end

  initial
    begin
      clk_video = 1'b1;
      forever
        begin
          #(STEP_V / 2) clk_video = ~clk_video;
        end
    end

  initial
    begin
      clk_audio = 1'b1;
      forever
        begin
          #(STEP_A / 2) clk_audio = ~clk_audio;
        end
    end

  // generate reset signal
  initial
    begin
      reset = 1'b0;
      repeat (2) @(posedge clk) reset <= 1'b1;
      @(posedge clk) reset <= 1'b0;
    end

  reg resetv1;
  always @(posedge clk_video)
    begin
      resetv1 <= reset;
      resetv <= resetv1;
    end

  reg reseta1;
  always @(posedge clk_audio)
    begin
      reseta1 <= reset;
      reseta <= reseta1;
    end

  // stop simulation after TICKS
  initial
    begin
      repeat (TICKS) @(posedge clk);
      $finish;
    end

  // I2C pin
  wire scl_i;
  wire scl_o;
  wire sda_i;
  wire sda_o;
  wire [31:0] i2c_in;
  wire [31:0] i2c_out;

  assign I2C_SCL = scl_o ? 1'bz : 1'b0;
  assign I2C_SDA = sda_o ? 1'bz : 1'b0;

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

  // key in simulation
  reg key_in0;
  reg key_in1;
  initial
    begin
      key_in0 = 1'b1;
      key_in1 = 1'b1;
      #20000 key_in0 = 1'b0;
      #20000 key_in0 = 1'b1;
      #20000 key_in0 = 1'b0;
      #20000 key_in0 = 1'b1;
      #20000 key_in0 = 1'b0;
      #20000 key_in0 = 1'b1;
      #20000 key_in0 = 1'b0;
      #20000 key_in0 = 1'b1;
      forever
        begin
          #1000 key_in0 = 1'b0;
          #1000000000 key_in0 = 1'b1;
        end
    end

  // key
  wire [7:0] key;
  assign key[7:2] = 6'd0;
  stabilizer stabilizer_0
    (
     .clk (clk),
     .reset (reset),
     .in (~key_in0),
     .out (key[0])
     );

  stabilizer stabilizer_1
    (
     .clk (clk),
     .reset (reset),
     .in (~key_in1),
     .out (key[1])
     );

  // connector
  localparam PLANES = 4;
  wire signed [32-1:0] count_h;
  wire signed [32-1:0] count_v;
  wire signed [32-1:0] color_all;
  wire [32-1:0] color [0:PLANES-1];
  reg [32-1:0]  layer [0:PLANES-1];
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
     .clk (clk),
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

     .synth_audio_ext_clka_exp (clk_audio),
     .synth_audio_ext_reseta_exp (reseta),
     .synth_audio_ext_audio_r_exp (audio_r),
     .synth_audio_ext_audio_l_exp (audio_l)
     );

endmodule
