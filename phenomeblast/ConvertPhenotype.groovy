def pf = new File(args[0])

def lines = []
pf.eachLine { line ->
  lines << line
  if (line.length()<1) {
    if (lines.size()>0) {
      boolean found = false
      lines.each {
	if (it.indexOf("intersection_of: PATO:0000001")>-1) {
	  found = true
	}
      }
      if (found) {
	lines = lines.collect {
	  if (it.indexOf("inheres_in ")>-1) {
	    it.replaceAll("inheres_in","part_of")
	  } else {
	    it
	  }
	}
      }
      lines = lines.collect {
	if (it.indexOf("intersection_of: qualifier ")>-1) {
	  it.replaceAll("intersection_of","!intersection_of")
	} else {
	  it
	}
      }
      lines = lines.collect {
	if (it.indexOf("intersection_of: PATO")>-1) {
	  it.replaceAll("PATO","has-quality PATO")
	} else {
	  it
	}
      }
      lines = lines.collect {
	if (it.indexOf("inheres_in ")>-1) {
	  it.replaceAll("inheres_in "," ")
	} else {
	  it
	}
      }
      lines = lines.collect {
	if (it.indexOf("inheres_in_part_of ")>-1) {
	  it.replaceAll("inheres_in_part_of ","part_of ")
	} else {
	  it
	}
      }
      lines = lines.collect {
	if (it.indexOf("has_part MP")>-1) {
	  it.replaceAll("has_part "," ")
	} else {
	  it
	}
      }
      lines = lines.collect {
	if (it.indexOf("has_part HP")>-1) {
	  it.replaceAll("has_part "," ")
	} else {
	  it
	}
      }
      lines = lines.collect {
	if (it.indexOf("has_part PATO")>-1) {
	  it.replaceAll("has_part "," ")
	} else {
	  it
	}
      }
      
      lines.each { println it }
    }
    term = ""
    lines = []
  }
}