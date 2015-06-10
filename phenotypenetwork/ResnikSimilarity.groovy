import java.util.concurrent.*
import java.text.*


def THREADS = 5

pool = Executors.newFixedThreadPool(THREADS)
defer = { c -> pool.submit(c as Callable) }

def phenotypes = [:]
new File("allphenotypes/").eachFile { f ->
  f.splitEachLine("\t") { line ->
    def id = line[0]
    def pt = line[1]
    if (phenotypes[id]==null) {
      phenotypes[id] = new TreeSet()
    }
    phenotypes[id].add(pt)
  }
}

def pmap = [:] // phenotype class to all its super-classes
new File("../data/phenotypes-resnik.txt").splitEachLine("\t") { line ->
  def id = line[0]?.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":")
  if (id.indexOf("MP")>-1 || 
      id.indexOf("HP")>-1 ||
      id.indexOf("DDPHENO")>-1 ||
      id.indexOf("FBcv")>-1 ||
      id.indexOf("WBPhenotype")>-1)
  {
    def pts = line[1..-1]?.collect { it.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":") }
    pmap[id] = new TreeSet()
    pts.each { pmap[id].add(it) }
  }
}

println pmap