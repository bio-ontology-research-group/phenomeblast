
def mgi2omim = [:]
new File(args[0]).splitEachLine("\t") { line ->
  def mgi = line[0]
  def o = line[1]
  def val = line[2]
  Expando exp = new Expando()
  exp.omim = o
  exp.val = val
  if (mgi2omim[mgi] == null) {
    mgi2omim[mgi] = new HashSet()
  }
  mgi2omim[mgi].add(exp)
}

def pos = [:]
new File(args[1]).splitEachLine("\t") { line->
  def dis = line[0]
  def mgi = line[1]
  if (pos[mgi] == null) {
    pos[mgi] = new TreeSet()
  }
  pos[mgi].add(dis)
}

mgi2omim.each { mgi, omims ->
  def somims = omims.sort{it.val}.reverse()
  def size = somims.size()
  def positives = pos[mgi]
  def index = 0
  if (positives != null) {
    somims.each {

      if (it.omim in positives) {
	println "$mgi\t"+it.omim+"\t"+(index/size)+"\t"+it.val
      }
      index += 1
    }
  }
}