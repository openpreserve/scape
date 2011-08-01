import re
import sys
import os

#linere = re.compile(r'^(\d+)\s+(\w+)\(([^)]+)\)\s+\=\s*(.*)$')
linere = re.compile(r'^(\S+)\s+(\w+)\(([^)]+)\)\s+\=\s*(.*)$')

# Does not cope with mmap memory mapped files?

def main():
  openfiles = dict()
  filesread = set()
  with open(sys.argv[1]) as f:
    for line in f:
      mo = linere.match(line)
      if mo is None:
        print "Unmatched line %r" % line
        continue
      pid, command, args, results = mo.groups()
      if command == 'open':
        fn = args.split(',', 1)[0].strip('"').rstrip('0').rstrip('\\')
        fd = results.split(' ', 1)[0]
        openfiles[fd] = fn
        print "OPENED:",fn,fd
      elif command == 'read':
        #if results != '0':
        fd = args.split(',', 1)[0].lstrip('0').lstrip('x')
        if fd in openfiles:
          filesread.add(openfiles[fd])
        else:
	      print "ERROR: fd",fd,"not found!"
      #else:
      #  print "Unknown command %r" % command	
  for item in sorted(filesread):
	print "SORTED\t",item, "\t",os.popen("file -b "+item,'r').read()

main()
