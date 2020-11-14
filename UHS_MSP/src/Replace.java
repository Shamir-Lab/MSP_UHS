
import java.io.*;

public class Replace {

	private String replaceTableFile;
	private String outputGraphFile;
	private int k;
	private int bufSize;
	private long largestID;
	
	private FileReader fr;
	private BufferedReader bfr;
	private FileWriter fw;
	private BufferedWriter bfw;
	
	private int readLen;
	
	public Replace(String infile, String outfile, int k, int bufferSize, int readLen, long largestID){
		this.replaceTableFile = infile;
		this.outputGraphFile = outfile;
		this.k = k;
		this.bufSize = bufferSize;
		this.readLen = readLen;
		this.largestID = largestID;
	}
	
	private void DoReplace() throws IOException{
		fr = new FileReader(replaceTableFile);
		bfr = new BufferedReader(fr, bufSize);
		fw = new FileWriter(outputGraphFile);
		bfw = new BufferedWriter(fw, bufSize);
		
		long originalID, replaceID;
		
		String str;
		String[] strs = null;
		
		if((str=bfr.readLine())!=null){
			strs = str.split("\t");
			originalID = new Long(strs[0]);
			replaceID = new Long(strs[1]);
		}
		else{
			originalID = Long.MAX_VALUE;
			replaceID = Long.MAX_VALUE;
		}
		
		int modValue = ((readLen-k+1)<<1);
		
		for(long i=0; i<largestID; i+=2){
			
			if(i == originalID){
				bfw.write(replaceID + " ");
				
				if(strs.length > 3){
					long rangeEnd = Long.parseLong(strs[4]);
					if(strs[2].equals("+")){
						for(long temp=replaceID+2; temp<=rangeEnd; temp+=2){
							bfw.write(temp + " ");
						}
					}
					else if(strs[2].equals("-")){
						for(long temp=replaceID-2; temp>=rangeEnd; temp-=2){
							bfw.write(temp + " ");
						}
					}
					i = Long.parseLong(strs[3]);	
				}
				
				if((str=bfr.readLine())!=null){
					strs = str.split("\t");
					originalID = new Long(strs[0]);
					replaceID = new Long(strs[1]);
				}
				else{
					originalID = Long.MAX_VALUE;
					replaceID = Long.MAX_VALUE;
				}
			}
			else{
				bfw.write(i + " ");
			}
			
			if((i+2) % modValue == 0)
				bfw.newLine();	
		}
		
		bfw.close();
		fw.close();
		bfr.close();
		fr.close();			
	}
	
	private void DoReplaceBin() throws IOException{
		fr = new FileReader(replaceTableFile);
		bfr = new BufferedReader(fr, bufSize);
		DataOutputStream out = null;
		out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(outputGraphFile)), bufSize));
		
		long originalID, replaceID;
		
		String str;
		String[] strs = null;
		
		if((str=bfr.readLine())!=null){
			strs = str.split("\t");
			originalID = new Long(strs[0]);
			replaceID = new Long(strs[1]);
		}
		else{
			originalID = Long.MAX_VALUE;
			replaceID = Long.MAX_VALUE;
		}
		
		
		for(long i=0; i<largestID; i+=2){
			
			if(i == originalID){
				out.writeLong(replaceID);
				
				if(strs.length > 3){
					long rangeEnd = Long.parseLong(strs[4]);
					if(strs[2].equals("+")){
						for(long temp=replaceID+2; temp<=rangeEnd; temp+=2){
							out.writeLong(temp);
						}
					}
					else if(strs[2].equals("-")){
						for(long temp=replaceID-2; temp>=rangeEnd; temp-=2){
							out.writeLong(temp);
						}
					}
					i = Long.parseLong(strs[3]);	
				}
				
				if((str=bfr.readLine())!=null){
					strs = str.split("\t");
					originalID = new Long(strs[0]);
					replaceID = new Long(strs[1]);
				}
				else{
					originalID = Long.MAX_VALUE;
					replaceID = Long.MAX_VALUE;
				}
			}
			else{
				out.writeLong(i);
			}
				
		}

		out.close();
		bfr.close();
		fr.close();			
	}
	
	public void Run(boolean readable) throws Exception{
		
		long time1=0;
		
		long t1 = System.currentTimeMillis();
		System.out.println("Replace IDs Begin!");	
		
		if(readable)
			DoReplace();
		else
			DoReplaceBin();		
		
		long t2 = System.currentTimeMillis();
		time1 = (t2-t1)/1000;
		System.out.println("Time used for replacing IDs: " + time1 + " seconds!");	
		
	}
	
    public static void main(String[] args){
    	
    	String infile = "E:\\test.txt";
    	String outfile = "E:\\testOut.txt";
    	int k = 15, bufferSize = 8192, readLen = 101;
    	long largestID = 0;
    	boolean readable = false;
    	
    	if(args[0].equals("-help")){
    		System.out.print("Usage: java -jar Replace.jar -in InputTablePath -out outGraphPath -k k -L readLength -m largestID[options]\n" +
	        			       "Options Available: \n" + 
	        			       "[-b bufferSize] : (Integer) Read/Writer Buffer Size. Default: 8192" + "\n" +
	        			       "[-r readable] : (Boolean) Output Format: true means readable text, false means binary. Default: false" + "\n");
    		return;
    	}
    	
    	for(int i=0; i<args.length; i+=2){
    		if(args[i].equals("-in"))
    			infile = args[i+1];
    		else if(args[i].equals("-out"))
    			outfile = args[i+1];
    		else if(args[i].equals("-k"))
    			k = new Integer(args[i+1]);
    		else if(args[i].equals("-m"))
    			largestID = new Long(args[i+1]);
    		else if(args[i].equals("-b"))
    			bufferSize = new Integer(args[i+1]);
    		else if(args[i].equals("-L"))
    			readLen = new Integer(args[i+1]);
    		else if(args[i].equals("-r"))
    			readable = new Boolean(args[i+1]);
    		else{
    			System.out.println("Wrong with arguments. Abort!");
    			return;
    		}
    	}
    	
		
		Replace bdgraph = new Replace(infile, outfile, k, bufferSize, readLen, largestID);
	
		try{
			
			System.out.println("Program Configuration:");
	    	System.out.print("Input Table File: " + infile + "\n" +
	    					 "Output Graph File: " + outfile + "\n" +
	    					 "Kmer Length: " + k + "\n" +
	    					 "Read Length: " + readLen + "\n" +
	    					 "Largest ID: " + largestID + "\n" +
	    					 "R/W Buffer Size: " + bufferSize + "\n" +
	    					 "Output Format: " + (readable==true?"Text":"Binary") + "\n");
		
			bdgraph.Run(readable);
		}
		catch(Exception E){
			System.out.println("Exception caught!");
			E.printStackTrace();
		}
		
	}	

}