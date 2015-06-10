def geno2gene = [:]
def gene2geno = [:]
new File("data/genotype_features.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def gene = line[-1]
  if (gene.indexOf("ZDB-GENE")>-1) {
    geno2gene[id] = gene
    if (gene2geno[gene] == null) {
      gene2geno[gene] = new TreeSet()
    }
    gene2geno[gene].add(id)
  }
}

def dis2gene = [:]
new File("data/morbidmap").splitEachLine("\\|") { line ->
  def id = line[0].split(",")[-1].trim()
  if (id.indexOf(" ")>-1) {
    id = id.substring(0,id.indexOf(" ")).trim()
  }
  if (id ==~ /\d+/) {
    def gene = line[-2]
    if (dis2gene[id] == null) {
      dis2gene[id] = new TreeSet()
    }
    dis2gene[id].add(gene)
  }
}

/*
def mim2gene = [:]
new File("data/mim2gene.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def gene = line[2]
  def type = line[1]
  if (type == "gene") {
    mim2gene[id] = gene
  }
}
*/

def zfin2omim = [:]
def omim2zfin = [:]
new File("data/human_orthos.txt").splitEachLine("\t") { line ->
  def zfin = line[0]
  def omim = line[-2]
  zfin2omim[zfin] = omim
  omim2zfin[omim] = zfin
}

dis2gene.each { dis, genes ->
  genes.each { g ->
    def gene = omim2zfin[g]
    if (gene!=null) {
      //      println "OMIM:$dis\t$gene"
      gene2geno[gene].each { geno ->
	println "OMIM:$dis\t$geno"
      }
    }
  }
}