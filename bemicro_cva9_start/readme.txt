●このプロジェクトはBeMicro CV A9用です。

●I/O電圧のジャンパ設定について
このプロジェクトはボードのI/O電圧を3.3Vに設定することを前提にしています。工場出荷設定では3.3Vに設定してあるはずですが、念のため
BeMicro CV A9 Hardware Reference Guide
http://www.alterawiki.com/wiki/BeMicro_CV_A9#Documentation
のp.23を参照してVCCIO選択ジャンパ (J11)のpin 1とpin 2が接続されていることを確認してください。

●ビルド方法
Quartus II Ver.15.0 以上でプロジェクトファイルbemicro_cva9_start.qpfを開いて「Start Compilation」、「Programmer」で転送して実行します。

