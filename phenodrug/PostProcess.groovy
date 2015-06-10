def infile = new File(args[0])

infile.splitEachLine("\t") {
  if (it.size()>5) {
    it.each { print it+"\t" }
  }
}