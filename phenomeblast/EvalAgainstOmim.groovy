
def f0 = new File("HMD_HumanDisease.rpt")
def f1 = new File("MGI_PhenoGenoMP.rpt")
def f2 = new File(args[0])

def gene2omim = [:]
def omim2gene = [:]
def marker2gene = [:]


f0.splitEachLine("\t") {
  if (it[1].indexOf("<")>-1) {
    def name = it[1].substring(0,it[1].indexOf("<")).toLowerCase()
    def omim = it[4]
    if (gene2omim[name]==null) {
      gene2omim[name] = new TreeSet()
    }
    gene2omim[name].add(omim)
    if (omim2gene[omim]==null) {
      omim2gene[omim] = new TreeSet()
    }
    omim2gene[omim].add(name)
  }
}

f1.splitEachLine("\t") {
  def marker = it[-1].trim()
  if (it[1].indexOf("<")>-1) {
    def name = it[1].substring(0,it[1].indexOf("<")).toLowerCase()
    if (marker.indexOf(",")<0) {
      marker2gene[marker] = name
    }
  }
}
f2.splitEachLine("\t") {
  def omim = it[0]
  omim = omim.substring(omim.indexOf('_')+1,omim.indexOf('>'))
  if (omim2gene[omim]!=null) {
    if (it.size()>1) {
      it[1..(it.size()-1)].each { mgi ->
	mgi = "MGI:"+mgi.substring(mgi.indexOf('_')+1,mgi.indexOf('>'))
	if (marker2gene[mgi]!=null) {
	  if (omim2gene[omim].contains(marker2gene[mgi])) {
	    println "OMIM:"+omim+"\t"+mgi
	  }
	}
      }
    }
  }
}