Synthesijer用スプライトライブラリのサンプルです。

●このプロジェクトはFPGA開発ボード「Terasic DE0-CV」に対応しています。

●ビルド方法
Linux上でのビルドを想定しています。QuartusをUbuntuに導入する方法については以下のページを参考にしてください。
http://cellspe.matrix.jp/zerofpga/inst_quartus.html

高位合成ツール「Synthesijer」を使用しています。
Synthesijerはあらかじめ以下の方法でインストールしているものとします。
http://cellspe.matrix.jp/zerofpga/synthesijer.html

端末で、

cd sprite_example/synthesijer

make

でコンパイルされます。

次に、Quartus Ver.15.0 以上でプロジェクトファイルを開いて「Start Compilation」、「Programmer」で転送して実行します。

ボードのプロジェクトファイルは以下の場所にあります。
Terasic DE0-CV： de0-cv/de0_cv_start.qpf
