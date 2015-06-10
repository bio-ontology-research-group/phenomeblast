def infile = new File(args[0])

infile.splitEachLine("\t") { line ->
  try {
    def d = new Double(line[2])
    if (d < (0.05/252)) {
      
    }
  } catch (Exception E) {}
}