FPGAブロックくずしゲーム・プロジェクト・ファイル説明書

●このプロジェクトはBeMicro Max 10用です。

●I/O電圧のジャンパ設定について
このプロジェクトはボードのI/O電圧を3.3Vに設定することを前提にしています。
BeMicro Max 10 Getting Started User Guide

http://www.alterawiki.com/wiki/BeMicro_Max_10#Documentation

https://parts.arrow.com/item/detail/arrow-development-tools/bemicromax10

のp.5を参照してVCCIO選択ジャンパ (J1,J9)が3.3V側に設定されていることを確認してください。

●このプロジェクトを実行するには別途製作した映像・音声・コントローラー・インターフェース・ボードが必要です。下記のページを参考にして製作してください。
http://cellspe.matrix.jp/zerofpga/avinterface.html
このボードをBeMicro Max 10ボード上のJ5ピンヘッダに繋ぎます。
【注意点】上記の記事で紹介しているフラットケーブルを挿そうとすると隣のJ7と干渉するのでフラットケーブルのコネクタの端を少し削る必要があります。そのまま無理に挿すとボードを痛める恐れがあるので注意してください。

●ビルド方法
Linux上でのビルドを想定しています。Quartus IIをUbuntuに導入する方法については以下のページを参考にしてください。
http://cellspe.matrix.jp/zerofpga/inst_quartus.html

高位合成ツール「Synthesijer」を使用しています。
Synthesijerはあらかじめ以下の方法でインストールしているものとします。
http://cellspe.matrix.jp/zerofpga/synthesijer.html

BeMicro Max 10では容量がぎりぎりなのでコンパイル環境の違いによってFPGAの容量に収まらない可能性があります。動作確認できているのはsynthesijer_20150711バージョン、Quartus II 15.0.1の環境です。

端末で、
tar xzf sjr_block_bemicro_max10.tar.gz

cd sjr_block_bemicro_max10/synthesijer

make

次に、Quartus II Ver.15.0 以上でプロジェクトファイルbemicro_max10_start.qpfを開いて「Start Compilation」、
「Programmer」で「Start」で転送して実行します。
