import java.util.logging.Logger
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.profiles.*
import org.semanticweb.owlapi.util.*
import org.mindswap.pellet.KnowledgeBase
import org.mindswap.pellet.expressivity.*
import org.mindswap.pellet.*
import org.semanticweb.owlapi.io.*
import org.semanticweb.elk.owlapi.*

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

def mbm = new File("morbidmap")
def mpf = new File("mammalian_phenotype.obo")
def f1 = new File("forPhenomeNet3.txt")
def pf = new File("phenotypes.txt")

def onturi = "http://bioonto.de/pgkb.owl#"

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLDataFactory fac = manager.getOWLDataFactory()
def factory = fac

OWLOntology ont = manager.loadOntologyFromOntologyDocument(mpf)
def ontology = ont

def id2class = [:] // maps an OBO-ID to an OWLClass
ontology.getClassesInSignature(true).each {
  def a = it.toString()
  a = a.substring(a.indexOf("obo/")+4,a.length()-1)
  a = a.substring(a.indexOf('#')+1)
  a = a.replaceAll("_",":")
  a = a.replaceAll("tp://bio2rdf.org/","")
  if (id2class[a] == null) {
    id2class[a] = it
  }
}

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)

OWLReasonerFactory fac1 = new ElkReasonerFactory()
OWLReasoner reasoner = fac1.createReasoner(ont,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

def dis2ogene = [:]
def ogene2dis = [:]
mbm.splitEachLine("\\|") { line ->
  try {
    def oid = new Integer(line[0].substring(line[0].size()-10, line[0].size()-4))
    def gid = line[2]
    if (dis2ogene[oid] == null) {
      dis2ogene[oid] = new TreeSet()
    }
    dis2ogene[oid].add(gid)
    if (ogene2dis[gid] == null) {
      ogene2dis[gid] = new TreeSet()
    }
    ogene2dis[gid].add(oid)
  } catch (Exception E) {}
}

def a2p = [:]
def a2o = [:]
def omims = new TreeSet()
f1.splitEachLine("\t") { line ->
  def id = line[1]
  a2p[id] = new TreeSet()
  def oid = line[1]
  a2o[id] = oid
  omims.add(oid)
  def mps = line[2..-1]
  mps = mps.collect { 
    if (id2class[it]!=null) {
      reasoner.getSuperClasses(id2class[it], false).getFlattened()
    } else {
      null
    }
  }
  mps = mps.flatten().collect {
    it.toString().replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":")
  }
  def ss = new TreeSet(mps)
  a2p[id] = ss
}

def o2p = [:]
pf.splitEachLine("\t") { line ->
  def id = line[0]
  if (id.indexOf("OMIM")>-1 && id.indexOf("ANIKA")==-1 && id.indexOf("kristin")==-1) {
    id = id.replaceAll("<http://purl.obolibrary.org/obo/OMIM:","").replaceAll(">","")
    o2p[id] = new TreeSet()
    if (line.size()>1) {
      line[1..-1].each {
	def s = it.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":")
	o2p[id].add(s)
      }
    }
  }
}

o2p.each { omim, op ->
  def s1 = op
  a2p.each { mgi, mp ->
    def s2 = mp
    if (s1!=null && s2!=null) {
      println "$mgi\tOMIM:$omim\t"+simGIC(s1, s2)
    }
  }
}