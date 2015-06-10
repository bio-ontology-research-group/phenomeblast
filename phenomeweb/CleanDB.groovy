import groovy.sql.Sql


def dir = "/home/ao/apache-tomcat-6.0.29/webapps/PhenomeBlast-0.1/resf2/"
def sql = Sql.newInstance("jdbc:mysql://localhost:3306/phenomeweb", "root",
			  "", "com.mysql.jdbc.Driver")

def l = []
sql.eachRow("select * from Genotype") {
  if (! new File(dir+it.id+".txt").exists()) {
    l << it.id
  }
}
l.each {
  sql.execute ("DELETE FROM Genotype WHERE id='$it'")
}
