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

module audio_output
  (
   input        clk,
   input        reset,
   input [31:0] data,
   input        valid_toggle,
   output       full,
   input        ext_clka,
   input        ext_reseta,
   output       ext_audio_r,
   output       ext_audio_l
   );

  // data read frequency: 18MHz / 48000Hz = 375 (-1)
  parameter READ_FREQ = 374;
  parameter FIFO_DEPTH_IN_BITS = 3;

  // data write
  reg           toggle;
  reg           toggle_prev;
  reg [31:0]    data_in;
  wire          req_cw;

  assign req_cw = ((toggle_prev != toggle) && (full == 1'b0)) ? 1'b1 : 1'b0;

  always @(posedge clk)
    begin
      if (reset == 1'b1)
        begin
          toggle <= 1'b0;
          data_in <= 1'd0;
          toggle_prev <= 1'b0;
        end
      else
        begin
          toggle <= valid_toggle;
          data_in <= data;
          toggle_prev <= toggle;
        end
    end

  // data read
  wire          req_ca;
  wire          empty_ca;
  reg [15+1:0]  sample_sum_r_ca;
  reg [15+1:0]  sample_sum_l_ca;
  wire [31:0]   data_out_ca;
  reg [31:0]    data_out_reg_ca;
  reg           data_valid_ca;
  reg [8:0]     read_freq_counter;

  assign req_ca = ((empty_ca == 1'b0) && (read_freq_counter == 9'd0)) ? 1'b1 : 1'b0;
  assign ext_audio_r = sample_sum_r_ca[15+1];
  assign ext_audio_l = sample_sum_l_ca[15+1];

  always @(posedge ext_clka)
    begin
      if (ext_reseta == 1'b1)
        begin
          sample_sum_r_ca <= 1'd0;
          sample_sum_l_ca <= 1'd0;
          data_valid_ca <= 1'b0;
          data_out_reg_ca <=  1'd0;
          read_freq_counter <= 1'd0;
        end
      else
        begin
          // read data delays 1 cycle
          data_valid_ca <= req_ca;
          if (data_valid_ca)
            begin
              data_out_reg_ca <= data_out_ca;
            end
          // delta sigma
          sample_sum_r_ca <= sample_sum_r_ca[15:0] + data_out_reg_ca[31:16];
          sample_sum_l_ca <= sample_sum_l_ca[15:0] + data_out_reg_ca[15:0];
          // read data once per READ_FREQ+1 cycles
          if (read_freq_counter == 9'd0)
            begin
              read_freq_counter <= READ_FREQ;
            end
          else
            begin
              read_freq_counter <= read_freq_counter - 1'd1;
            end
        end
    end

  cdc_fifo
    #(
      .DATA_WIDTH (32),
      .ADDR_WIDTH (FIFO_DEPTH_IN_BITS)
      )
  cdc_fifo_0
    (
     .clk_cr (ext_clka),
     .data_cr (data_out_ca),
     .req_cr (req_ca),
     .empty_cr (empty_ca),
     .clk_cw (clk),
     .data_cw (data_in),
     .req_cw (req_cw),
     .full_cw (full)
     );

endmodule
