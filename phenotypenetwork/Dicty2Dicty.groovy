
def ddb2uniprot = [:]
def uniprot2ddb = [:]
new File("DDB-GeneID-UniProt.txt").splitEachLine("\t") { line ->
  def ddb = line[1]
  def uni = line[3]
  ddb2uniprot[ddb] = uni
  uniprot2ddb[uni] = ddb
}

def dbs2ddb = [:]
def ddb2dbs = [:].withDefault { new TreeSet() }
new File("all-mutants-ddb_g.txt").splitEachLine("\t") { line ->
  def dbs = line[0]
  def ddb = line[3]
  dbs2ddb[dbs] = ddb
  ddb2dbs[ddb].add(dbs)
}
new File("gene_association.dictyBase").splitEachLine("\t") { line ->
  if (!line[0].startsWith("!")) {
    if (line[6] == "IPI") {
      def uni = line[7]
      def ddb = line[1]
      def p1 = ddb2dbs[ddb]
      def p2 = uniprot2ddb[uni?.replaceAll("UniProtKB:","")]
      if (p1 && p2) {
	p2 = ddb2dbs[p2]
	if (p2) {
	  p1.each { x ->
	    p2.each { y ->
	      println "$x\t$y"
	    }
	  }
	}
      }
    }
  }
}