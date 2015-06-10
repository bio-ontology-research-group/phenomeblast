import cern.jet.stat.Descriptive
import cern.colt.list.*

def kegg = new File("eval/kegg/ko")

def infile = new File("all/all-simgic")
def indexfile = new File("all/phenotypes-simgic.txt")
def morbidmap = new File("eval/morbidmap")
def jaxdiseases = new File("eval/MGI_Geno_Disease.rpt")

def baselineout = new PrintWriter(new BufferedWriter(new FileWriter(new File("eval/baselinedisease-mouse.txt"))))
def baselineoutother = new PrintWriter(new BufferedWriter(new FileWriter(new File("eval/baselineother-mouse.txt"))))

def orthoout = new PrintWriter(new BufferedWriter(new FileWriter(new File("eval/ortholog-mouse.txt"))))
def pwout = new PrintWriter(new BufferedWriter(new FileWriter(new File("eval/pathway-mouse.txt"))))
def diseaseout = new PrintWriter(new BufferedWriter(new FileWriter(new File("eval/disease-mouse.txt"))))
def disease2out = new PrintWriter(new BufferedWriter(new FileWriter(new File("eval/disease-kegg-mouse.txt"))))

//def borderflow = new File("all/borderflow-filtered")

def x2disease = [:]
morbidmap.eachLine { line ->
  def f1 = line.substring(0,line.indexOf("|"))
  def m1 = (f1 =~ /\s[0-9][0-9][0-9][0-9][0-9][0-9]\s/)
  def f2 = line.substring(line.indexOf('|'))
  def m2 = (f2 =~ /\|[0-9][0-9][0-9][0-9][0-9][0-9]\|/)
  if (m1 && m2) {
    def gene = "OMIM_"+m2[0].substring(1,m2[0].length()-1)
    def disease = "OMIM_"+(m1[0].trim())
    if (x2disease[disease]==null) {
      x2disease[disease] = new TreeSet()
    }
    x2disease[disease].add(gene)
  }
}
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

def nodes = new TreeMap()

def cutoff = 0.0
def row = 0
def list = []
indexfile.splitEachLine("\t") {
  list = it
}

String filter (String s) {
  if (s==null) {
    s = new String()
  } else {
    if (s.indexOf('#')>-1) {
      s = s.substring(s.indexOf('#')+1,s.length()-1)
    } 
  }
  s
}

def bldisease = []
infile.splitEachLine("\t") { line ->
  def second = list[row]
  second = filter(second)
  if (allele2gene[second]!=null) {
    second = allele2gene[second]
  }
  def a2 = mmap[second]
  if (a2!=null || second.startsWith("OMIM")) {
    DoubleArrayList dal = new DoubleArrayList()
    def disease = []
    for (int col = 0 ; col < line.size() ; col++) {
      def val = new Double(line[col])
      def first = list[col+row]
      first = filter(first)
      if (allele2gene[first]!=null) {
	first = allele2gene[first]
      }
      def a1 = mmap[first]
      if (a1!=null || first.startsWith("OMIM")) {
	if (a1!=null || a2!=null) {
	  if (((first.indexOf("MGI")>-1) && (second.indexOf("OMIM")>-1))|| ((first.indexOf("OMIM")>-1) && (first.indexOf("MGI")>-1))) {
	  dal.add(val)
	  def dis = null
	  def kid = null
	  if (a1==null) {
	    dis = first
	    kid = a2
	  } else {
	    dis = second
	    kid = a1
	  }
	  if (x2disease[dis]!=null) {
	    def genes = x2disease[dis]
	    genes = genes.collect { mmap[it] }.flatten()
	    if (kid.intersect(genes).size()>0) {
	      disease << val
	    }
	  }
	  }
	}
      }
    } // for
    DoubleArrayList dal2 = new DoubleArrayList()
    IntArrayList ial = new IntArrayList()
    dal.sort()
    Descriptive.frequencies(dal, dal2, ial)
    def diseaserank = []
    disease.each {
      def p = Descriptive.quantileInverse(dal2,it)
      diseaserank << p
    }
    diseaserank.each {diseaseout.print(it+"\t")}
    diseaseout.println("")
  }
  row+=1
}
diseaseout.close()

