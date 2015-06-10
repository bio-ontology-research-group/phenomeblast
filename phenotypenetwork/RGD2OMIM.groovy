def rdo2omim = [:].withDefault { new TreeSet() }
def id = ""
new File("RDO.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
  }
  if (line.startsWith("alt_id: OMIM:")) {
    def omim = line.substring(8).trim()
    rdo2omim[id].add(omim)
  }
}

new File("rattus_genes_rdo").splitEachLine("\t") { line ->
  if (!line[0].startsWith("!")) {
    def rgd = "RGD:"+line[1]
    def rdo = line[4]
    rdo2omim[rdo]?.each { omim ->
      println "$rgd\t$omim"
    }
  }
}