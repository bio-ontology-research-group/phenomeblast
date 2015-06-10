import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary


def dir = new File("final-combined/")
def uberon = new File("uberon.obo")
def outfile = "/tmp/phene.owl"
def omim = new File("phenotype_annotation.omim")
def jax = new File("MGI_PhenoGenoMP.rpt")
def zfin = new File("zfin.txt")


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


// // /********************************** Create ZFIN genes and genotypes ********************************/

def zfin2eq = [:]
zfin.splitEachLine("\t") {
  def name = it[0].trim()
  /* place G- in front of ID for genes so searching (using grep) is easier */
  def gene = "G-"+name.substring(0,name.lastIndexOf("-"))
  def zfa = it[6].trim()
  def pato = it[10].trim()
  def zfac = id2class[zfa]
  def patoc = id2class[pato]
  if ((zfac!=null) && (patoc!=null)) {
    def cl1 = factory.getOWLObjectSomeValuesFrom(hasquality,patoc)
    def cl2 = factory.getOWLObjectIntersectionOf(zfac,cl1)
    def cl3 = factory.getOWLObjectSomeValuesFrom(hp,cl2)
    /* collect info for genotypes */
    if (zfin2eq[name]==null) {
      zfin2eq[name]=new TreeSet()
    }
    zfin2eq[name].add(cl3)
  }
  //   /* now genes */
  //   if (zfin2eq[gene]==null) {
  //     zfin2eq[gene]=new TreeSet()
  //   }
  //   zfin2eq[gene].add(cl3)
}

zfin2eq.keySet().each {
  def name = it
  def val = zfin2eq[it]
  def cl = factory.getOWLObjectIntersectionOf(val)
  def zfinc = factory.getOWLClass(IRI.create(onturi+name))
  def ax = factory.getOWLEquivalentClassesAxiom(zfinc,cl)
  manager.addAxiom(ontology,ax)
}

// /********************************** Create MGI Genotypes ********************************/

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



// /********************************** Read OMIM files *********************************/

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
  if (omimsynopsis[cl].size()>0) {
    def intersectionClass = factory.getOWLObjectIntersectionOf(omimsynopsis[cl])
    def ax = factory.getOWLEquivalentClassesAxiom(cl,intersectionClass)
    manager.addAxiom(ontology,ax)
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
