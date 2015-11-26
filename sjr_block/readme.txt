FPGAブロックくずしゲーム・プロジェクト・ファイル説明書

●このプロジェクトはFPGA開発ボード「BeMicro CV A9」、「BeMicro Max 10」、「Terasic DE0-CV」に対応しています。

●このプロジェクトを実行するには別途製作した映像・音声・コントローラー・インターフェース・ボードが必要です。下記のページを参考にして製作してください。

BeMicro CV A9：
http://cellspe.matrix.jp/zerofpga/avinterface.html
これをJ1ピンヘッダに接続します。

BeMicro Max 10：
http://cellspe.matrix.jp/zerofpga/avinterface.html
これをJ5ピンヘッダに接続します。

Terasic DE0-CV：
http://cellspe.matrix.jp/zerofpga/audio_adapter.html
これをGPIO 1に接続します。

●（BeMicro CV A9の場合）I/O電圧のジャンパ設定について
このプロジェクトはボードのI/O電圧を3.3Vに設定することを前提にしています。
BeMicro CV A9 Hardware Reference Guide
http://www.alterawiki.com/wiki/BeMicro_CV_A9#Documentation
のp.23を参照してVCCIO選択ジャンパ (J11)のpin 1とpin 2が接続されていることを確認してください。

●（BeMicro Max 10の場合）I/O電圧のジャンパ設定について
このプロジェクトはボードのI/O電圧を3.3Vに設定することを前提にしています。
BeMicro Max 10 Getting Started User Guide
http://www.alterawiki.com/wiki/BeMicro_Max_10#Documentation
https://parts.arrow.com/item/detail/arrow-development-tools/bemicromax10
のp.5を参照してVCCIO選択ジャンパ (J1,J9)が3.3V側に設定されていることを確認してください。

●ビルド方法
Linux上でのビルドを想定しています。QuartusをUbuntuに導入する方法については以下のページを参考にしてください。
http://cellspe.matrix.jp/zerofpga/inst_quartus.html

高位合成ツール「Synthesijer」を使用しています。
Synthesijerはあらかじめ以下の方法でインストールしているものとします。
http://cellspe.matrix.jp/zerofpga/synthesijer.html

端末で、

cd sjr_block/synthesijer

BeMicro CV A9、Terasic DE0-CVの場合、

make

BeMicro Max 10の場合、

make mini

でコンパイルされます。

次に、Quartus Ver.15.0 以上でプロジェクトファイルを開いて「Start Compilation」、「Programmer」で転送して実行します。

各ボードのプロジェクトファイルは以下の場所にあります。
BeMicro CV A9： bemicro_cva9/bemicro_cva9_start.qpf
BeMicro Max 10： bemicro_max10/bemicro_max10_start.qpf
Terasic DE0-CV： de0-cv/de0_cv_start.qpf
