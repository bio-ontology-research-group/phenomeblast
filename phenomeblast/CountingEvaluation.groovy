import cern.jet.stat.*
import cern.colt.list.*

def directory = "/tmp/mappings/"

def generate(File f) {
  def m = [:]
  f.splitEachLine("\t") { line ->
    m[line[0]] = line.size()-1
  }
  m
}

double genlist(Map m) {
  def a = 0
  m.keySet().each {
    a+=m[it]
  }
  return a/m.size()
}

/* */
Map score(Map x2y, Map x2x, DoubleArrayList test, DoubleArrayList reference) {
  def map = [:]
  def l = new DoubleArrayList()
  x2y.keySet().each { key ->
    def val1 = new Double(x2y[key])
    def val2 = new Double(x2x[key])
    double q1 = Descriptive.quantileInverse(test, val1)
    double q2 = Descriptive.quantileInverse(reference, val2)
    map[key] = q1/q2
  }
  return map
}

double evaluate(Map m) {
  double a = 0
  m.keySet().each {
    a+=m[it]
  }
  a/m.size()
}

def hp2mp = generate(new File(directory+"hp2mp.txt"))
def hp2wp = generate(new File(directory+"hp2wp.txt"))
def hp2yp = generate(new File(directory+"hp2yp.txt"))
def hp2hp = generate(new File(directory+"hp2hp.txt"))
def hp2fp = generate(new File(directory+"hp2fp.txt"))

def mp2hp = generate(new File(directory+"mp2hp.txt"))
def mp2wp = generate(new File(directory+"mp2wp.txt"))
def mp2yp = generate(new File(directory+"mp2yp.txt"))
def mp2mp = generate(new File(directory+"mp2mp.txt"))
def mp2fp = generate(new File(directory+"mp2fp.txt"))

def wp2hp = generate(new File(directory+"wp2hp.txt"))
def wp2mp = generate(new File(directory+"wp2mp.txt"))
def wp2yp = generate(new File(directory+"wp2yp.txt"))
def wp2wp = generate(new File(directory+"wp2wp.txt"))
def wp2fp = generate(new File(directory+"wp2fp.txt"))

def yp2hp = generate(new File(directory+"yp2hp.txt"))
def yp2mp = generate(new File(directory+"yp2mp.txt"))
def yp2wp = generate(new File(directory+"yp2wp.txt"))
def yp2yp = generate(new File(directory+"yp2yp.txt"))
def yp2fp = generate(new File(directory+"yp2fp.txt"))

def fp2hp = generate(new File(directory+"fp2hp.txt"))
def fp2mp = generate(new File(directory+"fp2mp.txt"))
def fp2wp = generate(new File(directory+"fp2wp.txt"))
def fp2yp = generate(new File(directory+"fp2yp.txt"))
def fp2fp = generate(new File(directory+"fp2fp.txt"))

def zfin2hp = generate(new File(directory+"zfin2hp.txt"))
def zfin2mp = generate(new File(directory+"zfin2mp.txt"))
def zfin2yp = generate(new File(directory+"zfin2yp.txt"))
def zfin2wp = generate(new File(directory+"zfin2wp.txt"))
def zfin2fp = generate(new File(directory+"zfin2fp.txt"))

def hp2mpl = genlist(hp2mp)
def hp2wpl = genlist(hp2wp)
def hp2ypl = genlist(hp2yp)
def hp2hpl = genlist(hp2hp)
def hp2fpl = genlist(hp2fp)

def mp2hpl = genlist(mp2hp)
def mp2wpl = genlist(mp2wp)
def mp2ypl = genlist(mp2yp)
def mp2mpl = genlist(mp2mp)
def mp2fpl = genlist(mp2fp)

def wp2hpl = genlist(wp2hp)
def wp2mpl = genlist(wp2mp)
def wp2ypl = genlist(wp2yp)
def wp2wpl = genlist(wp2wp)
def wp2fpl = genlist(wp2fp)

def yp2hpl = genlist(yp2hp)
def yp2mpl = genlist(yp2mp)
def yp2wpl = genlist(yp2wp)
def yp2ypl = genlist(yp2yp)
def yp2fpl = genlist(yp2fp)

def fp2hpl = genlist(fp2hp)
def fp2mpl = genlist(fp2mp)
def fp2wpl = genlist(fp2wp)
def fp2ypl = genlist(fp2yp)
def fp2fpl = genlist(fp2fp)

def zfin2hpl = genlist(zfin2hp)
def zfin2mpl = genlist(zfin2mp)
def zfin2wpl = genlist(zfin2wp)
def zfin2ypl = genlist(zfin2yp)
def zfin2fpl = genlist(zfin2fp)

println "MP2MP "+mp2mpl
println "HP2HP "+hp2hpl
println "WP2WP "+wp2wpl
println "YP2YP "+yp2ypl
println "FP2FP "+fp2fpl
println ""
println "MP2MP "+mp2mpl/mp2mpl
println "HP2MP "+hp2mpl/mp2mpl
println "WP2MP "+wp2mpl/mp2mpl
println "YP2MP "+yp2mpl/mp2mpl
println "FP2MP "+fp2mpl/mp2mpl
println ""
println "MP2HP "+mp2hpl/hp2hpl
println "HP2HP "+hp2hpl/hp2hpl
println "WP2HP "+wp2hpl/hp2hpl
println "YP2HP "+yp2hpl/hp2hpl
println "FP2HP "+fp2hpl/hp2hpl
println ""
println "MP2WP "+mp2wpl/wp2wpl
println "HP2WP "+hp2wpl/wp2wpl
println "WP2WP "+wp2wpl/wp2wpl
println "YP2WP "+yp2wpl/wp2wpl
println "FP2WP "+fp2wpl/wp2wpl
println ""
println "MP2YP "+mp2ypl/yp2ypl
println "HP2YP "+hp2ypl/yp2ypl
println "WP2YP "+wp2ypl/yp2ypl
println "YP2YP "+yp2ypl/yp2ypl
println "FP2YP "+fp2ypl/yp2ypl
println ""
println "MP2FP "+mp2fpl/fp2fpl
println "HP2FP "+hp2fpl/fp2fpl
println "WP2FP "+wp2fpl/fp2fpl
println "YP2FP "+yp2fpl/fp2fpl
println "FP2FP "+fp2fpl/fp2fpl
println ""
println "ZFIN2MP "+zfin2mpl/mp2mpl
println "ZFIN2HP "+zfin2hpl/hp2hpl
println "ZFIN2WP "+zfin2wpl/wp2wpl
println "ZFIN2YP "+zfin2ypl/yp2ypl
println "ZFIN2FP "+zfin2fpl/fp2fpl





// def zfin2omim = generate(new File(directory+"zfin2omim.txt"))

// def mgi2hp = generate(new File(directory+"mgi2hp.txt"))
// def mgi2mp = generate(new File(directory+"mgi2mp.txt"))
// def mgi2yp = generate(new File(directory+"mgi2yp.txt"))
// def mgi2wp = generate(new File(directory+"mgi2wp.txt"))
// def mgi2omim = generate(new File(directory+"mgi2omim.txt"))


// def omim2hp = generate(new File(directory+"omim2hp.txt"))
// def omim2mp = generate(new File(directory+"omim2mp.txt"))
// def omim2yp = generate(new File(directory+"omim2yp.txt"))
// def omim2wp = generate(new File(directory+"omim2wp.txt"))
// def omim2zfin = generate(new File(directory+"omim2zfin.txt"))
// def omim2mgi = generate(new File(directory+"omim2mgi.txt"))
