import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.elk.owlapi.*


def all2hom = [:] // MGI and OMIM ids to HOM cluster ID
def hom2all = [:] // inverse
new File("HOM_MouseHumanSequence.rpt").splitEachLine("\t") { line ->
  def mgi = line[5]
  def omim = line[3]
  def hom = line[0]
  if (hom2all[hom] == null) {
    hom2all[hom] = new TreeSet()
  }
  if (mgi!=null) {
    all2hom[mgi] = hom
    hom2all[hom].add(mgi)
  }
  if (omim!=null) {
    all2hom[omim] = hom
    hom2all[hom].add(omim)
  }
}



def ofile = new File("gwas/efo.owl")

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(ofile)

OWLDataFactory fac = manager.getOWLDataFactory()

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasonerFactory fac1 = new ElkReasonerFactory()
OWLReasoner reasoner = fac1.createReasoner(ont,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

def efo2doid = [:]
OWLAnnotationProperty label = fac.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
OWLAnnotationProperty synonym = fac.getOWLAnnotationProperty(IRI.create("http://www.ebi.ac.uk/efo/alternative_term"));
OWLAnnotationProperty citation = fac.getOWLAnnotationProperty(IRI.create("http://www.ebi.ac.uk/efo/definition_citation"));
ont.getClassesInSignature().each { cl ->
  def ts = new LinkedHashSet()
  ts.add(cl)
  reasoner.getSubClasses(cl, false).getFlattened().each { ts.add(it) }
  ts.each { cl2 ->
    cl2.getAnnotations(ont, citation)?.each { annotation ->
      OWLLiteral val = (OWLLiteral) annotation.getValue()
      if (val.getLiteral().indexOf("OMIM")>-1) {
	def efo = cl.toString().replaceAll("<","").replaceAll(">","")
	if (efo2doid[efo] == null) {
	  efo2doid[efo] = new TreeSet()
	}
	efo2doid[efo].add(val.getLiteral().replaceAll("http://purl.obolibrary.org/obo/","").replaceAll("_",":"))
      }
    }
  }
}

def pmid2doid = [:]
new File("gwas/GWAS-EFO-Mappings122012.csv").splitEachLine("\t") { line ->
  def trait = line[0]
  def efo = line[2]
  def pmid = line[4]
  pmid2doid[pmid] = efo2doid[efo]
}

new File("gwascatalog.txt").splitEachLine("\t") { line ->
  def pmid = line[1]
  if (pmid2doid[pmid]!=null) {
    line[13]?.split(",").each { gene ->
      if (all2hom[gene]!=null) {
	hom2all[all2hom[gene]].each { g ->
	  if (g.indexOf("MGI:")>-1) {
	    pmid2doid[pmid].each { dis ->
	      println "$dis\t$g"
	    }
	  }
	}
      }
    }
  }
}

System.exit(0)
