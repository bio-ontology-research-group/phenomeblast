import com.wcohen.ss.*
import com.wcohen.ss.api.*

def id = ""
def id2name = [:]
def name2id = [:]
new File("doid.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(4).trim()
    id2name[id] = new TreeSet()
  }
  if (line.startsWith("name:")) {
    def name = line.substring(6).trim().toLowerCase()
    id2name[id].add(name)
    if (name2id[name] == null) {
      name2id[name] = new TreeSet()
    }
    name2id[name].add(id)
  }
  if (line.startsWith("synonym")) {
    def name = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")).trim().toLowerCase()
    id2name[id].add(name)
    if (name2id[name] == null) {
      name2id[name] = new TreeSet()
    }
    name2id[name].add(id)
  }
}

def traitsdone = new TreeSet()
def dist = new SmithWaterman()
new File("gwascatalog.txt").splitEachLine("\t") { line ->
  def trait = line[7].toLowerCase().trim()
  if (! (trait in traitsdone)) {
    def traitw = new BasicStringWrapper(trait)
    def besthit = ""
    def secondbesthit = ""
    def score = 1000
    name2id.keySet().each { name ->
      def namew = new BasicStringWrapper(name)
      def d = -dist.score(traitw, namew)
      if (d < score) {
	score = d
	secondbesthit = besthit
	besthit = namew.unwrap()
      }
    }
    print "$trait\t$besthit\t"
    name2id[besthit].each {
      print it+","
    }
    print "\t"
    print "$secondbesthit\t"
    name2id[secondbesthit].each {
      print it+","
    }
    println "\t$score"
    traitsdone.add(trait)
  }
}