
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;


public class Map{
	
	private int k;
	private int numOfBlocks;
	private int bufSize;
	
	private Object lock_blocks = new Object();
	
	private int capacity;
	
	private int blockID;
	
	private long forAndVal;
	private long forAndVal32;
	
	private static int[] valTable = new int[]{0,-1,1,-1,-1,-1,2,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,3};

	public Map(int kk, int numberOfBlocks, int bufferSize, int HScapacity){
		this.k = kk;
		this.numOfBlocks = numberOfBlocks;
		this.bufSize = bufferSize;
		this.capacity = HScapacity;
		this.blockID = 0;
		this.forAndVal = (long)Math.pow(2, 2*(k-32)) - 1;
		this.forAndVal32 = (long)Math.pow(2, 2*k) - 1;
	}
	
	public class MyThread extends Thread{
		private CountDownLatch threadsSignal;
		
		public MyThread(CountDownLatch threadsSignal){
			super();
			this.threadsSignal = threadsSignal; 
		}
		
		@Override
		public void run(){
			System.out.println(Thread.currentThread().getName() + "Start..."); 
			
			FileReader fr;
			BufferedReader bfr;
			FileWriter fw;
			BufferedWriter bfw;
			
			
			String line;
			
			int p,j;
			long cnt;
			Kmer64 k1, k1_rev;
			
			
			try{
				File dir = new File("Maps");
				if(!dir.exists())
					dir.mkdir();
				
				while(blockID<numOfBlocks){
					
					synchronized (lock_blocks){
						p = blockID;
						blockID++;
					}
								
					fr = new FileReader("Nodes/nodes"+p);
					bfr = new BufferedReader(fr, bufSize);
					fw = new FileWriter("Maps/maps"+p);
					bfw = new BufferedWriter(fw, bufSize);
					
					HashMap<Kmer64,Long> nodes = new HashMap<Kmer64,Long>(capacity);
					
					while((line = bfr.readLine()) != null){
						
						String[] strs = line.split("\t");
						cnt = Long.parseLong(strs[1]); 
						
						long preOriginal = -1, preReplace = -1, Original = -1, Replace = -1;
						long diff = -1;
						boolean newOut = true, next = false;
						
						Long ReplaceObj, Replace_revObj;
						
						char[] lineCharArray = strs[0].toCharArray();
						k1 = new Kmer64(lineCharArray,0,k,false);
						k1_rev = new Kmer64(lineCharArray,0,k,true);
						
						int bound = strs[0].length() - k + 1;
						
						for(j = 0; j < bound; j++){
				
							if(j != 0){
								if(k > 32){
									k1 = new Kmer64((k1.low<<2) + valTable[lineCharArray[k+j-1]-'A'], ((k1.high<<2) + valTable[lineCharArray[k+j-33]-'A']) & forAndVal);
									k1_rev = new Kmer64((k1_rev.low>>>2) + ((k1_rev.high&3)<<62), (k1_rev.high>>>2) + ((long)((valTable[lineCharArray[k+j-1]-'A']^3))<<((k-33)<<1)));
								}
								else{
									k1 = new Kmer64(((k1.low<<2) + valTable[lineCharArray[k+j-1]-'A']) & forAndVal32, 0);
									k1_rev = new Kmer64((k1_rev.low>>>2) + ((long)((valTable[lineCharArray[k+j-1]-'A']^3))<<((k-1)<<1)), 0);
								}
							}
							
							ReplaceObj = nodes.get(k1);
							Replace_revObj = nodes.get(k1_rev);
							
							if(ReplaceObj == null && Replace_revObj == null){
								nodes.put(k1, cnt+j*2);
								
								if(!newOut && !next){
									bfw.write(preOriginal+"\t"+preReplace);
									bfw.newLine();
									
									newOut = true;
								}
								
							}
							else{
								if(ReplaceObj!=null){
									Original = cnt+j*2;
									Replace = ReplaceObj;
								}
								else{
									Original = cnt+j*2;
									Replace = Replace_revObj+1;
								}
								
								if(newOut){
									bfw.write(Original+"\t"+Replace+"\t");
									newOut = false;
									next = true;
								}
								
								else if(Original-preOriginal==2){
									if(next){
										diff = Replace - preReplace;
										if(diff==2){
											bfw.write("+\t");
											next = false;
										}
										else if(diff==-2){
											bfw.write("-\t");
											next = false;
										}
										else{
											bfw.write("\n"+Original+"\t"+Replace+"\t");
										}
									}
									else{
										if(Replace - preReplace != diff){
											bfw.write(preOriginal+"\t"+preReplace);
											bfw.newLine();
											
											bfw.write(Original+"\t"+Replace+"\t");
											next = true;
										}
									}
								}
								
								else if(next==true){
									
									bfw.write("\n"+Original+"\t"+Replace+"\t");
								}
								
								preOriginal = Original;
								preReplace = Replace;
							}
							
						}
						
						if(!newOut && !next){
							bfw.write(preOriginal+"\t"+preReplace);
							bfw.newLine();
						}
						else if(next){
							bfw.newLine();
						}
					}
					
					nodes.clear();
					
					bfw.close();
					fw.close();
					bfr.close();
					fr.close();	
					
				}
							
			}catch(Exception E){
				System.out.println("Exception caught!");
				E.printStackTrace();
			}
			
			threadsSignal.countDown();  
			System.out.println(Thread.currentThread().getName() + "End. Remaining" + threadsSignal.getCount() + " threads");  
			
		}
	}
	
	
	private void BuildMap(int threadNum) throws Exception{
		CountDownLatch threadSignal = new CountDownLatch(threadNum);
		
		for(int i=0;i<threadNum;i++){
			Thread t = new MyThread(threadSignal);
			t.start();
		}
		threadSignal.await();
		System.out.println(Thread.currentThread().getName() + "End."); 
	}
	
