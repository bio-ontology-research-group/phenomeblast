public class SimPair implements Comparable {

  public Double val = null
  public String term = null

  SimPair(def term, def val) {
    this.val = val
    this.term = term
  }

  
  public int compareTo(Object o) {
    if (o.val != this.val) {
      this.val.compareTo(o.val)
    } else {
      this.term.compareTo(o.term)
    }
  }
}