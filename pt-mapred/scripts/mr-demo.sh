#! /usr/bin/env bash

CMD="/home/ait/hadoop-0.20.205.0/bin/hadoop"

HINPUT="hinput.txt"
HINOUTPUT="hinput-output.txt"

DEFAULTJAR="./pt-mapred-0.0.1-SNAPSHOT-jar-with-dependencies.jar"

DEFAULTINDIR="input"
DEFAULTOUTDIR="output"
DEFAULTOUTFILE="part-r-00000"
DEFAULTOUTEXT="out"

DEFAULTREPO="toolspecs"

state=""

usage() {
	echo "Usage: $0 prepareinput|clean|run|storetoolspec|removetoolspec|listtoolspecs|outputfile|outputdir|list"
	echo "    Type -h for detail usage on each option"
}

# configures hinput and hinput-output
puttohinput () {
	echo "--input hdfs://$1" >> $HINPUT
	echo -n "--input hdfs://$1" >> $HINOUTPUT
	echo " --output hdfs://$2" >> $HINOUTPUT
}


# takes input files and puts them on hdfs
# and generates a hinput file with the references to them on hdfs

prepareinput () {
	src=""
	dest=$DEFAULTINDIR
	outdir=$DEFAULTOUTDIR
	outext=$DEFAULTOUTEXT

	for i in $*; do 
		[ "$i" = "-s" ] && state="src" && continue
		[ "$i" = "-d" ] && state="dest" && continue
		[ "$i" = "-o" ] && state="outdir" && continue
		[ "$i" = "-x" ] && state="outext" && continue
		[ "$i" = "-h" ] && state="usage" && break
		[ "$state" = "src" ] && src="$src $i"
		[ "$state" = "dest" ] && dest="$i" 
		[ "$state" = "outdir" ] && outdir="$i"
		[ "$state" = "outext" ] && outext="$i"
	done

	if [ "$state" = "usage" -o -z "$src" -o -z "$dest" -o -z "$outdir" -o -z "$outext" ]; then
		echo "Usage: $0 prepareinput "
		echo "   -s src-files or directory "
		echo "   [-d destination-dir on hdfs=\"$DEFAULTINDIR\"]"
		echo "   [-o output-dir for results on hdfs=\"$DEFAULTOUTDIR\"]"
		echo "   [-x extension of result-files=\"$DEFAULTOUTEXT\"]"
		exit 1
	fi

	rm $HINPUT 2&> /dev/null
	rm $HINOUTPUT 2&> /dev/null

	$CMD fs -rm $HINPUT 2&> /dev/null
	$CMD fs -rm $HINOUTPUT 2&> /dev/null

	# find home dir
	$CMD fs -touchz empty
	home=`$CMD fs -ls empty | awk '{print $8}'`
	home=`dirname $home`
	$CMD fs -rm empty

	[ `echo "$dest" | head -c 1` != "/" ] && dest="$home/$dest"
	[ `echo "$outdir" | head -c 1` != "/" ] && outdir="$home/$outdir"

	# if src is a directory and the only stuff to copy
	if [ `echo $src | wc -w` -eq 1 ] && [ -d $src ]; then
		$CMD fs -put $src $dest
		#dest=$dest/`basename $src`
	else
		$CMD fs -mkdir $dest &> /dev/null
		for file in $src; do
			if [ -f $file ]; then
				base=`basename $file`
				$CMD fs -put $file $dest/$base
			fi
		done
	fi

	# only files with length>0 (ie. no directories)
	files=`$CMD fs -ls $dest | awk '$5>0 {print $8}'`

	for file in $files; do
		puttohinput $file $outdir/`basename $file`.$outext
	done

	echo "Generated $HINPUT and putting it to home-dir on hdfs ..."
	$CMD fs -put $HINPUT $HINPUT
	echo "Generated $HINOUTPUT and putting it to home-dir on hdfs ..."
	$CMD fs -put $HINOUTPUT $HINOUTPUT
}

# cleans (output) dir

clean () {
	dir=$DEFAULTOUTDIR
	for i in $*; do 
		[ "$i" = "-d" ] && state="dir" && continue
		[ "$i" = "-h" ] && state="usage" && break
		[ "$state" = "dir" ] && dir="$i"
	done

	if [ "$state" = "usage" ]; then
		echo "Usage: $0 clean"
		echo "     [-d directory to remove]"
		exit 1
	fi

	$CMD fs -rmr $dir

}

