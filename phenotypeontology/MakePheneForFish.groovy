import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.elk.owlapi.*
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.profiles.*
import org.semanticweb.owlapi.util.*
import org.semanticweb.elk.owlapi.*


def minNumClass = 1 // any entity with less than minNumClass annotations will not be included

def dir = new File("owl-el-fish/")
def uberon = new File("obo/uberon.obo")
def cell = new File("obo/cell.obo")
def behavior = new File("obo/behavior.obo")
def outfile = "/tmp/phene-fish.owl"

def mouse = new File("mouse/mousephenotypes.txt")
def fish = new File("fish/fishphenotypes-gene.txt")
def omim = new File("omim/omimphenotypes.txt")

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
def ont = ontology
def fac = factory

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

/********************************** Make Zebrafish Phenotype Ontology ******************************/
OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)

OWLReasonerFactory f1 = new ElkReasonerFactory()
OWLReasoner reasoner = f1.createReasoner(ont,config)

OWLAnnotationProperty label = fac.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI())

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

def c = { String s ->
  factory.getOWLClass(IRI.create(onturi+s))
}

def r = { String s ->
  factory.getOWLObjectProperty(IRI.create(onturirel+s))
}

def phenotypes = new HashSet()
fish.splitEachLine("\t") { line ->
  def e = line[1]
  def e2 = line[3]
  def q = line[2]
  Expando exp = new Expando()
  exp.e = e
  exp.e2 = e2
  exp.q = q
  phenotypes.add(exp)
}

def count = 1 // global ID counter

def edone = new HashSet()
def e2p = [:]
def a2b = [:]
def a2c = [:]
/* Create abnormality of E classes */
phenotypes.each { exp ->
  def e = id2class[exp.e]
  def q = id2class[exp.q]
  def e2 = id2class[exp.e2]
  if (e!=null && ! (e in edone)) {
    edone.add(e)
    def cl = c("ZPO:$count")
    //    addAnno(cl,OWLRDFVocabulary.RDFS_LABEL,id2name[exp.e]+" phenotype")
    //    addAnno(cl,OWLRDFVocabulary.RDF_DESCRIPTION,"The mass of $oname that is used as input in a single $name is decreased.")
    manager.addAxiom(ont, factory.getOWLEquivalentClassesAxiom(
		       cl,
		       fac.getOWLObjectSomeValuesFrom(
			 r("has-part"),
			 fac.getOWLObjectIntersectionOf(
			   fac.getOWLObjectSomeValuesFrom(
			     r("part-of"), e),
			   fac.getOWLObjectSomeValuesFrom(
			     r("has-quality"), id2class["PATO:0000001"])))))
    count += 1
  }
  if (e2p[e]== null) {
    e2p[e] = new HashSet()
  }
  if (e!=null && q!=null && ! (q in e2p[e])) {
    e2p[e].add(q)
    def cl = c("ZPO:$count")
    //    addAnno(cl,OWLRDFVocabulary.RDFS_LABEL,id2name[exp.e]+" "+id2name[exp.q])
    manager.addAxiom(ont, factory.getOWLEquivalentClassesAxiom(
		       cl,
		       fac.getOWLObjectSomeValuesFrom(
			 r("has-part"),
			 fac.getOWLObjectIntersectionOf(
			   e,
			   fac.getOWLObjectSomeValuesFrom(
			     r("has-quality"), q)))))
    count += 1
  }
  if (a2b[e] == null) {
    a2b[e] = new HashSet()
    a2c[e] = new HashSet()
  }
  if (e!=null && q!=null && e2!=null && (! (q in a2b[e])) && (! (e2 in a2c[e]))) {
    a2b[e].add(q)
    a2c[e].add(e2)
    def cl = c("ZPO:$count")
    //    addAnno(cl,OWLRDFVocabulary.RDFS_LABEL,id2name[exp.e]+" "+id2name[exp.q]+" towards "+id2name[exp.e2])
    manager.addAxiom(ont, factory.getOWLEquivalentClassesAxiom(
		       cl,
		       fac.getOWLObjectSomeValuesFrom(
			 r("has-part"),
			 fac.getOWLObjectIntersectionOf(
			   e,
			   fac.getOWLObjectSomeValuesFrom(
			     r("has-quality"), 
			     fac.getOWLObjectIntersectionOf(
			       q, fac.getOWLObjectSomeValuesFrom(r("towards"), e2)))))))
    count += 1
  }
}

manager.addAxiom(ont, fac.getOWLTransitiveObjectPropertyAxiom(r("has-part")))
manager.addAxiom(ont, fac.getOWLTransitiveObjectPropertyAxiom(r("part-of")))
manager.addAxiom(ont, fac.getOWLReflexiveObjectPropertyAxiom(r("has-part")))
manager.addAxiom(ont, fac.getOWLReflexiveObjectPropertyAxiom(r("part-of")))

/*manager.addAxiom(
  ont, fac.getOWLEquivalentObjectPropertiesAxiom(
  r("part-of"), fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050"))))*/


/*OWLImportsDeclaration importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/zfa.owl"))
manager.applyChange(new AddImport(ont, importDecl1))
importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/pato.owl"))
manager.applyChange(new AddImport(ont, importDecl1))
importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/go.owl"))
manager.applyChange(new AddImport(ont, importDecl1))*/


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

/********************************** Create ZFIN genes and genotypes ********************************/

def zfin2cl = [:]
fish.splitEachLine("\t") {
  def name = it[0].trim()
  /* place G- in front of ID for genes so searching (using grep) is easier */
  def e1 = it[1]?.trim()
  def q = it[2]?.trim()
  def e2 = it[3]?.trim()
  e1 = id2class[e1]
  q = id2class[q]
  e2 = id2class[e2]
  def cl = null
  if (e1!=null && q!=null && e2==null) { // just do EQ
    cl = fac.getOWLObjectSomeValuesFrom(
      r("has-part"),
      fac.getOWLObjectIntersectionOf(
	e1,
	fac.getOWLObjectSomeValuesFrom(
	  r("has-quality"), q)))

  } else if (e1!=null && q!=null) { // do E1 Q E2
    cl = fac.getOWLObjectSomeValuesFrom(
      r("has-part"),
      fac.getOWLObjectIntersectionOf(
	e1,
	fac.getOWLObjectSomeValuesFrom(
	  r("has-quality"), 
	  fac.getOWLObjectIntersectionOf(
	    q, fac.getOWLObjectSomeValuesFrom(r("towards"), e2)))))
  }
  if (cl!=null) {
    if (zfin2cl[name]==null) {
      zfin2cl[name]=new TreeSet()
    }
    zfin2cl[name].add(cl)
  } else {
    //    println "Error at: $e1\t$q\t$e2"
  }
}

mouse2eq.each { mgi, val ->
  if (zfin2cl[mgi]!=null) {
    zfin2cl[mgi].addAll(val)
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
    def zfinc = factory.getOWLClass(IRI.create(onturi+name))
    def ax = factory.getOWLEquivalentClassesAxiom(zfinc,cl)
      manager.addAxiom(ontology,ax)
  } else {
    println "Error at $name"
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
  if (flyc!=null && cl!=null) {
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
