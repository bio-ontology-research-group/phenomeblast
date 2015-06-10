def cutoff = new Double(args[1])
new File(args[0]).splitEachLine("\t") { line ->
  def d = new Double(line[2])
  if (d > cutoff) {
    println line[0]+"\t"+line[1]+"\t"+line[2]
  }
}