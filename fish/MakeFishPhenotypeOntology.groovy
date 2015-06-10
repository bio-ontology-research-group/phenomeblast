import java.util.logging.Logger
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.profiles.*
import org.semanticweb.owlapi.util.*
import org.semanticweb.owlapi.io.*
import org.semanticweb.elk.owlapi.*
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary

def ontfile = new File("zebrafish_anatomy.obo")
def patofile = new File("quality.obo")
def gofile = new File("gene_ontology_ext.obo")

def id2name = [:]
new File("quality.tbl").splitEachLine("\t") { line ->
  def id = line[0]
  def name = line[1]
  id2name[id] = name
}
new File("zebrafish_anatomy.tbl").splitEachLine("\t") { line ->
  def id = line[0]
  def name = line[1]
  id2name[id] = name
}

String formatClassNames(String s) {
  s=s.replace("<http://purl.obolibrary.org/obo/","")
  s=s.replace(">","")
  s=s.replace("_",":")
  s
}

def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  o longOpt:'output-file', 'output file',args:1, required:true
  //  t longOpt:'threads', 'number of threads', args:1
  //  k longOpt:'stepsize', 'steps before splitting jobs', arg:1
}

def opt = cli.parse(args)
if( !opt ) {
  //  cli.usage()
  return
}
if( opt.h ) {
  cli.usage()
  return
}

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLDataFactory fac = manager.getOWLDataFactory()
OWLDataFactory factory = fac

def ontset = new TreeSet()
OWLOntology ont = manager.loadOntologyFromOntologyDocument(ontfile)
ontset.add(ont)
ont = manager.loadOntologyFromOntologyDocument(patofile)
ontset.add(ont)

ont = manager.createOntology(IRI.create("http://lc2.eu/temp.owl"), ontset)

OWLOntology outont = manager.createOntology(IRI.create("http://phenomebrowser.net/fish-phenotype.owl"))
def onturi = "http://phenomebrowser.net/fish-phenotype.owl#"

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)

OWLReasonerFactory f1 = new ElkReasonerFactory()
OWLReasoner reasoner = f1.createReasoner(ont,config)

OWLAnnotationProperty label = fac.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI())

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

def r = { String s ->
  if (s == "part-of") {
    factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050"))
  } else if (s == "has-part") {
    factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000051"))
  } else {
    factory.getOWLObjectProperty(IRI.create("http://phenomebrowser.net/#"+s))
  }
}

def c = { String s ->
  factory.getOWLClass(IRI.create(onturi+s))
}

def id2class = [:] // maps a name to an OWLClass
ont.getClassesInSignature(true).each {
  def aa = it.toString()
  aa = formatClassNames(aa)
  if (id2class[aa] != null) {
  } else {
    id2class[aa] = it
  }
}

def addAnno = {resource, prop, cont ->
  OWLAnnotation anno = factory.getOWLAnnotation(
    factory.getOWLAnnotationProperty(prop.getIRI()),
    factory.getOWLTypedLiteral(cont))
  def axiom = factory.getOWLAnnotationAssertionAxiom(resource.getIRI(),
                                                     anno)
  manager.addAxiom(outont,axiom)
}



def phenotypes = new HashSet()
new File("phenotype.txt").splitEachLine("\t") { line ->
  def e = line[6]
  def e2 = line[8]
  def q = line[10]
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
    addAnno(cl,OWLRDFVocabulary.RDFS_LABEL,id2name[exp.e]+" phenotype")
    //    addAnno(cl,OWLRDFVocabulary.RDF_DESCRIPTION,"The mass of $oname that is used as input in a single $name is decreased.")
    manager.addAxiom(outont, factory.getOWLEquivalentClassesAxiom(
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
    addAnno(cl,OWLRDFVocabulary.RDFS_LABEL,id2name[exp.e]+" "+id2name[exp.q])
    manager.addAxiom(outont, factory.getOWLEquivalentClassesAxiom(
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
    addAnno(cl,OWLRDFVocabulary.RDFS_LABEL,id2name[exp.e]+" "+id2name[exp.q]+" towards "+id2name[exp.e2])
    manager.addAxiom(outont, factory.getOWLEquivalentClassesAxiom(
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

manager.addAxiom(outont, fac.getOWLTransitiveObjectPropertyAxiom(r("has-part")))
manager.addAxiom(outont, fac.getOWLTransitiveObjectPropertyAxiom(r("part-of")))
manager.addAxiom(outont, fac.getOWLReflexiveObjectPropertyAxiom(r("has-part")))
manager.addAxiom(outont, fac.getOWLReflexiveObjectPropertyAxiom(r("part-of")))

manager.addAxiom(
  outont, fac.getOWLEquivalentObjectPropertiesAxiom(
    r("part-of"), fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050"))))


OWLImportsDeclaration importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/zfa.owl"))
manager.applyChange(new AddImport(outont, importDecl1))
importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/pato.owl"))
manager.applyChange(new AddImport(outont, importDecl1))
importDecl1 = fac.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/go.owl"))
manager.applyChange(new AddImport(outont, importDecl1))



manager.saveOntology(outont, IRI.create("file:"+opt.o))
System.exit(0)
