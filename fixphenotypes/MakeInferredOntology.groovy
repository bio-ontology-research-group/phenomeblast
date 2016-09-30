@Grapes([
          @Grab(group='org.semanticweb.elk', module='elk-owlapi', version='0.4.2'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='4.2.1'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='4.2.1'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='4.2.1'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-parsers', version='4.2.1'),
          @GrabConfig(systemClassLoader=true)
        ])

import org.semanticweb.owlapi.model.parameters.*
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerConfiguration
import org.semanticweb.elk.reasoner.config.*
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.owllink.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.search.*;
import org.semanticweb.owlapi.manchestersyntax.renderer.*;
import org.semanticweb.owlapi.reasoner.structural.*


OWLOntologyManager manager = OWLManager.createOWLOntologyManager()
OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(args[0]))
OWLOntology outont = manager.createOntology(IRI.create("http://aber-owl.net/inferred-ontology.owl"))
def onturi = "http://aber-owl.net/phenotype.owl#"
OWLDataFactory fac = manager.getOWLDataFactory()
ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
ElkReasonerFactory f1 = new ElkReasonerFactory()
OWLReasoner reasoner = f1.createReasoner(ont,config)
reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)
InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredEquivalentClassAxiomGenerator()])
generator.fillOntology(fac, outont)
manager.saveOntology(outont, IRI.create("file:"+args[1]))
