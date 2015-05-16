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

module sjr_vga_example
  (
   input        CLOCK_50,
   input [1:0]  CLOCK_27,
   input [3:0]  KEY,
   output       VGA_HS,
   output       VGA_VS,
   output [3:0] VGA_R,
   output [3:0] VGA_G,
   output [3:0] VGA_B
   );

  wire          clk_video;
  wire          clk_audio;

  reg           areset;
  reg           aresetv;

  // synchronize reset
  always @(posedge CLOCK_50)
    begin
      areset <= ~KEY[0];
    end

  always @(posedge clk_video)
    begin
      aresetv <= ~KEY[0];
    end


  vga_pll vga_pll_0
    (
     .inclk0 (CLOCK_27[1]),
     .c0 (clk_video),
     .c1 (clk_audio)
     );


  Sjr_Top sjr_top0
    (
     .clk (CLOCK_50),
     .reset (areset),
     .class_obj_0000_class_vram_0000_ext_clkv_exp_exp (clk_video),
     .class_obj_0000_class_vram_0000_ext_resetv_exp_exp (aresetv),
     .class_obj_0000_class_vram_0000_ext_vga_hs_exp_exp (VGA_HS),
     .class_obj_0000_class_vram_0000_ext_vga_vs_exp_exp (VGA_VS),
     .class_obj_0000_class_vram_0000_ext_vga_r_exp_exp (VGA_R),
     .class_obj_0000_class_vram_0000_ext_vga_g_exp_exp (VGA_G),
     .class_obj_0000_class_vram_0000_ext_vga_b_exp_exp (VGA_B)
     );

endmodule

