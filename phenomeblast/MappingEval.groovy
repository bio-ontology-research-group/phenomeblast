//def m1 = new File("mappings/hp2mp.txt")
//def m1 = new File("mappings/mp2hp.txt")
def m1 = new File("mgi2omim-2-or-more.txt")

def map = [:]
m1.splitEachLine("\t") {
  map[it[0]] = new TreeSet()
  if (it.size()>1) {
    def n = it[0]
    it[1..it.size()-1].each {
      map[n].add(it)
    }
  }
}

def count = 0
def l = []
map.values().each {
  l << it.size()
  if (it.size()>0) count++
}
l = l.sort()
//l.each {println it}
println count
//println map["<http://bioonto.de/phene.owl#OMIM_187500>"].size()
