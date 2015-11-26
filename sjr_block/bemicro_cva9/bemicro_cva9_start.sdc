#------------------------------------------------------------
create_clock -period "50.0 MHz" [get_ports DDR3_CLK_50MHZ]
create_clock -period "24.0 MHz" [get_ports CLK_24MHZ]

#------------------------------------------------------------
derive_pll_clocks

#------------------------------------------------------------
derive_clock_uncertainty

#------------------------------------------------------------
set_clock_groups -asynchronous -group av_pll_0|av_pll_inst|altera_pll_i|general[2].gpll~PLL_OUTPUT_COUNTER|divclk -group av_pll_0|av_pll_inst|altera_pll_i|general[0].gpll~PLL_OUTPUT_COUNTER|divclk

set_clock_groups -asynchronous -group av_pll_0|av_pll_inst|altera_pll_i|general[2].gpll~PLL_OUTPUT_COUNTER|divclk -group av_pll_0|av_pll_inst|altera_pll_i|general[1].gpll~PLL_OUTPUT_COUNTER|divclk
