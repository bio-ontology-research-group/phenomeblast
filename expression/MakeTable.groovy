new File("aspirin-mp.txt").splitEachLine("\t") { line ->
  def mpt = line[4]
  def mp = line[5]
  def p = new Double(line[6])
  println "$mpt ({\\tt $mp}) & $p \\\\"
}