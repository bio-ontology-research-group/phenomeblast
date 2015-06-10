import groovy.sql.Sql


def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'index-file', 'PhenomeNET index file', args:1, required:true
  p longOpt:'phenomenet-file', 'PhenomeNET file', args:1, required:true
  o longOpt:'output-file', 'output file', args:1, required:true
  m longOpt:'mouse', 'use mouse miRNAs for prediction (default is human)', args:0
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


def mim = new File("data/mim2gene")
def mousemapfile = new File("data/MGI_Coordinate.rpt")
def mousegenes = new File("data/MGI_PhenotypicAllele.rpt")

def humanfile = new File("data/human_predictions_S_C_aug2010.txt")
def mousefile = new File("data/mouse_predictions_S_C_aug2010.txt")

def infile = new File(opt.p)
def indexfile = new File(opt.i)
def fout = new PrintWriter(new BufferedWriter(new FileWriter(opt.o)))


def list = []
indexfile.splitEachLine("\t") {
  list = it
}

list = list.collect { a ->
  a = a.replaceAll("<http://purl.obolibrary.org/obo/","")
  a = a.replaceAll(">","")
  a = a.replaceAll("\n","")
  a.trim()
}

def gid2mim = [:]

def row = 0

def diseaselist = new TreeSet()
mim.splitEachLine("\t") { line ->
  if (!line[0].startsWith("#")) {
    def gid = line[1].trim()
    def omim = line[0].trim()
    def type = line[2]
    if (type=="phenotype") {
      diseaselist.add("OMIM:"+omim)
    }
    //    def temp = gid2gene[gid]
    if (gid2mim[gid]==null) {
      gid2mim[gid] = new TreeSet()
    }
    gid2mim[gid].add("OMIM:"+omim)
  }
}

def mirna2mim = [:]
humanfile.splitEachLine("\t") { line ->
  if (!line[0].startsWith("#")) {
    def mirna = line[0].trim()
    def gid = line[2].trim()
    if (mirna2mim[mirna]==null) {
      mirna2mim[mirna] = new TreeSet()
    }
    if (gid2mim[gid]!=null) {
      mirna2mim[mirna].addAll(gid2mim[gid])
    }
  }
}

def gene2allele = [:]
mousegenes.splitEachLine("\t") {
  if (!it[0].startsWith("#")) {
    def alid = it[0]
    def geneid = it[5]
    if (gene2allele[geneid]==null) {
      gene2allele[geneid] = new TreeSet()
    }
    gene2allele[geneid].add(alid)
  }
}

def gid2mgi = [:]
mousemapfile.splitEachLine("\t") { line ->
  if (!line[0].startsWith("1. MGI")) {
    def mgi = line[0]
    def gid = line[10]
    if (gid!=null) {
      if (gid2mgi[gid]==null) {
	gid2mgi[gid] = new TreeSet()
      }
      gid2mgi[gid].add(mgi)
      if (gene2allele[mgi]!=null) {
	gid2mgi[gid].addAll(gene2allele[mgi])
      }
    }
  }
}

def mirna2mgi = [:]
mousefile.splitEachLine("\t") { line ->
  if (!line[0].startsWith("#")) {
    def mirna = line[0].trim()
    def gid = line[2].trim()
    if (mirna2mgi[mirna]==null) {
      mirna2mgi[mirna] = new TreeSet()
    }
    if (gid2mgi[gid]!=null) {
      mirna2mgi[mirna].addAll(gid2mgi[gid])
    }
  }
}

def mousenodes = new TreeSet()
def humannodes = new TreeSet()
mirna2mgi.values().each { mousenodes.addAll(it) }
mirna2mim.values().each { humannodes.addAll(it) }

humannodes = humannodes.intersect(list)
mousenodes = mousenodes.intersect(list)
diseaselist = diseaselist.intersect(list)

println "Diseases: "+diseaselist.size()
println "Human entries: "+humannodes.size()
println "Mouse entries: "+mousenodes.size()
println "Human miRNAs: "+mirna2mim.keySet().size()
println "Mouse miRNAs: "+mirna2mgi.keySet().size()

def tempcounter = 0
def analmaphuman = [:]
def analmapmouse = [:]
if (opt.m) {
  humannodes = mousenodes
  mirna2mim = mirna2mgi
}
infile.splitEachLine("\t") { line ->
  def b = null
  b = list[row]
  for (int col = 0 ; col < line.size() ; col++) {
    def d = new Double(line[col])
    def a = list[col+row]
    if ((a in diseaselist) || (b in diseaselist)) {
      def dis = null
      def gene = null
      if ((a in diseaselist) && (b in humannodes)) {
	dis = a
	gene = b
      } else if ((a in humannodes) && (b in diseaselist)) {
	dis = b
	gene = a
      }
      if ((dis!=null) && (gene!=null)) {
	if (analmaphuman[dis]==null) {
	  analmaphuman[dis] = []
	}
	Expando exp = new Expando()
	exp.gene = gene
	exp.val = d
	analmaphuman[dis] << exp
      }
    }
  }

  /* Once we know we hit a disease row here we can finish the analysis of the disease and discard from analmap */
  /* b is the disease from the row counter */

  if (b!=null && b in diseaselist) {
    print "Testing microRNAs for disease $b. "
    def l = analmaphuman[b].sort{ it.val }.reverse()
    println "Size of analmap: "+l.size()
    mirna2mim.keySet().each { rna ->
      def gids = mirna2mim[rna]
      if ((gids!=null) && (gids.size()>2)) {
	def index = 0
	fout.print("$b\t$rna\t"+l.size()+"\t")
	l.each { exp ->
	  if (exp.gene in gids) {
	    fout.print(index+"\t")
	  }
	  index+=1
	}
      }
      fout.println("")
    }
    analmaphuman.remove(b)
  }
  println row++
}

fout.flush()
fout.close()
