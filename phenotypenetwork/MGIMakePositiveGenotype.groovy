def name2gt = [:]
new File("mousephenotypes-names.txt").splitEachLine("\t") { line ->
  if (line[-1]=="genotype") {
    def id = line[0]
    def name = line[1]
    def bg = line[2]
    name2gt["$name\t$bg"] = id
  }
}

new File("MGI_Geno_Disease.rpt").splitEachLine("\t") { line ->
  def name = line[0]
  def bg = line[3]
  line[-1].split(",").each { omim ->
    println "OMIM:"+omim+"\t"+name2gt["$name\t$bg"]
  }
}