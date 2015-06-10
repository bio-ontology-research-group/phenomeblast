def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'input-file', 'miRNA-disease prediction file', args:1, required:true
  p longOpt:'positive-file', 'file with true positive associations', args:1, required:true
  m longOpt:'mapping-file', 'file with mappings between miRBase accessions and miRNA names', args:1, required:true
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


def positivefile = new File(opt.p) 
def mappingfile = new File(opt.m)
def inputfile = new File(opt.i)

def dofile = new File("eval/HumanDO.obo")
def omim2do = [:]
def do2omim = [:]
def oboid = ""
dofile.eachLine { line ->
  if (line.startsWith("id: DOID:")) {
    oboid = line.substring(9).trim()
  }
  if (line.startsWith("xref: OMIM:")) {
    def omim = line.substring(6).trim()
    omim2do[omim] = oboid
    if (do2omim[oboid] == null) {
      do2omim[oboid] = new TreeSet()
    }
    do2omim[oboid].add(omim)
  }
}

def miRACC2name = [:]
mappingfile.splitEachLine("\t") { line ->
  def acc = line[0]
  def name = line[1]
  name = name.substring(4)
  miRACC2name[acc] = name
}

def miRNA2dis = [:]
positivefile.splitEachLine("\t") { line ->
  def id = line[0]
  def omim = line[1]
  if (miRNA2dis[id] == null) {
    miRNA2dis[id] = new TreeSet()
  }
  miRNA2dis[id].add(omim)
}

def results = []
inputfile.splitEachLine(" ") { line ->
  def omim = line[0]
  def mirna = line[1]
  def val = Double.parseDouble(line[2])
  Expando exp = new Expando()
  exp.omim = omim
  exp.mirna = miRACC2name[mirna]
  exp.val = val
  if (exp.mirna!=null && omim2do[omim]!=null) {
    results << exp
  }
}

results.each { exp ->
  if (exp.val < 0.05) {
    println exp.mirna+"\tDOID:"+omim2do[exp.omim]+"\t1"
  } else {
    println exp.mirna+"\tDOID:"+omim2do[exp.omim]+"\t0"
  }
}


//results = results.sort { it.val }

/*
def max = results.size()-1
def count = 0
def countpos = 0
results.each { exp ->
  def omim = exp.omim
  def mirna = exp.mirna
  if (omim in miRNA2dis[mirna]) {
    def tp = countpos/maxpos
    def fp = count/max
    println "$tp\t$fp"
    countpos += 1
  }
  count += 1
}
*/