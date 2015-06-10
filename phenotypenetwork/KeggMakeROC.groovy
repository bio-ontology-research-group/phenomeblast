import cern.colt.list.*
import cern.jet.stat.*

def baseline = new File(args[0])
def disease = new File(args[1])

DoubleArrayList baselist = new DoubleArrayList()
baseline.splitEachLine("\t") { line ->
  line.each {
    try {
      Double d = new Double(it)
      baselist.add(d)
    } catch (Exception E) {
      println "Did not add $it."
    }
  }
}

DoubleArrayList diseaselist = new DoubleArrayList()
disease.splitEachLine("\t") { line ->
  line.each {
    try {
      Double d = new Double(it)
      diseaselist.add(d)
    } catch (Exception E) {
    }
  }
}
def dsize = diseaselist.size()
def bsize = baselist.size()
baselist.sort()
diseaselist.sort()

for (double d = 0; d <= 1.01 ; d+=0.0005) {
  def fp = 1-Descriptive.quantileInverse(baselist,1-d)
  def tp = 1-Descriptive.quantileInverse(diseaselist,1-d)
  println "$fp $tp"
}
