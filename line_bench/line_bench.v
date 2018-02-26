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

module line_bench
  #(
    parameter WIDTH_BITS = 6,
    parameter COLOR_BITS = 8,
    parameter COUNT_PERIOD = 50000000
    )
  (
   input                    clk,
   input                    reset,
   input                    clkv,
   input                    resetv,
   input                    mode,
   output reg signed [31:0] count,
   output                   vga_hs,
   output                   vga_vs,
   output [3:0]             vga_r,
   output [3:0]             vga_g,
   output [3:0]             vga_b
   );

  localparam TRUE = 1'b1;
  localparam FALSE = 1'b0;
  localparam ONE = 1'd1;
  localparam ZERO = 1'd0;
  localparam S_ONE = 2'sd1;
  localparam S_MONE = -2'sd1;

  localparam SPRITE_X = 64;
  localparam SPRITE_Y = 0;
  localparam SPRITE_SCALE = 5;

  localparam RAND_WIDTH = 32;

  integer                   i;

  // cycle_count
  reg [31:0]                cycle_count;
  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          cycle_count <= ZERO;
        end
      else
        if (cycle_count == ZERO)
          begin
            cycle_count <= COUNT_PERIOD - 1;
          end
        else
          begin
            cycle_count <= cycle_count - ONE;
          end
    end

  // run_done one shot
  reg run_done_d1;
  wire run_done_oneshot;
  always @(posedge clk)
    begin
      run_done_d1 <= run_done;
    end
  assign run_done_oneshot = ((run_done == TRUE) && (run_done_d1 == FALSE)) ? TRUE : FALSE;

  // line count
  reg [31:0]     line_count;
  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          line_count <= ZERO;
          count <= ZERO;
        end
      else
        begin
          if (cycle_count == ZERO)
            begin
              count <= line_count;
              line_count <= ZERO;
            end
          else if (run_done_oneshot == TRUE)
            begin
              line_count <= line_count + ONE;
            end
        end
    end

  // line bench state machine
  reg [1:0]      state;
  localparam STATE_IDLE = 0;
  localparam STATE_START = 1;
  localparam STATE_RUN = 2;
  wire           go_next;
  assign go_next = ((mode == FALSE) || (cycle_count == ZERO)) ? TRUE : FALSE;

  always @(posedge clk)
    begin
      if (reset == TRUE)
        begin
          state <= STATE_IDLE;
        end
      else
        begin
          case (state)
            STATE_START:
              begin
                if (start_done == TRUE)
                  begin
                    state <= STATE_RUN;
                  end
                else
                  begin
                    state <= STATE_START;
                  end
              end
            STATE_RUN:
              begin
                if (run_done == TRUE)
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
                if ((line_busy == FALSE) && (go_next == TRUE))
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

  // bench start
  reg start_done;
  reg [RAND_WIDTH-1:0] rand_sample;
  always @(posedge clk)
    begin
      if ((state == STATE_START) && (rand_valid == TRUE))
        begin
          rand_sample <= rand_data;
          start_done <= TRUE;
          line_start <= TRUE;
        end
      else
        begin
          rand_sample <= rand_sample;
          start_done <= FALSE;
          line_start <= FALSE;
        end
    end

  // bench run
  reg run_done;
  always @(posedge clk)
    begin
      if ((state == STATE_RUN) && (line_busy == FALSE))
        begin
          run_done <= TRUE;
        end
      else
        begin
          run_done <= FALSE;
        end
    end

  // vga
  wire                  vga_vsync;
  wire [32-1:0]         vga_vcount;
  wire [32-1:0]         ext_vga_count_h;
  wire [32-1:0]         ext_vga_count_v;
  wire [8-1:0]          ext_vga_color;

  // layer
  localparam PLANES = 1;
  wire signed [32-1:0]  color_all;
  wire signed [32-1:0]  color [0:PLANES-1];
  reg signed [32-1:0]   layer [0:PLANES-1];
  always @*
    begin
      layer[0] = (color[0] == 0) ? 0 : color[0];
      for (i = 0; i < PLANES-1; i = i + 1)
        begin
          layer[i+1] = (color[i+1] == 0) ? layer[i] : color[i+1];
        end
    end
  assign color_all = layer[PLANES-1]; // color_all: delay 8

  wire [WIDTH_BITS-1:0] line_x;
  wire [WIDTH_BITS-1:0] line_y;
  wire                  line_busy;
  wire                  line_valid;
  wire [COLOR_BITS-1:0] line_color;
  reg                   line_valid1;
  reg                   line_we;
  reg [WIDTH_BITS-1:0]  line_x2;
  reg [WIDTH_BITS*2-1:0] line_y2;
  reg [WIDTH_BITS*2-1:0] line_addr;
  reg                    line_start;

  always @(posedge clk)
    begin
      line_addr <= line_x2 + line_y2;
      line_x2 <= line_x;
      line_y2 <= {line_y, 6'b0};
      line_we <= line_valid1;
      line_valid1 <= line_valid;
    end

`ifdef USE_V2
  line2
`else
  line
`endif
    #(
      .WIDTH_BITS (WIDTH_BITS)
      )
  line_0
    (
     // input
     .x0 (rand_sample[23:18]),
     .y0 (rand_sample[17:12]),
     .x1 (rand_sample[11:6]),
     .y1 (rand_sample[5:0]),
     .color_in (rand_sample[31:24]),
     .clk (clk),
     .reset (reset),
     .start (line_start),
     // output
     .busy (line_busy),
     .valid (line_valid),
     .x (line_x),
     .y (line_y),
     .color_out (line_color)
     );

  sprite
    #(
      .SPRITE_SIZE_BITS (WIDTH_BITS)
      )
  sprite_0
    (
     .clk (clk),
     .reset (reset),

     .bitmap_length (),
     .bitmap_address (line_addr),
     .bitmap_din (line_color),
     .bitmap_dout (),
     .bitmap_we (line_we),
     .bitmap_oe (FALSE),

     .x (SPRITE_X),
     .y (SPRITE_Y),
     .scale (SPRITE_SCALE),

     .ext_clkv (clkv),
     .ext_resetv (resetv),
     .ext_color (color[0]),
     .ext_count_h (ext_vga_count_h),
     .ext_count_v (ext_vga_count_v)
     );

  vga_iface vga_iface_0
    (
     .clk (clk),
     .reset (reset),

     .vsync (vga_vsync),
     .vcount (vga_vcount),
     .ext_clkv (clkv),
     .ext_resetv (resetv),
     .ext_color (color_all),
     .ext_vga_hs (vga_hs),
     .ext_vga_vs (vga_vs),
     .ext_vga_de (),
     .ext_vga_r (vga_r),
     .ext_vga_g (vga_g),
     .ext_vga_b (vga_b),
     .ext_count_h (ext_vga_count_h),
     .ext_count_v (ext_vga_count_v)
     );

  reg rand_next;
  wire rand_valid;
  wire [RAND_WIDTH-1:0] rand_data;

  rand
    #(
      .WIDTH_D (RAND_WIDTH)
      )
  rand_0
    (
     .clk (clk),
     .reset (reset),
     .next (1'b1),
     .seed (1'b0),
     .seed_we (1'b0),
     .valid (rand_valid),
     .data (rand_data)
     );

endmodule
