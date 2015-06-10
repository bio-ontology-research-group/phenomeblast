map = [:]

new File(args[0]).splitEachLine("\t") { line ->
  if (map[line[0]] == null ) {
    map[line[0]] = []
  }
  Expando exp = new Expando()
  exp.omim = line[1]
  exp.val = new Double(line[2])
  map[line[0]] << exp
}

map.each { mgi, l ->
  l = l.sort { it.val }
  l[-1..1].each {
    println "$mgi\t"+it.omim+"\t"+it.val
  }
}
