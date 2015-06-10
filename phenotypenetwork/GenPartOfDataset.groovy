File f = new File ("eval/baselinedisease.txt")

def s = f.getText().split("\t")

2000000.times { println s[it] }
