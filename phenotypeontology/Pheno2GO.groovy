import java.util.regex.Matcher
import java.util.regex.Pattern
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
OWLReasoner reasoner = fac1.createReasoner(ont)

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


def map = [:]
def id = ""
new File(args[0]).eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
    if (id.indexOf(" ")>-1) {
      id = id.substring(0, id.indexOf(" ")).trim()
    }
  }
  if (line.indexOf("GO")>-1 && ! line.startsWith("!")) {
    line = line.substring(line.indexOf("GO")).trim()
    if (line.indexOf(" ")) {
      line = line.substring(0,line.indexOf(" ")).trim()
    }
    map[id] = line
  }
}

def m2 = [:]
def s = new TreeSet()
map.each { k, v ->
  if (id2class[v]) {
    //    println "$k\t$v"
    m2[k] = new TreeSet()
    m2[k].add(v)
    def cl = id2class[v]
    reasoner.getSuperClasses(cl, false).getFlattened().each { c ->
      //      println k+"\t"+c.toString().replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":")
      m2[k].add(c.toString().replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":"))
    }
  }
}

def go2pw = [:]
new File("pathwaysList.txt").splitEachLine("\t") { line ->
  if (line.size()>1) {
    go2pw[line[0]] = new TreeSet()
    line[1..-1].each { go2pw[line[0]].add(it) }
  }
}

m2.each { hpo, gos ->
  if (gos) {
    gos.each { go ->
      if (go2pw[go]) {
	go2pw[go].each {
	  println "$hpo\t$it\t$go"
	}
      }
    }
  }
}

reasoner.finalize()
System.exit(0)
