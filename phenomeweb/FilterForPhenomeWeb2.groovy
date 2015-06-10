import groovy.sql.Sql

def prefixlist = ["MGI", "OMIM", "ZDB", "DECIPHER", "S0", "WB", "ORPHA",
		  "FB", "RGD", "STITCHORIG", "MEDLINESTITCHTM", "MEDLINEDOID", "PMCDOID", "MEDLINEDERMO", "PMCDOID", "DBS"]

def max = 250
def infile = new File(args[0])
def indexfile = new File(args[1])
def fout = new PrintWriter(new BufferedWriter(new FileWriter(args[2])))

def cutoff = 0.03

def row = 0


def list = []
indexfile.splitEachLine("\t") {
  list = it
}

def s = [:]

def counter = 0 

infile.splitEachLine("\t") { line ->
  def b = list[row]
  def prefixb = ""
  prefixlist.each {
    if (b.startsWith(it)) {
      prefixb = it
    }
  }
  if (s[b] == null) {
    s[b] = [:] //new PriorityQueue()
    prefixlist.each {
      s[b][it] = new PriorityQueue()
    }
  }
  for (int col = 0 ; col < line.size() ; col++) {
    def a = list[col+row]
    def d = new Double(line[col])
    if (d>cutoff) {
      def prefixa = ""
      prefixlist.each {
	if (a.startsWith(it)) {
	  prefixa = it
	}
      }
      if (prefixa && prefixb) {
	if (s[a] == null) {
	  s[a] = [:]
	  prefixlist.each {
	    s[a][it] = new PriorityQueue()
	  }
	  //	s[a] = new PriorityQueue()
	}
	SimPair pair1 = new SimPair(a, d)
	SimPair pair2 = new SimPair(b, d)
	s[a][prefixb].add(pair2)
	s[b][prefixa].add(pair1)
	if (s[a][prefixb].size()>max) {
	  s[a][prefixb].poll()
	}
	if (s[b][prefixa].size()>max) {
	  s[b][prefixa].poll()
	}
      }
    }
  }
  println row
  row++
}

s.each { key, map ->
  map.each { pref, queue ->
    while (queue.size()>0) {
      def it = queue.poll()
      fout.println(key+"\t"+it.term+"\t"+it.val)
    }
  }
}

fout.flush()
fout.close()

//println count