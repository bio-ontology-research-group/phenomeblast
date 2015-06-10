import cern.colt.bitvector.*
import cern.colt.matrix.*
import cern.colt.matrix.impl.*
import cern.colt.list.*

//def maxic = 16 // to normalize IC to values <=1
def cutoff = 0.000

def requiredIC = 1

def icmap = [:]

DoubleMatrix2D all = DoubleFactory2D.dense.make(numEntries, numEntries)

def outfile = new File("all/phenotypes-simgic.txt")
def icfile = new File("all/phenotypes-info.txt")
def phenotypefile = new File("all/phenotypes.txt")

def DoubleMatrix1D makeList(Set set, int length, Map ics, Map inverse) {
  DoubleMatrix1D vector = new SparseDoubleMatrix1D(length)
  set.each {
    vector.setQuick(it, ics[inverse[it]])
  }
  return vector
}

def double simGIC(Set v1, Set v2, Map ics, Map inverse) { // v1 and v2 are sets of indices
  def inter = 0.0
  def un = 0.0
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

icfile.splitEachLine("\t") {
  def id = it[0]
  def ic = new Double(it[1])
  if (ic>requiredIC) {
    icmap[id] = ic
  }
}

def phenotypemap = [:]
phenotypefile.splitEachLine("\t") { line ->
  if (line.size()>1) {
    def name = line[0]
    phenotypemap[name] = new LinkedHashSet()
    line[1..-2].each { phenotypemap[name].add(it) }
  }
}

def indexMap = [:]
def inverseIndexMap = [:]
def count = 0
icmap.keySet().each {
  indexMap[it] = count
  inverseIndexMap[count] = it
  count+=1
}
phenotypemap.keySet().each {
  phenotypemap[it] = new TreeSet(phenotypemap[it].collect{ indexMap[it]!=null?indexMap[it]:-1 })
  phenotypemap[it] = phenotypemap[it].minus(-1)
}

def vectorLength = icmap.keySet().size()

def numEntries = phenotypemap.keySet().size()

def fout = new PrintWriter(new BufferedWriter(new FileWriter(outfile)))

phenotypemap.keySet().each {
  fout.print(it+"\t")
}
fout.println("")

println "Calculating similarity"
def rowcounter = 0
phenotypemap.keySet().each { outer ->
  def vec1 = phenotypemap[outer]
  def colcounter = 0
  phenotypemap.keySet().each { inner ->
    def vec2 = phenotypemap[inner]
    def res = simGIC(vec1, vec2, icmap, inverseIndexMap)
    if (res<cutoff) {
       res = 0.0
    }
    fout.print(res+"\t")
    colcounter++
  }
  rowcounter += 1
  println rowcounter + " of " + numEntries + " done."
  fout.println("")
}

fout.flush()
fout.close()
