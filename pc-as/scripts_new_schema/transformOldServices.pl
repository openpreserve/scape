#!/usr/bin/perl

use strict;
use XML::XPath;
use XML::Smart;
use Data::Dumper;

my %info2xpath=(
	'name' => '//toolspec/name/text()',
	'homepage' => '//toolspec/homepage/text()',
	'dependencies' => '//toolspec/installation/os[@type=\'linux\']/text()',
#   'service_name' => '//toolspec/services/service/@name',
   'operation_description' => '//toolspec/services/service/operations/operation/description/text()',
   'operation_command' => '//toolspec/services/service/operations/operation/command/text()',
   'output_extension' => '//toolspec/services/service/operations/operation/outputs/output/Extension/text()',
);

my $oldXML = shift or die("Must provide the old XML location!");
my $newXML = shift or die("Must provide the new XML location!");
my $newXMLFileName = `basename $newXML`;
$newXMLFileName =~ s/\..+$//;
chomp $newXMLFileName;
print ">$newXMLFileName<\n";

my $xp = XML::XPath->new(filename => $oldXML);
my $value, my $nodeset;
my %res=();

for my $info (keys %info2xpath){
	$nodeset = $xp->find($info2xpath{$info});
	foreach my $node ($nodeset->get_nodelist){
		if($node->getNodeType == 3 ){
			$value=$node->string_value;
		}
		if($node->getNodeType == 2 ){
			$value=$node->getData;
		}
		last;
	}
	chomp $value;
	$value =~ s/^\s+//;
	$value =~ s/\s+$//;
	$res{$info}=$value;
}

#print Dumper(\%res);

my $outXMLObject = XML::Smart->new("<tool xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
	xsi:schemaLocation=\"http://scape-project.eu/tool tool-1.0_draft.xsd\"
	xmlns=\"http://scape-project.eu/tool\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"
	schemaVersion=\"1.0\"></tool>");

# //tool attributes
my $name_alt = $res{'name'};
$name_alt =~ s/\s+.*$//;
$name_alt =~ s/'s$//;
$outXMLObject->{tool}{name}=$name_alt;
$outXMLObject->{tool}{version}='1.0';
$outXMLObject->{tool}{homepage}=$res{'homepage'};

# //tool/installation
$outXMLObject->{tool}{installation}{dependency}={ operatingSystemName => 'Debian'};
$outXMLObject->{tool}{installation}{dependency}->content(0,$res{'dependencies'});
$outXMLObject->{tool}{installation}{license}={ type => 'Apache Licence 2.0'};
$outXMLObject->{tool}{installation}{license}->content(0,'Apache License, Version 2.0');

# //tool/otherProperties
$outXMLObject->{tool}{otherProperties}{property} = { name => 'cost' };
$outXMLObject->{tool}{otherProperties}{property}->content(0, '0');

# //tool/operations/operation
#$outXMLObject->{tool}{operations}{operation}={ name => $res{'service_name'} };
$outXMLObject->{tool}{operations}{operation}={ name => $newXMLFileName };
$outXMLObject->{tool}{operations}{operation}{description}->content($res{'operation_description'});
$outXMLObject->{tool}{operations}{operation}{command}->content($res{'operation_command'});

# //tool/operations/operation/inputs/input
$outXMLObject->{tool}{operations}{operation}{inputs}{input}={
	name => 'input',
	required => 'true',
};
$outXMLObject->{tool}{operations}{operation}{inputs}{input}{description}->content('Reference to input file');

# //tool/operations/operation/inputs/parameter
$outXMLObject->{tool}{operations}{operation}{inputs}{parameter}={
	name => 'params',
	required => 'false',
};
$outXMLObject->{tool}{operations}{operation}{inputs}{parameter}{description}->content('Additional conversion parameters');

# //tool/operations/operation/outputs/output
$outXMLObject->{tool}{operations}{operation}{outputs}{output}={
	name => 'output',
	required => 'true',
};
$outXMLObject->{tool}{operations}{operation}{outputs}{output}{description}->content('Reference to output file');
$outXMLObject->{tool}{operations}{operation}{outputs}{output}{extension}->content($res{'output_extension'});

#print $outXMLObject->dump_tree();

$outXMLObject->save($newXML);

1

