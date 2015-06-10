PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter("sidercmapphenotypes-positive.txt")))

def sidername2id = [:]
new File("siderphenotypes-names.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def name = line[1]?.toLowerCase()
  if (id.indexOf("ORIG")>-1) {
    sidername2id[name] = id
  }
}
def siderid2phenotype = [:].withDefault { new TreeSet() }
new File("stitchphenotypes.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def pheno = line[1]
  if (id.indexOf("ORIG")>-1) {
    siderid2phenotype[id].add(pheno)
  }
}


def cmapname2id = [:].withDefault { new TreeSet() }
new File("cmap/cmap_instances_02.csv").splitEachLine("\t") { line ->
  def id = line[0]
  def name = line[2]?.toLowerCase()
  if (name) {
    cmapname2id[name].add(id)
  }
}

cmapname2id.each { name, idset ->
  if (sidername2id[name]) {
    idset.each { id1 ->
      def id2 = sidername2id[name]
      fout.println("CMAP:$id1\t$id2")
    }
  }
}
fout.flush()
fout.close()

def cmapid2phenotype = [:].withDefault { new TreeSet() }
new File("enriched.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def pheno = line[5]
  cmapid2phenotype[id].add(pheno)
}

cmapname2id.each { name, idset ->
  idset.each { id ->
    cmapid2phenotype[id]?.each { pheno ->
      println "CMAP:$id\t$pheno"
    }
  }
}

siderid2phenotype.each { id, phenos ->
  phenos.each { pheno ->
    println "$id\t$pheno"
  }
}
