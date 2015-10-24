#!/usr/bin/python3
# -*- coding: utf-8 -*-
import sys, os, struct, re, codecs

top_module_file = '../bemicro_max10_start.v'
write_file = top_module_file
sjrtop_name = 'Sjr_I2C_OLED'
sjrtop_file = sjrtop_name + '.v'

#これはVerilogトップモジュールのインスタンス宣言部分を自動的に修正するためのスクリプトです。
#例えば、Synthesijer側トップモジュールのポート名の class_hoge_0000_class_obj_0000_fuge_exp_exp が class_hoge_0001_class_obj_0001_fuge_exp_exp に変わった時にVerilogトップモジュールのインスタンス宣言部分の該当個所を自動的に修正します。


#Verilogトップモジュールを読み込む
fr = codecs.open(top_module_file, 'r', 'utf-8')
text = ''.join(fr.read())
fr.close()

#インスタンス宣言部分の前のテキスト、宣言部分、後のテキストを切り出す
topgroups = re.search('(?si)(^.*?' + sjrtop_name + '[^\(]*\()(.*?)([\s]*\);.*?$)', text)

#Synthesijer側トップモジュールを読み込んでそのポート宣言部分をsjrtopに切り出す
fr = codecs.open(sjrtop_file, 'r', 'utf-8')
text = ''.join(fr.read())
fr.close()
sjrtop = re.search('(?si)module[\s]*' + sjrtop_name + '[\s]*\([\s]*(.*?)\);', text).group(1).split('\n')

#sjrtopのポート名をリストにする
sjrtopport = []
for line in sjrtop:
  portname = re.search('([^\s,]*?)[,]*$', line).group(1)
  if portname:
    sjrtopport.append(portname)

#トップモジュールのインスタンス宣言からポートの対応データを抽出
keywords = {}
for line in topgroups.group(2).split('\n'):
  ports = re.search('^[\s/]*\.(.*?)[\s\(]+(.*?)[\s\)]+.*$', line)
  if ports:
    # ext_?_exp という文字列を含んでいるポートはその文字列をキーワードにして対応データとする。そうでないものはポート名全体をキーワードとする。
    extport = re.search('(ext_.*?_exp)', ports.group(1))
    if extport:
      keywords[extport.group(1)] = ports.group(2)
    else:
      keywords[ports.group(1)] = ports.group(2)

#インスタンス宣言を作り直す
instsjrtop = '\n'
for port in sjrtopport:
  instsjrtop += '     .'
  extport = re.search('(ext_.*?_exp)', port)
  if extport:
    try:
      instsjrtop += port + ' (' + keywords[extport.group(1)] +'),\n'
    except:
      instsjrtop += port + ' (),\n'
  else:
    try:
      instsjrtop += port + ' (' + keywords[port] +'),\n'
    except:
      instsjrtop += port + ' (),\n'
instsjrtop = instsjrtop.rstrip(',\n')


#インスタンス宣言部分をすげ替えて完成
outtext = topgroups.group(1) + instsjrtop + topgroups.group(3) + '\n'

#ファイルに出力
fw = codecs.open(write_file, 'w', 'utf-8')
fw.write(outtext)
fw.close()
