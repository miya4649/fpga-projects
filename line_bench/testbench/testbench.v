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

`timescale 1ns / 1ps

module testbench;
  localparam STEP = 20; // 20 ns: 50MHz
  localparam STEPV = 39; // 39 ns: 25.x MHz
  localparam TICKS = 100000;

  localparam TRUE = 1'b1;
  localparam FALSE = 1'b0;
  localparam ONE = 1'd1;
  localparam ZERO = 1'd0;

  localparam WIDTH_BITS = 6;
  localparam COLOR_BITS = 8;
  localparam COUNT_PERIOD = 50000000;

  reg clk;
  reg reset;
  reg clkv;
  reg resetv;
  integer i;
  wire [31:0] count;
  wire        vga_hs;
  wire        vga_vs;
  wire [3:0]  vga_r;
  wire [3:0]  vga_g;
  wire [3:0]  vga_b;

  initial
    begin
      $dumpfile("wave.vcd");
      $dumpvars(5, testbench);
      $monitor("time: %8d reset: %1d", $time, reset);
    end

  // generate clock signal
  initial
    begin
      clk = TRUE;
      forever
        begin
          #(STEP / 2) clk = ~clk;
        end
    end

  // generate reset signal
  initial
    begin
      reset = FALSE;
      repeat (2) @(posedge clk) reset <= TRUE;
      @(posedge clk) reset <= FALSE;
    end

  // generate clock v signal
  initial
    begin
      clkv = TRUE;
      forever
        begin
          #(STEPV / 2) clkv = ~clkv;
        end
    end

  // generate reset v signal
  initial
    begin
      resetv = FALSE;
      repeat (2) @(posedge clkv) resetv <= TRUE;
      @(posedge clkv) resetv <= FALSE;
    end

  // stop simulation after TICKS
  initial
    begin
      repeat (TICKS) @(posedge clk);
      $finish;
    end

  line_bench
    #(
      .WIDTH_BITS (WIDTH_BITS),
      .COLOR_BITS (COLOR_BITS),
      .COUNT_PERIOD (COUNT_PERIOD)
      )
  line_bench_0
    (
     .clk (clk),
     .reset (reset),
     .clkv (clkv),
     .resetv (resetv),
     .mode (1'b0),
     .count (count),
     .vga_hs (vga_hs),
     .vga_vs (vga_vs),
     .vga_r (vga_r),
     .vga_g (vga_g),
     .vga_b (vga_b)
     );

endmodule
