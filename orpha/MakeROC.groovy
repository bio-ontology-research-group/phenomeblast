def infile = new File(args[0])


def list = []
infile.splitEachLine("\t") { line ->
  def max = new Integer(line[1])
  def val = new Integer(line[2])
  list << val/max
}

list.sort()
def max = list.size()

for (int i = 0 ; i < max ; i++) {
  def tp = i/max
  def fp = list[i]
  println "$fp\t$tp"
}