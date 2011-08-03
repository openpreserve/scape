import re
import sys
import os

#linere = re.compile(r'^(\d+)\s+(\w+)\(([^)]+)\)\s+\=\s*(.*)$')
linere = re.compile(r'^(\S+)\s+(\w+)\(([^)]+)\)\s+\=\s*(.*)$')

# Does not cope file-descriptor switches:
#fcntl(3, F_DUPFD, 10)             = 10
#close(3)                          = 0
#fcntl(10, F_SETFD, FD_CLOEXEC)    = 0
# but this is not a Killar.

def main():
  openfiles = dict()
  filesread = list()
  with open(sys.argv[1]) as f:
    for line in f:
      mo = linere.match(line)
      if mo is None:
        print "WARN: Unmatched line %r" % line
        continue
      pid, command, args, results = mo.groups()
      if command == 'open' or command == 'open_nocancel':
        fn = args.split(',', 1)[0].strip('"').rstrip('0').rstrip('\\')
        fd = int(results.split(' ', 1)[0])
        openfiles[fd] = fn
        #print "OPENED:",fn,fd
      elif command == 'read' or command == 'pread' or command =='read_nocancel':
        #if results != '0':
        fd = int(args.split(',', 1)[0],0) #.lstrip('0').lstrip('x')
        if fd in openfiles and not openfiles[fd] in filesread:
          filesread.append(openfiles[fd])
        else:
	  print "WARN: fd",fd,"not found!"
      elif command == 'mmap' or command == 'mmap2':
        fds = args.split(', ')[4]
        if fds != 'NULL' and fds != '-1':
          fd = int(fds,0)
          if fd in openfiles and not openfiles[fd] in filesread:
            filesread.append(openfiles[fd])
          else:
	    print "WARN: fd",fd,"not found! (mmap)"


      #else:
      #  print "Unknown command %r" % command	
  for item in filesread:
	print item
#	print "SORTED, ",item, ", ",os.popen("file -b '"+item+"'",'r').read().rstrip()

main()
