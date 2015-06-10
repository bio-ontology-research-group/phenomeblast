import java.util.concurrent.*
import java.text.*

def cutoff = 0.000

def minPhenotypes = 5
def icmap = new HashMap()

def icfile = new File("../data/phenotypes-info.txt")
def phenotypefile = new File("../data/phenotypes.txt")


def THREADS = 32
def pool = Executors.newFixedThreadPool(THREADS)
def defer = { c -> pool.submit(c as Callable) }

float simGIC(Set v1, Set v2, Map ics) { // v1 and v2 are sets of indices
  def inter = 0.0
  def  un = 0.0
  v1.each {
    if (v2.contains(it)) {
      inter+=ics[it]
    }
    un+=ics[it]
  }
  v2.each { un+=ics[it] }
  un-=inter
  if (un == 0.0) {
    return 0.0
  } else {
    return inter/un
  }
}

icfile.splitEachLine("\t") {
  def id = it[0]
  float ic = new Float(it[1])
  icmap[id] = ic
}

def phenotypemap = [:]
def max = 50000
def count = 0
phenotypefile.splitEachLine("\t") { line ->
  if (count < max) {
  if (line.size()>minPhenotypes) {
    def name = line[0]
    phenotypemap[name] = new LinkedHashSet()
    line[1..-2].each { phenotypemap[name].add(it) }
  }
  count += 1
  }
}
def phenotypelist = []
phenotypemap.keySet().each { phenotypelist  << it }

/* Make matrix */
def size = phenotypemap.keySet().size()
float[][] dist = new float[size][]
for (int i = 0 ; i < size ; i++) {
  dist[i] = new float[size-i]
  if (i%10000==0) { println "Step $i of $size" }
}

println "Matrix created, starting parallel computation"
def calc = { line ->
  def phenos = phenotypemap[phenotypelist[line]]
  for (int i = line; i < size ; i++) {
    def p2 = phenotypemap[phenotypelist[i]]
    dist[line][i-line] = simGIC(phenos, p2, icmap)
  }
}

List futures = []
for (int i = 0 ; i < size ; i++) {
  futures << pool.submit( { calc(i) } as Callable )
}
futures.each{println it.get()} 
pool.shutdown()
println dist[20000][0]
println dist[20000][1]
