#!/usr/bin/perl

use warnings;
use strict;
use File::Find;

=for comment

This is meant to recursively search for files in a given directory and replace 
text in them.
Invoke as: ./<scrpit_name> <DIR> <'text_to_replace'> <'new_text'>
Use with "'" to avoid bash errors.
"\","|" are not supported in replacement.
Change file extension in the first line of targets sub (default: cpp).                                                                                                                        
Regexes from command-line won't work.                                                                                                                                                         

=cut

unless ($#ARGV == 2) {die};

chomp(my $dir = $ARGV[0]);
chomp(my $pattern = $ARGV[1]);
chomp(my $replacement = $ARGV[2]);                                                                                                                                                            

$pattern =~ s!\(!\\(!g;
$pattern =~ s!\)!\\(!g;
$pattern =~ s!\[!\\[!g;
$pattern =~ s!\]!\\]!g;
$pattern =~ s!\{!\\{!g;
$pattern =~ s!\}!\\}!g;
$pattern =~ s!\*!\\*!g;
$pattern =~ s!\+!\\+!g;
$pattern =~ s!\^!\\^!g;
$pattern =~ s!\?!\\?!g;
$pattern =~ s!\.!\\.!g;

$replacement =~ s!\(!\\(!g;
$replacement =~ s!\)!\\(!g;
$replacement =~ s!\[!\\[!g;
$replacement =~ s!\]!\\]!g;
$replacement =~ s!\{!\\{!g;
$replacement =~ s!\}!\\}!g;
$replacement =~ s!\*!\\*!g;
$replacement =~ s!\+!\\+!g;
$replacement =~ s!\^!\\^!g;
$replacement =~ s!\?!\\?!g;

find( \&targets, $dir );

sub targets {
        return unless -f $_ && m/\.cpp/;
        open my $fh, "<", $_ or die "Failed to read file $!";
        local $/ = undef;
        my $text = <$fh>;
        $text =~ s!$pattern!$replacement!g;
        $text =~ s!\\!!g;
        close $fh;
        open $fh, ">", $_ or die "Failed to write file $!";
        print $fh $text;
        close $fh;
}
