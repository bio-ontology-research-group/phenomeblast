def directory = new File(args[0])

def infile = new File(args[1])

def id2name = [:]

def id = ""
def name = ""
directory.eachFile { pf ->
  pf.eachLine { line ->
    if (line=="[Term]") {
      id2name[id] = name
      id = ""
      name = ""
    }
    if (line.startsWith("id:")) {
      id = line.substring(3).trim()
    }
    if (line.startsWith("name:")) {
      name = line.substring(5).trim()
    }
  }
}

infile.splitEachLine("\t") {
  it.each {
    name = it.substring(it.indexOf("#")+1,it.length()-1).replaceAll("_",":").trim()
    print id2name[name]+"\t"
  }
  println ""
}