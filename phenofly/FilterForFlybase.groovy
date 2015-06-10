import groovy.sql.Sql

def cutoff = 0
def infile = new File("/mnt/net/phenomenet-09-09.txt")
def indexfile = new File("/mnt/net/phenomenet-09-09-index.txt")

def queryfile = new File(args[0])
def fout = new PrintWriter(new BufferedWriter(new FileWriter(args[1])))


def list = []
indexfile.splitEachLine("\t") {
  list = it
}

def flyset = new TreeSet()
def allelefile = new File("flymap-gene-allele.txt")
def allele2gene = [:]
allelefile.splitEachLine("\t") { line ->
  def al = line[0]
  def ge = line[1]
  allele2gene[al]=ge
  flyset.add(al)
  flyset.add(ge)
}

def qset = new TreeSet() // query set (for filtering)
def qmap = [:] // query map (pairs)
queryfile.splitEachLine("\t") {
  def gene = it[0]
  def q = it[1].replaceAll("_",":")
  qset.add(q)
  if (qmap[gene] == null) {
    qmap[gene]=new TreeSet()
  }
  qmap[gene].add(q)
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

def row = 0
def analmap = [:]
infile.splitEachLine("\t") { line ->
  def b = null
  for (int col = 0 ; col < line.size() ; col++) {
    def d = new Double(line[col])
    if (d>=cutoff) {
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
      if (a.startsWith("FB") || b.startsWith("FB")) {
	def flyq = null
	def query = null
	if ((a in qset) && (b in flyset)) {
	  query = a
	  if (allele2gene[b]!=null) {
	    flyq = allele2gene[b]
	  } else {
	    flyq = b
	  }
	} else if ((a in flyset) && (b in qset)) {
	  query = b
	  if (allele2gene[a]!=null) {
	    flyq = allele2gene[a]
	  } else {
	    flyq = a
	  }
	}
	if (flyq!=null && query!=null) {
	  def exp = new Expando()
	  exp.gene = query
	  exp.val = d
	  if (analmap[flyq]==null) {
	    analmap[flyq] = []
	  }
	  analmap[flyq] << exp
	  //	    println dis+"\t"+analmap[dis]
	}
      }
    }
  }

  /* b is the disease from the row counter */
  if (b!=null && ((b in flyset))){ // || (allele2gene[b] in flyset))) {
    println "Testing $b."
    def orig = b
    if (allele2gene[b]!=null) {
      b = allele2gene[b]
    }
    def l = analmap[b]?.sort { it.val }
    //    qmap.keySet().each { key ->
    if (l!=null) {
      def query = qmap[b]
      //      if (query?.size()>0) {
	def index = 0
	fout.print("$orig\t"+l.size()+"\t")
	l.each { exp ->
	  if (exp.gene in query) {
	    fout.print(index+"\t")
	  }
	  index+=1
	}
	fout.println("")
	//      }
      analmap.remove(b)
    }
  }
  println row
  row+=1
}
fout.flush()
fout.close()

//println count
