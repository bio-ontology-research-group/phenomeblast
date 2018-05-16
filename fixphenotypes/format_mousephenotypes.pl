use strict;

my ($line, @tmp, @arr, $i);
open IN, "outfile.txt" or die "cannot open outfile";

while ($line=<IN>)
{ chomp $line;
$line=~s/]//g;
$line=~s/[//g;

@arr=split('\t', $line);
@tmp=split(', ', $arr[2]);

for ( $i=0; $i<scalar(@tmp); $i++)
 { print $tmp[$i]."\t".$arr[1]."\n";}

}
close IN;
