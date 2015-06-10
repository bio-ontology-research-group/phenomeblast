Set s = new TreeSet()
new File("diseaseswithnoomimdef.txt").splitEachLine("\t") { line ->
  s.add(line[0])
}
new File(args[0]).eachLine { line ->
  def toks = line.split("\t")
  if (toks[0] in s) {
    println line
  }
}