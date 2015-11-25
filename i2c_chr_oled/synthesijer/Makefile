SYNTHESIJER?=$(lastword $(sort $(wildcard $(HOME)/synthesijer/synthesijer*.jar)))

SYNTHESIJER_LIB?=$(lastword $(sort $(wildcard $(HOME)/synthesijer/synthesijer_lib_????????)))

SYNTHESIJER_EXTRA_LIB?=$(lastword $(sort $(wildcard $(HOME)/synthesijer/synthesijer_extra-libs_????????)))

CLASSPATH=$(SYNTHESIJER):$(SYNTHESIJER_EXTRA_LIB)/bin:.

LIB_SJR_VERILOG=

EXLIB_SJR_VERILOG=

SOURCES=*.java $(SYNTHESIJER_EXTRA_LIB)/src/synthesijer/lib/wishbone/OpenCoresI2CMaster*.java


all: $(SOURCES) $(LIB_SJR_VERILOG) $(EXLIB_SJR_VERILOG)
	java -cp $(CLASSPATH) synthesijer.Main --chaining --operation-strength-reduction --verilog $(SOURCES)

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
