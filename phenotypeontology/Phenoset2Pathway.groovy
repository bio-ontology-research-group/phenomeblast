def pt2pw = [:]
new File("phenotype2pathway.txt").splitEachLine("\t") { line ->
  def pt = line[0]
  def pw = line[1]
  if (pt2pw[pt]==null) {
    pt2pw[pt] = new TreeSet()
  }
  pt2pw[pt].add(pw)
}

new File("../data/phenotypes.txt").splitEachLine("\t") { line ->
  if (line.size()>2) {
    def id = line[0]
    line[1..-1].each { p ->
      p = p.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":").trim()
      if (pt2pw[p] != null) {
	pt2pw[p].each {
	  println "$id\t$it"
	}
      }
    }
  }
}