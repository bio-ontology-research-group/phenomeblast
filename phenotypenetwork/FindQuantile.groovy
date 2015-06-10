def infile = new File(args[0])

def i = 0

def l = []
infile.splitEachLine("\t") {
 it.each { l << new Double(it) }
 if (l.size()>10000000) {
  l = l.sort()
  println l[5000000]
  println l[9000000]
  println l[9500000]
  println l[9900000]
  println l[9990000]
  System.exit(0)
 }
}
