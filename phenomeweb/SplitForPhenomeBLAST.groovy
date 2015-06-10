def infile = new File(args[0])
def outdir = args[1] // with trailing "/"

// <http:/purl.obolibrary.org/obo/

infile.splitEachLine("\t") {
  def clear = it[0].replaceAll("\\<http://purl.obolibrary.org/obo/","")
  clear = clear.replaceAll("\\>","")
  def name = outdir+clear+".txt"
  def fout = new PrintWriter(new BufferedWriter(new FileWriter(name)))
  fout.print(clear+"\t")
  it[1..-1].each { fout.print(it+"\t") }
  fout.println("")
  fout.flush()
  fout.close()
}
