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

`timescale 1ns / 1ps

module testbench;
  parameter STEP = 20; // 20ナノ秒：50MHz
  parameter TICKS = 4000;

  reg clkt;
  reg reset;
  wire [31:0] count;

  initial
    begin
      $dumpfile("wave.vcd");
      $dumpvars(5, testbench);
      $monitor("count: %d", count);
    end

  // クロックを生成
  initial
    begin
      clkt = 1'b1;
      forever
        begin
          #(STEP / 2) clkt = ~clkt;
        end
    end

  // 同期リセット信号を生成
  initial
    begin
      reset = 1'b0;
      repeat (2) @(posedge clkt) reset <= 1'b1;
      @(posedge clkt) reset <= 1'b0;
    end

  // 指定クロックでシミュレーションを終了させる
  initial
    begin
      repeat (TICKS) @(posedge clkt);
      $finish;
    end

  wire [7:0]  rom_addr;
  wire [31:0] rom_data;

  rom rom_0
    (
     .clk (clkt),
     .addr (rom_addr),
     .data_out (rom_data)
     );

  simple_cpu simple_cpu_0
    (
     .clk (clkt),
     .reset (reset),
     .rom_addr (rom_addr),
     .rom_data (rom_data),
     .port_in (),
     .port_out (count)
     );

endmodule
