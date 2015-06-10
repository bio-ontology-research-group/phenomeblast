def infile = new File(args[0])

def cl2xref = [:]
def flag = false
infile.eachLine {line ->
  if (line.startsWith("[Typedef]")) {
    flag = false
  }
  if (line.startsWith("[Term]")) {
    flag = true
  }
  if (flag) {
    if (line.startsWith("id: ")) {
      term = line.substring(4).trim()
      if (cl2xref[term]==null) {
	cl2xref[term]=new TreeSet()
      }
    }
    if (line.startsWith("xref: ")) {
      def xref = line.substring(6).trim()
      cl2xref[term].add(xref)
    }
  }
}

cl2xref.keySet().each { key ->
  def val = cl2xref[key]
  val.each {
    if ((it.indexOf(":")>1) && (it.indexOf("http")==-1)) {
      println key+"\t"+it
    }
  }
}
