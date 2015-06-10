import groovy.sql.Sql


def mim = new File("data/mim2gene")
def orthology = new File("data/HMD_Human4.rpt")
def drugs = new File("data/drugs.tsv")
def genes = new File("data/genes.tsv")
def diseases = new File("data/diseases.tsv")
def relations = new File("data/relationships.tsv")

def infile = new File(args[0])
def indexfile = new File(args[1])
def fout = new PrintWriter(new BufferedWriter(new FileWriter(args[2])))


def list = []
indexfile.splitEachLine("\t") {
  list = it
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

drugs.splitEachLine("\t") { line ->
  if (!line[0].startsWith("PharmGKB")) {
    def id = line[0]
    def exp = new Expando()
    exp.name = line[1]
    exp.genes = new TreeSet()
    drugmap[id]=exp
  }
}
genes.splitEachLine("\t") { line ->
  if (!line[0].startsWith("PharmGKB")) {
    def id = line[0]
    def exp = new Expando()
    if (gid2gene[line[1]]==null) {
      gid2gene[line[1]] = new TreeSet()
    }
    gid2gene[line[1]].add(id)
    exp.entrez = line[1]
    exp.ensembl = line[2]
    exp.uniprot = line[3]
    exp.name = line[4]
    exp.symbol = line[5]
    genemap[id]=exp
  }  
}
diseases.splitEachLine("\t") {line ->
  if (!line[0].startsWith("PharmGKB")) {
    def id = line[0]
    def exp = new Expando()
    exp.name = line[1]
    exp.mappings = []
    line[4]?.split(",").each { exp.mappings << it }
    diseasemap[id]=exp
  }
}

relations.splitEachLine("\t") {line ->
  if (!line[0].startsWith("Entity1")) {
    def id1 = line[0]
    def id2 = line[2]
    def nid1=id1.substring(id1.indexOf(':')+1)
    def nid2=id2.substring(id2.indexOf(':')+1)
    if (id1.startsWith("Gene") && id2.startsWith("Drug")) {
      def e1 = genemap[nid1]
      def e2 = drugmap[nid2]
      if (e1.drugs==null) {
	e1.drugs = new TreeSet()
      }
      e1.drugs.add(nid2)
      if (e2.genes == null) {
	e2.genes = new TreeSet()
      }
      e2.genes.add(nid1)
    }
  }
}

orthology.eachLine { line ->
  def tok = line.tokenize()
  if (tok[0]?.startsWith("MGI:")) {
    def id = tok[0]
    def mgid = tok[1]
    def hgid = tok[-4]
    def temp = gid2gene[hgid]
    x2gene[id] = temp
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
    x2gene["OMIM:"+omim] = temp
  }
}

mousegenes.splitEachLine("\t") {
  if (!it[0].startsWith("#")) {
    def alid = it[0]
    def geneid = it[5]
    def temp = x2gene[geneid]
    x2gene[alid] = temp
  }
}

def s = [:]

def counter = 0 

list = list.collect {
  def a = it
  a = a.replaceAll("<http://purl.obolibrary.org/obo/","")
  a = a.replaceAll(">","")
  a = a.replaceAll("\n","")
  a
}
println "Diseases: "+diseaselist.size()
def count1 = 0
def s1 = new TreeSet(x2gene.keySet())
list.each { if (it in s1) count1++ }
println "x2gene size: "+count1

def analmap = [:]
infile.splitEachLine("\t") { line ->
  def b = null
  for (int col = 0 ; col < line.size() ; col++) {
    def d = new Double(line[col])
    if (d>cutoff) {
      def a = list[col+row]
      b = list[row]
      a = a.replaceAll("<http://purl.obolibrary.org/obo/","")
      a = a.replaceAll(">","")
      a = a.replaceAll("\n","")
      b = b.replaceAll("<http://purl.obolibrary.org/obo/","")
      b = b.replaceAll(">","")
      b = b.replaceAll("\n","")
      a = a.trim()
      b = b.trim()
      if ((x2gene[a]!=null) || (x2gene[b]!=null)) {
	def dis = null
	def gene = null
	if ((a in diseaselist) && x2gene[b]!=null) {
	  dis = a
	  gene = x2gene[b]
	} else if ((x2gene[a]!=null) && (b in diseaselist)) {
	  dis = b
	  gene = x2gene[a]
	}
	if (dis!=null && gene!=null) {
	  gene.each { gene2 ->
	    def exp = new Expando()
	    exp.gene = gene2
	    exp.val = d
	    if (analmap[dis]==null) {
	      analmap[dis] = []
	    }
	    analmap[dis] << exp
	    //	    println dis+"\t"+analmap[dis]
	  }
	}
      }
    }
  }
  /* Once we know we hit a disease row here we can finish the analysis of the disease and discard from analmap */
  /* b is the disease from the row counter */

  if (b!=null && b in diseaselist) {
    println "Drug-testing $b."
    def l = analmap[b].sort { it.val }
    drugmap.keySet().each { key ->
      def drug = drugmap[key]
      if (drug.genes.size()>2) {
	def index = 0
	fout.print("$b\t$key\t"+l.size()+"\t")
	l.each { exp ->
	  if (exp.gene in drug.genes) {
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