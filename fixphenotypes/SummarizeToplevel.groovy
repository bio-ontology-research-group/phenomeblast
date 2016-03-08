def f = [:].withDefault { new LinkedHashSet() }
def s = [:].withDefault { new LinkedHashSet() }
new File("toplevel.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def first = line[1]?.replaceAll("\\[","")?.replaceAll("\\]","")?.split(",")?.collect { it.trim() }
  if (first)
    f[id].addAll(first)
  def second = line[2]?.replaceAll("\\[","")?.replaceAll("\\]","")?.split(",")?.collect { it.trim() }
  if (second)
    s[id].addAll(second)
}
s.each { k, v ->
  print "$k"
  v.each {
    if (it) {
      print "\t$it"
    }
  }
  println ""
}
