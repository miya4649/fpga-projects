/*
  Copyright (c) 2018, miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

// simple rand
// random = random * 60061 + 55511;

module rand
  #(
    parameter WIDTH_D = 32
    )
  (
   input                    clk,
   input                    reset,
   input                    next,
   input [WIDTH_D-1:0]      seed,
   input                    seed_we,
   output reg               valid,
   output reg [WIDTH_D-1:0] data
   );

  localparam TRUE = 1'b1;
  localparam FALSE = 1'b0;
  localparam ONE = 1'd1;
  localparam ZERO = 1'd0;

  localparam DEFAULT_SEED = 'hdeadbeef;
  localparam A = 60061;
  localparam B = 55511;

  reg                       next_d1;
  reg [WIDTH_D-1:0]         t1;
  
  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          data <= DEFAULT_SEED;
        end
      else
        begin
          if (seed_we)
            begin
              data <= seed;
            end
          else if (next_d1)
            begin
              data <= t1 + B;
            end
          else
            begin
              data <= data;
            end
        end
    end
  
  always @(posedge clk)
    begin
      next_d1 <= next;
    end

  always @(posedge clk)
    begin
      if (next)
        begin
          t1 <= data * A;
        end
      else
        begin
          t1 <= t1;
        end
    end

  always @(posedge clk)
    begin
      if (reset)
        begin
          valid <= TRUE;
        end
      else
        begin
          if (valid & next)
            begin
              valid <= FALSE;
            end
          else if (next_d1)
            begin
              valid <= TRUE;
            end
          else
            begin
              valid <= valid;
            end
        end
    end

endmodule
