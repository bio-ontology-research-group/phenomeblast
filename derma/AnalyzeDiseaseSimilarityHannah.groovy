/* Generates a distance matrix of diseases useful for hierarchical clustering */

import cern.colt.bitvector.*
import cern.colt.matrix.*
import cern.colt.matrix.impl.*
import cern.colt.list.*

def omim2name = [:]
def omimnamesf = new File("omimphenotypes-names.txt")
omimnamesf.splitEachLine("\t") { line ->
  def omim = line[0]
  def name = line[1]
  omim2name[omim] = name
}

def hannah = new TreeSet()
new File("derma2.tsv").splitEachLine("\t") { line ->
  def id = line[9]?.split(",")?.collect { "OMIM:"+(it.trim()) }
  id?.each { 
    if (it.length()>6) {
      hannah.add(it) 
    }
  }
}

def icfile = new File("phenotypes-info.txt")

def double simGIC(Set v1, Set v2) { // v1 and v2 are sets of indices
  def inter = 0.0
  def un = 0.0
  v1.each { 
    if (v2.contains(it)) {
      inter+=1
    }
    un+=1
  }
  v2.each { un+=1 }
  un-=inter
  if (un == 0.0) {
    return 0.0
  } else {
    return inter/un
  }
}

def icmap = [:]
icfile.splitEachLine("\t") {
  def id = it[0]
  def ic = new Double(it[1])
  icmap[id] = ic
}
def indexMap = [:]
def inverseIndexMap = [:]
def count = 0
icmap.keySet().each {
  indexMap[it] = count
  inverseIndexMap[count] = it
  count+=1
}

def disphenotypesall = [:]
new File("phenotypes.txt").splitEachLine("\t") { line ->
  def dis = line[0]
  dis = dis.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","")
  if (line.size()>1) {
    if (dis in hannah) {
      def omim = dis
      disphenotypesall[omim] = new TreeSet(line[1..-1])
    }
  }
}
println disphenotypesall.size()
def numEntries = disphenotypesall.size()
DoubleMatrix2D all = DoubleFactory2D.dense.make(numEntries, numEntries)
DoubleMatrix2D rare = DoubleFactory2D.dense.make(numEntries, numEntries)
DoubleMatrix2D common = DoubleFactory2D.dense.make(numEntries, numEntries)



def computeSimilarity = { pmap, matrix ->
  def counter1 = 0
  def counter2 = 0
  pmap.each { dis1, phenotypes1 ->
    counter2 = 0
    pmap.each { dis2, phenotypes2 ->
      def res = 1-simGIC(phenotypes1, phenotypes2)
      matrix.set(counter1, counter2, res)
      counter2+=1
    }
    counter1 +=1
  }
}

computeSimilarity(disphenotypesall, all)

def fout = null

dislist = []
disphenotypesall.keySet().each { 
  dislist << it
}

/*
dislist = dislist.collect { if (omim2name[it]?.length()>10) {
    omim2name[it].substring(0,10)+"("+it.substring(5)+")"
  } else {
    omim2name[it]+"("+it.substring(5)+")"
  }
}
*/

fout = new PrintWriter(new FileWriter("hannah-distance-all.txt"))
for (int i = 0 ; i < numEntries-1 ; i++) {
  fout.print(dislist[i])
  fout.print("\t")
}
fout.print(dislist[numEntries-1])
fout.println("")
for (int i = 0; i < numEntries ; i++) {
  fout.print(dislist[i]+"\t")
  for (int j = 0; j < numEntries ; j++) {
    fout.print(all.get(i, j))
    if( j<numEntries-1 ) {
      fout.print("\t")
    }
  }
  fout.println("")
}
fout.close()
