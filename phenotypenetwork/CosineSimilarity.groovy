import cern.colt.bitvector.*
import cern.colt.matrix.*
import cern.colt.matrix.impl.*
import cern.colt.list.*

def maxic = 16 // to normalize IC to values <=1
def requiredIC = 5.0 

def icmap = [:]

def outfile = new File("cosine2.txt")
def icfile = new File("phenotypes2-info.txt")
def phenotypefile = new File("phenotypes2.txt")

def BitVector makeBitVector(DoubleMatrix1D set) {
  BitVector vector = new BitVector(set.size())
  def i1 = new IntArrayList()
  def d1 = new DoubleArrayList()
  set.getNonZeros(i1,d1)
  i1.elements().each {
    vector.putQuick(it, true)
  }
  return vector
}

def DoubleMatrix1D makeList(List set, int length, Map ics, Map inverse) {
  DoubleMatrix1D vector = new SparseDoubleMatrix1D(length)
  set.each {
    vector.setQuick(it, ics[inverse[it]])
  }
  return vector
}

def double cosineSimilarity(DoubleMatrix1D v1, DoubleMatrix1D v2, BitVector b1, BitVector b2) { // with some performance tweaks
  def sim = 0.0
  def b = b1.clone()
  b.and(b2)
  if (b.cardinality()>1) {
    sim = v1.zDotProduct(v2)/Math.sqrt(v1.zDotProduct(v1)*v2.zDotProduct(v2))
  }
  return sim
}

icfile.splitEachLine("\t") {
  def id = it[0]
  def ic = new Double(it[1])
  if (ic>requiredIC) {
    ic = ic / maxic
    icmap[id] = ic
  }
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
def inverseIndexMap = [:]
def count = 0
icmap.keySet().each {
  indexMap[it] = count
  inverseIndexMap[count] = it
  count+=1
}

phenotypemap.keySet().each {
  phenotypemap[it] = phenotypemap[it].collect{ indexMap[it]!=null?indexMap[it]:-1 }
  phenotypemap[it] = phenotypemap[it].minus(-1)
}
def vectorLength = icmap.keySet().size()

def vectorMap = [:]
def bitvectorMap = [:]
def bitvectorSet = new HashSet()
phenotypemap.keySet().each { key ->
  def val = phenotypemap[key]
  DoubleMatrix1D vec = makeList(val, vectorLength, icmap, inverseIndexMap)
  BitVector bv = makeBitVector(vec)
  if (!bitvectorSet.contains(bv)) {
     bitvectorSet.add(bv)
     vectorMap[key] = vec
     bitvectorMap[key] = bv
  }
}

def numEntries = vectorMap.keySet().size()

//DoubleMatrix2D matrix = DoubleFactory2D.dense.make(numEntries, numEntries)

def fout = new PrintWriter(new BufferedWriter(new FileWriter(outfile)))

phenotypemap.keySet().each {
  fout.print(it+"\t")
}
fout.println("")

def rowcounter = 0
vectorMap.keySet().each { outer ->
  def vec1 = vectorMap[outer]
  def bvec1 = bitvectorMap[outer]
  def colcounter = 0
  vectorMap.keySet().each { inner ->
    def vec2 = vectorMap[inner]
    def bvec2 = bitvectorMap[inner]
    def res = cosineSimilarity(vec1, vec2, bvec1, bvec2)
//    matrix.set(rowcounter, colcounter, res)
    fout.print(res+"\t")
    colcounter++
  }
  rowcounter += 1
  println rowcounter + " of " + numEntries + " done."
  fout.println("")
}

fout.flush()
fout.close()
