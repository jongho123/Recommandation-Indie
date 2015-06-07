#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include "feature.h"

int main(int argc, char* argv[])
{
  if(argc < 3) {
    printf("./extract input output\n"); 
    exit(1);
  }

  int i;
  uint8_t buf[20000];
  FILE* fs = fopen(argv[2],"w"); 

  int length = extractFromFile(argv[1], buf);

  for(i=0; i < length; i++)
    fputc(buf[i], fs);

  fclose(fs);
  return 0;
}

