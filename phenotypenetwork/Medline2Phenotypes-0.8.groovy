def id2name = [:]
def id = ""
new File("doid.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
  }
  if (line.startsWith("name:")) {
    id2name[id] = line.substring(5).trim()
  }
}
new File("dermo-with-xrefs.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
  }
  if (line.startsWith("name:")) {
    id2name[id] = line.substring(5).trim()
  }
}
new File("human-phenotype-ontology.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
  }
  if (line.startsWith("name:")) {
    id2name[id] = line.substring(5).trim()
  }
}
new File("mammalian_phenotype.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
  }
  if (line.startsWith("name:")) {
    id2name[id] = line.substring(5).trim()
  }
}


MINSCORE = 0.8

new File("../../pmcanalysis/results/medline-simple").splitEachLine("\t") { line ->
  def did = line[0]
  def pheno = line[2]
  def score = new Double(line[4])
  def cooc = new Double(line[7])
  def oc1 = new Double(line[5])
  def oc2 = new Double(line[6])
  if (score > MINSCORE) {
    println "$did\t$pheno\t"+id2name[did]+"\t"+id2name[pheno]
  }
}
