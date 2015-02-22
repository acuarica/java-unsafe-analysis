
while (<>) {
  chomp;
  @x = split /","/;
  @x = map { s/^"//; s/"$//; $_ } @x;

  $repoKind = $x[1];
  $rev = $x[2];
  $project = $x[3];
  $url = $x[6];
  $file = $x[7];

  $project = $url;
  $project =~ s{.*/}{};

  if ($file =~ m{(.*)/([^/]*)}) {
    $dir = $1;
    $file = $2;
  }

  print "# project ", $project, "\n";
  print "# rev ", $rev, "\n";
  print "# url ", $url, "\n";
  print "# dir ", $dir, "\n";
  print "# file ", $file, "\n";

  if ($repoKind eq 'SVN') {
    $svn = $url;
    $svn =~ s{http://sourceforge.net/projects}{svn://svn.code.sf.net/p};

    print "mkdir -p 'out/$project$dir'", "\n";
    print "mkdir -p 'out.code/$project$dir'", "\n";

    print "curl 'http://sourceforge.net/p/$project/svn/$rev/tree$dir/$file?format=raw' > 'out/$project$dir/$file'", "\n";
    print "curl 'http://sourceforge.net/p/$project/code/$rev/tree$dir/$file?format=raw' > 'out.code/$project$dir/$file'", "\n";

    # print "svn cat -r$rev $svn/svn$dir/$file > 'out/$project$dir/$file'", "\n"
  }
  elsif ($repoKind eq 'CVS') {
    $dir =~ s{/trunk}{};

    print "mkdir -p 'out/$project$dir'", "\n";
    print "curl 'http://$project.cvs.sourceforge.net/viewvc/$project$dir/$file?revision=$rev&content-type=text%2Fplain' > 'out/$project$dir/$file'", "\n";
  }
}

print 'find out out.code -type f -exec egrep -l 404 {} \; > 404.txt', "\n";
print 'find out out.code -type f -exec egrep -l 404 {} \; | while read f ; do rm -f "$f" ; done', "\n";
print '(cd out.code ; tar cf - .) | (cd out ; tar xf -)', "\n";
print 'rm -rf out.code', "\n";
