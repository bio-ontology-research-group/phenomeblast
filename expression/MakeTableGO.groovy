def gos = []
def mps = []
new File("aspirin-mp.txt").splitEachLine("\t") { line ->
  def mpt = line[4]
  def mp = line[5]
  def p = new Double(line[6])
  Expando exp = new Expando()
  exp.mpt = mpt
  exp.mp = mp
  exp.p = p
  mps << exp
  //  println "$mpt ({\\tt $mp}) & $p \\\\"
}


new File("aspirin-go/refinement-GO:0008150-0.0005_0.0001.txt").splitEachLine("\t") { line ->
  def mpt = line[1]
  def mp = line[2]
  def p = new Double(line[6])
  if (p < 0.01 && line[3] == "+") {
    Expando exp = new Expando()
    exp.mpt = mpt
    exp.mp = mp
    exp.p = p
    gos << exp
  }
}

mps = mps.sort { it.p }
gos = gos.sort { it.p }

40.times {
  //  println mps[it].mpt+" ({\\tt "+mps[it].mp+"}) & "+mps[it].p+" & "+gos[it].mpt+" ({\\tt "+gos[it].mp+"}) & "+gos[it].p+" \\\\"
  println mps[it].mpt+" ({\\tt "+mps[it].mp+"}) & "+gos[it].mpt+" ({\\tt "+gos[it].mp+"}) \\\\"
}
println mps[40].p
println gos[40].p