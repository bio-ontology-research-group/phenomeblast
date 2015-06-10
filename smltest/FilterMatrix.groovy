new File(args[0]).splitEachLine("\t") { line ->
  def val = new Double(line[2])
  if (val > 0.1) {
    val = 1-val
    println line[0]+"\t"+line[1]+"\t"+val
  }
}