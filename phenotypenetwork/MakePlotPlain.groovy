def pwf = new File(args[0])

pwf.splitEachLine("\t") { line ->
  line.each {
    println it
  }
}