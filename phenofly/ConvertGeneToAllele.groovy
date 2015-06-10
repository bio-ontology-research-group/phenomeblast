def inf = new File(args[0])
def g2a = new File("flymap-gene-allele.txt")

def g2am = [:]
g2a.splitEachLine("\t") { line ->
  def g = line[1]
  def a = line[0]
  if (g2am[g] == null) {
    g2am[g] = new TreeSet()
  }
  g2am[g].add(a)
}

inf.splitEachLine("\t") { line ->
  def f = line[0]
  def o = line[1]
  g2am[f]?.each { al ->
    println "$al\t$o"
  }
}