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

module cdc_fifo
  #(
    parameter DATA_WIDTH = 8,
    parameter ADDR_WIDTH = 8
    )
  (
   // -------- clock domain: read  --------
   input                   clk_cr,
   output [DATA_WIDTH-1:0] data_cr,
   input                   req_cr,
   output                  empty_cr,
   // -------- clock domain: write --------
   input                   clk_cw,
   input [DATA_WIDTH-1:0]  data_cw,
   input                   req_cw,
   output                  full_cw
   );

  // binary to gray-code conversion
  function [DATA_WIDTH-1:0] bin2gray
    (
     input [DATA_WIDTH-1:0] data_in
     );
    begin
      bin2gray = {1'b0, data_in[DATA_WIDTH-1:1]} ^ data_in[DATA_WIDTH-1:0];
    end
  endfunction

  // -------- clock domain: read  --------
  reg  [ADDR_WIDTH-1:0] addr_r_cr = 1'd0;
  reg [ADDR_WIDTH-1:0]  addr_r_gray_cr = 1'd0;
  wire [ADDR_WIDTH-1:0] addr_r_next_cr;
  wire [ADDR_WIDTH-1:0] addr_w_sync_cr;
  assign addr_r_next_cr = addr_r_cr + 1'd1;
  assign empty_cr = (addr_r_gray_cr == addr_w_sync_cr) ? 1'b1 : 1'b0;

  always @(posedge clk_cr)
    begin
      if ((req_cr == 1'b1) && (empty_cr == 1'b0))
        begin
          addr_r_cr <= addr_r_next_cr;
          addr_r_gray_cr <= bin2gray(addr_r_next_cr);
        end
    end

  // -------- clock domain: write --------
  reg  [ADDR_WIDTH-1:0] addr_w_cw = 1'd0;
  wire [ADDR_WIDTH-1:0] addr_w_next_gray_cw;
  wire [ADDR_WIDTH-1:0] addr_r_gray_cw;
  wire                  we_cw;
  wire [ADDR_WIDTH-1:0] addr_w_next_cw;
  wire [ADDR_WIDTH-1:0] addr_r_sync_cw;
  reg [ADDR_WIDTH-1:0]  addr_w_gray_cw = 1'd0;
  assign addr_w_next_cw = addr_w_cw + 1'd1;
  assign full_cw = (addr_w_next_gray_cw == addr_r_sync_cw) ? 1'b1 : 1'b0;
  assign we_cw = ((req_cw == 1'b1) && (full_cw == 1'b0)) ? 1'b1 : 1'b0;
  assign addr_w_next_gray_cw = bin2gray(addr_w_next_cw);

  always @(posedge clk_cw)
    begin
      if (we_cw == 1'b1)
        begin
          addr_w_cw <= addr_w_next_cw;
          addr_w_gray_cw <= addr_w_next_gray_cw;
        end
    end

  synchronizer
    #(
      .DATA_WIDTH (ADDR_WIDTH)
      )
  synchronizer_0
    (
     .clk_out (clk_cw),
     .data_in (addr_r_gray_cr),
     .data_out (addr_r_sync_cw)
     );

  synchronizer
    #(
      .DATA_WIDTH (ADDR_WIDTH)
      )
  synchronizer_1
    (
     .clk_out (clk_cr),
     .data_in (addr_w_gray_cw),
     .data_out (addr_w_sync_cr)
     );

  dual_port_ram
    #(
      .DATA_WIDTH (DATA_WIDTH),
      .ADDR_WIDTH (ADDR_WIDTH)
      )
  dual_port_ram_0
    (
     .data_in (data_cw),
     .read_addr (addr_r_cr),
     .write_addr (addr_w_cw),
     .we (we_cw),
     .read_clock (clk_cr),
     .write_clock (clk_cw),
     .data_out (data_cr)
     );

endmodule
