CUTOFF = 0.01

def map = [:]
new File("cmap/cmap_instances_02.csv").splitEachLine("\t") { line ->
  Expando exp = new Expando()
  def instance = line[0]
  exp.name = line[2]
  exp.concentration = line[4]
  exp.duration = line[5]
  map[instance] = exp
}

def instance2pval = [:].withDefault { new LinkedHashSet() }
map.each { instance, exp ->
  try {
    new File("enrichment-wilcoxon-new-2/$instance/groups.txt").splitEachLine("\t") { line ->
      if (! line[0].startsWith("root_node")) {
	def name = line[1]
	def id = line[2]
	def pval_high = new Double(line[9])
	def pval_low = new Double(line[8])
	Expando exp2 = new Expando()
	exp2.id = id
	exp2.name = name
	exp2.high = pval_high
	exp2.low = pval_low
	instance2pval[instance].add(exp2)
      }
    }
  } catch (Exception E) {}
}

instance2pval.each { instance, set ->
  Expando exp1 = map[instance]
  set.each { exp2 ->
    if (exp2.low < CUTOFF) {
      println "$instance\t${exp1.name}\t${exp1.concentration}\t${exp1.duration}\t${exp2.name}\t${exp2.id}\t${exp2.low}"
    }
  }
}