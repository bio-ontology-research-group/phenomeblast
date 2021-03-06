import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary

def minNumClass = 1 // any entity with less than minNumClass annotations will not be included

def dir = new File("owl-el-human-mouse-only/")
def uberon = new File("obo/uberon.obo")
def cell = new File("obo/cell.obo")
def behavior = new File("obo/behavior.obo")
def outfile = "/tmp/phene.owl"

def fish = new File("fish/fishphenotypes.txt")
def yeast = new File("yeast/yeastphenotypes.txt")
def fly = new File("fly/flyphenotypes.txt")

def textmatches = new File("textmatches/lexical.txt")

def onturi = "http://purl.obolibrary.org/obo/"
def onturirel = "http://purl.obolibrary.org/obo/PHENOMENET_"

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

/*********************************************************************************
 Adding alt-ids as equivalent classes
*********************************************************************************/

def id2alt = [:]
new File("obo/").eachFile {
  if (it.toString().indexOf("human-phenotype-ontology.obo")>-1 || (it.toString().indexOf("mammalian_phenotype.obo")>-1)) {
    def tid = ""
    it.eachLine { line ->
      if (line.startsWith("id: ")) {
	tid = line.substring(3).trim()
      }
      if (line.startsWith("alt_id:")) {
	def aid = line.substring(7).trim()
	if (id2alt[tid] == null) {
	  id2alt[tid] = new TreeSet()
	}
	id2alt[tid].add(aid)
      }
      if (line.startsWith("property_value: alt:id ")) {
	line = line.replaceAll("property_value: alt:id ", "").trim()
	def aid = line.replaceAll("xsd:string", "").trim()
	if (id2alt[tid] == null) {
	  id2alt[tid] = new TreeSet()
	}
	id2alt[tid].add(aid)
      }
    }
  }  
}
id2alt.each { tid, s ->
  s.each { aid ->
    tid = tid.replaceAll(":","_")
    aid = aid.replaceAll(":","_")
    def cl1 = factory.getOWLClass(IRI.create(onturi+tid))
    def cl2 = factory.getOWLClass(IRI.create(onturi+aid))
    def ax = factory.getOWLEquivalentClassesAxiom(cl1,cl2)
    manager.addAxiom(ontology, ax)
  }
}

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


def po = factory.getOWLObjectProperty(IRI.create(onturirel+"part-of"))
def hp = factory.getOWLObjectProperty(IRI.create(onturirel+"has-part"))
def inheresin = factory.getOWLObjectProperty(IRI.create(onturirel+"inheres-in"))
def hasquality = factory.getOWLObjectProperty(IRI.create(onturirel+"has-quality"))
def qualifier = factory.getOWLObjectProperty(IRI.create(onturirel+"qualifier"))
def towards = factory.getOWLObjectProperty(IRI.create(onturirel+"towards"))
def imrt = factory.getOWLObjectProperty(IRI.create(onturirel+"increased_in_magnitude_relative_to"))
def dmrt = factory.getOWLObjectProperty(IRI.create(onturirel+"decreased_in_magnitude_relative_to"))


def thing = factory.getOWLThing()

/******************************** Text matching equivalences *****************************/

textmatches.splitEachLine("\t") {
  def hpo = it[0]?.replaceAll("_",":")?.trim()
  def mp = it[2]?.replaceAll("_",":")?.trim()
  def what = it[5]?.replaceAll("_",":")?.trim()

  def cl1 = id2class[hpo]
  def cl2 = id2class[mp]

  def ax = null
  if (cl1!=null && cl2!=null) {
    if (what == null) {
      ax = factory.getOWLEquivalentClassesAxiom(cl1,cl2)
    } else if (what == "<") {
      ax = factory.getOWLSubClassOfAxiom(cl1,cl2)
    } else {
      ax = factory.getOWLSubClassOfAxiom(cl2,cl1)
    }
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

behavior.eachLine {line ->
  if (line.startsWith("id: ")) {
    term = line.substring(4).trim()
    if (cl2xref[term]==null) {
      cl2xref[term]=new TreeSet()
    }
  }
  if (line.indexOf("property_value: http://purl.obolibrary.org/obo/xref ")>-1) {
    def xref = line.replaceAll("property_value: http://purl.obolibrary.org/obo/xref ", "")
    xref = xref.replaceAll(" xsd:string","").replaceAll("\"","").trim()
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

def p1 = factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/PHENOMENET_part_of"))
def ax = factory.getOWLEquivalentObjectPropertiesAxiom(po,p1)
manager.addAxiom(ontology, ax)

p1 = factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/PHENOMENET_has_part"))
ax = factory.getOWLEquivalentObjectPropertiesAxiom(hp,p1)
manager.addAxiom(ontology, ax)

p1 = factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/PHENOMENET_inheres_in"))
ax = factory.getOWLEquivalentObjectPropertiesAxiom(inheresin,p1)
manager.addAxiom(ontology, ax)

p1 = factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/PHENOMENET_has_quality"))
ax = factory.getOWLEquivalentObjectPropertiesAxiom(hasquality,p1)
manager.addAxiom(ontology, ax)

p1 = factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/PHENOMENET_qualifier"))
ax = factory.getOWLEquivalentObjectPropertiesAxiom(qualifier,p1)
manager.addAxiom(ontology, ax)

p1 = factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/PHENOMENET_towards"))
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
