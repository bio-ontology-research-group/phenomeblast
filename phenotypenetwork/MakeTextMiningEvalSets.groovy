// args[0]: input file to convert (replaces all DOIDs with the grid)
// args[1]: output directory

def prefixSet = []
new File("medline-grid.txt").eachLine { line ->
  prefixSet << line
}

def fout = [:]
prefixSet.each { 
  fout[it] = new PrintWriter(new FileWriter(args[1]+"/"+it)) 
}
new File(args[0]).splitEachLine("\t") { line ->
  def doid = line[0]
  def mgiid = line[1]
  doid = doid.replaceAll(".*DOID","DOID")
  prefixSet.each { fout[it].println(it+doid+"\t"+mgiid) }
}
fout.values().each { 
  it.flush()
  it.close()
}
