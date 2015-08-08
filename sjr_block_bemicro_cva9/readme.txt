FPGAブロックくずしゲーム・プロジェクト・ファイル説明書

●このプロジェクトはBeMicro CV A9用です。

●I/O電圧のジャンパ設定について
このプロジェクトはボードのI/O電圧を3.3Vに設定することを前提にしています。
BeMicro CV A9 Hardware Reference Guide
http://www.alterawiki.com/wiki/BeMicro_CV_A9#Documentation
のp.23を参照してVCCIO選択ジャンパ (J11)のpin 1とpin 2が接続されていることを確認してください。

●このプロジェクトを実行するには別途製作した映像・音声・コントローラー・インターフェース・ボードが必要です。下記のページを参考にして製作してください。
http://cellspe.matrix.jp/zerofpga/avinterface.html

●ビルド方法
Linux上でのビルドを想定しています。Quartus IIをUbuntuに導入する方法については以下のページを参考にしてください。
http://cellspe.matrix.jp/zerofpga/inst_quartus.html

高位合成ツール「Synthesijer」を使用しています。
Synthesijerはあらかじめ以下の方法でインストールしているものとします。
http://cellspe.matrix.jp/zerofpga/synthesijer.html

端末で、
tar xzf sjr_block_bemicro_cva9.tar.gz

cd sjr_block_bemicro_cva9/synthesijer

make

次に、Quartus II Ver.15.0 以上でプロジェクトファイルbemicro_cva9_start.qpfを開いて「Start Compilation」、「Programmer」で転送して実行します。
