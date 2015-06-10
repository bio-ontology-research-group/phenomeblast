import groovy.sql.Sql

def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'index-file', 'PhenomeNET index file', args:1, required:true
  p longOpt:'phenomenet-file', 'PhenomeNET file', args:1, required:true
  o longOpt:'output-file', 'output file', args:1, required:true
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
def orthology = new File("HMD_HumanSequence.rpt")

def drugbankfile = new File("drugbank.xml")

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
  a = a.trim()
  a
}

def mousegenes = new File("data/MGI_PhenotypicAllele.rpt")

def gid2mim = [:]

def cutoff = 0.0

def row = 0


def drugmap = [:]
def genemap = [:]
def diseasemap = [:]
def gid2gene = [:]
def x2gene = [:] // x == PhenomeNET identifiers, gene == GKB identifiers
def did2name = [:]
def did2target = [:]
def partner2uniprot = [:]
def slurper = new XmlSlurper().parse(drugbankfile)
slurper.partners.partner.each { partner ->
  def pid = partner.@id.text()
  if (partner2uniprot[pid]==null) {
    partner2uniprot[pid] = new TreeSet()
  }
  partner."external-identifiers"."external-identifier".each { eid ->
    if (eid.resource.text()=="UniProtKB") {
      partner2uniprot[pid].add(eid.identifier.text())
    }
  }
}


slurper.drug.each { ddrug ->
  def did = ddrug."drugbank-id".text()
  def dname = ddrug."name".text()
  did2name[did] = dname
  if (did2target[did]==null) {
    did2target[did] = new TreeSet()
  }
  ddrug."targets"."target".each { target ->
    def inhibitor = false
    target."actions"."action".each { action ->
      if (action=="inhibitor") {
	inhibitor = true
      }
    }
    if (!inhibitor) {
      target = target.@partner.text()
      did2target[did].add(target)
    }
  }
}

def uniprot2mgi = [:]
orthology.splitEachLine("\t") { line ->
  def mgi = line[1]
  def mid = line[9]
  def hids = line[10]
  hids.split(",").each { hid ->
    if (hid!=null) {
      if (uniprot2mgi[hid]==null) {
	uniprot2mgi[hid] = new TreeSet()
      }
      uniprot2mgi[hid].add(mgi)
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

def targetset = new TreeSet()
def did2mgi = [:] // merge the lists: drugbank-id (drug) to MGI identifiers (targets)
did2target.keySet().each { did ->
  if (did2mgi[did]==null) {
    did2mgi[did] = new TreeSet()
  }
  def targets = did2target[did]
  targets.each { partner ->
    def uniprots = partner2uniprot[partner]
    if (uniprots!=null) {
      uniprots.each { uniprot ->
	def mgis = uniprot2mgi[uniprot]
	mgis?.each { mgi ->
	  if (mgi in list) {
	    did2mgi[did].add(mgi)
	    targetset.add(mgi)
	    gene2allele[mgi]?.each { al ->
	      if (al in list) {
		did2mgi[did].add(al)
		targetset.add(al)
	      }
	    }
	  }
	}
      }
    }
  }
}

def diseaselist = new TreeSet()
mim.splitEachLine("\t") { line ->
  if (!line[0].startsWith("#")) {
    def gid = line[1].trim()
    def omim = line[0].trim()
    def type = line[2]
    if (type=="phenotype") {
      diseaselist.add("OMIM:"+omim)
    }
    def temp = gid2gene[gid]
    if (gid2mim[gid]==null) {
      gid2mim[gid] = new TreeSet()
    }
    gid2mim[gid].add(omim)
    //    x2gene["OMIM:"+omim] = gid
  }
}

println "Diseases: "+diseaselist.size()
println "Drug targets: "+targetset.size()

def s = [:]
def counter = 0 
def analmap = [:]
infile.splitEachLine("\t") { line ->
  def b = null
  for (int col = 0 ; col < line.size() ; col++) {
    def d = new Double(line[col])
    def a = list[col+row]
    b = list[row]
    if ((a in diseaselist) || (b in diseaselist)) {
      def dis = null
      def gene = null
      if ((a in diseaselist) && (b in targetset)) {
	dis = a
	gene = b
      } else if ((a in targetset) && (b in diseaselist)) {
	dis = b
	gene = a
      }
      if ((dis!=null) && (gene!=null)) {
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

  /* Once we know we hit a disease row here we can finish the analysis of the disease and discard from analmap */
  /* b is the disease from the row counter */

  if (b!=null && b in diseaselist) {
    println "Drug-testing $b with analmap size "+analmap[b].size()
    def l = analmap[b].sort{ it.val }.reverse()
    did2mgi.keySet().each { did ->
      def mgis = did2mgi[did]
      if ((mgis!=null) && (mgis.size()>2)) {
	def index = 0
	fout.print("$b\t$did\t"+l.size()+"\t")
	l.each { exp ->
	  if (exp.gene in mgis) {
	    fout.print(index+"\t")
	  }
	  index+=1
	}
      }
      fout.println("")
    }
    analmap.remove(b)
  }
  println row++
}
fout.flush()
fout.close()

//println count