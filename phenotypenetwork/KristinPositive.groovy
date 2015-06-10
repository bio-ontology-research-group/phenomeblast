def omimpos = new File("omim-genes-pos.txt")

def omap = [:]
omimpos.splitEachLine("\t") { line ->
  def o = line[0]
  def g = line[1]
  if (omap[o] == null) {
    omap[o] = new TreeSet()
  }
  omap[o].add(g)
}

def kset = new TreeSet()
new File("kristinall.txt").splitEachLine("\t") { line ->
  def o = line[0]
  o = o.replaceAll("kristinall","")
  if (o.startsWith("OMIM")) {
    kset.add(o)
  }
}

omap.keySet().intersect(kset).each { o ->
  def v = omap[o]
  v.each { g ->
    println "$o\t$g"
  }
}
