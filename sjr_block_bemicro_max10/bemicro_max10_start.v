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


/*
 Video Audio Controller Interface Board
 http://cellspe.matrix.jp/zerofpga/avinterface.html

 KEY5: GPIO_J5_24
 KEY4: GPIO_J5_26
 KEY3: GPIO_J5_28
 KEY2: GPIO_J5_32
 KEY1: GPIO_J5_34
 KEY0: GPIO_J5_36

 AUDIO_R: GPIO_J5_38
 AUDIO_L: GPIO_J5_40

 VGA_R0: GPIO_J5_02
 VGA_R1: GPIO_J5_04
 VGA_G0: GPIO_J5_06
 VGA_G1: GPIO_J5_08
 VGA_B0: GPIO_J5_10
 VGA_B1: GPIO_J5_14
 VGA_HS: GPIO_J5_16
 VGA_VS: GPIO_J5_18
*/


module bemicro_max10_start
  (
   input       SYS_CLK,
   input [0:0] PB,
   // KEY
   input       GPIO_J5_24,
   input       GPIO_J5_26,
   input       GPIO_J5_28,
   input       GPIO_J5_32,
   input       GPIO_J5_34,
   input       GPIO_J5_36,
   // AUDIO
   output      GPIO_J5_38,
   output      GPIO_J5_40,
   // VGA
   output      GPIO_J5_02,
   output      GPIO_J5_04,
   output      GPIO_J5_06,
   output      GPIO_J5_08,
   output      GPIO_J5_10,
   output      GPIO_J5_14,
   output      GPIO_J5_16,
   output      GPIO_J5_18
   );

  wire         clk_video;
  wire         clk_audio;
  wire         pll_locked;

  // key
  wire [7:0]   KEY_out;
  assign KEY_out = {2'b00,
                    GPIO_J5_24,
                    GPIO_J5_26,
                    GPIO_J5_28,
                    GPIO_J5_32,
                    GPIO_J5_34,
                    GPIO_J5_36};

  // audio output port
  wire         audio_r;
  wire         audio_l;
  assign GPIO_J5_38 = audio_r;
  assign GPIO_J5_40 = audio_l;

  // VGA port
  wire [7:0]   VGA_R_in;
  wire [7:0]   VGA_G_in;
  wire [7:0]   VGA_B_in;
  wire         VGA_HS_in;
  wire         VGA_VS_in;
  assign GPIO_J5_02 = VGA_R_in[2];
  assign GPIO_J5_04 = VGA_R_in[3];
  assign GPIO_J5_06 = VGA_G_in[2];
  assign GPIO_J5_08 = VGA_G_in[3];
  assign GPIO_J5_10 = VGA_B_in[2];
  assign GPIO_J5_14 = VGA_B_in[3];
  assign GPIO_J5_16 = VGA_HS_in;
  assign GPIO_J5_18 = VGA_VS_in;

  // generate reset signal
  wire         RESET_N;
  reg          reset;
  reg          reset1;
  reg          resetv;
  reg          resetv1;
  reg          reseta;
  reg          reseta1;
  reg          resetpll;
  reg          resetpll1;
  assign RESET_N = PB[0];

  always @(posedge SYS_CLK)
    begin
      resetpll1 <= ~RESET_N;
      resetpll <= resetpll1;
    end

  always @(posedge SYS_CLK)
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
     .inclk0 (SYS_CLK),
     .areset (resetpll),
     .c0 (clk_video),
     .c1 (clk_audio),
     .locked (pll_locked)
     );

  Sjr_Top sjr_top0
    (
     .clk (SYS_CLK),
     .reset (reset),
     .class_obj_0000_class_vram_0022_ext_clkv_exp_exp (clk_video),
     .class_obj_0000_class_vram_0022_ext_resetv_exp_exp (resetv),
     .class_obj_0000_class_vram_0022_ext_vga_hs_exp_exp (VGA_HS_in),
     .class_obj_0000_class_vram_0022_ext_vga_vs_exp_exp (VGA_VS_in),
     .class_obj_0000_class_vram_0022_ext_vga_de_exp_exp (),
     .class_obj_0000_class_vram_0022_ext_vga_r_exp_exp (VGA_R_in),
     .class_obj_0000_class_vram_0022_ext_vga_g_exp_exp (VGA_G_in),
     .class_obj_0000_class_vram_0022_ext_vga_b_exp_exp (VGA_B_in),
     .class_obj_0000_class_psg_0024_class_audio_0003_ext_clka_exp_exp_exp (clk_audio),
     .class_obj_0000_class_psg_0024_class_audio_0003_ext_reseta_exp_exp_exp (reseta),
     .class_obj_0000_class_psg_0024_class_audio_0003_ext_audio_r_exp_exp_exp (audio_r),
     .class_obj_0000_class_psg_0024_class_audio_0003_ext_audio_l_exp_exp_exp (audio_l),
     .class_obj_0000_class_boardKey_0026_ext_board_key_exp_exp (KEY_out)
     );

endmodule
