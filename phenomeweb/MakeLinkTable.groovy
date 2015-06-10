def results = new TreeSet()

new File("phenotypes/").eachFile { file ->
  file.splitEachLine("\t") { line ->
    results.add(line[0])
  }
}

def gt2id = [:]
new File("names/mousephenotypes-names.txt").splitEachLine("\t") { line ->
  if (line[-1]=="genotype") {
    def id = line[0]
    def aid = line[-2]
    gt2id[id] = aid
  }
}

results.each { p ->
  if (p.startsWith("OMIM")) {
    def id = p.substring(5)
    println "$p\thttp://omim.org/entry/$id"
  }
  if (p.startsWith("FB")) {
    println "$p\thttp://flybase.org/reports/"+p+".html"
  }
  if (p.startsWith("S0")) {
    println "$p\thttp://www.yeastgenome.org/cgi-bin/locus.fpl?dbid=$p"
  }
  if (p.startsWith("ZDB")) {
    println "$p\thttp://zfin.org/action/quicksearch?query=$p"
  }
  if (p.startsWith("WB")) {
    println "$p\thttp://www.wormbase.org/search/all/$p"
  }
  if (p.startsWith("RGD")) {
    def id = p.substring(4)
    println "$p\thttp://rgd.mcw.edu/rgdweb/report/gene/main.html?id=$id"
  }
  if (p.startsWith("ORPHANET")) {
    def id = p.substring(9)
    println "$p\thttp://www.orpha.net/consor/cgi-bin/Disease_Search_Simple.php?lng=EN&Disease_Disease_Search_diseaseType=ORPHA&Disease_Disease_Search_diseaseGroup=$id"
  }
  if (p.startsWith("MGI:")) {
    println "$p\thttp://www.informatics.jax.org/marker/$p"
  }
  if (p.startsWith("MGI-GT:")) {
    def id = gt2id[p]
    if (id!=null && id!="null") {
      def a = id.split("\\|")
      def b = a[0]
      println "$p\thttp://www.informatics.jax.org/marker/$b"
    } else {
      println "$p"
    }
  }
}
