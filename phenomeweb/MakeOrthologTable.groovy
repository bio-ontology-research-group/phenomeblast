def s = new TreeSet()
new File("data/Genotype.sql").splitEachLine("\t") { line->
  s.add(line[0])
}

def gene2allele = [:]
new File("orthology/MGI_GenePheno.rpt").splitEachLine("\t") {line ->
  def aid = line[2]
  def gid = line[-1]
  if (gene2allele[gid] == null) {
    gene2allele[gid] = new TreeSet()
  }
  gene2allele[gid].add(aid)
}
new File("orthology/HMD_Human5.rpt").splitEachLine("\t") { line ->
  if (line.size()>3) {
    def pos = line[1]
    def id = line[3]
    println "$id\t$pos"
    s.remove(id)
    gene2allele[id]?.each { 
      println "$it\t$pos"
      s.remove(it)
    }
  }
}
new File("orthology/mim2gene.txt").splitEachLine("\t") { line ->
  def oid = "OMIM:"+line[0]
  def gid = line[2]
  def gene = line[1]
  if (gene == "gene") {
    println "$oid\t$gid"
    s.remove(oid)
  }
}
new File("orthology/human_orthos.txt").splitEachLine("\t") { line ->
  def zid = line[0]
  def eid = line[-1]
  println "$zid\t$eid"
}


s.each { 
  println "$it\t-"
}