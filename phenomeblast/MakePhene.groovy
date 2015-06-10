import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary

def minNumClass = 0 // any entity with less than minNumClass annotations will not be included

def dir = new File("final-combined/")
def uberon = new File("uberon.obo")
def cell = new File("cell.obo")
def outfile = "/tmp/phene.owl"
def omim = new File("phenotype_annotation.omim")


def onturi = "http://purl.org/obo/owlapi/phene#"

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


def po = factory.getOWLObjectProperty(IRI.create(onturi+"part-of"))
def hp = factory.getOWLObjectProperty(IRI.create(onturi+"has-part"))
def inheresin = factory.getOWLObjectProperty(IRI.create(onturi+"inheres-in"))
def hasquality = factory.getOWLObjectProperty(IRI.create(onturi+"has-quality"))
def qualifier = factory.getOWLObjectProperty(IRI.create(onturi+"qualifier"))
def towards = factory.getOWLObjectProperty(IRI.create(onturi+"towards"))




/********************************** Read OMIM files *********************************/

def omimsynopsis = [:]

omim.splitEachLine("\t") {
  def hpoid = it[4]
  def isnot = it[3]
  def evidence = it[6]
  def omimid = "OMIM_"+it[1]
  def omimname = it[2]
  if ((evidence=="IEA")&&(isnot != "NOT")) {
    OWLClass cl = factory.getOWLClass(IRI.create(onturi+omimid))
    addAnno(cl,OWLRDFVocabulary.RDFS_LABEL,omimname)
    OWLClass omimclass = factory.getOWLClass(IRI.create(onturi+"OMIM"))
    def ax = factory.getOWLSubClassOfAxiom(cl,omimclass)
    manager.addAxiom(ontology,ax)
    if (omimsynopsis[cl]==null) {
      omimsynopsis[cl] = new TreeSet()
    }
    if (id2class[hpoid]) {
      omimsynopsis[cl].add(id2class[hpoid])
    }
  }
}

omimsynopsis.keySet().each { cl ->
  if (omimsynopsis[cl].size()>minNumClass) {
    def intersectionClass = factory.getOWLObjectIntersectionOf(omimsynopsis[cl])
    def ax = factory.getOWLEquivalentClassesAxiom(cl,intersectionClass)
    manager.addAxiom(ontology,ax)
  }
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

def p1 = factory.getOWLObjectProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#part_of"))
def ax = factory.getOWLEquivalentObjectPropertiesAxiom(po,p1)
manager.addAxiom(ontology, ax)

p1 = factory.getOWLObjectProperty(IRI.create("http://purl.org/obo/owlapi/phene#has_part"))
ax = factory.getOWLEquivalentObjectPropertiesAxiom(hp,p1)
manager.addAxiom(ontology, ax)

p1 = factory.getOWLObjectProperty(IRI.create("http://purl.org/obo/owlapi/phene#inheres_in"))
ax = factory.getOWLEquivalentObjectPropertiesAxiom(inheresin,p1)
manager.addAxiom(ontology, ax)

p1 = factory.getOWLObjectProperty(IRI.create("http://purl.org/obo/owlapi/phene#has_quality"))
ax = factory.getOWLEquivalentObjectPropertiesAxiom(hasquality,p1)
manager.addAxiom(ontology, ax)

p1 = factory.getOWLObjectProperty(IRI.create("http://purl.org/obo/owlapi/phene#qualifier"))
ax = factory.getOWLEquivalentObjectPropertiesAxiom(qualifier,p1)
manager.addAxiom(ontology, ax)

p1 = factory.getOWLObjectProperty(IRI.create("http://purl.org/obo/owlapi/phene#towards"))
ax = factory.getOWLEquivalentObjectPropertiesAxiom(towards,p1)
manager.addAxiom(ontology, ax)

ax = factory.getOWLTransitiveObjectPropertyAxiom(hp)
manager.addAxiom(ontology, ax)
ax = factory.getOWLReflexiveObjectPropertyAxiom(hp)
manager.addAxiom(ontology, ax)
ax = factory.getOWLTransitiveObjectPropertyAxiom(po)
manager.addAxiom(ontology, ax)
ax = factory.getOWLReflexiveObjectPropertyAxiom(po)
manager.addAxiom(ontology, ax)
//ax = factory.getOWLInverseObjectPropertiesAxiom(po,hp)
//manager.addAxiom(ontology, ax)
//ax = factory.getOWLInverseObjectPropertiesAxiom(inheresin,hasquality)
//manager.addAxiom(ontology, ax)
//ax = factory.getOWLFunctionalObjectPropertyAxiom(inheresin)
//manager.addAxiom(ontology, ax)

manager.saveOntology(ontology, IRI.create("file:"+outfile))
