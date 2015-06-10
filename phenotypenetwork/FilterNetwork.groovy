def infile = new File("tanimoto.txt")

def count = 0

def list = []

infile.splitEachLine("\t") {
  if (count==0) {
    count+=1
  } else {
    list << it
  }
}

list.each { print it+" " }
