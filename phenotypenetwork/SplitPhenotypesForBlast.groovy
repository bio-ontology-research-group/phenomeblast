def infile = new File(args[0])
def outdir = new File(args[1])
def dn = args[1].trim()

infile.eachLine { line ->
  def l = line.split("\t")
  def s = l[0]
  s = s.replaceAll("<http://purl.org/obo/owlapi/phene#","")
  s = s.replaceAll(">", "")
  def fout = new PrintWriter(new BufferedWriter(new FileWriter(new File(dn+"/"+s+".txt"))))
  fout.print(s+"\t")
  l[1..-1].each { fout.print(it+"\t") }
  fout.println()
  fout.flush()
  fout.close()
}