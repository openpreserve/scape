#!/bin/bash

case "$1" in
   start)
      echo "Starting OpenOffice.org conversion service"
      /usr/bin/openoffice.org --headless --accept="socket,port=8100;urp;" --nofirststartwizard
      if [ $? -eq 0 ]; then
         echo " Done"
      else
         echo " Some problem ocorred while starting $0"
      fi
   ;;
   stop)
      echo "Stopping OpenOffice.org conversion service"
      pgrep -f soffice.bin | xargs kill
      if [ $? -eq 0 ]; then
         echo " Done"
      else
         echo " Some problem ocorred while stopping $0"
      fi
   ;;
   status)
      PID=`pgrep -f soffice.bin`
      if [ $PID > 0 ]; then
         echo " OpenOffice.org conversion service is running"
      else
         echo " OpenOffice.org conversion service is not running"
      fi
   ;;
   *)
      echo "Usage: $0 {start|stop|status}"
      exit 1
   ;;
esac

exit 0
