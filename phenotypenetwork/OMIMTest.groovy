import javastat.inference.nonparametric.*

def infile = new File(args[0])

//def fout = new PrintWriter(new BufferedWriter(new FileWriter(new File("/tmp/base.txt"))))

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
  pwdist.keySet().each {key ->
    def val = pwdist[key]
    double[] valarr = val.toArray(new double[0])
    //def all = 0.0
    //val.each { all += it }
    //all = all/val.size()
    //  println key+"\t"+all+"\t"+val
    //    def fout2 = new PrintWriter(new BufferedWriter(new FileWriter(new File("/tmp/rout.txt"))))
    //val.each { fout2.println(it) }
    //fout2.flush()
    //fout2.close()
    //def proc = "/home/leechuck/Public/software/phenomeblast/phenotypenetwork/runr.sh".execute()
    //proc.waitFor()
    if (l.size()>5 && valarr.size()>5) {
      RankSumTest wilcox = new RankSumTest(new Double(0.05/256), "equal", l, valarr)
      def pval = wilcox.pValue
      if (pval < (0.05/256)) {
	if (pval < (0.05/(256*4929))) {
	  println dis + "\t" + key +"\t"+pval +"\tsignificant"
	} else {
	  println dis + "\t" + key +"\t"+pval
	}
      } 
      //else {
      //      println dis + "\t" + key +"\t"+pval
      //    }
    }
  }
}