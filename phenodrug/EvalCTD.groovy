
def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'input', 'input file (predictions from R)', args:1, required:true
  //  t longOpt:'threads', 'number of threads', args:1
  //  k longOpt:'stepsize', 'steps before splitting jobs', arg:1
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


/* First get the list of true positives */

def tpmap = [:]
def ctdfile = new File("data/ctd/CTD_chem_disease_relations.tsv")
def allset = new TreeSet()
ctdfile.splitEachLine("\t") { line ->
  if (line[0].indexOf("ChemicalName")==-1) {
    def cid = line[1]
    def omim = line[8]
    if (omim!=null) {
      omim = "OMIM:"+omim
      cid="MESH:"+cid
      if (tpmap[omim]==null) {
	tpmap[omim] = new TreeSet()
      }
      tpmap[omim].add(cid)
      allset.add(omim)
      allset.add(cid)
    }
  }
}


/* Second, get the list of associations from our predictions */
def resultfile = new File(opt.i)
def resultmap = [:]
resultfile.splitEachLine(" ") { line ->
  def omim = line[0]
  def chem = line[1]
  def score = new Double(line[2])
  def exp = new Expando()
  exp.chem = chem
  exp.score = score
  if (resultmap[omim] == null) {
    resultmap[omim] = []
  }
  resultmap[omim] << exp
}

/* Now get the indices of the TP in the ranked list of results */
def roclist = []
resultmap.keySet().each { omim ->
  def l = resultmap[omim].sort { it.score }
  def count = 0
  def size = l.size()
  l.each { exp ->
    if (exp.chem in tpmap[omim]) {
      roclist << count/size
    }
    count += 1
  }
}

roclist = roclist.sort()
println "0\t0"
def count = 0
def size = roclist.size()
roclist.each { val ->
  println "$val\t"+(count/size)
  count += 1
}
println "1\t1"
