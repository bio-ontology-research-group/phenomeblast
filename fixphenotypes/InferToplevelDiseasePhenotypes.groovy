@Grapes([
	  @Grab(group='org.semanticweb.elk', module='elk-owlapi', version='0.4.2'),
	  @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='4.1.0'),
	  @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='4.1.0'),
	  @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='4.1.0'),
	  @Grab(group='net.sourceforge.owlapi', module='owlapi-parsers', version='4.1.0'),
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
OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("a.owl"))

OWLDataFactory fac = manager.getOWLDataFactory()
ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
ElkReasonerFactory f1 = new ElkReasonerFactory()
OWLReasoner reasoner = f1.createReasoner(ont,config)
reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter("toplevel.txt")))
def first = new LinkedHashSet()
reasoner.getSubClasses(fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/MP_0000001")), true).getNodes()?.each { node ->
  def cl = node.getRepresentativeElement()
  first.add(cl)
}
def second = new LinkedHashSet()
first.each { f ->
  reasoner.getSubClasses(f, true).getNodes()?.each { node ->
    def cl = node.getRepresentativeElement()
    second.add(cl)
  }
}

println "First: "+first
println "Second: "+second

new File("diseasephenotypes.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def pheno = line[1]?.replaceAll("HP:","http://purl.obolibrary.org/obo/HP_")
  pheno = pheno?.replaceAll("MP:","http://purl.obolibrary.org/obo/MP_")
  def cl = fac.getOWLClass(IRI.create(pheno))
  def sup = reasoner.getSuperClasses(cl, false).getFlattened()
  fout.println("$id\t"+sup.intersect(first)+"\t"+sup.intersect(second))
}
