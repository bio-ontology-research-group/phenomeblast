def mpfile = new File("mp-equivalence-axioms.obo")
def pfile = new File("MGI_PhenoGenoMP.rpt")
Set defined = new TreeSet()
mpfile.eachLine { line ->
  if (line.startsWith("id:")) {
    def id = line.substring(4).trim()
    if (id.indexOf("!")>-1) {
      id = id.substring(0,id.indexOf("!")-1)
    }
    defined.add(id)
  }
}

def mgi2count = [:]
pfile.splitEachLine("\t") { line ->
  def mgi = line[3]
  if (! (mgi in defined)) {
    if (mgi2count[mgi]==null) {
      mgi2count[mgi] = 1
    } else {
      mgi2count[mgi] += 1
    }
  }
}

mgi2count.each { key, val ->
  println "$key\t$val"
}