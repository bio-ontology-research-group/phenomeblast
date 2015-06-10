import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary


def dir = new File("inel/")
def uberon = new File("uberon.obo")
def outfile = "/tmp/phene-george.owl"
def george = new File("george/")
def jax = new File("MGI_PhenoGenoMP.rpt")



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


def po = factory.getOWLObjectProperty(IRI.create("http://purl.org/obo/owlapi/phene#part-of"))
def inh = factory.getOWLObjectProperty(IRI.create("http://purl.org/obo/owlapi/phene#inheres-in"))



/********************************** Create George's Diseases ****************************/
def dis2hpo = [:]

george.eachFile { file ->
  def skip1 = true
  file.splitEachLine("\t") {
    if (skip1) {
      skip1=false
    } else {
      def dis = "GEORGE_"+it[0].replaceAll(":","_")
      def hpo = id2class[it[6]]
      if (dis2hpo[dis] == null) {
	dis2hpo[dis] = new TreeSet()
      }
      if (hpo!=null) {
	dis2hpo[dis].add(hpo)
      }
    }
  }
}
dis2hpo.keySet().each {key ->
  def value = dis2hpo[key]
  if (value.size()>0) {
    OWLClass cl = factory.getOWLClass(IRI.create(onturi+key))
    def intClass = factory.getOWLObjectIntersectionOf(value)
    ax = factory.getOWLEquivalentClassesAxiom(cl,intClass)
    manager.addAxiom(ontology,ax)
    //    addAnno(cl,OWLRDFVocabulary.RDFS_LABEL,name)
  }
}


/********************************** Create MGI Genotypes ********************************/

def checked = new TreeSet()
def gene2mp = [:]

jax.splitEachLine("\t") {
  def id = it[-1].replaceAll(":","_").trim()
  if (id.indexOf(",")>0) {
  
  } else {
    def name = it[1]
    if (gene2mp[id]==null) {
      gene2mp[id] = new TreeSet()
    }
    def mp = it[3]
    gene2mp[id].add(id2class[mp])
  }
}

gene2mp.keySet().each {key ->
  def value = gene2mp[key]
  if (gene2mp[key].size()>0) {
    OWLClass gtclass = factory.getOWLClass(IRI.create(onturi+"Genotype"))
    OWLClass cl = factory.getOWLClass(IRI.create(onturi+key))
    def ax = factory.getOWLSubClassOfAxiom(cl,gtclass)
    manager.addAxiom(ontology,ax)
    def intClass = factory.getOWLObjectIntersectionOf(value)
    ax = factory.getOWLEquivalentClassesAxiom(cl,intClass)
    manager.addAxiom(ontology,ax)
    //    addAnno(cl,OWLRDFVocabulary.RDFS_LABEL,name)
  }
}


/******************************** UBERON equivalences *****************************/


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

/******************************** ObjectProperty equivalences *****************************/

def p1 = factory.getOWLObjectProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#part_of"))
def ax = factory.getOWLEquivalentObjectPropertiesAxiom(po,p1)
manager.addAxiom(ontology, ax)


manager.saveOntology(ontology, IRI.create("file:"+outfile))
