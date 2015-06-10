/*

  Protocol: server listens on port and accepts a query as a single line containing OBO-style identifiers, e.g.:
  HP:0000102 MP:0000001 FBCV:0000001

  identifiers of multiple ontologies can be mixed
  
  Output is a list of all entities in PhenomeNET together with the SimGIC score based on the query
 */

import java.net.*
import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.elk.owlapi.*
import org.apache.commons.lang.*

SERVERADDRESS = "localhost"
SERVERPORT = 31337
FRONTENDPORT = 31338

def fn = args[0] // ontology filename

def names = [:]
def links = [:]
new File("../phenomeweb/names/").eachFile { f ->
  f.splitEachLine("\t") { line ->
    def id = line[0]
    def name = StringEscapeUtils.escapeHtml(line[1])
    names[id] = name
    if (id) {
      if (id.startsWith("OMIM")) {
	def p = id.substring(5)
	links[id] = "http://omim.org/entry/$p"
      }
      if (id.startsWith("FB")) {
	links[id] = "http://flybase.org/reports/"+id+".html"
      }
      if (id.startsWith("S0")) {
	links[id] = "http://www.yeastgenome.org/cgi-bin/locus.fpl?dbid=$id"
      }
      if (id.startsWith("ZDB")) {
	links[id] = "http://zfin.org/action/quicksearch?query=$id"
      }
      if (id.startsWith("WB")) {
	links[id] = "http://www.wormbase.org/search/all/$id"
      }
      if (id.startsWith("RGD")) {
	def p = id.substring(4)
	links[id] = "http://rgd.mcw.edu/rgdweb/report/gene/main.html?id=$p"
      }
      if (id.startsWith("DOID")) {
	links[id] = "https://www.ebi.ac.uk/ontology-lookup/?termId=$id"
      }
      if (id.startsWith("ORPHANET")) {
	def p = id.substring(9)
	links[id] = "http://www.orpha.net/consor/cgi-bin/Disease_Search_Simple.php?lng=EN&Disease_Disease_Search_diseaseType=ORPHA&Disease_Disease_Search_diseaseGroup=$p"
      }
      if (id.startsWith("MGI:")) {
	links[id] = "http://www.informatics.jax.org/searchtool/Search.do?query=$id"
      }
    }
  }
}

def mpfile = new File("../phenotypeontology/obo/mammalian_phenotype.obo")
def mid = ""
def obsolete = new TreeSet()
mpfile.eachLine { line ->
  if (line.startsWith("id:")) {
    mid = line.substring(4).trim().replaceAll(":","_")
  }
  if (line.startsWith("is_obsolete: true")) {
    obsolete.add(mid)
  }
  if (line.startsWith("alt_id:")) {
    def aid = line.substring(8).trim().replaceAll(":","_")
    obsolete.add(aid)
  }
}

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(fn))

OWLDataFactory fac = manager.getOWLDataFactory()

def id2class = [:] // maps an OBO-ID to an OWLClass
ont.getClassesInSignature(true).each {
  def a = it.toString()
  a = a.substring(a.indexOf("obo/")+4,a.length()-1)
  a = a.replaceAll("_",":")
  if (id2class[a] == null) {
    id2class[a] = it
  }
}

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasonerFactory fac1 = new ElkReasonerFactory()
OWLReasoner reasoner = fac1.createReasoner(ont)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

ServerSocket server = null

try {
  server = new ServerSocket(FRONTENDPORT)
} catch (IOException e) {
  println e
}
while (true) {
  server.accept() { client ->
    client.setSoTimeout(0)
    client.withStreams { input, output ->
      def sout = new BufferedWriter(new OutputStreamWriter(output))
      def sin = new BufferedReader(new InputStreamReader(input))
      def phenotypes = sin.readLine()
      println "Phenotypes: " + phenotypes
      if (phenotypes.startsWith("TRANSLATE ")) { // only translate, no similarity! Syntax: TRANSLATE <searchstring> <queryterm>
	phenotypes = phenotypes.split(" ")
	println "Translating " + phenotypes[2]
	def sclass = new TreeSet()
	if (id2class[phenotypes[2]]) {
	  try {
	    sclass.addAll(reasoner.getSuperClasses(id2class[phenotypes[2]], false).getFlattened())
	    sclass.addAll(reasoner.getEquivalentClasses(id2class[phenotypes[2]]).getEntities())
	  } catch (Exception E) {}
	}
	sclass = sclass.collect { it.toString() }
	sclass.retainAll { it.indexOf(phenotypes[1])>-1 }
	sclass.each { 
	  sout.print("$it ")
	}
	sout.println("")
	sout.flush()
	sout.close()
      } else {
	phenotypes = phenotypes.split(" ")
	def sclass = new TreeSet()
	phenotypes.each {
	  if (id2class[it]) {
	    try {
	      sclass.addAll(reasoner.getSuperClasses(id2class[it], false).getFlattened())
	      sclass.addAll(reasoner.getEquivalentClasses(id2class[it]).getEntities())
	    } catch (Exception E) {}
	  }
	}
	sclass = sclass.collect { it.toString() }
	sclass.retainAll { it.indexOf("MP")>-1 }
	def requestSocket = new Socket(SERVERADDRESS, SERVERPORT)
	def r = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()))
	def w = new BufferedWriter(new OutputStreamWriter(requestSocket.getOutputStream()))
	String s = ""
	sclass.each { s+=(it + " ") }
	w.println(s)
	w.flush()
	def is = ""
	def pmap = [:]
	while ((is = r.readLine()) != null) {
	  is = is.split(" ")
	  pmap[is[0]] = new Double(is[1])
	}
	requestSocket.close()
	pmap.each { e, v ->
	  if (names[e] && links[e]) {
	    sout.println("$e\t$v\t"+names[e]+"\t"+links[e])
	  } else if (names[e]) {
	    sout.println("$e\t$v\t"+names[e]+"\t")
	  } else {
	    sout.println("$e\t$v\tname not found\t")
	  }
	}
	sout.flush()
      }
    }
    client.close()
  }
}

server.close()
reasoner.finalize()

System.exit(-1)