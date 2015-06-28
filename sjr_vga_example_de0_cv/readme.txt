●このプロジェクトはTerasic DE0-CV Board用です。

●ビルド方法
Synthesijerはあらかじめ以下の方法でインストールしているものとします。
http://cellspe.matrix.jp/zerofpga/synthesijer.html

このプロジェクトではビルドにpython3が必要なのでインストールします。
sudo apt-get install python3

sjr_vga_example_de0_cv/synthesijer にcdしてmakeします。

Quartus II Ver.15.0 以上でプロジェクトファイルsjr_vga_example_de0_cv.qpfを開いて「Start Compilation」、「Programmer」で転送して実行します。

