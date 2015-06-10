new File("../data/phenotypes.txt").splitEachLine("\t") { line ->
  if (line.size()>1) {
    line = line.collect { it.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":") }
    line[1..-1].each { p ->
      println line[0]+"\t"+p
    }
  }
}