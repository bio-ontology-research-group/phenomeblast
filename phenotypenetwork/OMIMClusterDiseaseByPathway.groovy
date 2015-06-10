def infile = new File(args[0])

def map = [:]
def omimmap = [:]
def omimomim = [:]

infile.splitEachLine(" ") { line ->
  def omim = line[0]
  def pw = line[1]
  if (map[pw]==null) {
    map[pw] = new TreeSet()
  }
  map[pw].add(omim)
  if (omimmap[omim] == null) {
    omimmap[omim] = new TreeSet()
  }
  omimmap[omim].add(pw)
}

map.keySet().each { key ->
  def val = map[key]
  print key+"\t"
  val.each { print it+"\t" }
  println ""
}

// omimmap.keySet().each { key ->
//   def val = omimmap[key]
//   val.each { pw ->
//     def pws = map[pw]
//     if (omimomim[key] == null) {
//       omimomim[key] = new TreeSet()
//     }
//     omimomim[key].addAll(pws)
//   }
// }
// omimomim.keySet().each {
//   omimomim[it].remove(it)
// }
// omimomim.keySet().each { nr1 ->
//   omimomim[nr1].each { nr2 ->
//     println nr1 + "\t"+ nr2 + "\t1"
//   }
// }
