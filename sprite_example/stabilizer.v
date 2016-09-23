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

module stabilizer
  #(
    parameter SYNC_DEPTH = 3,
    parameter LENGTH_IN_BITS = 4
    )
  (
   input  clk,
   input  reset,
   input  in,
   output out
   );

  // synchronize
  wire    in_s;

  shift_register_vector
    #(
      .WIDTH (1),
      .DEPTH (SYNC_DEPTH)
      )
  shift_register_vector_0
    (
     .clk (clk),
     .data_in (in),
     .data_out (in_s)
     );

  reg [LENGTH_IN_BITS-1:0] count;
  reg                      out_reg;
  reg                      out_pre;
  always @(posedge clk)
    begin
      if (reset == 1'b1)
        begin
          count <= 1'd0;
          out_reg <= 1'b0;
          out_pre <= 1'b0;
        end
      else
        begin
          if (out_pre != in_s)
            begin
              out_pre <= in_s;
              count <= 1'd0;
            end
          else
            begin
              count <= count + 1'd1;
              if (count == (1 << LENGTH_IN_BITS) - 1)
                begin
                  out_reg <= out_pre;
                end
            end
        end
    end
  assign out = out_reg;

endmodule
