import cern.jet.stat.Descriptive
import cern.colt.list.*

def kegg = new File("kegg/ko")

//def infile = new File("all/all-simgic")
//def indexfile = new File("all/phenotypes-simgic.txt")
def jaxdiseases = new File("MGI_Geno_Disease.rpt")

//def baselineout = new PrintWriter(new BufferedWriter(new FileWriter(new File("eval/baselinedisease-mouse.txt"))))
//def baselineoutother = new PrintWriter(new BufferedWriter(new FileWriter(new File("eval/baselineother-mouse.txt"))))


//def borderflow = new File("all/borderflow-filtered")

def x2disease = [:]
jaxdiseases.splitEachLine("\t") { line ->
  def gene = line[5].replaceAll(":","_").trim()
  def dis = line[6]
  if (dis.indexOf(",")==-1) {
    def disease = "OMIM_"+dis
    if (x2disease[disease]==null) {
      x2disease[disease] = new TreeSet()
    }
    x2disease[disease].add(gene)
  } else {
    dis.split(",").each { 
      disease = "OMIM_"+it
      if (x2disease[disease]==null) {
	x2disease[disease] = new TreeSet()
      }
      x2disease[disease].add(gene)
    }
  }
}

Map genMapping(File f) {
  genMapping(f, "")
}

Map genMapping(File f, String s) {
  def m = [:]
  f.splitEachLine("\t") { line ->
    def kid = line[0].substring(4).trim()
    def oid = line[1].substring(line[1].indexOf(":")+1).trim()
    m[kid]=s+oid
  }
  m
}

Map genRevMapping(File f) {
  genRevMapping(f, "")
}

Map genRevMapping(File f, String s) {
  def m = [:]
  f.splitEachLine("\t") { line ->
    def kid = line[0].substring(4).trim()
    def oid = line[1].substring(line[1].indexOf(":")+1).trim()
    m[s+oid]=kid
  }
  m
}

def mouse = genMapping(new File("kegg/mmu_mgi.list"), "MGI_")
def worm = genMapping(new File("kegg/cel_wormbase-cel.list"))
def fish = genMapping(new File("kegg/dre_zfin.list"))
def fly = genMapping(new File("kegg/dme_flybase-dme.list"))
def yeast = genMapping(new File("kegg/sce_sgd-sce.list"))
def human = genMapping(new File("kegg/hsa_omim.list"), "OMIM_")

def rmouse = genRevMapping(new File("kegg/mmu_mgi.list"), "MGI_")
def rworm = genRevMapping(new File("kegg/cel_wormbase-cel.list"))
def rfish = genRevMapping(new File("kegg/dre_zfin.list"))
def rfly = genRevMapping(new File("kegg/dme_flybase-dme.list"))
def ryeast = genRevMapping(new File("kegg/sce_sgd-sce.list"))
def rhuman = genRevMapping(new File("kegg/hsa_omim.list"), "OMIM_")

def allmap = new TreeMap()
allmap.putAll(rmouse)
allmap.putAll(rworm)
allmap.putAll(rfish)
allmap.putAll(rfly)
allmap.putAll(ryeast)
allmap.putAll(rhuman)

def f1 = new File("MGI_PhenotypicAllele.rpt")
def allele2id = [:]
def allele2gene = [:]
def gene2allele = [:]
f1.splitEachLine("\t") {
  def alleleid = it[0]
  def allelename = it[1]
  def geneid = it[5]
  allele2id[allelename] = alleleid
  allele2gene[alleleid] = geneid
  if (gene2allele[geneid]==null) {
    gene2allele[geneid] = new TreeSet()
  }
  gene2allele[geneid].add(alleleid)
}


