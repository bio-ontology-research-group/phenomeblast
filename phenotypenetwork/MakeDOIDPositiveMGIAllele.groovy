def do2omim = [:]
def omim2do = [:]
def id = ""
new File(args[0]).eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(4).trim()
    do2omim[id] = new TreeSet()
  }
  if (line.indexOf("xref: OMIM")>-1) {
    line = line.substring(11).trim()
    if (line.indexOf(" ")>-1) {
      line = line.substring(0,line.indexOf(" ")).trim()
    }
    if (line ==~ /\d\d\d\d\d\d/) {
      do2omim[id].add(line)
      omim2do[line] = id
    }
  }
}

new File("MGI_Geno_Disease.rpt").splitEachLine("\t") { line ->
  line[2].split("\\|").each { mgi ->
    line[-1].split(",").each { 
      if (omim2do[it]) {
	it = omim2do[it]
	def omim = it
	println args[1]+"$omim\t$mgi"
      }
    }
  }
}