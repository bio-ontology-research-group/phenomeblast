import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import de.tudresden.inf.lat.cel.owlapi.*

def fn = args[0] // ontology filename

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(fn))

OWLDataFactory fac = manager.getOWLDataFactory()

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasoner reasoner = new CelReasoner(ont,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

ont.getClassesInSignature().each { cl ->
  def clst = cl.toString()
  if ((clst.indexOf("ZDB")>-1) || (clst.indexOf("MGI")>-1) || (clst.indexOf("OMIM")>-1)) {
    def s = reasoner.getSuperClasses(cl, false).getFlattened()
    def t = reasoner.getEquivalentClasses(cl).getEntities()
    s.addAll(t)
    
    print clst+"\t"
    s.each {
      def clname = it.toString()
      if ( clname.indexOf("HP_")>-1 ) {
        print it.toString()+"\t"
      }
    }
  }
  println ""
}

reasoner.finalize()


