import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.elk.owlapi.*

def jaccard(Set s1, Set s2) {
  if (s1 == null || s2 == null || s1.size() == 0 || s2.size() == 0) {
    return 0
  } else {
    s1.intersect(s2).size() / (s1 + s2).size()
  }
}

// How much of s1 is contained in s2
def coverage(Set s1, Set s2) {
  if (s1 == null || s2 == null || s1.size() == 0 || s2.size() == 0) {
    return 0
  } else {
    s1.intersect(s2).size() / s2.size()
  }
}


def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'input-file-1', 'DO definitions', args:1, required:true
  m longOpt:'omim-file', 'file with OMIM phenotypes', args:1, required:true
  o longOpt:'output', 'output file', args:1, required:true
}
def opt = cli.parse(args)
if( !opt ) {
  //  cli.usage()
  return
}
if( opt.h ) {
    cli.usage()
    return
}

def fout = new PrintWriter(new BufferedWriter(new FileWriter(opt.o)))

// find DO->OMIM mappings
def doid2omim = [:].withDefault { new TreeSet() }
def oid = ""
new File("HumanDO.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    oid = line.substring(4).trim()
  }
  if (line.startsWith("xref: OMIM")) {
    def omim = line.substring(6).trim()
    doid2omim[oid].add(omim)
  }
}

println "Reading disease phenotypes"
def doid2pheno = [:].withDefault { new TreeSet() }
new File(opt.i).splitEachLine("\t") { line ->
  def doid = line[0]
  def pheno = line[1]
  if (pheno.indexOf("HP")>-1) {
    doid2pheno[doid].add(pheno)
  }
}

println "reading omim file"
def omim2pheno = [:].withDefault { new TreeSet() }
new File(opt.m).splitEachLine("\t") { line ->
  def omim = line[0]
  def pheno = line[1]
  omim2pheno[omim].add(pheno)
}
//println omim2pheno
println "Classifying ontology"

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("monarch/monarch.owl"))

OWLDataFactory fac = manager.getOWLDataFactory()

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasonerFactory fac1 = new ElkReasonerFactory()
OWLReasoner reasoner = fac1.createReasoner(ont,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

println "Processing disease phenotypes"
def doid2phenoc = [:]
doid2pheno.each { doid, phenos ->
  def closed = new TreeSet()
  phenos.each { pheno ->
    pheno = pheno.replaceAll(":","_")
    def cl = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+pheno))
    def s = reasoner.getSuperClasses(cl, false).getFlattened()
    def t = reasoner.getEquivalentClasses(cl).getEntities()
    s.addAll(t)
    closed.addAll(s)
  }
  doid2phenoc[doid.toString()] = closed
}

println "Processing OMIM definitions"
def omim2phenoc = [:]
omim2pheno.each { omim, phenos ->
  def closed = new TreeSet()
  phenos.each { pheno ->
    pheno = pheno.replaceAll(":","_")
    def cl = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+pheno))
    def s = reasoner.getSuperClasses(cl, false).getFlattened()
    def t = reasoner.getEquivalentClasses(cl).getEntities()
    s.addAll(t)
    closed.addAll(s)
  }
  omim2phenoc[omim.toString()] = closed
}

def avgplain = []
def covplain = []
def avgclosed = []
def covclosed = []
doid2omim.each { doid, omims ->
  omims.each { omim ->
    def ps1 = omim2pheno[omim]
    def ps2 = doid2pheno[doid]
    def jac = jaccard(ps1, ps2)
    def cov = coverage(ps2, ps1)
    avgplain << jac
    covplain << cov
    fout.print("$jac\t$cov\t")
    ps1 = omim2phenoc[omim]
    ps2 = doid2phenoc[doid]
    jac = jaccard(ps1, ps2)
    cov = coverage(ps2, ps1)
    avgclosed << jac
    covclosed << cov
    fout.println("$jac\t$cov")
  }
}
fout.flush()
fout.close()

def avg1 = 0
avgplain.each { avg1+= it }
def avg2 = 0
avgclosed.each { avg2+=it }
def cov1 = 0
covplain.each { cov1 += it }
def cov2 = 0
covclosed.each { cov2 += it }
avg1 = avg1 / avgplain.size()
avg2 = avg2 / avgclosed.size()
cov1 = cov1 / covplain.size()
cov2 = cov2 / covclosed.size()
println "Average Jaccard plain: $avg1"
println "Average Jaccard closed: $avg2"
println "Average Coverage plain: $cov1"
println "Average Coverage closed: $cov2"
