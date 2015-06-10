def includeset = new TreeSet()
def pfile = new File("../data/phenotypes.txt")
pfile.splitEachLine("\t") { line ->
  if (line.size()>3) {
    def id = line[0]
    includeset.add(id)
  }
}

def results = new TreeSet()
new File("names/").eachFile { file ->
  file.splitEachLine("\t") { line ->
    if (line.size()>0) {
      if (line[0].indexOf("DERMO")>-1 || line[0].indexOf("DOID")>-1) {
	results.add("MEDLINE"+line[0]+"\t"+line[1])
	results.add("PMC"+line[0]+"\t"+line[1])
      } else if (line.size()<=2 && line[1] && line[1].size()>1) {
	results.add(line[0]+"\t"+line[1])
      } else { // special treatment for mouse
	if (line[-1]=="allele") { // allele
	  results.add(line[0]+"\t"+line[1])
	} else { // gene
	  results.add(line[0]+"\t"+line[2]+" ("+line[1]+")")
	}
      }
    }
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
  def r = p.split("\t")
  if (r[0] in includeset) {

    if (p.startsWith("OMIM")) {
      def id = r[0].substring(5)
      println "$p\thttp://omim.org/entry/$id"
    }
    if (p.startsWith("PMCOMIM")) {
      def id = r[0].substring(8)
      println "$p\thttp://omim.org/entry/$id"
    }
    if (p.startsWith("MEDLINEOMIM")) {
      def id = r[0].substring(12)
      println "$p\thttp://omim.org/entry/$id"
    }
    if (p.startsWith("FB")) {
      println "$p\thttp://flybase.org/reports/"+r[0]+".html"
    }
    if (p.startsWith("MEDLINEDOID")) {
      def doid = r[0].substring(7)
      println "$p\thttp://bioportal.bioontology.org/ontologies/50042/?p=terms&conceptid="+doid
    }
    if (p.startsWith("PMCDOID")) {
      def doid = r[0].substring(3)
      println "$p\thttp://bioportal.bioontology.org/ontologies/50042/?p=terms&conceptid="+doid
    }
    if (p.startsWith("MEDLINEDERMO")) {
      def doid = r[0].substring(7)
      println "$p\thttp://bioportal.bioontology.org/ontologies/50491/?p=terms&conceptid="+doid
    }
    if (p.startsWith("PMCDERMO")) {
      def doid = r[0].substring(3)
      println "$p\thttp://bioportal.bioontology.org/ontologies/50491/?p=terms&conceptid="+doid
    }
    if (p.startsWith("MEDLINESTITCHTM")) {
      def sid = r[0].substring(16)
      println "$p\thttp://stitch.embl.de/cgi/show_network_section.pl?identifier=CID"+sid
    }
    if (p.startsWith("STITCHORIG")) {
      def sid = r[0].substring(11)
      println "$p\thttp://stitch.embl.de/cgi/show_network_section.pl?identifier=CID"+sid
    }
    if (p.startsWith("ID")) {
      println "$p\t"
    }
    if (p.startsWith("S0")) {
      println "$p\thttp://www.yeastgenome.org/cgi-bin/locus.fpl?dbid="+r[0]
    }
    if (p.startsWith("ZDB")) {
      println "$p\thttp://zfin.org/action/quicksearch?query="+r[0]
    }
    if (p.startsWith("DBS")) {
      println "$p\thttp://dictybase.org//db/cgi-bin/search/search.pl?query="+r[0]
    }
    if (p.startsWith("WB")) {
      println "$p\thttp://www.wormbase.org/search/all/"+r[0]
    }
    if (p.startsWith("RGD")) {
      def id = r[0].substring(4)
      println "$p\thttp://rgd.mcw.edu//rgdweb/search/search.html?term=$id"
    }
    if (p.startsWith("ORPHANET")) {
      def id = r[0].substring(9)
      println "$p\thttp://www.orpha.net/consor/cgi-bin/Disease_Search_Simple.php?lng=EN&Disease_Disease_Search_diseaseType=ORPHA&Disease_Disease_Search_diseaseGroup=$id"
    }
    if (p.startsWith("MEDLINEORPHANET")) {
      def id = r[0].substring(16)
      println "$p\thttp://www.orpha.net/consor/cgi-bin/Disease_Search_Simple.php?lng=EN&Disease_Disease_Search_diseaseType=ORPHA&Disease_Disease_Search_diseaseGroup=$id"
    }
    if (p.startsWith("PMCORPHANET")) {
      def id = r[0].substring(12)
      println "$p\thttp://www.orpha.net/consor/cgi-bin/Disease_Search_Simple.php?lng=EN&Disease_Disease_Search_diseaseType=ORPHA&Disease_Disease_Search_diseaseGroup=$id"
    }
    if (p.startsWith("MGI:")) {
      println "$p\thttp://www.informatics.jax.org/searchtool/Search.do?query="+r[0]
    }
  }
}
