/*
  Copyright (c) 2016, miya
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

public class Program extends Asm
{
  // label
  // value: 0 <= X < LABEL_SIZE (unique)
  private static final int L_0 = 0;
  private static final int L_1 = 1;
  private static final int L_2 = 2;
  private static final int L_3 = 3;
  private static final int L_4 = 4;
  private static final int L_5 = 5;
  private static final int L_6 = 6;
  private static final int L_7 = 7;
  private static final int L_8 = 8;
  private static final int L_9 = 9;
  private static final int L_10 = 10;
  private static final int L_11 = 11;
  private static final int L_12 = 12;

  private void example1()
  {
    // example1: simple counter
    as_mvi(0, 0);
    as_mvi(1, 1);
    label(L_0);
    as_add(0, 0, 1);
    as_out(0);
    as_bc(1, addr_rel(L_0));
  }

  private void example2()
  {
    // example2: counter with wait
    as_mvi(0, 0);
    as_mvi(1, 1);
    label(L_0);
    as_add(0, 0, 1);
    as_out(0);

    // wait
    as_mvi(2, 0);
    as_mvi(3, 0);
    as_mvih(3, 4);
    label(L_1);
    as_add(2, 2, 1);
    as_cgt(4, 3, 2);
    as_bc(4, addr_rel(L_1));

    as_bc(1, addr_rel(L_0));
  }

  @Override
  public void program()
  {
    //example1();
    example2();
  }
}
