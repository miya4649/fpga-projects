●このプロジェクトはAltera DE1 Board用です。

●ビルド方法
Synthesijerはあらかじめ以下の方法でインストールしているものとします。
http://cellspe.matrix.jp/zerofpga/synthesijer.html

Synthesijer配布物のsynthesijer-code/lib/verilog/singleportram.vをこのディレクトリ（sjr_vga_example以下）にコピーします。

sjr_vga_example/synthesijer にcdしてmakeします。

Quartus II Ver.13.0.1 でプロジェクトファイルsjr_vga_example.qpfを開いて「Start Compilation」、「Programmer」で転送して実行します。

