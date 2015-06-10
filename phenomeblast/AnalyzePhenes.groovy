import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import de.tudresden.inf.lat.cel.owlapi.*


OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(args[0]))

OWLDataFactory fac = manager.getOWLDataFactory()

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasoner reasoner = new CelReasoner(ont,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

ont.getClassesInSignature().each { cl ->
  def clst = cl.toString()
  if (clst.indexOf("OMIM")>-1) {
    def s = reasoner.getSubClasses(cl, false).getFlattened()
    def t = reasoner.getEquivalentClasses(cl).getEntities()
    print clst+"\t"
    s.each {
      if (it.toString().indexOf("MGI_")>-1) {
	print it.toString()+"\t"
      }
    }
    t.each {
<<<<<<< AnalyzePhenes.groovy
      if (it.toString().indexOf("MGI")>-1) {
	print it.toString()+"\t"
      }
    }
    println ""
  }
}

println "\nNow looking for DIRECT superclasses\n"

ont.getClassesInSignature().each { cl ->
  def clst = cl.toString()
  if (clst.indexOf("OMIM")>-1) {
    def s = reasoner.getSuperClasses(cl, true).getFlattened()
    print clst+"\t"
    s.each {
      if (it.toString().indexOf("MGI")>-1) {
	print it.toString()+"\t"
      }
    }
=======
      if (it.toString().indexOf("MGI_")>-1) {
	print it.toString()+"\t"
      }
    }
>>>>>>> 1.8
    println ""
  }
}

reasoner.finalize()
//reasoner.getSubClasses(fac.getOWLClass(IRI.create("http://purl.org/obo/owlapi/phene#HP_0000118")), false).each { println it }