# runs a MR-Wrapper job
run () {
	jar=$DEFAULTJAR
	repo=$DEFAULTREPO
	outdir=$DEFAULTOUTDIR

	for i in $*; do 
		[ "$i" = "-j" ] && state="jar" && continue
		[ "$i" = "-i" ] && state="input" && continue
		[ "$i" = "-o" ] && state="outdir" && continue
		[ "$i" = "-t" ] && state="toolspec" && continue
		[ "$i" = "-a" ] && state="action" && continue
		[ "$i" = "-r" ] && state="repo" && continue
		[ "$i" = "-h" ] && state="usage" && break
		[ "$state" = "jar" ] && jar="$i"
		[ "$state" = "input" ] && input="$i"
		[ "$state" = "outdir" ] && outdir="$i"
		[ "$state" = "toolspec" ] && toolspec="$i"
		[ "$state" = "action" ] && action="$i"
		[ "$state" = "repo" ] && repo="$i"
	done

	if [ "$state" = "usage" -o -z "$jar" -o -z "$input" -o -z "$outdir" -o -z "$toolspec" -o -z "$action" -o -z $repo ]; then
		echo "Usage: $0 run "
		echo "   -i hinput-file"
		echo "   -t toolspec"
		echo "   -a action"
		echo "   [-o output-dir=\"~/$DEFAULTOUTDIR\"]"
		echo "   [-j jar=\"$DEFAULTJAR\"]"
		echo "   [-r toolspec-repository=\"~/$DEFAULTREPO\""
		exit 1
	fi
	$CMD fs -test -e $outdir
	if [ $? -eq 0 ]; then
		echo "Output directory $outdir already exists. Delete before proceeding? [y|n]"
		read y
		if [ "$y" = "y" -o "$y" = "Y" ]; then 
			$CMD fs -rmr $outdir
		else
			echo "Aborting."
			exit 0
		fi

	fi
	$CMD jar $jar -i $input -o $outdir -t $toolspec -a $action -r $repo
}

storetoolspec () {
	repo=$DEFAULTREPO

	for i in $*; do
		[ "$i" = "-t" ] && state="toolspec" && continue
		[ "$i" = "-r" ] && state="repo" && continue
		[ "$i" = "-h" ] && state="usage" && break
		[ "$state" = "toolspec" ] && toolspecs="$toolspecs $i"
		[ "$state" = "repo" ] && repo="$i"
	done

	if [ "$state" = "usage" -o -z "$toolspecs" -o -z "$repo" ]; then
		echo "Usage: $0 storetoolspec"
		echo "    -t toolspec(s) to store"
		echo "    [-r repository to store to=\"$DEFAULTREPO\"]"
		exit 1
	fi

	$CMD fs -mkdir $repo &> /dev/null

	for file in $toolspecs; do
		if [ -f $file ]; then 
			echo "Storing $file ..."
			$CMD fs -put $file $repo/`basename $file`
		fi
	done
}

removetoolspec () {
	repo=$DEFAULTREPO

	for i in $*; do
		[ "$i" = "-t" ] && state="toolspec" && continue
		[ "$i" = "-r" ] && state="repo" && continue
		[ "$i" = "-h" ] && state="usage" && break
		[ "$state" = "toolspec" ] && toolspecs="$toolspecs $i"
		[ "$state" = "repo" ] && repo="$i"
	done

	if [ "$state" = "usage" -o -z "$toolspecs" -o -z "$repo" ]; then
		echo "Usage: $0 removetoolspec"
		echo "    -t toolspec(s) to remove"
		echo "    [-r repository to remove from=\"$DEFAULTREPO\"]"
		exit 1
	fi
	
	for file in $toolspecs; do
		echo "Removing $file ..."
		$CMD fs -rm $repo/$file
	done
}

listtoolspecs () {
	repo=$DEFAULTREPO

	for i in $*; do
		[ "$i" = "-r" ] && state="repo" && continue
		[ "$i" = "-h" ] && state="usage" && break
		[ "$state" = "repo" ] && repo="$i"
	done

	if [ "$state" = "usage" -o -z "$repo" ]; then
		echo "Usage: $0 listtoolspecs"
		echo "    [-r repository to list from=\"$DEFAULTREPO\"]"
		exit 1
	fi

	$CMD fs -ls $repo | awk '{print $8}'
		
}

outputfile () {
	outdir=$DEFAULTOUTDIR
	file=$DEFAULTOUTFILE

	for i in $*; do
		[ "$i" = "-f" ] && state="file" && continue
		[ "$i" = "-o" ] && state="outdir" && continue
		[ "$i" = "-c" ] && compressed=1 && continue
		[ "$i" = "-h" ] && state="usage" && break
		[ "$state" = "outdir" ] && outdir="$i"
		[ "$state" = "file" ] && file="$i"
	done

	if [ "$state" = "usage" -o -z "$outdir" ]; then
		echo "Usage: $0 outputfile"
		echo "   [-f print output-file (=\"$DEFAULTOUTFILE\") from output-dir]"
		echo "   [-o output-dir on hdfs=\"$DEFAULTOUTDIR\"]"
		echo "   [-c (if output is compressed)]"
		exit 1
	fi

	option="-text"
	[ -z "$compressed" ] && option="-cat"

	$CMD fs $option $outdir/$file
}

outputdir () {
	outdir=$DEFAULTOUTDIR

	for i in $*; do
		[ "$i" = "-o" ] && state="outdir" && continue
		[ "$i" = "-h" ] && state="usage" && break
		[ "$state" = "outdir" ] && outdir="$i"
	done

	if [ "$state" = "usage" -o -z "$outdir" ]; then
		echo "Usage: $0 outputdir"
		echo "   [-o output-dir on hdfs=\"$DEFAULTOUTDIR\"]"
		exit 1
	fi

	$CMD fs -ls $outdir | awk '{print $8}'
}

list () {

	for i in $*; do
		[ "$i" = "-d" ] && state="dir" && continue
		[ "$i" = "-h" ] && state="usage" && break
		[ "$state" = "dir" ] && dir="$i"
	done
	
	if [ "$state" = "usage" -o -z "$dir" ]; then
		echo "Usage: $0 list"
		echo "   -d dir on hdfs to list"
		exit 1
	fi

	$CMD fs -ls $dir
}

if [ "$1" = "-h" ]; then
	usage
else
	$1 $*
fi
