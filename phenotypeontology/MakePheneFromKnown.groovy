import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary


def dir = new File("inel/")
def uberon = new File("uberon.obo")
def outfile = "/tmp/phene.owl"
def omim = new File("phenotype_annotation.omim")
def jax = new File("HMD_HumanDisease.rpt")


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

/********************************** Create Genotypes ********************************/

def checked = new TreeSet()

jax.splitEachLine("\t") {
  def id = it[0].replaceAll(":","_")
  def name = it[1]
  def mpset = new TreeSet()
  def mp = it[6]?.split(",").each { mp ->
    mpset.add(id2class[mp])
  }
  if (mpset.size()>0 && (!checked.contains(id))) {
    OWLClass gtclass = factory.getOWLClass(IRI.create(onturi+"Genotype"))
    OWLClass cl = factory.getOWLClass(IRI.create(onturi+id))
    def ax = factory.getOWLSubClassOfAxiom(cl,gtclass)
    manager.addAxiom(ontology,ax)
    def intClass = factory.getOWLObjectIntersectionOf(mpset)
    ax = factory.getOWLEquivalentClassesAxiom(cl,intClass)
    manager.addAxiom(ontology,ax)
    addAnno(cl,OWLRDFVocabulary.RDFS_LABEL,name)
    checked.add(id)
  }
}

/********************************** Read OMIM files *********************************/

def omimsynopsis = [:]

omim.splitEachLine("\t") {
  def hpoid = it[4]
  def evidence = it[6]
  def omimid = "OMIM_"+it[1]
  def omimname = it[2]
  if (evidence=="IEA") {
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
  def intersectionClass = factory.getOWLObjectIntersectionOf(omimsynopsis[cl])
  def ax = factory.getOWLEquivalentClassesAxiom(cl,intersectionClass)
  manager.addAxiom(ontology,ax)
}

/***********************************************************************************/


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
	def ax = factory.getOWLEquivalentClassesAxiom(cl1,cl2)
	manager.addAxiom(ontology, ax)
      }
    }
  }
}



manager.saveOntology(ontology, IRI.create("file:"+outfile))
