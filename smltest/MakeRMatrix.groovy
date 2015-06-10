def map = [:].withDefault { [:] }
new File(args[0]).splitEachLine("\t") { line ->
  def val = 1 - new Double(line[2])
  map[line[0]][line[1]] = val
}

def lastIndex = map.keySet().size() - 1
map.keySet().eachWithIndex { it, index ->
  if (index != lastIndex) {
    print "$it\t" 
  } else {
    println "$it"
  }
}

map.keySet().each { doid1 ->
  print "$doid1\t"
  map.keySet().eachWithIndex { doid2, index ->
    if (index != lastIndex) {
      print map[doid1][doid2]+"\t"
    } else {
      println map[doid1][doid2]
    }
  }
}
