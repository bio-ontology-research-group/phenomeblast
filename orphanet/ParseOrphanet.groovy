import com.wcohen.ss.*
import com.wcohen.ss.api.*

def hpfile = new File("human-phenotype-ontology.obo")
def mpfile = new File("mammalian_phenotype.obo")
def orphafile = new File("en_product5.xml")

def id2lab = [:]
def lab2id = [:]
def id = ""
def addLabelsToMap = { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
    id2lab[id] = new TreeSet()
  }
  if (line.startsWith("name:")) {
    def name = line.substring(5).trim()
    id2lab[id].add(name.toLowerCase())
  }
  if (line.startsWith("synonym")) {
    def name = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""))
    id2lab[id].add(name.toLowerCase())
  }
}

def reverseAddLabelsToMap = { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
  }
  if (line.startsWith("name:")) {
    def name = line.substring(5).trim().toLowerCase()
    if (lab2id[name]==null) {
          lab2id[name] = new TreeSet()
    }
    lab2id[name].add(id)
  }
  if (line.startsWith("synonym")) {
    def name = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")).toLowerCase()
    if (lab2id[name]==null) {
          lab2id[name] = new TreeSet()
    }
    lab2id[name].add(id)
  }
}

hpfile.eachLine (addLabelsToMap)
mpfile.eachLine (addLabelsToMap)
hpfile.eachLine (reverseAddLabelsToMap)
mpfile.eachLine (reverseAddLabelsToMap)

Set splitOrphaNames(String name) {
  println name
  def s = name.tokenize()
  def res = []
  res << ""
  s.each { tok ->
    if (tok.indexOf("/")==-1) {
      res = res.collect { it+" "+tok }
    } else {
      tres = []
      tok.split("/").each { t ->
	res.each {
	  tres << it+" "+t
	}
      }
      println tres
      res = tres
    }
  }
}

def oid2name = [:]
def oname2id = [:]
def oid2parent = [:]
def slurper = new XmlSlurper().parse(orphafile)
slurper.SignList.Sign.each { sign ->
  def sid = sign.@id
  def name = sign.Name.text().toLowerCase()
  oid2name[sid] = name
  oname2id[name] = sid
  sign.SignChildList.Sign.each { child ->
    def cid = child.@id.toString()
    if (oid2parent[cid]==null) {
      oid2parent[cid] = new TreeSet()
    }
    oid2parent[cid].add(sid.toString())
  }
}

def allnames = lab2id.keySet().collect { new BasicStringWrapper(it) }

oid2name.keySet().each { oid ->
  def oname = oid2name[oid]
  if (oname.indexOf("/")>-1) {
    /* Get distance to all names in MP/HP */
    StringWrapper w1 = new MultiStringWrapper(oname, "/")
    //    Levenstein dist = new Levenstein()
    NeedlemanWunsch dist = new NeedlemanWunsch()
    //    JaroWinkler dist = new JaroWinkler()
    def score = 1000
    def besthit = ""
    allnames.each { name ->
      def d = -dist.score(name, w1)
      if (d < score) {
	score = d
	besthit = name.unwrap()
      }
    }
    println "$oid\t${lab2id[besthit]}\t${oid2parent[oid.toString()]}\t$oname\t$besthit\t$score\t"
  }
}