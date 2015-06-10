
def fout = new PrintWriter(new BufferedWriter(new FileWriter(args[0])))


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
  def omim = null
  if (line[6].indexOf(",")==-1) {
    omim = ["OMIM:"+line[6]]
  } else {
    omim = line[6].split(",").collect { "OMIM:"+it }
  }
  def mgi = line[5]
  def name = line[1]
  if (cmap[name]!=null) {
    omim.each { o ->
      if (map[o] == null) {
	map[o] = new TreeSet()
      }
      map[o].add(cmap[name])
    }
  }
}

map.keySet().each { omim ->
  map[omim].each { fout.println("$omim\t$it") }
}

fout.flush()
fout.close()
