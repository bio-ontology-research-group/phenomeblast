import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.elk.owlapi.*

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("human-phenotype-ontology.obo"))

OWLDataFactory fac = manager.getOWLDataFactory()

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasonerFactory fac1 = new ElkReasonerFactory()
OWLReasoner reasoner = fac1.createReasoner(ont)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

def id2class = [:] // maps an OBO-ID to an OWLClass
ont.getClassesInSignature(true).each {
  def a = it.toString()
  a = a.substring(a.indexOf("obo/")+4,a.length()-1)
  a = a.replaceAll("_",":")
  if (id2class[a] == null) {
    id2class[a] = it
  }
}

def ts = new TreeSet()
new File("../data/phenotypes.txt").splitEachLine("\t") { line ->
  if (line.size()>2) {
    line[1..-1].each { ts.add(it.replaceAll("<","").replaceAll(">","")) }
  }
}
def tsa = ts.toArray()

Random rand = new Random()
def fout =  new PrintWriter(new BufferedWriter(new FileWriter("testqueries.txt")))
1000.times { n ->
  def sclass = new TreeSet()
  10.times {
    def index = rand.nextInt(ts.size())
    def cl = fac.getOWLClass(IRI.create(tsa[index]))
    println tsa[index]
    sclass.addAll(reasoner.getSuperClasses(cl, false).getFlattened())
    sclass.addAll(reasoner.getEquivalentClasses(cl).getEntities())
  }
  fout.print("TESTQUERY:$n\t")
  sclass.each { fout.print("$it\t") }
  fout.println("")
}
fout.flush()
fout.close()