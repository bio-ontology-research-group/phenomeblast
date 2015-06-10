def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'input-file', 'PhenomeNET input file', args:1, required:true
  d longOpt:'index-file', 'PhenomeNET index file',args:1, required:true
  p longOpt:'positive-file', 'file with true positive associations', args:1, required:true
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


def infile = new File(opt.i)
def indexfile = new File(opt.d)
def pfile = new File(opt.p)
def fout = new PrintWriter(new BufferedWriter(new FileWriter(opt.o)))


def map = [:]
pfile.splitEachLine("\t") { line ->
  def id1 = line[0].trim()
  def id2 = line[1].trim()
  if (map[id1]==null) {
    map[id1] = new TreeSet()
  }
  map[id1].add(id2)
}

def diseaselist = new TreeSet()
def allelelist = new TreeSet()
map.keySet().each {
  diseaselist.add(it)
  allelelist.addAll(map[it])
}

def s = [:]

def counter = 0 

def list = []
indexfile.splitEachLine("\t") {
  list = it
}

list = list.collect {
  def a = it
  a = a.replaceAll("<http://purl.obolibrary.org/obo/","")
  a = a.replaceAll(">","")
  a = a.replaceAll("\n","")
  a.trim()
}
def positiveindexlist = []
def row = 0
infile.splitEachLine("\t") { line ->
  def l = []
  def b = list[row]
  if (b in diseaselist) {
    for (int col = 0 ; col < line.size() ; col++) {
      def d = new Double(line[col])
      def a = list[col]
      def dis = b
      def gene = a
      if (gene in allelelist) {
	if ((dis!=null) && (gene!=null)) {
	  Expando exp = new Expando()
	  exp.gene = gene
	  exp.val = d
	  l << exp
	}
      }
    }
  }
  /* Once we know we hit a disease row here we can finish the analysis of the disease and discard from analmap */
  /* b is the disease from the row counter */

  if (b!=null && b in diseaselist) {
    println "Testing disease $b."
    def tempmap = new TreeMap<Double, Integer>()
    l.each {
      if (tempmap[it.val] == null) {
	tempmap[it.val] = 0
      }
      tempmap[it.val] += 1
    }
    l = l.sort { it.val }.reverse()
    def alleles = map[b]
    def index = 0
    def max = l.size()-1
    l.each { exp ->
      if (exp.gene in alleles) {
	positiveindexlist << (index/max)
      }
      index+=1
    }
  }
  if (row%1000==0) {
    println row
  }
  row+=1
}

positiveindexlist = positiveindexlist.sort()
def max = positiveindexlist.size()
fout.println("0\t0")
for (int i = 0 ; i < max ; i++) {
  def tp = i/max
  def fp = positiveindexlist[i]
  fout.println("$fp\t$tp")
}
fout.println("1\t1")

fout.flush()
fout.close()

//println count