def diseaseflag = false
def keggmap = [:]
Expando exp = new Expando()
kegg.eachLine { line ->
  line = line.trim()
  if (line.startsWith("ENTRY")) {
    diseaseflag = true
    if (exp.id!=null) {
      keggmap[exp.id] = exp
    }
    exp = new Expando()
    exp.pathway = new TreeSet()
    exp.keggdisease = new TreeSet()
    exp.id=line.substring(10,20).trim()
  }
  if (line.startsWith("DISEASE")) {
    line = line.substring(8).trim()
    diseaseflag = true
  }
  if (line.startsWith("PATHWAY")) {
    line = line.substring(8).trim()
    diseaseflag=false
  }
  if (line.startsWith("GENES")) {
    line = line.substring(8).trim()
    diseaseflag=false
  }
  if (diseaseflag && line.startsWith("H")) { // disease information
    def pw = line.substring(0,6).trim()
    exp.keggdisease.add(pw)
  }
  if (line.startsWith("ko")) { // pathway information
    def pw = line.substring(0,8).trim()
    exp.pathway.add(pw)
  }
  if (line.startsWith("MMU:")) {
    exp.mouse = []
    line = line.substring(4).trim()
    line.tokenize().each { tok ->
      if (tok.indexOf("(")>-1) {
	tok = tok.substring(0,tok.indexOf("("))
      }
      exp.mouse << mouse[tok]
    }
  }
  if (line.startsWith("CEL:")) {
    exp.worm = []
    line = line.substring(4).trim()
    line.tokenize().each { tok ->
      if (tok.indexOf("(")>-1) {
	tok = tok.substring(0,tok.indexOf("("))
      }
      exp.worm << worm[tok]
    }
  }
  if (line.startsWith("DRE:")) {
    exp.fish = []
    line = line.substring(4).trim()
    line.tokenize().each { tok ->
      if (tok.indexOf("(")>-1) {
	tok = tok.substring(0,tok.indexOf("("))
      }
      exp.fish << fish[tok]
    }
  }
  if (line.startsWith("DME:")) {
    exp.fly = []
    line = line.substring(4).trim()
    line.tokenize().each { tok ->
      if (tok.indexOf("(")>-1) {
	tok = tok.substring(0,tok.indexOf("("))
      }
      exp.fly << fly[tok]
    }
  }
  if (line.startsWith("HSA:")) {
    exp.human = []
    line = line.substring(4).trim()
    line.tokenize().each { tok ->
      if (tok.indexOf("(")>-1) {
	tok = tok.substring(0,tok.indexOf("("))
      }
      exp.human << human[tok]
    }
  }
  if (line.startsWith("SCE:")) {
    exp.yeast = []
    line = line.substring(4).trim()
    line.tokenize().each { tok ->
      if (tok.indexOf("(")>-1) {
	tok = tok.substring(0,tok.indexOf("("))
      }
      exp.yeast << yeast[tok]
    }
  }
}

def mmap = [:] 
keggmap.keySet().each { key ->
  def val = keggmap[key]
  val.human.each { 
    if (mmap[it]==null) {
      mmap[it] = new TreeSet()
    }
    if (key.length()>0)
    mmap[it].add(key)
  }
  val.mouse.each { 
    if (mmap[it]==null) {
      mmap[it] = new TreeSet()
    }
    if (key.length()>0)
    mmap[it].add(key)
  }
  val.worm.each { 
    if (mmap[it]==null) {
      mmap[it] = new TreeSet()
    }
    if (key.length()>0)
    mmap[it].add(key)
  }
  val.fly.each { 
    if (mmap[it]==null) {
      mmap[it] = new TreeSet()
    }
    if (key.length()>0)
    mmap[it].add(key)
  }
  val.yeast.each { 
    if (mmap[it]==null) {
      mmap[it] = new TreeSet()
    }
    if (key.length()>0)
    mmap[it].add(key)
  }
  val.fish.each { 
    if (mmap[it]==null) {
      mmap[it] = new TreeSet()
    }
    if (key.length()>0)
    mmap[it].add(key)
  }
}

x2disease.keySet().each { key ->
  def val = x2disease[key]
  val.each {
    def ke = mmap[it]
    ke.each { k ->
      def ex = keggmap[k]
      if (ex!=null) {
	if (ex.omimdisease == null) {
	  ex.omimdisease = new TreeSet()
	}
	ex.omimdisease.add(key)
      }
    }
  }
}

def wormout = new PrintWriter(new FileWriter("fly-worm.txt"))
def mouseout = new PrintWriter(new FileWriter("fly-mouse.txt"))
def humanout = new PrintWriter(new FileWriter("fly-human.txt"))
def fishout = new PrintWriter(new FileWriter("fly-fish.txt"))
def diseaseout = new PrintWriter(new FileWriter("fly-disease.txt"))
def yeastout = new PrintWriter(new FileWriter("fly-yeast.txt"))
keggmap.keySet().each { k ->
  def ex = keggmap[k]
  ex.fly.each { fl ->
    ex.worm.each { wormout.println(fl+"\t"+it) }
    ex.mouse.each { mouseout.println(fl+"\t"+it) }
    ex.human.each { humanout.println(fl+"\t"+it) }
    ex.fish.each { fishout.println(fl+"\t"+it) }
    ex.omimdisease.each { diseaseout.println(fl+"\t"+it) }
    ex.yeast.each { yeastout.println(fl+"\t"+it) }
  }
}

wormout.flush()
mouseout.flush()
humanout.flush()
fishout.flush()
diseaseout.flush()
yeastout.flush()
