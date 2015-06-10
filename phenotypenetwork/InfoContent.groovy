//def infile = new File("phenotype.txt")

// second arg: cut-off IC
// third arg: replace everything below cut-off with this value

def minic = 7

def infile = new File(args[0])

def total = 0
def counting = [:]
infile.splitEachLine("\t") { line ->
  if (line.size()>1) {
    total+=1
    line[1..line.size()-1].each { mp ->
      if (counting[mp]==null) {
	counting[mp]=0
      }
      counting[mp]+=1
    }
  }
}


counting.keySet().each { key ->
  def value = counting[key]
  def ic = -Math.log(value/total)/Math.log(2)
  // if (ic > 3 && ic < 8) {
  //   ic = ic * 10
  // } else if (ic>=8) {
  //   ic = 2
  // }
  println key+"\t"+ic
}