	public void Run(int numThreads) throws Exception{
		long time1=0;
		
		long t1 = System.currentTimeMillis();
		System.out.println("Build Maps Begin!");	
		BuildMap(numThreads);	
		long t2 = System.currentTimeMillis();
		time1 = (t2-t1)/1000;
		System.out.println("Time used for building maps: " + time1 + " seconds!");
		
	}
	
	public static void main(String[] args){
    	
    	int k = 15, numBlocks = 256, numThreads = 1, bufferSize = 8192, hsmapCapacity = 1000000;
    	
    	if(args[0].equals("-help")){
    		System.out.print("Usage: java -jar Map.jar -k k -NB numOfBlocks [options]\n" +
	        			       "Options Available: \n" + 
	        			       "[-t numOfThreads] : (Integer) Number Of Threads. Default: 1" + "\n" +
	        			       "[-b bufferSize] : (Integer) Read/Writer Buffer Size. Default: 8192" + "\n" +
	        			       "[-c capacity] : (Integer) Hashmap Capacity. Default: 1000000" + "\n");
    		return;
    	}
    	
    	for(int i=0; i<args.length; i+=2){
    		if(args[i].equals("-k"))
    			k = new Integer(args[i+1]);
    		else if(args[i].equals("-NB"))
    			numBlocks = new Integer(args[i+1]);
    		else if(args[i].equals("-t"))
    			numThreads = new Integer(args[i+1]);
    		else if(args[i].equals("-b"))
    			bufferSize = new Integer(args[i+1]);
    		else if(args[i].equals("-c"))
    			hsmapCapacity = new Integer(args[i+1]);
    		else{
    			System.out.println("Wrong with arguments. Abort!");
    			return;
    		}
    	}
    	
		
		Map bdgraph = new Map(k, numBlocks, bufferSize, hsmapCapacity);
	
		try{
			System.out.println("Program Configuration:");
	    	System.out.print("Kmer Length: " + k + "\n" +
	    					 "# Of Blocks: " + numBlocks + "\n" +
	    					 "# Of Threads: " + numThreads + "\n" +
	    					 "R/W Buffer Size: " + bufferSize + "\n" +
	    					 "Hashmap Capacity: " + hsmapCapacity + "\n");
		
			bdgraph.Run(numThreads);	
			
		}
		catch(Exception E){
			System.out.println("Exception caught!");
			E.printStackTrace();
		}
		
	}	

}