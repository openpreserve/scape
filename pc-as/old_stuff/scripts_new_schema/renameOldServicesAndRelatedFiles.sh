#!/bin/bash

cd `dirname $0`

# ../services/ & ../services_new_schema/
if [ ! -d ../services_new_schema/ ]; then
	mkdir ../services_new_schema/
fi

regexSH=".sh$"
regexPOSTINST=".postinst$"
regexXML=".xml$"
for i in $(ls ../services/)
do
	file=`basename $i`
	newFile=`echo -n $file | sed -e 's#abiword\|jodconverter\|pdfbox#office-&#' -e 's#avidemux\|ffmpeg\|handbrake\|mencoder#video-&#' -e 's#gimp\|graphicsmagick\|imageio\|imagemagick\|inkscape\|jasper\|netpbm\|openjpeg\|sanselan\|tesseract#image-&#' -e 's#sox#audio-&#' -e 's#^scape-pc-as#digital-preservation-migration#' `
	
	#if [[ "$file" =~ $regexSH ]] || [[ "$file" =~ $regexPOSTINST ]]; then
	if [[ "$file" =~ $regexSH ]]; then
		cp "../services/$i" "../services_new_schema/$newFile"
	fi
	if [[ $file =~ $regexXML ]]; then
		./transformOldServices.pl "../services/$i" "../services_new_schema/$newFile"
#		if [ ! -f ../toolwrapper/toolwrapper_core/target/toolwrapper_core-0.0.1-SNAPSHOT-jar-with-dependencies.jar ]; then
#			echo "Cannot find jar to generate the bash wrappers! see: ../toolwrapper/toolwrapper_core/target/"
#			exit 1
#		fi
#		java -jar ../toolwrapper/toolwrapper_core/target/toolwrapper_core-0.0.1-SNAPSHOT-jar-with-dependencies.jar -t "../services_new_schema/$newFile" -o "../services_new_schema"
	fi
done