row = 0
infile = new File("all/all-simgic")
infile.splitEachLine("\t") { line ->
  def second = list[row]
  second = filter(second)
  if (allele2gene[second]!=null) {
    second = allele2gene[second]
  }
  def a2 = mmap[second]
  if (a2!=null) {
    DoubleArrayList dal = new DoubleArrayList()
    def orthologs = []
    def pathways = []
    for (int col = 0 ; col < line.size() ; col++) {
      def val = new Double(line[col])
      def first = list[col+row]
      first = filter(first)
      if (allele2gene[first]!=null) {
	first = allele2gene[first]
      }
      def a1 = mmap[first]
      
      if (((first.indexOf("MGI")>-1) && (second.indexOf("MGI")>-1))) {
      if (a1!=null) {
	dal.add(val)
	if (a1.intersect(a2).size()!=0) {
	  if (first!=second) {
	    orthologs << val
	  }
	}
	def path1 = new TreeSet()
	def path2 = new TreeSet()
	a1.each {
	  keggmap[it].pathway.each { path1.add(it) }
	}
	a2.each {
	  keggmap[it].pathway.each { path2.add(it) }
	}
	if (path1.intersect(path2).size()!=0) {
	  if ((first.indexOf("MGI")>-1 || first.indexOf("OMIM")>-1) && (second.indexOf("MGI")>-1 || second.indexOf("OMIM"))) {
	    if (first!=second) {
	      pathways << val
	    }
	  }
	}
      }
      }
    } // for
    DoubleArrayList dal2 = new DoubleArrayList()
    IntArrayList ial = new IntArrayList()
    dal.sort()
    Descriptive.frequencies(dal, dal2, ial)
    def pwrank = []
    pathways.each {
      def p = Descriptive.quantileInverse(dal2,it)
      pwrank << p
    }
    def orthrank = []
    orthologs.each {
      def p = Descriptive.quantileInverse(dal2,it)
      orthrank << p
    }
    pwrank.each {pwout.print(it+"\t")}
    pwout.println("")
    orthrank.each {orthoout.print(it+"\t")}
    orthoout.println("")
  }
  row+=1
}

row = 0
infile = new File("all/all-simgic")
infile.splitEachLine("\t") { line ->
  def second = list[row]
  second = filter(second)
  if (allele2gene[second]!=null) {
    second = allele2gene[second]
  }
  def a2 = mmap[second]
  if (a2!=null) {
    DoubleArrayList dal = new DoubleArrayList()
    def keggdisease = []
    for (int col = 0 ; col < line.size() ; col++) {
      def val = new Double(line[col])
      def first = list[col+row]
      first = filter(first)
      if (allele2gene[first]!=null) {
	first = allele2gene[first]
      }
      def a1 = mmap[first]
      if (((first.indexOf("MGI")>-1) && (second.indexOf("MGI")>-1))) {
      
      if (a1!=null) {
	dal.add(val)
	def path1 = new TreeSet()
	def path2 = new TreeSet()
	a1.each {
	  keggmap[it].keggdisease.each { path1.add(it) }
	}
	a2.each {
	  keggmap[it].keggdisease.each { path2.add(it) }
	}
	if (path1.intersect(path2).size()!=0) {
	  keggdisease << val
	}
      }
      }
    } // for
    DoubleArrayList dal2 = new DoubleArrayList()
    IntArrayList ial = new IntArrayList()
    dal.sort()
    Descriptive.frequencies(dal, dal2, ial)
    def pwrank = []
    keggdisease.each {
      def p = Descriptive.quantileInverse(dal2,it)
      pwrank << p
    }
    pwrank.each {disease2out.print(it+"\t")}
    pwout.println("")
  }
  row+=1
}
