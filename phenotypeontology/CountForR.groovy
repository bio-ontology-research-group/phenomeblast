def f = new File(args[0])

f.splitEachLine("\t") {
  println it.size()-1
}