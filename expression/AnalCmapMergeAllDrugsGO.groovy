MAX = 7559

def mean = { s ->
  def val = 0
  s.each { val += it }
  /*
  // we split ranks in the middle so that the highest rank gets a 0, middle rank will be the highest
  s.each {
    if (it < MAX/2) {
      val += it
    } else if (it > MAX) {
      println it
    } else {
      val += (MAX-it)
    }
  }
  */
  val / s.size()
}

def mean2 = { s ->
  def val = 0
  // we split ranks in the middle so that the highest rank gets a 0, middle rank will be the highest
  s.each {
    if (it < MAX/2) {
      val += it
    } else if (it > MAX) {
      println it
    } else {
      val += (MAX-it)
    }
  }
  val / s.size()
}

def map = [:].withDefault { new TreeSet() } // drugname -> set<instanceid>
def map2 = [:] // instanceid -> drugname
new File("cmap/cmap_instances_02.csv").splitEachLine("\t") { line ->
  Expando exp = new Expando()
  def instance = line[0]
  exp.name = line[2]
  if (exp.name && instance) {
    map[exp.name].add(instance)
    map2[instance] = exp.name
  }
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
      if (new Integer(r) > MAX) {
	MAX = new Integer(r)
      }
      def g = probe2gene[p]
      if (g) {
	def d = map2[drugnum2instance[drug]]
	drug2gene2rank[d][g].add(new Integer(r))
      }
    }
  } else { // find out at which place in the list which drug instance can be found
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
new File("gene_association.mgi").splitEachLine("\t") { line ->
  def id = line[1]
  def pheno = line[4]
  println "$id\t$pheno"
  if (id && pheno) {
    id2pheno[id].add(pheno)
  }
}

drug2gene2rankavg.each { drug, g2e ->
  println "Drug $drug..."
  PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter("training-go/" + drug)))
  g2e.each { g, e ->
    def id = g2id[g]
    if (id) {
      def pheno = id2pheno[id]
      pheno?.each { p ->
	fout.println("$g\t$p\t$e")
      }
    }
  }
  fout.flush()
  fout.close()
}