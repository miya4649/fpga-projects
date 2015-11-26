SYNTHESIJER?=$(lastword $(sort $(wildcard $(HOME)/synthesijer/synthesijer*.jar)))

SYNTHESIJER_LIB?=$(lastword $(sort $(wildcard $(HOME)/synthesijer/synthesijer_lib_????????)))

SYNTHESIJER_EXTRA_LIB?=$(lastword $(sort $(wildcard $(HOME)/synthesijer/synthesijer_extra-libs_????????)))

CLASSPATH=$(SYNTHESIJER):$(SYNTHESIJER_EXTRA_LIB)/bin:.

LIB_SJR_VERILOG=singleportram.v dualportram.v synthesijer_logic_rshift32.v

EXLIB_SJR_VERILOG=

SOURCES_SUB=AudioOutput.java BoardKey.java BoardSwitch.java HexLED.java RedLED.java SoundGenerator.java VgaVram8.java

SOURCES=$(SOURCES_SUB) Sjr_Block.java

SOURCES_MINI=$(SOURCES_SUB) Sjr_Block_Mini.java


all: $(SOURCES) $(LIB_SJR_VERILOG) $(EXLIB_SJR_VERILOG)
	java -cp $(CLASSPATH) synthesijer.Main --chaining --operation-strength-reduction --verilog $(SOURCES)

mini: $(SOURCES_MINI) $(LIB_SJR_VERILOG) $(EXLIB_SJR_VERILOG)
	java -cp $(CLASSPATH) synthesijer.Main --chaining --operation-strength-reduction --verilog $(SOURCES_MINI)

$(LIB_SJR_VERILOG):
	cp $(SYNTHESIJER_LIB)/verilog/$@ ./

$(EXLIB_SJR_VERILOG):
	cp $(SYNTHESIJER_EXTRA_LIB)/hdl_lib/verilog/$@ ./

clean:
	-rm -f *.class
	-rm -f *.v
	-rm -f *.vhd
	-rm -f *.xml
	-rm -f *.dot
	-rm -f *.o
	-rm -f sim
	-rm -f *.vcd
	-rm -f *.txt
	-rm -f *.ir
