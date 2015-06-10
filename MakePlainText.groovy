def id2name = [:]
def id = ""
new File("mammalian_phenotype.obo").eachLine {  line ->
  if (line.startsWith("id")) {
    id = line.substring(3).trim()
  }
  if (line.startsWith("name")) {
    id2name[id]= line.substring(5).trim()
  }
}
new File(args[0]).splitEachLine("\t") { line ->
  line[0..-1].each {
    it = it.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":")
    if (id2name[it]!=null) {
      print id2name[it]
    } else {
      print it
    }
    print "\t"
  }
  println ""
}