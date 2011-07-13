import re
import sys

#linere = re.compile(r'^(\d+)\s+(\w+)\(([^)]+)\)\s+\=\s*(.*)$')
linere = re.compile(r'^(\S+)\s+(\w+)\(([^)]+)\)\s+\=\s*(.*)$')

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
        print "OPENED:",fn
      elif command == 'read':
        if results != '0':
          fd = args.split(',', 1)[0]
          filesread.add(openfiles[fd])
      else:
        print "Unknown command %r" % command	
  print sorted(filesread)

main()
