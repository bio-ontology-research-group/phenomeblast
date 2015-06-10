def infile = new File(args[0])
def indexfile = new File(args[1])
def fout = new PrintWriter(new BufferedWriter(new FileWriter(args[2])))


def mousegenes = new File("data/MGI_PhenotypicAllele.rpt")
def diseases = new File("data/MGI_Geno_Disease.rpt")

def mgi2mim = [:]

def cutoff = 0.0

def row = 0

def cmap = [:]
mousegenes.splitEachLine("\t") { line ->
  if (!line[0].startsWith("#")) {
    def id = line[0]
    def name = line[1]
    cmap[name] = id
  }
}

def map = [:]
diseases.splitEachLine("\t") { line ->
  def omim = "OMIM:"+line[6]
  def mgi = line[5]
  def name = line[1]
  if (cmap[name]!=null) {
    if (map[omim] == null) {
      map[omim] = new TreeSet()
    }
    map[omim].add(cmap[name])
  }
}

def diseaselist = new TreeSet()
def allelelist = new TreeSet()
map.keySet().each {
  diseaselist.add(it)
  allelelist.addAll(map[it])
}

def s = [:]

def counter = 0 

def list = []
indexfile.splitEachLine("\t") {
  list = it
}

list = list.collect {
  def a = it
  a = a.replaceAll("<http://purl.obolibrary.org/obo/","")
  a = a.replaceAll(">","")
  a = a.replaceAll("\n","")
  a.trim()
}
println "Diseases: "+diseaselist.size()
println "Alleles: "+allelelist.size()

def analmap = [:]
infile.splitEachLine("\t") { line ->
  def b = null
  for (int col = 0 ; col < line.size() ; col++) {
    def d = new Double(line[col])
    def a = list[col+row]
    b = list[row]
    if (a in diseaselist || b in diseaselist) {
      def dis = null
      def gene = null
      if ((a in diseaselist) && (b in allelelist)) {
	dis = a
	gene = b
      } else if ((a in allelelist) && (b in diseaselist)) {
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
    println "Testing disease $b."
    def l = analmap[b].sort { it.val }
    def alleles = map[b]
    def index = 0
    fout.print("$b\t"+l.size()+"\t")
    l.each { exp ->
      if (exp.gene in alleles) {
	fout.print(index+"\t")
      }
      index+=1
    }
    fout.println("")
    fout.flush()
    analmap.remove(b)
  }
  println row++
}
fout.flush()
fout.close()

//println count