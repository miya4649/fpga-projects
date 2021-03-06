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

module sjr_vga_example_de0_cv
  (
   input        CLOCK_50,
   input [3:0]  KEY,
   output       VGA_HS,
   output       VGA_VS,
   output [3:0] VGA_R,
   output [3:0] VGA_G,
   output [3:0] VGA_B
   );

  wire          clk_video;
  wire          clk_audio;

  // main
  wire          main_busy;
  wire          main_req;
  assign main_req = 1'b1;

  // synchronize reset
  reg           areset;
  reg           aresetv;
  reg           areset1;
  reg           aresetv1;

  always @(posedge CLOCK_50)
    begin
      areset1 <= ~KEY[0];
      areset <= areset1;
    end

  always @(posedge clk_video)
    begin
      aresetv1 <= ~KEY[0];
      aresetv <= aresetv1;
    end


  av_pll av_pll_0
    (
     .refclk (CLOCK_50),
     .rst (areset),
     .outclk_0 (clk_video),
     .outclk_1 (clk_audio)
     );


  Sjr_Top sjr_top0
    (
     .clk (CLOCK_50),
     .reset (areset),
     .obj_vram_ext_clkv_exp (clk_video),
     .obj_vram_ext_resetv_exp (aresetv),
     .obj_vram_ext_vga_hs_exp (VGA_HS),
     .obj_vram_ext_vga_vs_exp (VGA_VS),
     .obj_vram_ext_vga_de_exp (),
     .obj_vram_ext_vga_r_exp (VGA_R),
     .obj_vram_ext_vga_g_exp (VGA_G),
     .obj_vram_ext_vga_b_exp (VGA_B)
     );

endmodule

