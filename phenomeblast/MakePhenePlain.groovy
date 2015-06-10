import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary


def dir = new File("final-human-mouse/")
def uberon = new File("uberon.obo")
def outfile = "/tmp/phene-plain2.owl"
def omim = new File("phenotype_annotation.omim")
def jax = new File("MGI_PhenoGenoMP.rpt")
def zfin = new File("zfin.txt")


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
    if (id2class[xref]!=null) {
      cl2xref[term].add(xref)
    }
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

// cl2xref.keySet().each { key ->
//   def val = cl2xref[key]
//   if (val.size()>=2) {
//     Iterator iter = val.iterator()
//     OWLClass cl1 = id2class[iter.next()]
//     OWLClass cl2 = id2class[iter.next()]
//     def ax = factory.getOWLEquivalentClassesAxiom(cl1,cl2)
//     manager.addAxiom(ontology, ax)
//     while (iter.hasNext()) {
//       OWLClass cl3 = id2class[iter.next()]
//       cl1 = cl2
//       cl2 = cl3
//       ax = factory.getOWLEquivalentClassesAxiom(cl1,cl2)
//       manager.addAxiom(ontology, ax)
//     }
//   }
// }

/******************************** ObjectProperty equivalences *****************************/

def p1 = factory.getOWLObjectProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#part_of"))
def ax = factory.getOWLEquivalentObjectPropertiesAxiom(po,p1)
manager.addAxiom(ontology, ax)


manager.saveOntology(ontology, IRI.create("file:"+outfile))
