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
 
 KEY5: GPIO_Pin24: DIFF_RX_N8
 KEY4: GPIO_Pin26: DIFF_RX_N7
 KEY3: GPIO_Pin28: DIFF_RX_N6
 KEY2: GPIO_Pin32: DIFF_RX_N5
 KEY1: GPIO_Pin34: DIFF_RX_N4
 KEY0: GPIO_Pin36: DIFF_RX_N3
 
 AUDIO_R: GPIO_Pin38: DIFF_RX_N2
 AUDIO_L: GPIO_Pin40: DIFF_RX_N1
 
 VGA_R0: GPIO_Pin:2 GPIO2
 VGA_R1: GPIO_Pin:4 GPIO4
 VGA_G0: GPIO_Pin:6 GPIO6
 VGA_G1: GPIO_Pin:8 GPIO8
 VGA_B0: GPIO_Pin:10 GPIO_D
 VGA_B1: GPIO_Pin:14 DIFF_TX_N9
 VGA_HS: GPIO_Pin:16 LVDS_TX_O_P3
 VGA_VS: GPIO_Pin:18 LVDS_TX_O_P0
*/


module bemicro_cva9_start
  (
   input        CLK_24MHZ,
   output [7:0] USER_LED,
   input [1:0]  TACT,
   // KEY
   input        DIFF_RX_N8,
   input        DIFF_RX_N7,
   input        DIFF_RX_N6,
   input        DIFF_RX_N5,
   input        DIFF_RX_N4,
   input        DIFF_RX_N3,
   // AUDIO
   output       DIFF_RX_N2,
   output       DIFF_RX_N1,
   // VGA
   output       GPIO2,
   output       GPIO4,
   output       GPIO6,
   output       GPIO8,
   output       GPIO_D,
   output       DIFF_TX_N9,
   output       LVDS_TX_O_P3,
   output       LVDS_TX_O_P0
   );

  wire          clk_sys;
  wire          clk_video;
  wire          clk_audio;
  wire          pll_locked;

  // main
  wire          main_busy;
  wire          main_req;
  assign main_req = 1'b1;

  // key
  wire [7:0]    KEY_out;
  assign KEY_out = {2'b00,
                    DIFF_RX_N8,
                    DIFF_RX_N7,
                    DIFF_RX_N6,
                    DIFF_RX_N5,
                    DIFF_RX_N4,
                    DIFF_RX_N3};

  // red leds
  wire [15:0]   LEDR_in;
  assign USER_LED = ~LEDR_in[7:0];

  // audio output port
  wire          audio_r;
  wire          audio_l;
  assign DIFF_RX_N2 = audio_r;
  assign DIFF_RX_N1 = audio_l;

  // VGA port
  wire [7:0]    VGA_R_in;
  wire [7:0]    VGA_G_in;
  wire [7:0]    VGA_B_in;
  wire          VGA_HS_in;
  wire          VGA_VS_in;
  assign GPIO2 = VGA_R_in[2];
  assign GPIO4 = VGA_R_in[3];
  assign GPIO6 = VGA_G_in[2];
  assign GPIO8 = VGA_G_in[3];
  assign GPIO_D = VGA_B_in[2];
  assign DIFF_TX_N9 = VGA_B_in[3];
  assign LVDS_TX_O_P3 = VGA_HS_in;
  assign LVDS_TX_O_P0 = VGA_VS_in;

  // generate reset signal
  wire          RESET_N;
  reg           reset;
  reg           reset1;
  reg           resetv;
  reg           resetv1;
  reg           reseta;
  reg           reseta1;
  reg           resetpll;
  reg           resetpll1;
  assign RESET_N = TACT[0];

  always @(posedge CLK_24MHZ)
    begin
      resetpll1 <= ~RESET_N;
      resetpll <= resetpll1;
    end

  always @(posedge clk_sys)
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
     .refclk (CLK_24MHZ),
     .rst (resetpll),
     .outclk_0 (clk_video),
     .outclk_1 (clk_audio),
     .outclk_2 (clk_sys),
     .locked (pll_locked)
     );

  Sjr_Block Sjr_Block_0
    (
     .clk (clk_sys),
     .reset (reset),
     .vram_ext_clkv_exp (clk_video),
     .vram_ext_resetv_exp (resetv),
     .vram_ext_vga_hs_exp (VGA_HS_in),
     .vram_ext_vga_vs_exp (VGA_VS_in),
     .vram_ext_vga_de_exp (),
     .vram_ext_vga_r_exp (VGA_R_in),
     .vram_ext_vga_g_exp (VGA_G_in),
     .vram_ext_vga_b_exp (VGA_B_in),
     .psg_audio_ext_clka_exp (clk_audio),
     .psg_audio_ext_reseta_exp (reseta),
     .psg_audio_ext_audio_r_exp (audio_r),
     .psg_audio_ext_audio_l_exp (audio_l),
     .hexLED_ext_hex_led_exp (),
     .redLED_ext_red_led_exp (LEDR_in),
     .boardSW_ext_board_switch_exp (),
     .boardKey_ext_board_key_exp (KEY_out)
     );

endmodule
