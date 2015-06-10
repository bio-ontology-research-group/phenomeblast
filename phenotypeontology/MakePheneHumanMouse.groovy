import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary

def minNumClass = 1 // any entity with less than minNumClass annotations will not be included

def dir = new File("owl-el-mouse/")
def uberon = new File("obo/uberon.obo")
def cell = new File("obo/cell.obo")
def behavior = new File("obo/behavior.obo")
def outfile = "/tmp/phene.owl"

def mouse = new File("mouse/mousephenotypes.txt")
def omim = new File("omim/omimphenotypes.txt")
def orphanet = new File("orphanetphenotypes.txt")

def anika1 = new File("anika/omim2hp_ont_e_wof_ref.txt")
def anika2 = new File("anika/omim2mp_intr_all_ref.txt")

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

/********************************** Create Anika's disease definitions ********************************/

def anika2eq = [:]
anika1.splitEachLine("\t") {
  def name = "ANIKA1"+it[0].trim()
  if (anika2eq[name]==null) {
    anika2eq[name]=new TreeSet()
  }
  it[1..-1]?.each { c ->
    if (id2class[c]!=null) {
      anika2eq[name].add(id2class[c])
    } else {
      println "Could not find class $c"
    }
  }
}

anika2.splitEachLine("\t") {
  def name = "ANIKA2"+it[0].trim()
  if (anika2eq[name]==null) {
    anika2eq[name]=new TreeSet()
  }
  it[1..-1].each { c ->
    anika2eq[name].add(id2class[c])
  }
}

anika2eq.keySet().each {
  def name = it
  def val = anika2eq[it]
  def cl = null
  if (val.size()>=2) {
    cl = factory.getOWLObjectIntersectionOf(val)
  } else if (val.size()==1) {
    val.each { cl = it }
  }
  def flyc = factory.getOWLClass(IRI.create(onturi+name))
  def ax = factory.getOWLEquivalentClassesAxiom(flyc,cl)
  //  manager.addAxiom(ontology,ax)
}

/********************************** Create mouse genes and genotypes ********************************/

def mouse2eq = [:]
mouse.splitEachLine("\t") {
  def name = it[0].trim()
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
  if (cl!=null) {
    def flyc = factory.getOWLClass(IRI.create(onturi+name))
    def ax = factory.getOWLEquivalentClassesAxiom(flyc,cl)
    manager.addAxiom(ontology,ax)
  }
}

/********************************** Create human genes and genotypes ********************************/

def omim2eq = [:]
omim.splitEachLine("\t") {
  def name = it[0].trim()
  if (omim2eq[name]==null) {
    omim2eq[name]=new TreeSet()
  }
  def id = it[1].trim()
  if (id2class[id]!=null) {
    omim2eq[name].add(id2class[id])
  }
}

omim2eq.keySet().each {
  def name = it
  def val = omim2eq[it]
  def cl = null
  if (val.size()>=2) {
    cl = factory.getOWLObjectIntersectionOf(val)
  } else {
    val.each { cl = it }
  }
  def flyc = factory.getOWLClass(IRI.create(onturi+name))
  def ax = factory.getOWLEquivalentClassesAxiom(flyc,cl)
  manager.addAxiom(ontology,ax)
}

/********************************** Create OrphaNet diseases ********************************/

def orpha2eq = [:]
orphanet.splitEachLine("\t") {
  def name = it[0].trim()
  if (orpha2eq[name]==null) {
    orpha2eq[name]=new TreeSet()
  }
  def id = it[1].trim()
  if (id2class[id]!=null) {
    orpha2eq[name].add(id2class[id])
  }
}

orpha2eq.keySet().each {
  def name = it
  def val = orpha2eq[it]
  def cl = null
  if (val.size()>=2) {
    cl = factory.getOWLObjectIntersectionOf(val)
  } else if (val.size()==1) {
    val.each { cl = it }
  }
  if (cl!=null) {
    def flyc = factory.getOWLClass(IRI.create(onturi+name))
    def ax = factory.getOWLEquivalentClassesAxiom(flyc,cl)
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
	if (cl1!=null && cl2!=null) {
	  def ax = factory.getOWLEquivalentClassesAxiom(cl1,cl2)
	  manager.addAxiom(ontology, ax)
	}
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
