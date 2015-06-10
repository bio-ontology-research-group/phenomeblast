def fout = new PrintWriter(new BufferedWriter(new FileWriter("orphanetphenotypes-names.txt")))

def symptomfile = new File("en_product4.xml")
def phenofile = new File("phenotypes-clean.txt")


def dis2sign = [:]
def slurper = new XmlSlurper().parse(symptomfile)
slurper.DiseaseList.Disease.each { dis ->
  def id = dis.Orphanum.text()
  def name = dis.Name.text()
  fout.println("ORPHANET:$id\t$name")
  dis2sign[id] = new TreeSet()
  dis.DiseaseSignList.DiseaseSign.each { sign ->
    def sid = sign.Sign.@id.toString()
    dis2sign[id].add(sid)
  }
}
fout.flush()
fout.close()

def sign2pheno = [:]
def sign2par = [:]
phenofile.splitEachLine("\t") { line ->
  def sid = line[0]
  def par = line[2]
  if (sid!=par) {
    sign2par[sid] = par
  }
  if (sign2pheno[sid]==null) {
    sign2pheno[sid] = new TreeSet()
  }
  if (line[1]!=null) {
    def l = line[1].tokenize()
    l.each { sign2pheno[sid].add(it) }
  }
}

sign2par.keySet().each { sid ->
  def rem = sid
  def par = sign2par[sid]
  while (sid!=par && sid!=null && par!=null) {
    if (sign2pheno[par]!=null) {
      sign2pheno[rem].addAll(sign2pheno[par])
    }
    sid = par
    par = sign2par[sid]
  }
}

dis2sign.keySet().each { dis ->
  def signs = dis2sign[dis]
  signs.each { sign ->
    sign2pheno[sign].each {
      println "ORPHANET:$dis\t$it"
    }
  }
}