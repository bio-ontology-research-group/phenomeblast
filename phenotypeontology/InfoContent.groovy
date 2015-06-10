def infile = new File("omim2mp.txt")

def total = 0
def counting = [:]
infile.splitEachLine("\t") { line ->
  total++
  if (line.size()>1) {
    line[1..line.size()-1].each { mp ->
      if (counting[mp]==null) {
	counting[mp]=0
      }
      counting[mp]++
    }
  }
}


counting.keySet().each { key ->
  def value = counting[key]
  def ic = -Math.log(value/total)/Math.log(2)
  println key+"\t"+ic
}
