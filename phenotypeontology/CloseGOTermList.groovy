import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.elk.owlapi.*


OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("obo/gene_ontology_edit.obo"))

OWLDataFactory fac = manager.getOWLDataFactory()

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasonerFactory fac1 = new ElkReasonerFactory()
OWLReasoner reasoner = fac1.createReasoner(ont,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)
def ontology = ont
def id2class = [:]
ontology.getClassesInSignature(true).each {
  def aa = it.toString()
  aa = aa.substring(aa.indexOf('#')+1,aa.length()-1)
  aa = aa.replaceAll("_",":")
  aa = aa.replaceAll("<http://purl.obolibrary.org/obo/","")
  if (id2class[aa] != null) {
  } else {
    id2class[aa] = it
  }
}
ontology.getObjectPropertiesInSignature(true).each {
  def aa = it.toString()
  aa = aa.substring(aa.indexOf('#')+1,aa.length()-1)
  aa = aa.replaceAll("_",":")
  if (id2class[aa] != null) {
  } else {
    id2class[aa] = it
  }
}

new File(args[0]).eachLine { line ->
  line = line.trim()
  if (id2class[line]) {
    println line
    def cl = id2class[line]
    reasoner.getSuperClasses(cl, false).getFlattened().each { c ->
      println c.toString().replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":")
    }
  }
}


reasoner.finalize()
