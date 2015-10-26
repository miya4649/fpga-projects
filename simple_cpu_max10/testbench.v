`timescale 1ns / 1ps

module testbench;
  parameter STEP = 20; // 20ナノ秒：50MHz
  parameter TICKS = 20000;

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

  SimpleCPU SimpleCPU_0
    (
     .clk (clkt),
     .reset (reset),
     .class_led_0000_ext_red_led_exp (count)
     );

endmodule
