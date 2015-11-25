`timescale 1ns / 1ps

module testbench;
  parameter STEP = 20; // 50MHz
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

  // generate clock
  initial
    begin
      clkt = 1'b1;
      forever
        begin
          #(STEP / 2) clkt = ~clkt;
        end
    end

  // generate reset signal
  initial
    begin
      reset = 1'b0;
      repeat (2) @(posedge clkt) reset <= 1'b1;
      @(posedge clkt) reset <= 1'b0;
    end

  // stop simulation in TICKS clock
  initial
    begin
      repeat (TICKS) @(posedge clkt);
      $finish;
    end

  SimpleCPU SimpleCPU_0
    (
     .clk (clkt),
     .reset (reset),
     .port_out_in (32'd0),
     .port_out_we (1'b0),
     .port_out_out (count),
     .port_in_in (32'd0),
     .port_in_we (1'b0),
     .port_in_out ()
     );

endmodule
