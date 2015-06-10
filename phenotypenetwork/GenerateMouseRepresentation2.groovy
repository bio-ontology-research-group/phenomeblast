import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.elk.owlapi.*


def minphenotypes = 1 // minimum number of directly annotated phenotypes
def mininfphenotypes = 5 // minimum number of inferred phenotypes

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
OWLReasoner reasoner = fac1.createReasoner(ont, config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)


def id2pheno = [:]
def generateTheRest = { infile ->
  infile.splitEachLine("\t") { line ->
    def id = line[0]
    def pheno = line[1]
    if (pheno) {
      if (id2pheno[id] == null) {
	id2pheno[id] = new TreeSet()
      }
      id2pheno[id].add(pheno)
    }
  }
}
println "Generating mouse phenotype data"
generateTheRest(new File("phenotypes/mousephenotypes.txt"))
println "Generating human phenotype data"
generateTheRest(new File("phenotypes/omimphenotypes.txt"))
generateTheRest(new File("phenotypes/dicty-phenotypes.txt"))
generateTheRest(new File("phenotypes/wormphenotypes.txt"))
generateTheRest(new File("phenotypes/ratphenotypes.txt"))
generateTheRest(new File("phenotypes/mirnaphenotypes.txt"))
generateTheRest(new File("phenotypes/textminedphenotypes.txt"))
generateTheRest(new File("phenotypes/stitchphenotypes.txt"))
generateTheRest(new File("phenotypes/cmapphenotypes.txt"))

def id2super = [:]
id2pheno.values().flatten().each {
  if (id2class[it]) {
    try {
      id2super[it] = reasoner.getSuperClasses(id2class[it], false).getFlattened()
    } catch (Exception E) {}
  }
}

id2pheno.each { geno, pheno ->
  def supset = new TreeSet()
  if (pheno.size()>=minphenotypes) {
    pheno.each { p ->
      def sup = id2super[p]
      if (sup) {
	sup.each { s ->
	  def str = s.toString()
	  if (str.indexOf("MP_")>-1) {
	    supset.add(str)
	  }
	}
      }
    }
    if (supset.size()>0 && geno && geno.length()>0 && (supset.size()+pheno.size()>=mininfphenotypes)) {
      fout.print("$geno\t")
      supset.each { fout.print("$it\t") }
      fout.println("")
    }
  }
}

ont.getClassesInSignature().each { cl ->
  def clst = cl.toString()
  if ((clst.indexOf("ZDB")>0) || // fish
      (clst.indexOf("S000")>0) || // yeast
      (clst.indexOf("ANIKA")>0) || // Anika's diseases
      (clst.indexOf("FBgn")>0) || // Fly genes
      (clst.indexOf("FBal")>0)) // fly alleles
  { 
    try {
      def s = reasoner.getSuperClasses(cl, false)?.getFlattened()
      def t = reasoner.getEquivalentClasses(cl).getEntities()
      s.addAll(t)
      def name  = clst.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":")
      if (s && s.size()>=mininfphenotypes) {
	fout.print(name+"\t")
	s.each {
	  def clname = it.toString()
	  def onn = clname.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","")
	  if (clname.indexOf("MP_")>-1 && (! (onn in obsolete))) {
	    fout.print(it.toString()+"\t")
	  }
	}
	fout.println("")
      }
    } catch (Exception E) {
      println "Error at $clst"
    }
  }
}


fout.flush()
fout.close()
reasoner.finalize()

System.exit(-1)