// find DO->OMIM mappings
def doid2omim = [:].withDefault { new TreeSet() }
def oid = ""
new File("HumanDO.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    oid = line.substring(4).trim()
  }
  if (line.startsWith("xref: OMIM")) {
    def omim = line.substring(6).trim()
    doid2omim[oid].add(omim)
  }
}

new File(args[0]).splitEachLine("\t") { line ->
  doid2omim[line[0]].each { omim ->
    println "${line[0]}\t$omim"
  }
}