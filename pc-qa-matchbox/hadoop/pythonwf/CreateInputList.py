#!/usr/bin/env python

import sys, getopt

hdfshome = ''
try:
   opts, args = getopt.getopt(sys.argv[1:],"hd:",["hdfshome="])
except getopt.GetoptError:
   print 'CreateInputList.py -d <hdfshome>'
   sys.exit(2)
for opt, arg in opts:
   if opt == '-h':
      print 'CreateInputList.py -d <hdfshome>'
      sys.exit()
   elif opt in ("-d", "--hdfshome"):
      hdfshome = arg

print 'hdfshome in CreateInputList.py', hdfshome

# evaluated file names from the temporary file (tmp) should be edited and saved in output file
outputfile = open('inputfiles', 'w')
tmpfile = open('tmp', 'r')

# take the path to the input file and create HDFS path for each file
for line in tmpfile:
   if not 'items' in line:
      line = line.split(' ')
      print line[-1],
      outputfile.write('hdfs://' + hdfshome + '/' + line[-1])

tmpfile.close()
outputfile.close()



