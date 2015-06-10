import groovy.sql.Sql


def infile = new File(args[0])
def indexfile = new File(args[1])
def fout = new PrintWriter(new BufferedWriter(new FileWriter(args[2])))

def cutoff = 0.1

def row = 0


def list = []
indexfile.splitEachLine("\t") {
  list = it
}

def s = [:]

def counter = 0 

infile.splitEachLine("\t") { line ->
  for (int col = 0 ; col < line.size() ; col++) {
    def d = new Double(line[col])
    if (d>cutoff) {
      def a = list[col+row]
      def b = list[row]
      /*      a = a.replaceAll("<http://purl.obolibrary.org/obo/","")
      a = a.replaceAll(">","")
      a = a.replaceAll("\n","")
      b = b.replaceAll("<http://purl.obolibrary.org/obo/","")
      b = b.replaceAll(">","")
      b = b.replaceAll("\n","")
      a = a.trim()
      b = b.trim()
      */
      fout.println("$a\t$b\t$d")
    }
  }
  println row
  row++
}
fout.flush()
fout.close()

//println count