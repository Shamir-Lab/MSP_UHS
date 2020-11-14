Description:UHS_MSP is a disk-based software to build de Bruijn graph from DNA sequences using Universal Hitting Set.
To run the software you need to get the relvant UHS from http://acgt.cs.tau.ac.il/docks/ and save it in a file as "res_k". where k is the k value of the UHS.

The code is written in Java.
UHS_MSP.jar is for the default k = 12 parameter and UHS_MSP_10.jar, UHS_MSP_14.jar are for k = 10,14.

How to run the jar file:
java -jar jar file -in InputPath -k kmerLength -L readLength –NB NumberOfBlocks –p MinimumSubstringLength –t threads -b bufferSize -r
readable -x seed.
to run the program with the default parameters we ran in the paper run as follows:
java -jar IHS_MSP.jar -in InputPath(Data) -k 61 -L 124 –NB 1000 –p 12 –t 1 -b -r true -x 11101101.


