SJR:=$(wildcard $(HOME)/synthesijer/synthesijer-*.jar)
SJRLIB:=$(wildcard $(HOME)/synthesijer/synthesijer_lib_????????)
SYNTHESIJER?=$(SJR)
SYNTHESIJER_LIB?=$(SJRLIB)
CLASSPATH=$(SYNTHESIJER):.
LIB_SJR_VERILOG=../singleportram.v

SOURCES=*.java
HDLFILES=*.v

all: $(SOURCES) $(LIB_SJR_VERILOG)
	java -cp $(CLASSPATH) synthesijer.Main --chaining --operation-strength-reduction --verilog $(SOURCES)
	cp $(HDLFILES) ../

$(LIB_SJR_VERILOG):
	cp $(SYNTHESIJER_LIB)/verilog/singleportram.v ../

sim: $(SOURCES)
	javac -cp $(CLASSPATH) $(SOURCES)
	java TopSim

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
