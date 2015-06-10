
new File("entrez/Homo_sapiens.gene_info").splitEachLine("\t") { line ->
  def geneid = line[1]
  def location = line[7]
  println "$geneid\t$location"
}

println "-\t-"