/* groovy GenerateGeneric infile.owl Subclass Superclass */

import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import de.tudresden.inf.lat.cel.owlapi.*

def ontfile = args[0]
def infile = new File(args[1])
def outfile = new File(args[2])
def onturi = "http://purl.obolibrary.org/obo/"
def onturirel = "http://purl.obolibrary.org/obo/"

def fout = new PrintWriter(new FileWriter(outfile))

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(ontfile))

OWLDataFactory fac = manager.getOWLDataFactory()

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasoner reasoner = new CelReasoner(ont,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

def classes = [:]
infile.splitEachLine("\t") { line ->
  def id = line[0].trim()
  def cl = fac.getOWLClass(IRI.create(onturi+id))
  classes[id] = cl
}

classes.keySet().each { id ->
  def cl = classes[id]
  def s = reasoner.getSuperClasses(cl, false).getFlattened()
  def t = reasoner.getEquivalentClasses(cl).getEntities()
  s.addAll(t)
  fout.print(id+"\t")
  s.each {
    def clname = it.toString()
    if ( (clname.indexOf("MP_")>-1) ||
	 (clname.indexOf("HP_")>-1) ||
	 (clname.indexOf("WBPhenotype_")>-1) ||
	 (clname.indexOf("FBcv_")>-1) ||
	 (clname.indexOf("APO_")>-1))
    {
      fout.print(it.toString()+"\t")
    }
  }
  fout.println("")
}
fout.close()
reasoner.finalize()
//reasoner.getSubClasses(fac.getOWLClass(IRI.create("http://purl.org/obo/owlapi/phene#HP_0000118")), false).each { println it }

