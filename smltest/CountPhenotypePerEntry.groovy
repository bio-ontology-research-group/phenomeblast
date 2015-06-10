def map = [:].withDefault { new TreeSet() }
new File(args[0]).splitEachLine("\t") { line ->
  def omim = line[0]
  def pheno = line[1]
  if (omim.startsWith("OMIM")) {
    map[omim].add(pheno)
  }
}

def counter = 0
map.each { omim, phenos ->
  counter += phenos.size()
}
println "Phenos: $counter"
println "Diseases: "+map.size()
println "Phenos/disease: "+(counter/map.size())
