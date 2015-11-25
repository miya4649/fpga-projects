SYNTHESIJER?=$(lastword $(sort $(wildcard $(HOME)/synthesijer/synthesijer*.jar)))

SYNTHESIJER_LIB?=$(lastword $(sort $(wildcard $(HOME)/synthesijer/synthesijer_lib_????????)))

SYNTHESIJER_EXTRA_LIB?=$(lastword $(sort $(wildcard $(HOME)/synthesijer/synthesijer_extra-libs_????????)))

CLASSPATH=$(SYNTHESIJER):$(SYNTHESIJER_EXTRA_LIB)/bin:.

LIB_SJR_VERILOG=singleportram.v synthesijer_arith_rshift32.v synthesijer_lshift32.v synthesijer_logic_rshift32.v synthesijer_mul32.v

EXLIB_SJR_VERILOG=

SOURCES=SimpleCPU.java


all: $(SOURCES) $(LIB_SJR_VERILOG) $(EXLIB_SJR_VERILOG)
	java -cp $(CLASSPATH) synthesijer.Main --chaining --operation-strength-reduction --verilog $(SOURCES)

$(LIB_SJR_VERILOG):
	cp $(SYNTHESIJER_LIB)/verilog/$@ ./

$(EXLIB_SJR_VERILOG):
	cp $(SYNTHESIJER_EXTRA_LIB)/hdl_lib/verilog/$@ ./

sim: $(SOURCES)
	javac -cp $(CLASSPATH) SimpleCPU.java SimTop.java
	java SimTop

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
