import cern.colt.bitvector.*
import cern.colt.matrix.*

def outfile = new File("tanimoto3.txt")
def icfile = new File("phenotypes3-info.txt")
def phenotypefile = new File("phenotypes3.txt")


def BitVector makeBitVector(List set, int length) {
  BitVector vector = new BitVector(length)
  set.each {
    vector.putQuick(it, true)
  }
  return vector
}

def double calcTanimoto(BitVector vec1, BitVector vec2) {
  BitVector vec = vec1.copy()
  vec.and(vec2)
  def v1 = vec1.cardinality()
  def v2 = vec2.cardinality()
  def v = vec.cardinality()
  double res = v/(v1+v2-v)
}

def icmap = [:]
icfile.splitEachLine("\t") {
  def id = it[0]
  def ic = new Double(it[1])
  icmap[id] = ic
}

def phenotypemap = [:]
phenotypefile.splitEachLine("\t") { line ->
  if (line.size()>1) {
    def name = line[0]
    phenotypemap[name] = new TreeSet()
    line[1..-2].each { phenotypemap[name].add(it) }
  }
}

def indexMap = [:]
def count = 0
icmap.keySet().each {
  indexMap[it] = count++
}

phenotypemap.keySet().each {
  phenotypemap[it] = phenotypemap[it].collect { indexMap[it] }
}

def vectorLength = icmap.keySet().size()

def vectorMap = [:]
phenotypemap.keySet().each { key ->
  def val = phenotypemap[key]
  BitVector vec = makeBitVector(val, vectorLength)
  vectorMap[key] = vec
}


def numEntries = vectorMap.keySet().size()
//DoubleMatrix2D matrix = DoubleFactory2D.sparse.make(numEntries, numEntries)

def fout = new PrintWriter(new BufferedWriter(new FileWriter(outfile)))

phenotypemap.keySet().each {
  fout.print(it+"\t")
}
fout.println("")

def rowcounter = 0
vectorMap.keySet().each { outer ->
  def vec1 = vectorMap[outer]
  def colcounter = 0
  vectorMap.keySet().each { inner ->
    def vec2 = vectorMap[inner]
    def res = calcTanimoto(vec1, vec2)
//    matrix.set(rowcounter, colcounter, res)
    fout.print(res+"\t")
    colcounter++
  }
  rowcounter += 1
  if (0 == (rowcounter%100)) {
    println rowcounter + " of " + numEntries + " done."
  }
  fout.println("")
}

fout.flush()
fout.close()
