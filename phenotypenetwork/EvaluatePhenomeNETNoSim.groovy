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
  def id1 = line[0]
  def id2 = line[1]
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
def analmap = [:]
def row = 0
infile.splitEachLine("\t") { line ->
  def b = null
  for (int col = 0 ; col < line.size() ; col++) {
    def d = new Double(line[col])
    def a = list[col]
    b = list[row]
    if (a in diseaselist || b in diseaselist) {
      def dis = null
      def gene = null
      if ((a in diseaselist) && (b in allelelist)) {
	dis = a
	gene = b
      } else if ((a in allelelist) && (b in diseaselist)) {
	dis = b
	gene = a
      }
      if ((dis!=null) && (gene!=null)) {
	if (b!=null && b in diseaselist) { // added: only do this when we finish anyway
	  if (analmap[dis]==null) {
	    analmap[dis] = []
	  }
	  Expando exp = new Expando()
	  exp.gene = gene
	  exp.val = d
	  analmap[dis] << exp
	}
      }
    }
  }
  /* Once we know we hit a disease row here we can finish the analysis of the disease and discard from analmap */
  /* b is the disease from the row counter */

  if (b!=null && b in diseaselist) {
    println "Testing disease $b."
    def l = analmap[b].sort { it.val }
    def alleles = map[b]
    def index = 0
    //    fout.print("$b\t"+l.size()+"\t")
    def max = l.size()-1
    l.each { exp ->
      if (exp.gene in alleles) {
	positiveindexlist << (1-index/max)
      }
      index+=1
    }
    analmap.remove(b)
  }
  if (row%100==0) {
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
