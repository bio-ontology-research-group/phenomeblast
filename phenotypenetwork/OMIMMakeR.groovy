import javastat.inference.nonparametric.*

def infile = new File(args[0])
def outbase = new PrintWriter(new BufferedWriter(new FileWriter(new File("eval/omim-base.txt"))))
def outdist = new PrintWriter(new BufferedWriter(new FileWriter(new File("eval/omim-dist.txt"))))

def dismap = [:]
def disbase = [:]

infile.splitEachLine("\t") { line ->
  def dis = line[0]
  def mgi = line[1]
  if (dismap[dis] == null) {
    dismap[dis] = [:]
  }
  if (disbase[dis] == null) {
    disbase[dis] = []
  }
  def d = new Double(line[2])
  if (d>0 && (line.size()>3)) {
    disbase[dis] << d
  }
  if (line.size()>3) {
    line[3..-1].each {
      if (dismap[dis][it]==null) {
	dismap[dis][it] = []
      }
      dismap[dis][it] << d
    }
  }
}

dismap.keySet().each { dis ->
  def outmap = new TreeMap()
  def outmap2 = new TreeMap()
  def pwdist = dismap[dis]
  double[] l = disbase[dis].toArray(new double[0])
  outbase.print(dis+"\t")
  disbase[dis].each { outbase.print(it+"\t") }
  outbase.println("")
  pwdist.keySet().each {key ->
    def val = pwdist[key]
    if (val.size()>1) {
      outdist.print(dis+"\t"+key+"\t")
      val.each { outdist.print(it+"\t") }
      outdist.println("")
    }
  }
}
outdist.flush()
outbase.flush()
outdist.close()
outbase.close()
