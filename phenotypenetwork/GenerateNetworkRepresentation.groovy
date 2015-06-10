import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import de.tudresden.inf.lat.cel.owlapi.*

def fn = args[0] // ontology filename

def fout = new PrintWriter(new BufferedWriter(new FileWriter(new File(args[1]))))

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
  if ((clst.indexOf("ZDB")>0) || // fish
      (clst.indexOf("MGI")>0) || // mouse
      (clst.indexOf("OMIM")>0) || // human and disease
      (clst.indexOf("S000")>0) || // yeast
      (clst.indexOf("WBGene")>0) || // worm-alleles
      (clst.indexOf("WBVar")>0) || // worm-alleles
      (clst.indexOf("WBRNAi")>0) || // worm-alleles
      (clst.indexOf("FBal")>0)) // fly
  { 
    def s = reasoner.getSuperClasses(cl, false).getFlattened()
    def t = reasoner.getEquivalentClasses(cl).getEntities()
    s.addAll(t)
    
    fout.print(clst+"\t")
    s.each {
      def clname = it.toString()
      if ( 
	(clname.indexOf("ZDB")==-1) && 
	(clname.indexOf("MGI")==-1) && 
	(clname.indexOf("OMIM")==-1) &&
	(clname.indexOf("S000")==-1) && 
	(clname.indexOf("WBGene")==-1) && 
	(clname.indexOf("WBVar")==-1) && 
	(clname.indexOf("WBRNAi")==-1) && 
	(clname.indexOf("FBal")==-1)
      ) 
      {
	fout.print(it.toString()+"\t")
      }
    }
    fout.println("")
  }
}
fout.flush()
fout.close()
reasoner.finalize()


