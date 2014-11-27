# Copyright (c) 2013-2014 Alexander Savochkin
# Chemical wikipedia search (chwise.net) web-site source code

# This file is part of ChWiSe.Net infrastructure.

# ChWiSe.Net infrastructure is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.


#!/usr/bin/perl -w

use strict;
use utf8;
use Parse::MediaWikiDump;

my ($file, $optputpath) = @ARGV;

#Check that output directory exists
if (-d $optputpath) {
    #Directory already exists
} 
elsif (-e $optputpath) {
    #$optputpath exists but it is not directory
    warn $optputpath.' is a file. Must be directory';
    exit 1;
}
else {
    #$optputpath
    mkdir $optputpath;
}

for (my $i = 0; $i <= 9; ++$i) {
    my $path = $optputpath.'/'.$i;
    next if (-d $path);
    die 'Can\'t create path '.$optputpath.'/'.$i.' There is file with such name' if (-e $path.'/'.$i);
    mkdir $path;
}


my $pages = Parse::MediaWikiDump::Pages->new($file);
my $page;

my ($total_count, $compound_count) = (0,0);
print STDERR "Start iterating\n";
while(defined($page = $pages->next)) {
    #main namespace only
    next unless $page->namespace eq '';
    ++$total_count;
    #Select articles containing chembox or drugbox with SMILES field inside
    if (${$page->text}  =~ /\{\{(chembox|drugbox).*\|\s*SMILES\s*=\s*([^\n\}\|]+)/is) {
        my $smiles = $2;
        next if $smiles =~ /^\s*$/;
        ++$compound_count;
        my $title = $page->title;
        print STDERR "$compound_count \t $total_count \t $title \t $2 \n";

        #Write file to $optputpath directory
        my $page_id = $page->id;
        my $first_digit = substr $page_id, 0, 1;
        my $path_file = $optputpath.'/'.$first_digit.'/'.$page_id;

        open CHEM_DOCUMENT_FILE, ">$path_file";
        binmode CHEM_DOCUMENT_FILE, ':utf8';
        print CHEM_DOCUMENT_FILE $title."\n";
        print CHEM_DOCUMENT_FILE $smiles."\n";
        print CHEM_DOCUMENT_FILE ${$page->text};
        close CHEM_DOCUMENT_FILE;

    }
}

print STDERR "Total: $total_count";
