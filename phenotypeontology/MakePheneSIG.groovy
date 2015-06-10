import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary

def minNumClass = 1 // any entity with less than minNumClass annotations will not be included

def dir = new File("sig/")
def uberon = new File("obo/uberon.obo")
def cell = new File("obo/cell.obo")
def outfile = "/tmp/mouse-phene.owl"

def mouse = new File("mouse/mousephenotypes.txt")

def textmatches = new File("textmatches/lexical.txt")


def onturi = "http://gong.manchester.ac.uk/MouseGenes.owl#"
def onturirel = "http://purl.org/obo/owl/OBO_REL#"

def id2class = [:] // maps an OBO-ID to an OWLClass



OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
OWLDataFactory factory = manager.getOWLDataFactory()

def ontSet = new TreeSet()

dir.eachFile {
  if (it.isFile()) {
    ontSet.add(manager.loadOntologyFromOntologyDocument(it))
  }
}

OWLOntology ontology = manager.createOntology(IRI.create(onturi+"phenomebrowser"), ontSet)

ontology.getClassesInSignature(true).each {
  def a = it.toString()
  a = a.substring(a.indexOf("obo/")+4,a.length()-1)
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

def hasphene = factory.getOWLObjectProperty(IRI.create(onturirel+"has-phenotype"))



def thing = factory.getOWLThing()


/********************************** Create mouse genes and genotypes ********************************/

def mouse2eq = [:]
mouse.splitEachLine("\t") {
  def name = it[0].trim().replaceAll(":","_")
  if (mouse2eq[name]==null) {
    mouse2eq[name]=new TreeSet()
  }
  def id = it[1].trim()
  if (id2class[id]!=null) {
    mouse2eq[name].add(id2class[id])
  }
}

mouse2eq.keySet().each {
  def name = it
  def val = mouse2eq[it]
  def cl = null
  if (val.size()>=2) {
    cl = factory.getOWLObjectIntersectionOf(val)
  } else {
    val.each { cl = it }
  }
  def flyc = factory.getOWLClass(IRI.create(onturi+name))
  
  def ax = factory.getOWLEquivalentClassesAxiom(flyc,factory.getOWLObjectSomeValuesFrom(hasphene,cl))
  manager.addAxiom(ontology,ax)
}

/******************************** UBERON and CELL equivalences *****************************/


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

cell.eachLine {line ->
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
	def ax = factory.getOWLEquivalentClassesAxiom(cl1,cl2)
	manager.addAxiom(ontology, ax)
      }
    }
  }
}


/******************************** ObjectProperty equivalences *****************************/

//ax = factory.getOWLInverseObjectPropertiesAxiom(po,hp)
//manager.addAxiom(ontology, ax)
//ax = factory.getOWLInverseObjectPropertiesAxiom(inheresin,hasquality)
//manager.addAxiom(ontology, ax)
//ax = factory.getOWLFunctionalObjectPropertyAxiom(inheresin)
//manager.addAxiom(ontology, ax)

manager.saveOntology(ontology, IRI.create("file:"+outfile))
