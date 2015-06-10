import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.elk.owlapi.*

def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'input-file', 'DO-DO similarity file', args:1, required:true
  o longOpt:'output', 'output DIRECTORY', args:1, required:true
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


def infile = new File(opt.i)
OWLOntologyManager manager = OWLManager.createOWLOntologyManager()
OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("HumanDO.obo"))
OWLDataFactory fac = manager.getOWLDataFactory()
OWLReasonerFactory reasonerFactory = null
ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
OWLReasonerFactory fac1 = new ElkReasonerFactory()
OWLReasoner reasoner = fac1.createReasoner(ont)
reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

def dotop = ["DOID:1287", "DOID:28", "DOID:77", "DOID:2914", "DOID:16", "DOID:17", "DOID:863", "DOID:15", "DOID:1579", "DOID:0060118", "DOID:18", "DOID:14566", "DOID:150", "DOID:0014667", "DOID:630", "DOID:0080015", "DOID:225", "DOID:104", "DOID:1564", "DOID:1398", "DOID:934", "DOID:0050117"]


def doid2name = [:]
def doid2icd = [:]
def id = ""
new File("HumanDO.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
  }
  if (line.startsWith("name:")) {
    doid2name[id] = line.substring(5).trim()
  }
  if (line.startsWith("xref: ICD9CM:")) {
    doid2icd[id] = line.substring(13).trim()
  }
}

// get supercategories...
def doid2cat = [:].withDefault { new TreeSet() }
def cat2doid = [:].withDefault { new TreeSet() }

def s = [:]

def counter = 0 

def analmap = [:].withDefault { new LinkedHashSet() }

infile.splitEachLine("\t") { line ->
  def doid = line[0]?.replaceAll("http://phenomebrowser.net/smltest/","")
  def mgi = line[1]?.replaceAll("http://phenomebrowser.net/smltest/","")
  def score = new Double(line[2])
  Expando exp = new Expando()
  exp.doid = mgi
  exp.score = score
  analmap[doid].add(exp)
  def cl = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+doid.replaceAll(":","_")))
  reasoner.getSuperClasses(cl, false).getFlattened().each { sclass ->
    sclass = sclass.toString().replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll("_",":").replaceAll(">","")
    if (sclass in dotop) {
      doid2cat[doid].add(sclass)
      cat2doid[sclass].add(doid)
    }
  }
}
cat2doid.each { cat, dos ->
  println "$cat\t"+dos.size()
}

cat2doid.each { cat, doidset ->
  def fout = new PrintWriter(new BufferedWriter(new FileWriter(opt.o+"/"+cat)))
  def prank = [:].withDefault {0}
  def nrank = [:].withDefault {0}
  doidset.each { doid ->
    def expset = analmap[doid]
    expset = expset.sort { it.score }.reverse()
    expset.eachWithIndex { exp, i ->
      if (exp.doid in doidset) {
	prank[i] += 1
      } else {
	nrank[i] += 1
      }
    }
  }
  def totpos = 0
  def totneg = 0

  def maxrank = 0

  prank.each { rank, num ->
    totpos += num
    if (rank > maxrank) {
      maxrank = rank
    }
  }
  nrank.each { rank, num ->
    totneg += num
    if (rank > maxrank) {
      maxrank = rank
    }
  }
  def curpos = 0
  def curneg = 0
  for (int i = 0 ; i < maxrank ; i++) {
    def tp = prank[i]
    def tn = nrank[i]
    curpos += tp
    curneg += tn
    def tpr = curpos / totpos
    def fpr = curneg / totneg
    fout.println("$fpr\t$tpr")
  }

  fout.flush()
  fout.close()
}

def l = [:].withDefault { [] }
analmap.keySet().each { doid ->
  
  def expset = analmap[doid]
  def pos = map[omim]
  expset = expset.sort { it.score }.reverse()
  def index = 0
  expset.each { exp ->
    if (exp.mgi in pos) {
      l << index/expset.size()
    }
    index += 1
  }
}

l = l.sort()
def max = l.size()
fout.println("0\t0")
for (int i = 0 ; i < max ; i++) {
  def tp = i/max
  def fp = l[i]
  fout.println("$fp\t$tp")
}
fout.println("1\t1")


//println count
