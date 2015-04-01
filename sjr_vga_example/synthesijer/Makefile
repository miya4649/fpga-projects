SIJERPATH=$(HOME)/synthesijer
SIJERLINKPATH=synthesijer/bin
SYNTHESIJER=$(SIJERPATH)/$(SIJERLINKPATH)
CLASSPATH=$(SYNTHESIJER):.

SOURCES=*.java
HDLFILES=*.v

all: $(SOURCES)
	java -cp $(CLASSPATH) synthesijer.Main --chaining --operation-strength-reduction --verilog $(SOURCES)
	cp $(HDLFILES) ../

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
