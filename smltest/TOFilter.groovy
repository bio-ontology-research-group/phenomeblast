def s1 = new TreeSet()
new File("omimphenotypes.txt").splitEachLine("\t") { line ->
  s1.add(line[0])
}

def s2 = new TreeSet()
def oid = ""
def doid2omim = [:].withDefault { new TreeSet() }
new File("HumanDO.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    oid = line.substring(4).trim()
  }
  if (line.startsWith("xref: OMIM")) {
    def omim = line.substring(6).trim()
    s2.add(omim)
    doid2omim[oid].add(omim)
  }
}

def targetset = s2.minus(s1)

new File(args[0]).eachLine { line ->
  def tok = line.split("\t")
  if (doid2omim[tok[0]]?.intersect(targetset)?.size() > 0) {
    println line
  }
}