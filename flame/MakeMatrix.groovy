def map = [:].withDefault { [:] }
new File("../smltest/doid2doid-matrix.txt").splitEachLine("\t") { line ->
  def d1 = line[0]
  def d2 = line[1]
  def val = line[2]
  map[d1][d2] = val
}
println map.size()+" "+map.size()
PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter("index.txt")))
def im = [:]
def counter = 0
map.keySet().each {
  im[counter] = it
  fout.println("$counter\t$it")
  counter +=1
}
fout.flush()
fout.close()
map.each { d, m ->
  print m[im[0]]
  for (int i = 1 ; i < counter ; i++) {
    print " "+m[im[i]]
  }
  println ""
}