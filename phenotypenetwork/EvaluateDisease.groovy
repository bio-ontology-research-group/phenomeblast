def clusterfile = new File(args[0])

def f1 = new File("MGI_PhenotypicAllele.rpt")
def f2 = new File("HMD_HumanDisease.rpt")
def gene2disease = [:]
def allele2disease = [:]
f2.splitEachLine("\t") {
  def allid = it[0]
  def omimid = "OMIM_"+it[4]
  if (allele2disease[allid]==null) {
    allele2disease[allid] = new TreeSet()
  }
  allele2disease[allid].add(omimid)
}
f1.splitEachLine("\t") {
  def alleleid = it[0]
  def geneid = it[5]
  if (geneid!=null) {
    geneid = geneid.replaceAll(":","_")
    if (gene2disease[geneid]==null) {
      gene2disease[geneid] = new TreeSet()
    }
    if (allele2disease[alleleid]!=null) {
      gene2disease[geneid].addAll(allele2disease[alleleid])
    }
  }
}

def diag = [:]
clusterfile.splitEachLine("\t") { line->
  if (line[0]!="ID") {
    def cl = line[1]
    if (cl) {
      cl = cl.substring(1,cl.length()-1)
      def list = cl.tokenize(",")
      def genes = new TreeSet()
      def omim = []
      list.each {
	if (it.indexOf("OMIM")>-1) {
	  omim << it
	}
	if (it.indexOf("MGI")>-1) {
	  genes << it
	}
	if (it.indexOf("ZDB")>-1) {
	  genes << it
	}
      }
      omim.each {
	if (diag[it]==null) {
	  diag[it] = new TreeSet()
	}
	diag[it].addAll(genes)
      }
    }
  }
}

diag.keySet().each { omim ->
  def val = diag[omim]
  omim = omim.replaceAll("<http://bioonto.de/phene.owl#","")
  omim = omim.replaceAll(">","")
  val.each { mgi ->
    mgi = mgi.replaceAll("<http://bioonto.de/phene.owl#","")
    mgi = mgi.replaceAll(">","")
    if ((gene2disease[mgi]!=null) && (gene2disease[mgi].contains(omim))) {
      println omim
    }
  }
}