def do2omim = [:]
def omim2do = [:]
def id = ""
new File("HumanDO.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(4).trim()
    do2omim[id] = new TreeSet()
  }
  if (line.indexOf("xref: OMIM")>-1) {
    line = line.substring(11).trim()
    if (line ==~ /\d\d\d\d\d\d/) {
      do2omim[id].add(line)
      omim2do[line] = id
    }
  }
}
do2omim.each { d, omims ->
  omims.each { o ->
    println "$d\tOMIM:$o"
  }
}