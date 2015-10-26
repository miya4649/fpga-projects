SJR:=$(wildcard $(HOME)/synthesijer/synthesijer-*.jar)
SJRLIB:=$(wildcard $(HOME)/synthesijer/synthesijer_lib_????????)
SJREXTLIB:=$(wildcard $(HOME)/synthesijer/synthesijer_extra-libs_????????)
SYNTHESIJER?=$(SJR)
SYNTHESIJER_LIB?=$(SJRLIB)
SYNTHESIJER_EXTRA_LIB?=$(SJREXTLIB)
CLASSPATH=$(SYNTHESIJER):$(SYNTHESIJER_EXTRA_LIB)/bin:.
LIB_SJR_VERILOG=../singleportram.v

SOURCES=SimpleCPU.java RedLED.java
HDLFILES=*.v

all: $(SOURCES) $(LIB_SJR_VERILOG)
	java -cp $(CLASSPATH) synthesijer.Main --chaining --operation-strength-reduction --verilog $(SOURCES)
	cp $(HDLFILES) ../
	python3 fix_sjr_top.py

$(LIB_SJR_VERILOG):
	cp $(SYNTHESIJER_LIB)/verilog/singleportram.v ../

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
