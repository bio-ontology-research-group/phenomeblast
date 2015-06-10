def infile = new File("ind.txt")
def inf2 = new File("/media/disk/phenomenet/phenomeweb.sql")

def map = [:]

infile.splitEachLine("\t") {
  map[it[0]] = it[1]
}

inf2.splitEachLine("\t") {
  println map[it[0]]+"\t"+map[it[1]]+"\t"+it[2]
}
