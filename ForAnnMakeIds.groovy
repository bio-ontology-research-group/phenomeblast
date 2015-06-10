def map = [:]
new File("Ids-new.txt").splitEachLine("\t") { line ->
  if (map[line[0]]==null) {
    map[line[0]] = new TreeSet()
  }
  map[line[0]].add(line[1])
}

map.each { key, value ->
  print "$key\tOMIM\t"
  value.each { print "$it\t" }
  println ""
}
