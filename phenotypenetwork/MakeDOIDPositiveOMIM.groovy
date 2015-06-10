def do2omim = [:]
def omim2do = [:]
def id = ""
new File(args[0]).eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(4).trim()
    do2omim[id] = new TreeSet()
  }
  if (line.indexOf("xref: OMIM")>-1) {
    line = line.substring(11).trim()
    if (line ==~ /\d\d\d\d\d\d/) {
      do2omim[id].add(line)
      omim2do[line] = id
    }
  }
}


def all2hom = [:] // MGI and OMIM ids to HOM cluster ID
def hom2all = [:] // inverse
new File("HOM_MouseHumanSequence.rpt").splitEachLine("\t") { line ->
  def mgi = line[5]
  def omim = line[7]
  def hom = line[0]
  if (hom2all[hom] == null) {
    hom2all[hom] = new TreeSet()
  }
  if (mgi!=null) {
    all2hom[mgi] = hom
    hom2all[hom].add(mgi)
  }
  if (omim!=null) {
    all2hom[omim] = hom
    hom2all[hom].add(omim)
  }
}

new File("genemap").splitEachLine("\\|") { line ->
  def gid = line[9]
  def dis = line[13]
  dis = dis.split(";")
  dis.each { d ->
    def temp = d.split(",")
    def did = temp[-1].trim()
    if (did.indexOf("(") > -1) {
      did = did.substring(0,did.indexOf("(")).trim()
    }
    if (did ==~ /\d\d\d\d\d\d/) {
      if (omim2do[did]) {
	did = omim2do[did]
	def hom = all2hom[gid]
	if (hom) {
	  hom2all[hom].each { h ->
	    if (h.indexOf("MGI")>-1) {
	      println args[1]+"$did\t$h"
	    }
	  }
	}
      }
    }
  }
}