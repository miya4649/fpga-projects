#!/usr/bin/python3
# -*- coding: utf-8 -*-
import sys, os, struct, re, codecs

read_file = '../sjr_vga_example_de0_cv.v'
write_file = '../sjr_vga_example_de0_cv.v'
sjrtop_file = 'Sjr_Top.v'


#Verilogトップモジュールを読み込む
fr = codecs.open(read_file, 'r', 'utf-8')
text = ''.join(fr.read())
fr.close()

#インスタンス宣言部分の前のテキスト、宣言部分、後のテキストを切り出す
topgroups = re.search('(?si)(^.*?Sjr_Top[\s]*sjr_top0[\s]*\()(.*?)([\s]*\);.*?$)', text)


#Sjr_Top.vを読み込んでそのポート宣言部分をsjrtopに切り出す
fr = codecs.open(sjrtop_file, 'r', 'utf-8')
text = ''.join(fr.read())
fr.close()
sjrtop = re.search('(?si)module[\s]*Sjr_Top[\s]*\([\s]*(.*?)\);', text).group(1).split('\n')

#Sjr_Topのポート名をリストにする
sjrtopport = []
for line in sjrtop:
  portname = re.search('([^\s,]*?)[,]*$', line).group(1)
  if portname:
    sjrtopport.append(portname)

#トップモジュールのsjr_top0インスタンス宣言からポートの対応データを抽出
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

#sjr_top0インスタンス宣言を作り直す
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
