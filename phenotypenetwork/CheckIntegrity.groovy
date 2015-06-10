def infile = new File(args[0])

def i = 0
infile.splitEachLine("\t") {
 if (it.size()!=(i-1)) {
  println "Alert: line $i"
 }
 i = it.size()
}
