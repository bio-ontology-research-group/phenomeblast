
def homo = [:].withDefault { [] }
new File("HOM_MouseHumanSequence.rpt").splitEachLine("\t") { line ->
  if (line[0].startsWith("Homo")) {} else {
    Expando exp = new Expando()
    exp.id = line[0]
    exp.org = line[2]
    exp.mgi = line[5]
    exp.pos = line[8]
    homo[exp.id] << exp
  }
}

def mouse2human = [:]
homo.each { id, list ->
  def mgi = null
  def human = null
  list.each { 
    if (it.mgi!=null && it.mgi.length()>0) {
      mgi = it
    } else {
      human = it
    }
  }
  if (mgi!=null && human!=null) {
    mouse2human[mgi.mgi] = human.pos
  }
}

def map = [:].withDefault { [] }
new File(args[0]).splitEachLine("\t") { line ->
  def did = line[0]
  def mgi = line[1].replaceAll("http://phenomebrowser.net/smltest/", "")
  def score = new Double(line[2])
  println "$did\t$mgi\t$score\t"+mouse2human[mgi]
}

