def infile = new File("eval/tetralogy.txt")

def fout = new PrintWriter(new BufferedWriter(new FileWriter(new File("/tmp/base.txt"))))

def l = []
Map pwdist = [:]
infile.splitEachLine("\t") { line ->
  def d = new Double(line[2])
  if (d>0 && (line.size()>3)) {
    l << d
  }
  if (line.size()>3) {
    line[3..-1].each {
      if (pwdist[it]==null) {
	pwdist[it] = new TreeSet()
      }
      pwdist[it].add(new Double(line[2]))
    }
  }
}

l = l.sort()
l.each { fout.println(it) }
fout.flush()
fout.close()

def outmap = new TreeMap()
def outmap2 = new TreeMap()
pwdist.keySet().each {key ->
  def val = pwdist[key]
  def all = 0.0
  val.each { all += it }
  all = all/val.size()
  //  println key+"\t"+all+"\t"+val
  def fout2 = new PrintWriter(new BufferedWriter(new FileWriter(new File("/tmp/rout.txt"))))
  val.each { fout2.println(it) }
  fout2.flush()
  fout2.close()
  def proc = "/home/leechuck/Public/software/phenomeblast/phenotypenetwork/runr.sh".execute()
  proc.waitFor()
  def pval = proc.getText()
  def d = new Double(pval.tokenize()[1])
  outmap[key] = d
  outmap2[key] = all
}
outmap.keySet().each { key ->
  if (outmap[key] < 0.0001953125) {
    println key +"\t"+outmap[key]+"\t"+outmap2[key]+"\tsignificant"
  } else {
    println key +"\t"+outmap[key]+"\t"+outmap2[key]
  }
}