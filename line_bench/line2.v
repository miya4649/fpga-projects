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

module line2
  #(
    parameter WIDTH_BITS = 6,
    parameter COLOR_BITS = 8
    )
  (
   input signed [WIDTH_BITS:0]      x0,
   input signed [WIDTH_BITS:0]      y0,
   input signed [WIDTH_BITS:0]      x1,
   input signed [WIDTH_BITS:0]      y1,
   input [COLOR_BITS-1:0]           color_in,
   input                            clk,
   input                            reset,
   input                            start,
   output                           busy,
   output                           valid,
   output reg signed [WIDTH_BITS:0] x,
   output reg signed [WIDTH_BITS:0] y,
   output reg [COLOR_BITS-1:0]      color_out
   );

  localparam TRUE = 1'b1;
  localparam FALSE = 1'b0;
  localparam ONE = 1'd1;
  localparam ZERO = 1'd0;
  localparam S_ONE = 2'sd1;
  localparam S_MINUS_ONE = -2'sd1;
  localparam S_ZERO = 1'sd0;

  localparam COUNT_INIT = 2;
  localparam STATE_IDLE = 0;
  localparam STATE_START = 1;
  localparam STATE_INIT = 2;
  localparam STATE_RUN = 3;

  wire                              valid_end;
  reg signed [1:0]                  ix;
  reg signed [1:0]                  iy;
  reg signed [WIDTH_BITS:0]         dx_t1;
  reg signed [WIDTH_BITS:0]         dy_t1;
  reg signed [WIDTH_BITS:0]         dx;
  reg signed [WIDTH_BITS:0]         dy;
  reg signed [WIDTH_BITS+1:0]       dy2;
  reg signed [WIDTH_BITS*2:0]       erx;
  reg signed [WIDTH_BITS*2:0]       ery;
  reg signed [WIDTH_BITS*2:0]       ery2;
  reg signed [WIDTH_BITS:0]         xe;
  reg signed [WIDTH_BITS:0]         ye;
  reg [2:0]                         count;
  reg [1:0]                         state;

  function signed [WIDTH_BITS:0] myabs
    (
     input signed [WIDTH_BITS:0] abs_in
     );
    begin
      if (abs_in < S_ZERO)
        begin
          myabs = -abs_in;
        end
      else
        begin
          myabs = abs_in;
        end
    end
  endfunction

  always @(posedge clk)
    begin
      if (reset)
        begin
          state <= STATE_IDLE;
        end
      else
        begin
          case (state)
            STATE_START:
              begin
                state <= STATE_INIT;
              end
            STATE_INIT:
              begin
                if (count == ZERO)
                  begin
                    state <= STATE_RUN;
                  end
                else
                  begin
                    state <= STATE_INIT;
                  end
              end
            STATE_RUN:
              begin
                if (valid_end)
                  begin
                    state <= STATE_IDLE;
                  end
                else
                  begin
                    state <= STATE_RUN;
                  end
              end
            // STATE_IDLE:
            default:
              begin
                if (start)
                  begin
                    state <= STATE_START;
                  end
                else
                  begin
                    state <= STATE_IDLE;
                  end
              end
          endcase
        end
    end

  always @(posedge clk)
    begin
      if (reset)
        begin
          count <= ZERO;
        end
      else
        begin
          if (state == STATE_INIT)
            begin
              count <= count - ONE;
            end
          else
            begin
              count <= COUNT_INIT;
            end
        end
    end

  always @(posedge clk)
    begin
      if (reset)
        begin
          dx_t1 <= S_ZERO;
          dy_t1 <= S_ZERO;
          xe <= S_ZERO;
          ye <= S_ZERO;
          color_out <= ZERO;
        end
      else
        begin
          if (state == STATE_START)
            begin
              dx_t1 <= x1 - x0;
              dy_t1 <= y1 - y0;
              xe <= x1;
              ye <= y1;
              color_out <= color_in;
            end
        end
    end

  always @(posedge clk)
    begin
      if (reset)
        begin
          dx <= S_ZERO;
          dy <= S_ZERO;
          dy2 <= S_ZERO;
        end
      else
        begin
          dx <= myabs(dx_t1);
          dy <= myabs(dy_t1);
          dy2 <= dy << 1;
        end
    end

  always @(posedge clk)
    begin
      if (state == STATE_START)
        begin
          if (x0 > x1)
            begin
              ix <= -2'sd1;
            end
          else
            begin
              ix <= 2'sd1;
            end
        end
    end

  always @(posedge clk)
    begin
      if (state == STATE_START)
        begin
          if (y0 > y1)
            begin
              iy <= -2'sd1;
            end
          else
            begin
              iy <= 2'sd1;
            end
        end
    end

  always @(posedge clk)
    begin
      if (reset)
        begin
          x <= S_ZERO;
          y <= S_ZERO;
          erx <= S_ZERO;
          ery <= S_ZERO;
          ery2 <= S_ZERO;
        end
      else
        begin
          case (state)
            STATE_START:
              begin
                x <= x0;
                y <= y0;
                erx <= S_ZERO;
                ery <= S_ZERO;
                ery2 <= S_ZERO;
              end
            STATE_INIT:
              begin
                erx <= dx >> 1;
                ery <= dy >> 1;
                ery2 <= ery + dy2;
              end
            STATE_RUN:
              begin
                if ((erx == ery) || ((erx > ery) && (ery2 > erx)))
                  begin
                    ery2 <= ery + dy2;
                    x <= x + ix;
                    y <= y + iy;
                    erx <= erx + dx;
                    ery <= ery + dy;
                  end
                else if (erx > ery)
                  begin
                    ery2 <= ery + dy2;
                    x <= x + ix;
                    ery <= ery + dy;
                  end
                else if (ery > erx)
                  begin
                    y <= y + iy;
                    erx <= erx + dx;
                  end
              end
          endcase
        end
    end

  assign valid_end = (((x == xe) && (dx >= dy)) || ((y == ye) && (dy > dx))) ? 1'b1 : 1'b0;
  assign valid = ((state == STATE_RUN) & (~valid_end)) ? 1'b1 : 1'b0;
  assign busy = (state == STATE_IDLE) ? 1'b0 : 1'b1;

endmodule
