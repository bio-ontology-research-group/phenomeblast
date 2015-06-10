import org.coode.owlapi.turtle.*
import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary

OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
OWLDataFactory fac = manager.getOWLDataFactory()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(args[0]))

manager.saveOntology(ont, new TurtleOntologyFormat(), IRI.create("file:"+args[1]))
