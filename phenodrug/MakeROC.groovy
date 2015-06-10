def infile = new File(args[0])


def list = []
infile.splitEachLine("\t") { line ->
  if (line.size()>3) {
    def max = new Integer(line[2])
    line[3..-1].each {
      def val = new Integer(it)
      list << val/max
    }
  }
}

list.sort()
def max = list.size()

for (int i = 0 ; i < max ; i++) {
  def tp = i/max
  def fp = list[i]
  println "$tp\t$fp"
}