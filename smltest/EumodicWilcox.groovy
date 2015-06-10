def map = [:].withDefault { new LinkedHashSet() }
new File("eumodic-new-new-sim.txt").splitEachLine("\t") { line ->
  def id1 = line[0]
  def id2 = line[1].replaceAll("http://phenomebrowser.net/smltest/","")
  def val = new Double(line[2])
  Expando exp = new Expando()
  exp.id = id2
  exp.val = val
  map[id1].add(exp)
}


def minval = [] // mgi -> maximum similarity
def minval2 = [] // mgi -> maximum similarity
map.each { mgi, eumodicset ->
  def max = 10000
  def flag = false // in EUMODIC or not
  def list = eumodicset.sort { it.val }.reverse()
  list.eachWithIndex { exp, i ->
    if (exp.id == mgi) {
      minval2 << i
    } else {
      minval << i
    }
  }
}
/*
print "a = c("
minval2.each{print it+","}
println ""
*/

print "x = c("
minval.each{print it+","}
println ""
