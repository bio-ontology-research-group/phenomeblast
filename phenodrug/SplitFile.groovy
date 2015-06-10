
def infile = new File(args[0])
def outf = args[1]
def splits = new Integer(args[2])

def l = []
def count = 0

splits.times {
  l[it] = new PrintWriter(new FileWriter(outf+it))
}
infile.eachLine {
  l[count%splits].println(it)
  count+=1
}

splits.times {
  l[it].flush()
  l[it].close()
}
