


@Grapes([
	  @Grab(group='org.semanticweb.elk', module='elk-owlapi', version='0.4.2'),
	  @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='4.0.2'),
	  @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='4.0.2'),
	  @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='4.0.2')
	])


import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.util.*
import org.semanticweb.elk.owlapi.ElkReasonerFactory


OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
OWLOntologyManager manager = OWLManager.createOWLOntologyManager()
OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration()
config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT)
def fSource = new FileDocumentSource(new File("GO_1.ont"))
OWLOntology ont = manager.loadOntologyFromOntologyDocument(fSource, config)
OWLReasoner oReasoner = reasonerFactory.createReasoner(ont);
oReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
ont.getObjectPropertiesInSignature(true).each { println it }
