def f = new File("index.txt")

def s= new TreeSet()

f.splitEachLine("\t") {
  def str = it[0].trim().toLowerCase()
  if (s.contains(str)==false) {
    s.add(str)
    println it[0]+"\t"+it[1]
  }
}