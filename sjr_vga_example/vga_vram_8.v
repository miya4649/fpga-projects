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

module vga_vram_8
  (
   input                    clk,
   input                    reset,
   output signed [32-1 : 0] data_length,
   input signed [32-1 : 0]  data_address,
   input signed [8-1 : 0]   data_din,
   output signed [8-1 : 0]  data_dout,
   input                    data_we,
   input                    data_oe,
   output                   vsync,
   input signed [32-1 : 0]  offset_h,
   input signed [32-1 : 0]  offset_v,
   input                    ext_clkv,
   input                    ext_resetv,
   output                   ext_vga_hs,
   output                   ext_vga_vs,
   output signed [8-1 : 0]  ext_vga_r,
   output signed [8-1 : 0]  ext_vga_g,
   output signed [8-1 : 0]  ext_vga_b
   );

  parameter C_VGA_MAX_H = 800;
  parameter C_VGA_MAX_V = 525;
  parameter C_VGA_WIDTH = 640;
  parameter C_VGA_HEIGHT = 480;
  parameter C_VGA_SYNC_H_START = 656;
  parameter C_VGA_SYNC_V_START = 490;
  parameter C_VGA_SYNC_H_END = 752;
  parameter C_VGA_SYNC_V_END = 492;

  reg [9:0]                 count_h;
  reg [9:0]                 count_v;
  reg [9:0]                 count_hp;
  reg [9:0]                 count_vp;
  wire                      vga_hs;
  wire                      vga_vs;
  wire                      pixel_valid;
  reg [1:0]                 vga_hs_delay;
  reg [1:0]                 vga_vs_delay;
  reg [1:0]                 pixel_valid_delay;

  // H counter
  always @(posedge ext_clkv)
    begin
      if (ext_resetv == 1'b1)
        begin
          count_h <= 1'd0;
        end
      else
        begin
          if (count_h < C_VGA_MAX_H)
            begin
              count_h <= count_h + 1'd1;
            end
          else
            begin
              count_h <= 1'd0;
            end
        end
    end

  // V counter
  always @(posedge ext_clkv)
    begin
      if (ext_resetv == 1'b1)
        begin
          count_v <= 1'd0;
        end
      else
        begin
          if (count_h == 0)
            begin
              if (count_v < C_VGA_MAX_V)
                begin
                  count_v <= count_v + 1'd1;
                end
              else
                begin
                  count_v <= 1'd0;
                end
            end
        end
    end

  // viewport
  always @(posedge ext_clkv)
    begin
      if (ext_resetv == 1'b1)
        begin
          count_hp <= 1'd0;
          count_vp <= 1'd0;
        end
      else
        begin
          count_hp <= count_h + offset_h;
          count_vp <= count_v + offset_v;
        end
    end

  // H sync
  assign vga_hs = ((count_h >= C_VGA_SYNC_H_START) && (count_h < C_VGA_SYNC_H_END)) ? 1'b0 : 1'b1;
  // V sync
  assign vga_vs = ((count_v >= C_VGA_SYNC_V_START) && (count_v < C_VGA_SYNC_V_END)) ? 1'b0 : 1'b1;
  // Pixel valid
  assign pixel_valid = ((count_h < C_VGA_WIDTH) && (count_v < C_VGA_HEIGHT)) ? 1'b1 : 1'b0;
  // delay (1 cycle)
  always @(posedge ext_clkv)
    begin
      vga_hs_delay <= {vga_hs_delay[0], vga_hs};
      vga_vs_delay <= {vga_vs_delay[0], vga_vs};
      pixel_valid_delay <= {pixel_valid_delay[0], pixel_valid};
    end

  // ext out
  assign ext_vga_r = pixel_valid_delay[1] ? {vram_odata[7:5], 1'b0} : 4'd0;
  assign ext_vga_g = pixel_valid_delay[1] ? {vram_odata[4:2], 1'b0} : 4'd0;
  assign ext_vga_b = pixel_valid_delay[1] ? {vram_odata[1:0], 2'b0} : 4'd0;
  assign ext_vga_hs = vga_hs_delay[1];
  assign ext_vga_vs = vga_vs_delay[1];

  // vsync out
  assign vsync = ext_vga_vs;

  // VRAM
  wire [7:0]  vram_idata;
  wire [7:0]  vram_odata;
  wire [11:0] vram_raddr;
  wire [11:0] vram_waddr;
  wire        vram_we;
  wire        vram_rclock;
  wire        vram_wclock;
  assign vram_rclock = ext_clkv;
  assign vram_wclock = clk;
  assign vram_raddr = {count_vp[9:4], count_hp[9:4]};
  assign data_length = 4096;
  assign vram_waddr = data_address;
  assign vram_idata = data_din;
  assign data_dout = 1'd0;
  assign vram_we = data_we;

  dual_port_ram vram0
    (
     .data_in (vram_idata),
     .read_addr (vram_raddr),
     .write_addr (vram_waddr),
     .we (vram_we),
     .read_clock (vram_rclock),
     .write_clock (vram_wclock),
     .data_out (vram_odata)
     );

endmodule


module dual_port_ram
  #(
    parameter DATA_WIDTH=8,
    parameter ADDR_WIDTH=4096
    )
  (
   input [(DATA_WIDTH-1):0]  data_in,
   input [(ADDR_WIDTH-1):0]  read_addr,
   input [(ADDR_WIDTH-1):0]  write_addr,
   input                     we,
   input                     read_clock,
   input                     write_clock,
   output [(DATA_WIDTH-1):0] data_out
   );

  reg [DATA_WIDTH-1:0]       ram[ADDR_WIDTH-1:0];
  reg [(ADDR_WIDTH-1):0]     read_addr_reg;

  assign data_out = ram[read_addr_reg];

  always @(posedge read_clock)
    begin
      read_addr_reg <= read_addr;
    end

  always @(posedge write_clock)
    begin
      if (we)
        begin
          ram[write_addr] <= data_in;
        end
    end
endmodule
