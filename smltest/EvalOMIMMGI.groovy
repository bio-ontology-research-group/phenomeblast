def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'input-file', 'DO-MGI input file', args:1, required:true
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
def pfile = new File(opt.p)
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

def analmap = [:].withDefault { new LinkedHashSet() }

infile.splitEachLine("\t") { line ->
  def doid = line[0]
  def mgi = line[1]?.replaceAll("http://phenomebrowser.net/smltest/","")
  if (line[2]) {
    def score = new Double(line[2])
    Expando exp = new Expando()
    exp.mgi = mgi
    exp.score = score
    //    doid2omim[doid]?.each { omim ->
    analmap[doid].add(exp)
    //    }
  }
}


def l = []
analmap.each { omim, expset ->
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


fout.flush()
fout.close()

//println count
