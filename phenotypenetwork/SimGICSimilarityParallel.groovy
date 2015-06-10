import java.util.concurrent.*
import java.text.*

def THREADS = 5

def stepsize = 1000
def minPhenotype = 5

def initialElem = new Integer(args[0])
def endElem = new Integer(args[1])

pool = Executors.newFixedThreadPool(THREADS)
defer = { c -> pool.submit(c as Callable) }


/* Compares phenotype vectors from index to maxindex */
//def index = new Integer(args[0])
//def maxindex = new Integer(args[1])

//def maxic = 16 // to normalize IC to values <=1
def cutoff = 0.000

def requiredIC = -1

def icmap = new HashMap()

def outdir = "../data/phenotypes-simgic-"
def myoutfile = new File("../data/phenotypes-simgic.txt")
def icfile = new File("all/phenotypes-info.txt")
def phenotypefile = new File("all/phenotypes.txt")


float simGIC(Set v1, Set v2, Map ics, Map inverse, float icm) { // v1 and v2 are sets of indices
  def inter = 0.0
  def  un = 0.0
  v1.each { 
    if (v2.contains(it)) {
      inter+=ics[inverse[it]]
    }
    un+=ics[inverse[it]]
  }
  v2.each { un+=ics[inverse[it]] }
  un-=inter
  if (un == 0.0) {
    return 0.0
  } else {
    return inter/un
  }
}

def icmax = -1
icfile.splitEachLine("\t") {
  def id = it[0]
  float ic = new Float(it[1])
  if (ic > icmax) {
    icmax = ic
  }
  icmap[id] = ic
}

def phenotypemap = [:]
phenotypefile.splitEachLine("\t") { line ->
  if (line.size()>minPhenotype) {
    def name = line[0]
    phenotypemap[name] = new LinkedHashSet()
    line[1..-2].each { phenotypemap[name].add(it) }
  }
}

def indexMap = new HashMap()
def inverseIndexMap = new HashMap()
int count = 0
icmap.keySet().each {
  indexMap[it] = count
  inverseIndexMap[count] = it
  count+=1
}
phenotypemap.keySet().each {
  phenotypemap[it] = new LinkedHashSet(phenotypemap[it].collect{ indexMap[it]!=null?indexMap[it]:-1 })
  phenotypemap[it] = phenotypemap[it].minus(-1)
}

def phenotypelist = [] // phenotypelist is a list of TreeSets of indices
phenotypemap.keySet().each {
  phenotypelist << phenotypemap[it]
}

def vectorLength = icmap.keySet().size()

def numEntries = phenotypemap.keySet().size()

def myfout = new PrintWriter(new BufferedWriter(new FileWriter(myoutfile)))

phenotypemap.keySet().each {
  myfout.print(it+"\t")
}
myfout.flush()
myfout.close()

def rowcounter = 0
def numphenotype = phenotypelist.size()

println "Beginning parallel computation"

def calc = { from, to ->
  def fout = new PrintWriter(new BufferedWriter(new FileWriter(outdir+"$from-$to")))
  for (int i = from ; i < to ; i++) {
    println "Calculating $i of $numphenotype."
    def vec1 = phenotypelist[i]
    for (int j = i ; j < numphenotype ; j++) {
      def vec2 = phenotypelist[j]
      def res = simGIC(vec1, vec2, icmap, inverseIndexMap, icmax)
      fout.print(new DecimalFormat("#.####").format(res)+"\t")
    }
    fout.println("")
  }
  fout.flush()
  fout.close()
}

int start = initialElem
while (start < numphenotype && start < endElem) {
  def end = start + stepsize
  if (end>numphenotype) {
    end = numphenotype
  }
  if (end>endElem) {
    end = endElem
  }
  def start1 = new Integer(start)
  def end1 = new Integer(end)
  defer {
    calc(start1,end1)
  }
  start = end
}

pool.shutdown()
