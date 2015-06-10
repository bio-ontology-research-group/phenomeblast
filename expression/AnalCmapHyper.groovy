MAX = 7559
MAXRANK = 100
def mean = { s ->
  def val = 0
  s.each { val += it }
  val / s.size()
}

def probe2gene = [:]
new File("cmap/GPL96-15653.txt").splitEachLine("\t") { line ->
  if (line[0].indexOf("#")==-1) {
    def p = line[0]
    def g = line[10]?.toLowerCase()
    if (p && g) {
      probe2gene[p] = g
    }
  }
}

def drugnum2instance = [:]
def drug2probe2rank = [:].withDefault { [:] }
def drug2gene2rank = [:].withDefault { [:].withDefault { new TreeSet() } }
new File("cmap/rankMatrix.txt").splitEachLine("\t") { line ->
  if (! line[0].startsWith("probe")) {
    def p = line[0]
    line[1..-1].eachWithIndex { r, drug ->
      drug2probe2rank[drug][p] = new Integer(r)
      def g = probe2gene[p]
      if (g) {
	drug2gene2rank[drug][g].add(new Integer(r))
      }
    }
  } else {
    line[1..-1].eachWithIndex { inst, dnum ->
      drugnum2instance[dnum] = inst
    }
  }
}

def drug2gene2rankavg = [:].withDefault { [:] }
drug2gene2rank.each { drug, g2r ->
  g2r.each { g, ranks ->
    drug2gene2rankavg[drug][g] = mean(ranks)
  }
}

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

drug2gene2rankavg.each { drug, g2e ->
  println "Drug $drug, instance " + drugnum2instance[drug]
  PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter("training-hyper/" + drugnum2instance[drug])))
  g2e.each { g, e ->
    def id = g2id[g]
    if (id) {
      def pheno = id2pheno[id]
      pheno?.each { p ->
	if (e < MAXRANK) {
	  fout.println("$g\t$p\t1")
	} else {
	  fout.println("$g\t$p\t0")
	}
      }
    }
  }
  fout.flush()
  fout.close()
}
