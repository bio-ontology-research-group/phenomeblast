import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.elk.owlapi.*

def fn = args[0] // ontology filename

def fout = new PrintWriter(new BufferedWriter(new FileWriter(new File(args[1]))))

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(fn))

OWLDataFactory fac = manager.getOWLDataFactory()

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasonerFactory fac1 = new ElkReasonerFactory()
OWLReasoner reasoner = fac1.createReasoner(ont,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

ont.getClassesInSignature().each { cl ->
  def clst = cl.toString()
    def s = reasoner.getSuperClasses(cl, false).getFlattened()
    def t = reasoner.getEquivalentClasses(cl).getEntities()
    s.addAll(t)
    
    fout.print(clst+"\t")
    s.each {
      def clname = it.toString()
      fout.print(it.toString()+"\t")
    }
    fout.println("")
}
fout.flush()
fout.close()
reasoner.finalize()


