def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  m longOpt:'mouse', 'use mouse alleles/genes as positives', args:0
  g longOpt:'mgi', 'use MGI\'s gene-disease associations as positives', args:0
  //  o longOpt:'output-file', 'output file', args:1, required:true
}

def opt = cli.parse(args)
if( !opt ) {
  cli.usage()
  exit(-1)
}
if( opt.h ) {
    cli.usage()
    exit(0)
}


def allelefile = new File("MGI_PhenotypicAllele.rpt")
def orthologyfile = new File("HMD_HumanSequence.rpt")
def disfile = new File("MGI_Geno_Disease.rpt")

def orphafile = new File("en_product1.xml")
def orphafile2 = new File("en_product6.xml")

def orpha2geneomim = [:]
def orpha2geneuniprot = [:]
def orpha2omim = [:] // orpha diseases to OMIM diseases
def omim2orpha = [:] // orpha diseases to OMIM diseases

slurper = new XmlSlurper().parse(orphafile2)
slurper.DisorderList.Disorder.each { dis ->
  def id = dis.OrphaNumber.text()
  if (orpha2geneomim[id]==null) {
    orpha2geneomim[id] = new TreeSet()
    orpha2geneuniprot[id] = new TreeSet()
  }
  dis.GeneList.Gene.each { gene ->
    def ogene = gene.OrphaNumber.text()
    //    orpha2gene[id].add(ogene)
    gene.ExternalReferenceList.ExternalReference.each { ref ->
      def source = ref.Source.text()
      if (source == "OMIM") {
	def omim = ref.Reference.text()
	orpha2geneomim[id].add(omim)
	//	omimgenes.add(omim)
      }
      if (source == "UNIPROTKB/SWISSPROT") {
	def uniprot = ref.Reference.text()
	orpha2geneuniprot[id].add(uniprot)
      }
    }
  }
}

if (!opt.m) {
  orpha2geneomim.keySet().each { o ->
    def val = orpha2geneomim[o]
    val.each { omim ->
      println "ORPHANET:$o\tOMIM:$omim"
    }
  }
  System.exit(0)
}

def slurper = new XmlSlurper().parse(orphafile)
slurper.DiseaseList.Disease.each { dis ->
  def id = dis.Orphanum.text()
  dis.ExternalReferenceList.ExternalReference.each { ref ->
    def source = ref.Source.text()
    if (source == "OMIM") {
      if (orpha2omim[id]==null) {
	orpha2omim[id] = new TreeSet()
      }
      def omim = ref.Reference.text()
      if (omim2orpha[omim]==null) {
	omim2orpha[omim] = new TreeSet()
      }
      orpha2omim[id].add(omim)
      omim2orpha[omim].add(id)
    }
  }
}


def cmap = [:]
allelefile.splitEachLine("\t") { line ->
  if (!line[0].startsWith("#")) {
    def id = line[0]
    def name = line[1]
    cmap[name] = id
  }
}

def mgi2uniprot = [:]
def uniprot2mgi = [:]
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

def mgidismap = [:]
disfile.splitEachLine("\t") { line ->
  def omim = null
  if (line[6].indexOf(",")==-1) {
    omim = [line[6]]
  } else {
    omim = line[6].split(",").collect { it }
  }
  def mgi = line[5]
  def name = line[1]
  if (cmap[name]!=null) {
    omim.each { o ->
      if (mgidismap[o] == null) {
        mgidismap[o] = new TreeSet()
      }
      mgidismap[o].add(cmap[name])
    }
  }
}

if (opt.g) { // use MGI as true positive
  mgidismap.keySet().each { omim ->
    omim2orpha[omim]?.each { orpha ->
      mgidismap[omim].each {
	println("ORPHANET:$orpha\t$it") 
      }
    }
  }
} else { // use OrphaNET's associations
  orpha2geneuniprot.keySet().each { orpha ->
    orpha2geneuniprot[orpha].each { gene ->
      if (uniprot2mgi[gene]!=null) {
	uniprot2mgi[gene].each { m ->
	  println("ORPHANET:$orpha\t$m") 
	}
      }
    }
  }
}
