def f=new File("mpath2011h")
f.eachLine { line ->
  if (line.indexOf("MPATH:")==-1) { 
    println line 
  } else {
    def s = "MPATH:"
    def i1 = line.indexOf("MPATH:")
    def initial = line.substring(0,i1)
    def sub = line.substring(i1)
    def num = null
    if (sub.indexOf(' ')>-1) {
      num = sub.substring(sub.indexOf(':')+1, sub.indexOf(' '))
    } else {
      num = sub.substring(sub.indexOf(':')+1)
    }
    (7-num.length()).times{s+="0"}
    s=s+num
    println initial+s
  }
}

