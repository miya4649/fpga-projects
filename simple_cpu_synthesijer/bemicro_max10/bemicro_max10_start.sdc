#------------------------------------------------------------
create_clock -period "50.0 MHz" [get_ports SYS_CLK]
create_clock -period "24.0 MHz" [get_ports USER_CLK]

#------------------------------------------------------------
derive_pll_clocks

#------------------------------------------------------------
derive_clock_uncertainty

#------------------------------------------------------------
