
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

    if ($project eq 'jikesrvm') {
      print "svn cat $svn/svn$dir/$file > 'out/$project$dir/$file'", "\n"
    }
    else {
      print "svn cat $svn/code$dir/$file > 'out/$project$dir/$file'", "\n"
    }
  }
  elsif ($repoKind eq 'CVS') {
    $dir =~ s{/trunk}{};
    $rev = "1.30";

    print "mkdir -p 'out/$project$dir'", "\n";
    print "curl 'http://$project.cvs.sourceforge.net/viewvc/$project$dir/$file?revision=$rev&content-type=text%2Fplain' > 'out/$project$dir/$file'", "\n";
  }
    
}

