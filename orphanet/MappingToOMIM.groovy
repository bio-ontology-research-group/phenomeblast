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


def orphafile = new File("en_product1.xml")
def orphafile2 = new File("en_product6.xml")
def orthologyfile = new File("HMD_HumanSequence.rpt")
def disfile = new File("MGI_Geno_Disease.rpt")
def allelefile = new File("MGI_PhenotypicAllele.rpt")

def uniprot2mgi = [:] // Human UniProt to MGI
def mgi2uniprot = [:] // MGI to Human UniProt
def orpha2omim = [:] // orpha diseases to OMIM diseases
def orpha2gene = [:] // orpha diseases to orpha genes
def orpha2geneomim = [:] // orpha diseases to OMIM genes
def orpha2geneuniprot = [:] // orpha diseases to UniProt genes
def mgi2omim = [:] // mgi gene to OMIM disease
def omim2mgi = [:] // OMIM disease to MGI gene
def orpha2mgiORPHA = [:] // computer orpha disease to MGI via MGI/UniProt equiv
def orpha2mgiOMIM = [:] // computer orpha disease to MGI via Orpha/OMIM equiv
def allele2gene = [:] // MGI allele id to MGI gene id
def gene2allele = [:] // MGI allele id to MGI gene id

def diseaselist = new TreeSet()
def omimgenes = new TreeSet()

def slurper = new XmlSlurper().parse(orphafile)
slurper.DiseaseList.Disease.each { dis ->
  def id = dis.Orphanum.text()
  diseaselist.add("ORPHANET:"+id)
  dis.ExternalReferenceList.ExternalReference.each { ref ->
    def source = ref.Source.text()
    if (source == "OMIM") {
      if (orpha2omim[id]==null) {
	orpha2omim[id] = new TreeSet()
      }
      def omim = ref.Reference.text()
      orpha2omim[id].add(omim)
    }
  }
}

slurper = new XmlSlurper().parse(orphafile2)
slurper.DiseaseList.Disease.each { dis ->
  def id = dis.Orphanum.text()
  if (orpha2geneomim[id]==null) {
    orpha2geneomim[id] = new TreeSet()
    orpha2geneuniprot[id] = new TreeSet()
    orpha2gene[id] = new TreeSet()
  }
  dis.GeneList.Gene.each { gene ->
    def ogene = gene.Orphanum.text()
    orpha2gene[id].add(ogene)
    gene.ExternalReferenceList.ExternalReference.each { ref ->
      def source = ref.Source.text()
      if (source == "OMIM") {
	def omim = ref.Reference.text()
	orpha2geneomim[id].add(omim)
	omimgenes.add(omim)
      }
      if (source == "UNIPROTKB/SWISSPROT") {
	def uniprot = ref.Reference.text()
	orpha2geneuniprot[id].add(uniprot)
      }
    }
  }
}

allelefile.splitEachLine("\t") { line ->
  if (line[0].indexOf("#")==-1) {
    def alid = line[0]
    def mid = line[5]
    if (alid!=null && mid!=null && mid.length()>0) {
      allele2gene[alid] = mid
    }
    if (gene2allele[mid]==null) {
      gene2allele[mid] = new TreeSet()
    }
    gene2allele[mid].add(alid)
  }
}

orthologyfile.splitEachLine("\t") { line ->
  def mgi = line[1]
  def uniprot = line[10].split(",")
  if (uniprot.size()>0 && uniprot[0].length()>0) {
    mgi2uniprot[mgi] = uniprot
  }
  uniprot.each {
    if (uniprot2mgi[it] == null) {
      uniprot2mgi[it] = new TreeSet()
    }
    uniprot2mgi[it].add(mgi)
  }
}

disfile.splitEachLine("\t") { line ->
  def mgi = line[5]
  def omim = line[6]
  mgi.split(",").each { m ->
    omim.split(",").each { o ->
      if (mgi2omim[m] == null) {
	mgi2omim[m] = new TreeSet()
      }
      if (omim2mgi[o] == null) {
	omim2mgi[o] = new TreeSet()
      }
      mgi2omim[m].add(o)
      omim2mgi[o].add(m)
    }
  }
}

allele2gene.keySet().each { al ->
  def gene = allele2gene[al]
  if (mgi2omim[gene]!=null) {
    mgi2omim[al] = mgi2omim[gene]
  }
}
def tempmap = [:]
omim2mgi.keySet().each { omim ->
  tempmap[omim] = new TreeSet(omim2mgi[omim])
  omim2mgi[omim].each { mgi -> 
    if (gene2allele[mgi]!=null) {
      tempmap[omim].addAll(gene2allele[mgi])
    }
  }
}
omim2mgi = tempmap

orpha2geneuniprot.keySet().each { o ->
  if (orpha2mgiORPHA[o] == null ) {
    orpha2mgiORPHA[o] = new TreeSet()
  }
  def uniprots = orpha2geneuniprot[o]
  uniprots.each { u ->
    def mgis = uniprot2mgi[u]
    mgis.each { m ->
      orpha2mgiORPHA[o].add(m)
      if (gene2allele[m]!=null) {
	orpha2mgiORPHA[o].addAll(gene2allele[m])
      }
    }
  }
}

orpha2omim.keySet().each { o ->
  if (orpha2mgiOMIM[o] == null ) {
    orpha2mgiOMIM[o] = new TreeSet()
  }
  def omims = orpha2omim[o]
  omims?.each { mim ->
    omim2mgi[mim]?.each { mgi ->
      orpha2mgiOMIM[o].add(mgi)
      if (gene2allele[mgi]!=null) {
	orpha2mgiOMIM[o].addAll(gene2allele[mgi])
      }
    }
  }
}

//def orpha2mgiORPHA = [:] // computer orpha disease to MGI via MGI/UniProt equiv
//def orpha2mgiOMIM = [:] // computer orpha disease to MGI via Orpha/OMIM equiv


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



def omimdiseases = omim2mgi.keySet()
def mousegenes = mgi2omim.keySet()


def analmap = [:]
def analmaphuman = [:]
def row = 0
infile.splitEachLine("\t") { line ->
  def b = null
  b = list[row]
  for (int col = 0 ; col < line.size() ; col++) {
    def d = new Double(line[col])
    def a = list[col+row]

    if ((a in diseaselist) || (b in diseaselist)) {
      def dis = null
      def gene = null
      if ((a in diseaselist) && (b in mousegenes)) {
	dis = a
	gene = b
      } else if ((a in mousegenes) && (b in diseaselist)) {
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
      dis = null
      gene = null
      if ((a in diseaselist) && (b in omimgenes)) {
	dis = a
	gene = b
      } else if ((a in omimgenes) && (b in diseaselist)) {
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
    print "Finishing ANALysis for disease $b. "
    def l = analmap[b].sort{ it.val }.reverse()
    println "Size of analmap (1): "+l.size()
    def oid = b.substring(9)
    def index = 0
    l.each { exp ->
      if (exp.gene in orpha2mgiORPHA[oid]) {
	fout.println(b+"\t"+l.size()+"\t"+index+"\tORPHA")
	fout.flush()
      }
      if (exp.gene in orpha2mgiOMIM[oid]) {
	fout.println(b+"\t"+l.size()+"\t"+index+"\tOMIM")
	fout.flush()
      }
      index+=1
    }
    analmap.remove(b)
  }
  println row++
}

fout.flush()
fout.close()
