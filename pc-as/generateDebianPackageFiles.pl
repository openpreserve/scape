#!/usr/bin/perl

use strict;
use XML::XPath;
use XML::XPath::XMLParser;

my %info2xpath=(
	'dependencies' => '//toolspec/installation/os[@type=\'linux\']/text()',
	'version' => '//toolspec/version/text()',
	'description' => '//toolspec/services/service/description/text()',
);
my $projectName = shift or die("Must provide project name!");
die("Project name must not contain underscores!") if($projectName =~ m/.+_/);
my $warName = shift or dir("Must provide .war name!");
my $fileName = shift or die("Must provide .xml file containing the toolspec web service description!");

my $hashResultReference = getValuesFromXPath();
#for my $key (keys %$hashResultReference){
#	print $key." ".$hashResultReference->{$key}."\n";
#}
open F,">utf8","$projectName";
print F "Section: misc\n";
print F "Priority: optional\n";
print F "Homepage: www.scape-project.eu\n";
print F "Package: $projectName\n";
print F "Version: $hashResultReference->{'version'}\n";
print F "Maintainer: Helder Silva, Rui Castro <[hsilva,rcastro]\@keep.pt>\n";
print F "Pre-Depends: $hashResultReference->{'dependencies'}\n";
print F "Architecture: all\n";
print F "Files: $warName /var/lib/tomcat6/webapps/$warName\n";
print F "Description: $hashResultReference->{'description'}\n";
close F;

##################### functions #####################
sub getValuesFromXPath{
	my %res=();
	my $value, my $retValue, my $nodeset;
	my $xp = XML::XPath->new(filename => $fileName);
	for my $info (keys %info2xpath){
		$nodeset = $xp->find($info2xpath{$info}); 
	   foreach my $node ($nodeset->get_nodelist) {
	      $value = XML::XPath::XMLParser::as_string($node); 
      	last;
	   }	
		chomp $value;
		$value =~ s/^\s+//;
		$value =~ s/\s+$//;
		$res{$info}=$value;
	}
	return \%res;
}

1;
