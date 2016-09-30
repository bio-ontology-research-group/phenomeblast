/* Generate merged flat file of disease annotations */

def omimdiseases = new TreeSet()
new File("omim/mim2gene.txt").splitEachLine("\t") { line ->
  if (line[1]!="gene") { // phenotype
    omimdiseases.add("OMIM:"+line[0])
  }
}

def map = [:].withDefault { new TreeSet() }
new File("diseasephenotypes/phenotype_annotation.tab").splitEachLine("\t") { line ->
  def id = line[0]+":"+line[1]
  if (!id.startsWith("OMIM") || id in omimdiseases) {
    def pheno = line[4]
    map[id].add(pheno)
  }
}
new File("diseasephenotypes/filtered-doid-pheno-21.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def pheno = line[1]
  map[id].add(pheno)
}

map.each { k, v ->
  v.each {
    println "$k\t$it"
  }
}
