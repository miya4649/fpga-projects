Icarus Verilogコンパイラ用シミュレーション・ソースコードです。

●実行方法
Icarus Verilogとgtkwaveのインストールが必要です。
Ubuntuの場合：
sudo apt-get install iverilog gtkwave

testbench ディレクトリにcdして以下のコマンドを実行するとコンパイルとシミュレーションが実行されます。

./runtestbench.sh

出力されたwave.vcdというファイルをgtkwaveで開くと信号波形を見ることができます。
