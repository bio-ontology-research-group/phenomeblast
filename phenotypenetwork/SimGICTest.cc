#include <ctime>
#include <string>
#include <sstream>
#include <algorithm>
#include <iterator>
#include <string.h>
#include <stdlib.h>
#include <iostream>
#include <map>
#include <set>
#include <bitset>
#include <pthread.h>
#include <fstream>
#include <algorithm>
#include <unordered_set>
#include <unordered_map>
#include <boost/threadpool.hpp>
#include <cstring>
#include <netdb.h>      // Needed for the socket functions
#include <unistd.h>
#include <stdio.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>

#define PORTNUMBER 31337
#define BUFFERSIZE 7000000
#define MINPHENOTYPES 1
#define THREADS 1

using namespace std;
using namespace boost::threadpool;

float ** dist;
int size;
map<string, unordered_set<string> > phenotypes ;
map<string, unordered_set<string> > phenotypes_test ;
unordered_map<string, float > phenotypes_s ;
unordered_map<string, float > icmap;

void error(const char *msg)
{
  perror(msg);
  exit(1);
}

inline float resnik(unordered_set<string> s1, unordered_set<string> s2, string p1, string p2) {
  float max = 0.0f;
  unordered_set<string>::iterator it;
  for ( it=s1.begin() ; it != s1.end(); it++ ) {
    if (s2.find(*it)!=s2.end()) {
      if (icmap[*it]>max) {
	max = icmap[*it];
      }
    }
  }
  return max;
}

inline float simgic(unordered_set<string> s1, unordered_set<string> s2, string p2) {
  float inter = 0.0f;
  float  un = 0.0f;
  unordered_set<string>::iterator it;
  float p1_s = 0.0f ;
  
  for (it = s1.begin() ; it != s1.end(); it++) {
    p1_s += icmap[*it] ;
  }

  for ( it=s1.begin() ; it != s1.end(); it++ ) {
    if (s2.find(*it)!=s2.end()) {
      inter += icmap[*it];
    }
  }
  un = p1_s + phenotypes_s[p2] - inter;

  return inter/un;
}


inline float sumset(unordered_set<string> s, unordered_map<string, float> ic) {
  unordered_set<string>::iterator it;
  float result = 0.0f;
  for (it = s.begin(); it != s.end(); it++) {
    result += ic[*it];
  }
  return result;
}

// computes similarity between phenoset and all other phenotypes; returns map of phenotype -> similarity-val
void computeSim(unordered_set<string> phenoset) {
  map<string, unordered_set<string> >::iterator it;
  it=phenotypes.begin();
  while (it != phenotypes.end()) {
    string p2 = (*it).first;
    simgic(phenoset, (*it).second, p2) ;
    it++;
  }
}

unordered_set<string> &split(const string &s, char delim, unordered_set<string> &elems) {
  stringstream ss(s);
  string item;
  while (getline(ss, item, delim)) {
    elems.insert(item);
  }
  return elems;
}

int main (int argc, char *argv[]) {
  unordered_set<string>::iterator it;
  //int n;
  char buffer[BUFFERSIZE];
  char * id ;
  unordered_set<string> ps ;
  ifstream in("../data/phenotypes.txt");
  while (in) {
    in.getline(buffer, BUFFERSIZE);
    if(in) {
      char * tok = strtok(buffer, "\t");
      unordered_set<string> mp ;
      id = tok ;
      //cout << id << endl ;
      while ((tok=strtok(NULL, "\t"))!=NULL) {
	mp.insert(tok);
      }
      int mpsize = mp.size();
      mp.reserve(mpsize);
      if (mp.size()>=MINPHENOTYPES) {
	phenotypes[id]=mp;
      }
    }
  }
  in.close();

  ifstream icin("../data/phenotypes-info.txt");
  while(icin) {
    icin.getline(buffer, BUFFERSIZE);
    if (icin) {
      char * tok = strtok(buffer, "\t");
      char * val = strtok(NULL, "\t");
      float f = atof(val);
      icmap[tok] = f;
    }
  }
  icmap.reserve(icmap.size());

  ifstream intest("../smltest/testqueries.txt");
  while (intest) {
    intest.getline(buffer, BUFFERSIZE);
    if(intest) {
      char * tok = strtok(buffer, "\t");
      unordered_set<string> mp ;
      id = tok ;
      //cout << id << endl ;
      while ((tok=strtok(NULL, "\t"))!=NULL) {
	mp.insert(tok);
      }
      int mpsize = mp.size();
      mp.reserve(mpsize);
      if (mp.size()>=MINPHENOTYPES) {
	phenotypes_test[id]=mp;
      }
    }
  }
  intest.close();


  cout << "Size of icmap: " <<icmap.size() << endl;
  cout << "Size of phenotype map: " <<phenotypes.size() << endl;
  map<string, unordered_set<string> >::iterator it1;
  for (it1 = phenotypes.begin(); it1!=phenotypes.end(); it1++) {
    phenotypes_s[(*it1).first]= sumset((*it1).second, icmap) ;
  }

  size = phenotypes.size();
  cout << "Generating matrix of size " << size << "." << endl;
  /* make result matrix */
  dist = (float**)malloc (size*sizeof(float*));
  for (int i = 0 ; i < size ; i++) {
    dist[i] = (float*)malloc(sizeof(float)*(size-i));
  }

  cout << "Matrix generated." << endl;

  clock_t begin = clock();

  map<string, unordered_set<string> >::iterator it_test;
  it_test=phenotypes_test.begin();
  int count = 0 ;
  while (it_test != phenotypes_test.end()) {
    unordered_set<string> phenoset = (*it_test).second ;
    computeSim(phenoset) ;
    it_test++;
    cout << count << endl ;
    count++ ;
  }
  clock_t end = clock();
  double elapsed_secs = double(end - begin) / CLOCKS_PER_SEC;
  cout << "Elapsed seconds: " << elapsed_secs << endl ;

  return 0;
}
