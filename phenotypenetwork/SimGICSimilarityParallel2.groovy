import java.util.concurrent.*
import cern.colt.bitvector.*
import cern.colt.matrix.*
import cern.colt.matrix.impl.*
import cern.colt.list.*

def THREADS = 10

def stepsize = 1000

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

def icmap = new LinkedHashMap()

def myoutfile = new File("all/phenotypes-simgic.txt")
def icfile = new File("all/phenotypes-info.txt")
def phenotypefile = new File("all/phenotypes.txt")

def outdir = "all/phenotypes-simgic-"

def DoubleMatrix1D makeList(Set set, int length, Map ics, Map inverse) {
  DoubleMatrix1D vector = new SparseDoubleMatrix1D(length)
  set.each {
    vector.setQuick(it, ics[inverse[it]])
  }
  return vector
}

// def simGIC = { Set v1, Set v2, Map ics, Map inverse ->
//   def inter = 0.0
//   def un = 0.0
//   v1.each { 
//     if (v2.contains(it)) {
//       inter+=ics[inverse[it]]
//     }
//     un+=ics[inverse[it]]
//   }
//   v2.each { un+=ics[inverse[it]] }
//   un-=inter
//   if (un == 0.0) {
//     return 0.0
//   } else {
//     return inter/un
//   }
// }

def double simGIC(Set v1, Set v2, Map ics, Map inverse, Double icm) { // v1 and v2 are sets of indices
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

def double simGIC(Set v1, Set v2) { // v1 and v2 are sets of indices
  def inter = 0.0
  def un = 0.0
  v1.each { 
    if (v2.contains(it)) {
      inter+= 1
    }
    un+= 1
  }
  v2.each { un+= 1 }
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
  def ic = new Double(it[1])
  if (ic > icmax) {
    icmax = ic
  }
  icmap[id] = ic
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

def phenotypelist = []
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

//def calculateAndWriteOut = { from, to ->
//
//def buffer = new StringBuilder()
//def counter = 0
  
println "Beginning parallel computation"

def calc = { from, to ->
  def fout = new PrintWriter(new BufferedWriter(new FileWriter(new File(outdir+"$from-$to"))))
  for (int i = from ; i < to ; i++) {
    println "Calculating $i."
    def vec1 = phenotypelist[i]
    for (int j = i ; j < numphenotype ; j++) {
      def vec2 = phenotypelist[j]
      def res = simGIC(vec1, vec2, icmap, inverseIndexMap, icmax)
      fout.print(res+"\t")
    }
    fout.println ("")
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

// ((numphenotype/stepsize)+1).times { step ->
//   phenotypemap.keySet().each { outer ->
//     phenotypemap.keySet().each { inner ->
//     }
//     //    defer { 
//       //      calculateAndWriteOut(step*stepsize,(step+1)*stepsize) 
//     //    }
//   }
// }

    //    if ((counter >= from) && (counter < to)) {
      //      def missed = false
      //      def vec1 = phenotypemap[outer]
      //      def colcounter = 0
//       phenotypemap.keySet().each { inner ->
// 	if (!missed) {
// 	  if (inner==outer) {
// 	    missed = true
// 	  }
// 	}
// 	if (!missed) {
// 	  fout.print(0.0+"\t")
// 	} else {
// 	  def vec2 = phenotypemap[inner]
// 	  // def res = simGIC(vec1, vec2, icmap, inverseIndexMap) 
// 	  def res = 0.0
// 	  if (res<cutoff) {
// 	    res = 0.0
// 	  }
// 	  fout.print(res+"\t")
// 	}
// 	colcounter+=1
//       }
//      fout.println("\n")
      
//      rowcounter += 1
      //      println counter + " of " + numEntries + " done."
      //    }
      //    counter+=1
      //  }
//  fout.println(buffer)
      //  fout.flush()
      //  fout.close()




//1000.times { defer { 10000000000.times { def a = 0; a = a+100 } } }


pool.shutdown()
