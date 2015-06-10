import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary


def dir = new File("anatomy-el/")
def uberon = new File("anatomy-obo/uberon.obo")
def outfile = "/tmp/anatomy.owl"

def onturi = "http://bioonto.de/phene.owl#"
def id2class = [:] // maps an OBO-ID to an OWLClass

OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
OWLDataFactory factory = manager.getOWLDataFactory()

def ontSet = new TreeSet()

dir.eachFile {
  ontSet.add(manager.loadOntologyFromOntologyDocument(it))
}

OWLOntology ontology = manager.createOntology(IRI.create(onturi), ontSet)

ontology.getClassesInSignature(true).each {
  def a = it.toString()
  a = a.substring(a.indexOf('#')+1,a.length()-1)
  a = a.replaceAll("_",":")
  if (id2class[a] == null) {
    id2class[a] = it
  }
}

def addAnno = {resource, prop, cont ->
  OWLAnnotation anno = factory.getOWLAnnotation(
    factory.getOWLAnnotationProperty(prop.getIRI()),
    factory.getOWLTypedLiteral(cont))
  def axiom = factory.getOWLAnnotationAssertionAxiom(resource.getIRI(),
						     anno)
  manager.addAxiom(ontology,axiom)
}


def cl2xref = [:]

uberon.eachLine {line ->
  if (line.startsWith("id: ")) {
    term = line.substring(4).trim()
    if (cl2xref[term]==null) {
      cl2xref[term]=new TreeSet()
    }
  }
  if (line.startsWith("xref: ")) {
    def xref = line.substring(6).trim()
    cl2xref[term].add(xref)
  }
}

cl2xref.keySet().each { key ->
  def val = cl2xref[key]
  if (id2class[key]) {
    cl2xref[key].each {
      if (id2class[it]) {
	OWLClass cl1 = id2class[key]
	OWLClass cl2 = id2class[it]
	def ax = factory.getOWLSubClassOfAxiom(cl2,cl1)
	manager.addAxiom(ontology, ax)
      }
    }
  }
}



manager.saveOntology(ontology, IRI.create("file:"+outfile))
