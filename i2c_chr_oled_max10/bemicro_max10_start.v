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

module bemicro_max10_start
  (
   input SYS_CLK,
   input [3:0] PB,
   inout I2C_SCL,
   inout I2C_SDA
   );

  // generate reset signal (push button 1)
  wire   reset;
  reg    reset_reg1;
  reg    reset_reg2;
  assign reset = reset_reg2;

  always @(posedge SYS_CLK)
    begin
      reset_reg1 <= ~PB[0];
      reset_reg2 <= reset_reg1;
    end

  // I2C pin
  wire scl_i;
  wire scl_o;
  wire scl_oen;
  wire sda_i;
  wire sda_o;
  wire sda_oen;

  assign I2C_SCL = scl_oen ? 1'bz : scl_o;
  assign I2C_SDA = sda_oen ? 1'bz : sda_o;

  assign scl_i = I2C_SCL;
  assign sda_i = I2C_SDA;

  Sjr_I2C_OLED Sjr_I2C_OLED_0
    (
     .clk (SYS_CLK),
     .reset (reset),
     .class_i2c_if_0000_class_obj_0000_arst_i_exp_exp (1'b1),
     .class_i2c_if_0000_class_obj_0000_scl_pad_i_exp_exp (scl_i),
     .class_i2c_if_0000_class_obj_0000_scl_pad_o_exp_exp (scl_o),
     .class_i2c_if_0000_class_obj_0000_scl_padoen_o_exp_exp (scl_oen),
     .class_i2c_if_0000_class_obj_0000_sda_pad_i_exp_exp (sda_i),
     .class_i2c_if_0000_class_obj_0000_sda_pad_o_exp_exp (sda_o),
     .class_i2c_if_0000_class_obj_0000_sda_padoen_o_exp_exp (sda_oen)
     );

endmodule
