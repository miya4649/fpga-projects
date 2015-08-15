BeMicro Max 10用プロジェクト・テンプレート説明書

●このプロジェクトはBeMicro Max 10用です。

●I/O電圧のジャンパ設定について
このプロジェクトはボードのI/O電圧を3.3Vに設定することを前提にしています。
BeMicro Max 10 Getting Started User Guide

http://www.alterawiki.com/wiki/BeMicro_Max_10#Documentation

https://parts.arrow.com/item/detail/arrow-development-tools/bemicromax10

のp.5を参照してVCCIO選択ジャンパ (J1,J9)が3.3V側に設定されていることを確認してください。

●ピンアサインについて
IO_STANDARDはほとんど3.3-V LVTTLに設定してありますが以下のピンは2.5Vに設定しています。
ADT7420, GPIO_J4, I2C, EGの一部, SFLASH, USER_LED
PB（タクトスイッチ）は2.5 V SCHMITT TRIGGERです。

●ビルド方法
Linux上でのビルドを想定しています。Quartus IIをUbuntuに導入する方法については以下のページを参考にしてください。
http://cellspe.matrix.jp/zerofpga/inst_quartus.html

端末で、
tar xzf bemicro_max10_start.tar.gz

次に、Quartus II Ver.15.0 以上でプロジェクトファイルbemicro_max10_start.qpfを開いて「Start Compilation」、
「Programmer」で「Start」で転送して実行します。

●プロジェクト・テンプレートとしての使い方
このディレクトリを別名でコピーし、bemicro_max10_start.v を書き換えれば、新たにピンアサイン等の設定を行うことなく簡単に新規プロジェクトを作成することができます。
