def pwf = new File(args[0])

pwf.splitEachLine("\t") { line ->
  def flag = true
  line.each {
    if (flag && (it == "1.0")) {
      flag=false
    } else  {
      if (it.size()>0) {
	def v = new Double(it)
	println it
      }
    }
  }
}