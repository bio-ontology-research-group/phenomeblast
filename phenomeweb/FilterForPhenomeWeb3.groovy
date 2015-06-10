/* non-symmetrical version */

import groovy.sql.Sql

def prefixlist = ["MGI", "OMIM", "ZDB", "DECIPHER", "S0", "WB", "ORPHA",
		  "FB", "RGD", "STITCHORIG", "MEDLINESTITCHTM", "MEDLINEDOID", "PMCDOID", "MEDLINEDERMO", "PMCDOID", "DBS"]

def max = 100
def infile = new File(args[0])
def indexfile = new File(args[1])
def fout = new PrintWriter(new BufferedWriter(new FileWriter(args[2])))

def cutoff = 0.05

def row = 0

def list = []
indexfile.splitEachLine("\t") {
  list = it
}


def counter = 0 

infile.splitEachLine("\t") { line ->
  def b = list[row]
  def prefixb = null
  prefixlist.each {
    if (b.startsWith(it)) {
      prefixb = it
    }
  }
  def sb = [:]
  prefixlist.each {
    sb[it] = new PriorityQueue()
  }
  for (int col = 0 ; col < line.size() ; col++) {
    def a = list[col]
    def d = new Double(line[col])
    if (d>cutoff) {
      def prefixa = null
      prefixlist.each {
	if (a.startsWith(it)) {
	  prefixa = it
	}
      }
      if (prefixa && prefixb) {
	SimPair pair1 = new SimPair(a, d)
	sb[prefixa].add(pair1)
	if (sb[prefixa].size()>max) {
	  sb[prefixa].poll()
	}
      }
    }
  }
  sb.each { prefix, queue ->
    while (queue.size()>0) {
      def it = queue.poll()
      fout.println(b+"\t"+it.term+"\t"+it.val)
    }
  }
  
  println row
  row++
}

fout.flush()
fout.close()

//println count