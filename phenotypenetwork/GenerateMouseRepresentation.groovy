import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.elk.owlapi.*


def fn = args[0] // ontology filename

def fout = new PrintWriter(new BufferedWriter(new FileWriter(new File(args[1]))))

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

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasonerFactory fac1 = new ElkReasonerFactory()
OWLReasoner reasoner = fac1.createReasoner(ont,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

ont.getClassesInSignature().each { cl ->
  def clst = cl.toString()
  if ((clst.indexOf("ZDB")>0) || // fish
      (clst.indexOf("MGI")>0) || // mouse
      (clst.indexOf("OMIM")>0) || // human and disease
      (clst.indexOf("DECIPHER")>0) || // human disease
      (clst.indexOf("S000")>0) || // yeast
      (clst.indexOf("WBGene")>0) || // worm-alleles
      (clst.indexOf("WBVar")>0) || // worm-alleles
      (clst.indexOf("WBRNAi")>0) || // worm-alleles
      (clst.indexOf("ANIKA")>0) || // Anika's diseases
      (clst.indexOf("ORPHA")>0) || // OrphaNet
      (clst.indexOf("FBgn")>0) || // Fly genes
      (clst.indexOf("RGD")>0) || // Rat id
      (clst.indexOf("SIDER")>0) || // SIDER id
      (clst.indexOf("DBS")>0) || // Dictybase id
      (clst.indexOf("FBal")>0)) // fly alleles
  { 
    def s = reasoner.getSuperClasses(cl, false).getFlattened()
    def t = reasoner.getEquivalentClasses(cl).getEntities()
    s.addAll(t)
    
    fout.print(clst+"\t")
    s.each {
      def clname = it.toString()
      def onn = clname.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","")
      if (clname.indexOf("MP_")>-1 && (! (onn in obsolete))) {
	fout.print(it.toString()+"\t")
      }
    }
    fout.println("")
  }
}

fout.flush()
fout.close()
reasoner.finalize()


