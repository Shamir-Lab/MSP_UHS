import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BuildDeBruijnGraph {
	
    private static int[] valTable = new int[]{0,-1,1,-1,-1,-1,2,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,3};
    
    private static int GetDecimal(char[] a, int from, int to){
            int val=0;
            for(int i=from; i<to; i++){
                    val = val<<2;
                    val += valTable[a[i]-'A'];
            }
            
            return val;
    }


    public static byte[] uhsBitSet() throws IOException {
        int n = (int) Math.pow(4, 12) / 8;
        int i = 0;
        byte [] bits = new byte[n];
        for (int j = 0; j<bits.length; j++) {
                bits[j] = 0;
        }
        
        String DocksFile = "res_12.txt";
        FileReader frG = new FileReader(DocksFile);
        int count=0;

        BufferedReader reader;
        try {
                reader = new BufferedReader(frG);
                String line;// = reader.readLine();
                while ((line = reader.readLine()) != null) {
//                        if (count < 10) System.out.println(i + " " + line);
                        i = GetDecimal(line.toCharArray(), 0, 12);
                        //System.out.println(i);
                        bits[i / 8] |= 1 << (i % 8);
                        count++;
                }
                reader.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
        System.out.println(count);
        frG.close();
        return bits;
}

    
	public static void main(String[] args) throws IOException {
    	
    	String infile = "E:\\test.txt";
    	int k = 15, numBlocks = 256, pivot_len = 12, bufferSize = 8192, readLen = 101, numThreads = 1, hsmapCapacity = 1000000;
    int x = 0;
    	boolean readable = false;
    byte [] uhs_bits =uhsBitSet();

    	
    	if(args[0].equals("-help")){
    		System.out.print("Usage: java -jar BuildDeBruijnGraph.jar -in InputPath -k k -L readLength[options]\n" +
	        			       "Options Available: \n" + 
	        			       "[-NB numOfBlocks] : (Integer) Number Of Kmer Blocks. Default: 256" + "\n" + 
	        			       "[-p pivotLength] : (Integer) Pivot Length. Default: 12" + "\n" + 
	        			       "[-x xor] : (Integer) Xor. Default: 0" + "\n" + 
	        			       "[-t numOfThreads] : (Integer) Number Of Threads. Default: 1" + "\n" +
	        			       "[-b bufferSize] : (Integer) Read/Writer Buffer Size. Default: 8192" + "\n" + 
	        			       "[-r readable] : (Boolean) Output Format: true means readable text, false means binary. Default: false" + "\n");
    		return;
    	}
    	
    	for(int i=0; i<args.length; i+=2){
    		if(args[i].equals("-in"))
    			infile = args[i+1];
    		else if(args[i].equals("-k"))
    			k = new Integer(args[i+1]);
    		else if(args[i].equals("-x"))
    			x = new Integer(args[i+1]);
    		else if(args[i].equals("-NB"))
    			numBlocks = new Integer(args[i+1]);
    		else if(args[i].equals("-p"))
    			pivot_len = new Integer(args[i+1]);
    		else if(args[i].equals("-b"))
    			bufferSize = new Integer(args[i+1]);
    		else if(args[i].equals("-L"))
    			readLen = new Integer(args[i+1]);
    		else if(args[i].equals("-t"))
    			numThreads = new Integer(args[i+1]);
    		else if(args[i].equals("-r"))
    			readable = new Boolean(args[i+1]);
    		else{
    			System.out.println("Wrong with arguments. Abort!");
    			return;
    		}
    	}
    	
		
		Partition partition = new Partition(k, infile, numBlocks, pivot_len, bufferSize, readLen, x, uhs_bits);
		Map map = new Map(k, numBlocks, bufferSize, hsmapCapacity);
	
		try{
			
			System.out.println("Program Configuration:");
	    	System.out.print("Input File: " + infile + "\n" +
	    					 "Kmer Length: " + k + "\n" +
	    					 "Read Length: " + readLen + "\n" +
	    					 "# Of Blocks: " + numBlocks + "\n" +
	    					 "Pivot Length: " + pivot_len + "\n" +
	    					 "# Of Threads: " + numThreads + "\n" +
	    					 "R/W Buffer Size: " + bufferSize + "\n" +
	    					 "Output Format: " + (readable==true?"Text":"Binary") + "\n");
		
			long maxID = partition.Run();
			map.Run(numThreads);
			
			long time1=0;			
			long t1 = System.currentTimeMillis();
			System.out.println("Merge IDReplaceTables Begin!");	
			String sortcmd = "sort -t $\'\t\' -o IDReplaceTable +0 -1 -n -m Maps/maps*";
			Runtime.getRuntime().exec(new String[]{"/bin/sh","-c",sortcmd},null,null).waitFor();
			long t2 = System.currentTimeMillis();
			time1 = (t2-t1)/1000;
			System.out.println("Time used for merging: " + time1 + " seconds!");
			
			Replace replace = new Replace("IDReplaceTable", "OutGraph", k, bufferSize, readLen, maxID);
			replace.Run(readable);
			
		
		}
		catch(Exception E){
			System.out.println("Exception caught!");
			E.printStackTrace();
		}
		
	}	

}