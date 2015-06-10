def s1 = new TreeSet()
new File("omimphenotypes.txt").splitEachLine("\t") { line ->
  s1.add(line[0])
}

def s2 = new TreeSet()
def oid = ""
new File("HumanDO.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    oid = line.substring(4).trim()
  }
  if (line.startsWith("xref: OMIM")) {
    def omim = line.substring(6).trim()
    s2.add(omim)
  }
}


println s1.size()
println s2.size()
println s1.intersect(s2).size()
println s2.minus(s1)
