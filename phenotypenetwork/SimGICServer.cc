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
#include <netinet/in.h>
#include <arpa/inet.h>

#define PORTNUMBER 31337
#define BUFFERSIZE 7000000
#define MINPHENOTYPES 1
#define THREADS 24

using namespace std;
using namespace boost::threadpool;
//using boost::asio::ip::tcp;

float ** dist;
int size;
map<string, unordered_set<string> > phenotypes ;
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
void computeSim(unordered_set<string> phenoset, map<string, double> &result) {
  map<string, unordered_set<string> >::iterator it;
  it=phenotypes.begin();
  while (it != phenotypes.end()) {
    string p2 = (*it).first;
    result[p2] = simgic(phenoset, (*it).second, p2) ;
    it++;
  }
}

unordered_set<string> &split(const char *s, char delim, unordered_set<string> &elems) {
  stringstream ss(s);
  string item;
  while (getline(ss, item, delim)) {
    elems.insert(item);
  }
  return elems;
}

int handle(int * sockfd) { // returns 1 on success and 0 on failure
  char buffer[BUFFERSIZE];
  int n ;
  unordered_set<string> tokens ;
  map<string, double> res ;
  map<string, double>::iterator it2;
  bzero(buffer, BUFFERSIZE);
  stringstream ss;
  n = read( *sockfd, buffer, BUFFERSIZE - 1);
  if (n < 0) {
    printf("ERROR reading from socket");
    return 0 ;
  } 
  //string s (buffer);
  split(buffer, ' ', tokens) ;
  computeSim( tokens, res ) ;
  cout << "Computing similarity" << endl ;
  for (it2 = res.begin(); it2!=res.end(); it2++) {
    ss << (*it2).first << " " << (*it2).second << endl ;
  }
  cout << "Writing results to socket" << endl ;
  n = write(*sockfd, ss.str().c_str(), strlen(ss.str().c_str())) ;
  cout << "Closing socket" << endl ;
  close(*sockfd);
  free(sockfd) ;
  return 1 ;
}

int main (int argc, char *argv[]) {
  unordered_set<string>::iterator it;
  int sockfd, newsockfd, portno;
  socklen_t clilen;
  int *newsock ;
  struct sockaddr_in serv_addr, cli_addr;
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

  /*
  pid_t process_id = 0;
  // Create child process
  process_id = fork();
  // Indication of fork() failure
  if (process_id < 0) {
    printf("fork failed!\n");
    // Return failure in exit status
    exit(1);
  }
  // PARENT PROCESS. Need to kill it.
  if (process_id > 0) {
    printf("process_id of child process %d \n", process_id);
    // return success in exit status
    exit(0);
  }
  */
  cout << "Binding socket..."  << endl;


  sockfd = socket(AF_INET, SOCK_STREAM, 0);
  if (sockfd < 0) {
    error("ERROR opening socket");
  }
  int true_val = 1 ;
  if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &true_val, sizeof (int)) == -1) {
    error("ERROR setting socket options");
  }
  bzero((char *) &serv_addr, sizeof(serv_addr));
  portno = PORTNUMBER ;
  serv_addr.sin_family = AF_INET;
  serv_addr.sin_addr.s_addr = INADDR_ANY;
  serv_addr.sin_port = htons(portno);
  
  if (::bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
    error("ERROR on binding");
  }
  listen(sockfd,5);

  /* Make the threadpool */
  pool tp(THREADS);        /* initialise it to THREADS number of threads */

  while (1) {
    clilen = sizeof(cli_addr);
    newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
    printf("\nI got a connection from (%s , %d)\n", inet_ntoa(cli_addr.sin_addr), ntohs(cli_addr.sin_port));
    if (newsockfd < 0) {
      error("ERROR on accept");
    }
    newsock = (int*) malloc(sizeof(int));
    *newsock = newsockfd;
    tp.schedule(boost::bind(&handle, newsock ) ) ;
  }
  tp.wait() ;

  return 0;
}
