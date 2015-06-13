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
  parameter C_OFFSET_WIDTH = 10;

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
  wire [C_OFFSET_WIDTH-1:0] offset_h_sync;
  wire [C_OFFSET_WIDTH-1:0] offset_v_sync;

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
          count_hp <= count_h + offset_h_sync;
          count_vp <= count_v + offset_v_sync;
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

  dual_port_ram
    #(
      .DATA_WIDTH (8),
      .ADDR_WIDTH (12)
      )
  vram0
    (
     .data_in (vram_idata),
     .read_addr (vram_raddr),
     .write_addr (vram_waddr),
     .we (vram_we),
     .read_clock (vram_rclock),
     .write_clock (vram_wclock),
     .data_out (vram_odata)
     );

  cdc_synchronizer
    #(
      .DATA_WIDTH (C_OFFSET_WIDTH)
      )
  sync_offset_h
    (
     .clk_in (clk),
     .clk_out (ext_clkv),
     .data_in (offset_h[C_OFFSET_WIDTH-1:0]),
     .data_out (offset_h_sync),
     .reset_in (reset)
     );

  cdc_synchronizer
    #(
      .DATA_WIDTH (C_OFFSET_WIDTH)
      )
  sync_offset_v
    (
     .clk_in (clk),
     .clk_out (ext_clkv),
     .data_in (offset_v[C_OFFSET_WIDTH-1:0]),
     .data_out (offset_v_sync),
     .reset_in (reset)
     );

  cdc_synchronizer
    #(
      .DATA_WIDTH (1)
      )
  sync_vsync
    (
     .clk_in (ext_clkv),
     .clk_out (clk),
     .data_in (ext_vga_vs),
     .data_out (vsync),
     .reset_in (ext_resetv)
     );
endmodule


// latency: 2 clk_in cycles + 3 clk_out cycles
// data_in value must be held for 4 clk_out cycles
module cdc_synchronizer
  #(
    parameter DATA_WIDTH=8
    )
  (
   input                     clk_in,
   input                     clk_out,
   input [(DATA_WIDTH-1):0]  data_in,
   output [(DATA_WIDTH-1):0] data_out,
   input                     reset_in
   );

  reg [(DATA_WIDTH-1):0]     data_in_reg;
  reg [(DATA_WIDTH-1):0]     data_out_reg[2:0];
  reg                        change_flag_in;
  reg [2:0]                  change_flag_out;

  always @(posedge clk_in)
    begin
      if (reset_in == 1'b1)
        begin
          change_flag_in <= 1'b0;
        end
      else if (data_in_reg != data_in)
        begin
          change_flag_in <= ~change_flag_in;
        end
      data_in_reg <= data_in;
    end

  always @(posedge clk_out)
    begin
      if (change_flag_out[2] == change_flag_out[1])
        begin
          data_out_reg[2] <= data_out_reg[1];
        end
    end

  always @(posedge clk_out)
    begin
      change_flag_out <= {change_flag_out[1:0], change_flag_in};
      data_out_reg[1] <= data_out_reg[0];
      data_out_reg[0] <= data_in_reg;
    end

  assign data_out = data_out_reg[2];
endmodule
