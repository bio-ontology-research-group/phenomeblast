
def mean = { s ->
  def val = 0
  s.each { val += it }
  val / s.size()
}

def g2e = [:].withDefault { new TreeSet() }
new File("E-GEOD-11324_A-AFFY-44-analytics.tsv").splitEachLine("\t") { line ->
  if (! line[0].startsWith("Gene")) {
    try {
      def g = line[1]
      def p = new Double(line[3])
      g2e[g].add(p)
    } catch (Exception E) {}
  }
}
g2e = g2e.collectEntries { k, v -> [k.toLowerCase(), mean(v)] }

def g2id = [:]
new File("HMD_HumanPhenotype.rpt").splitEachLine("\t") { line ->
  def g = line[0]?.toLowerCase()?.trim()
  def id = line[4]?.trim()
  g2id[g] = id
}

def id2pheno = [:].withDefault { new TreeSet() }
new File("mousephenotypes.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def pheno = line[1]
  if (id && pheno) {
    id2pheno[id].add(pheno)
  }
}

g2e.each { g, e ->
  def id = g2id[g]
  if (id) {
    def pheno = id2pheno[id]
    pheno?.each { p ->
      println "$g\t$p\t$e"
    }
  }
}