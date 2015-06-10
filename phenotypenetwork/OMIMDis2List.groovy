def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  o longOpt:'output', 'output file', args:1, required:true
  g longOpt:'use-gene', 'match genes, not alleles', args:0
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


def diseases = new File("omim/morbidmap")
def orthology = new File("data/HMD_OMIM.rpt")
def mousegenes = new File("data/MGI_PhenotypicAllele.rpt")

def cutoff = 0.0

def row = 0

def allele2gene = [:]
def gene2allele = [:]
def cmap = [:]
mousegenes.splitEachLine("\t") { line ->
  if (!line[0].startsWith("#")) {
    def id = line[0]
    def name = line[1]
    def gid = line[5]?.split(",")
    allele2gene[id] = gid
    if (gene2allele[gid] == null) {
      gene2allele[gid] = new TreeSet()
    }
    gene2allele[gid].add(id)
    cmap[name] = id
  }
}



def omim2mgigene = [:]
orthology.splitEachLine("\\s+") { line ->
  if (line[0].indexOf("#") == -1) {
    def mgi = line[0]
    def omim = line[3]
    omim2mgigene[omim] = mgi
  }
}

def dis2ogene = [:]
diseases.splitEachLine("\\|") { line ->
  try {
    def oid = new Integer(line[0].substring(line[0].size()-10, line[0].size()-4))
    def gid = line[2]
    if (dis2ogene[oid] == null) {
      dis2ogene[oid] = new TreeSet()
    }
    dis2ogene[oid].add(gid)
  } catch (Exception E) {}
}

def dis2mgene = [:]
dis2ogene.keySet().each { o->
  dis2mgene[o] = dis2ogene[o].collect { omim2mgigene[it] }
}

if (opt.g) { // match only genes, not alleles
  dis2mgene.keySet().each { o ->
    dis2mgene[o].each { m ->
      if (o!=null && m!=null) {
	fout.println("OMIM:$o\t$m")
      }
    }
  }
} else { // match alleles
  dis2mgene.keySet().each { o ->
    dis2mgene[o].each { m ->
      gene2allele[m]?.each { a ->
	if (o!=null && a!=null) {
	  fout.println("OMIM:$o\t$a")
	}
      }
    }
  }
}

fout.flush()
fout.close()
