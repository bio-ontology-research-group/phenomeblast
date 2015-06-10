def kegg = new File("eval/kegg/ko")

def borderflow = new File("all/borderflow")


Map genMapping(File f) {
  genMapping(f, "")
}

Map genMapping(File f, String s) {
  def m = [:]
  f.splitEachLine("\t") { line ->
    def kid = line[0].trim()
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
    def kid = line[0].trim()
    def oid = line[1].substring(line[1].indexOf(":")+1).trim()
    m[s+oid]=kid
  }
  m
}

def mouse = genMapping(new File("eval/kegg/mmu_mgi.list"), "MGI_")
def worm = genMapping(new File("eval/kegg/cel_wormbase-cel.list"))
def fish = genMapping(new File("eval/kegg/dre_zfin.list"))
def fly = genMapping(new File("eval/kegg/dme_flybase-dme.list"))
def yeast = genMapping(new File("eval/kegg/sce_sgd-sce.list"))
def human = genMapping(new File("eval/kegg/hsa_omim.list"), "OMIM_")

def rmouse = genRevMapping(new File("eval/kegg/mmu_mgi.list"), "MGI_")
def rworm = genRevMapping(new File("eval/kegg/cel_wormbase-cel.list"))
def rfish = genRevMapping(new File("eval/kegg/dre_zfin.list"))
def rfly = genRevMapping(new File("eval/kegg/dme_flybase-dme.list"))
def ryeast = genRevMapping(new File("eval/kegg/sce_sgd-sce.list"))
def rhuman = genRevMapping(new File("eval/kegg/hsa_omim.list"), "OMIM_")

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
  def alleleid = it[0]?.replaceAll(":","_")?.trim()
  def allelename = it[1]?.replaceAll(":","_")?.trim()
  def geneid = it[5]?.replaceAll(":","_")?.trim()
  allele2id[allelename] = alleleid
  if ((geneid!=null) && (geneid.size()>2)) {
    allele2gene[alleleid] = geneid
  }
  if (gene2allele[geneid]==null) {
    gene2allele[geneid] = new TreeSet()
  }
  gene2allele[geneid].add(alleleid)
}
//allele2gene=[:]
def nodes = new TreeMap()
borderflow.splitEachLine("\t") { line ->
  if ((line[0].indexOf('#')>-1) && (line[1].indexOf('#')>-1)) {
    def first = line[0].substring(line[0].indexOf('#')+1,line[0].indexOf('>'))
    def second = line[1].substring(line[1].indexOf('#')+1,line[1].indexOf('>'))
    def val = new Double(line[2])
    def a1 = allmap[first]
    if (a1==null) {
      a1 = first
    }
    if (allele2gene[a1]!=null) {
      a1 = allele2gene[a1]
      if (allmap[a1]!=null) {
	a1 = allmap[a1]
      }
    }
    def a2 = allmap[second]
    if (a2==null) {
      a2 = second
    }
    if (allele2gene[a2]!=null) {
      a2 = allele2gene[a2]
      if (allmap[a2]!=null) {
	a2 = allmap[a2]
      }
    }
    if ((a1.indexOf(':')>-1 || a1.indexOf("OMIM")>-1) && (a2.indexOf(':')>-1 || a2.indexOf("OMIM")>-1)) {
      println a1+"\t"+a2+"\t"+val
    }
  }
}
