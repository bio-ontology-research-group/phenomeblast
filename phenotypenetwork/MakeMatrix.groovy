def infile = new File(args[0])
def indexfile = new File(args[1])

// def cutoff = new Double(args[2])  // cutoff is always 0
def cutoff = 0.0

def row = 0

def list = []
indexfile.splitEachLine("\t") {
  list = it
}

infile.splitEachLine("\t") { line ->
  for (int col = 0 ; col < line.size() ; col++) {
    def d = new Double(line[col])
    if (d>cutoff) {
      def a = list[col+row]
      def b = list[row]
      println "$a\t$b\t$d"
    }
  }
  row++
}

//println count