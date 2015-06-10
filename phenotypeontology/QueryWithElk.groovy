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


def onturi = "http://bioonto.de/pgkb.owl#"

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLDataFactory fac = manager.getOWLDataFactory()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("/tmp/phene.owl"))

def id2class = [:] // maps an OBO-ID to an OWLClass
ont.getClassesInSignature(true).each {
  def a = it.toString()
  a = a.substring(a.indexOf('#')+1,a.length()-1)
  a = a.replaceAll("<http://purl.obolibrary.org/obo/","")
  a = a.replaceAll("_",":")
  if (id2class[a] == null) {
    id2class[a] = it
  }
}


def c = { String s ->
  fac.getOWLClass(IRI.create(onturi+s))
}

def r = { String s ->
  fac.getOWLObjectProperty(IRI.create("http://bioonto.de/ro2.owl#"+s))
}

OWLReasonerFactory reasonerFactory = null

Set q = new TreeSet()
args[0..-1].each { id ->
  if (id2class[id]!=null) {
    q.add(id2class[id])
  }
}
println q

def qcl = c("QueryForGeorge")
def ax = fac.getOWLEquivalentClassesAxiom(qcl, fac.getOWLObjectIntersectionOf(q))
manager.addAxiom(ont,ax)

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)

OWLReasonerFactory f1 = new ElkReasonerFactory()
OWLReasoner reasoner = f1.createReasoner(ont,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

reasoner.getSubClasses(qcl, false).getFlattened().each {
  println it.toString()
}