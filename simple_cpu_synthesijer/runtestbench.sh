#!/bin/sh

EXE=main

iverilog -o $EXE *.v synthesijer/*.v

if [ $? -eq 0 ]; then
  vvp $EXE
fi
