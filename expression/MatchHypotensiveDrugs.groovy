def id2name = [:].withDefault { new TreeSet() }
def map = [:]
new File("cmap/cmap_instances_02.csv").splitEachLine("\t") { line ->
  Expando exp = new Expando()
  def instance = line[0]
  exp.name = line[2]
  exp.concentration = line[4]
  exp.duration = line[5]
  map[instance] = exp
}

def sidernames = new TreeSet()
def sidername2id = [:]
new File("siderphenotypes-names.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def name = line[1]?.toLowerCase()?.trim()
  id2name[id].add(name)
  sidernames.add(name)
  if (id.indexOf("ORIG")>-1) {
    sidername2id[name] = id
  }
}
def siderset = new TreeSet()
new File("stitchphenotypes.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def pheno = line[1]
  if (id.indexOf("ORIG")>-1 && (pheno.indexOf("HP:0002615")>-1 || pheno.indexOf("MP:0001596")>-1 || pheno.indexOf("HP:0000822")>-1 || pheno.indexOf("MP:0000231")>-1)) {
    siderset.add(id)
  }
}

def cmapnames = new TreeSet()
new File("cmap/cmap_instances_02.csv").splitEachLine("\t") { line ->
  def id = line[0]
  def name = line[2]?.toLowerCase()?.trim()
  if (name) {
    id2name[id].add(name)
    cmapnames.add(name)
  }
}

def cmapset = new TreeSet()
new File("enriched.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def pheno = line[5]
  if (pheno == "MP:0001596" || pheno == "MP:0000231") {
    cmapset.add(id)
  }
}

def a1 = cmapset.collect {id2name[it]}.flatten().unique()
def a2 = siderset.collect {id2name[it]}.flatten().unique()
def a3 = a1.intersect(a2)
a2.each { 
  if (it && it in cmapnames && (! (it in a3))) {
    println "$it not found"
  }
}
