
public class Kmer64 extends Object{
	
	public long high;
	public long low;
	
	private final static char[] baseDic = {'A','C','G','T'};
	private final static int[] intDic = {0,-1,1,-1,-1,-1,2,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,3};  
	
	private final int base2int(char base){
		return intDic[base-'A'];
	}
	
	private final char int2base(int intv){
		return baseDic[intv];
	}
	
	public Kmer64(char[] str, boolean rev){
		
		this.high = this.low = 0;
		
		int len = str.length;
		
		if(!rev){
			if(len <= 32){
				for(int i=0; i<=len-1; i++){
					this.low = (this.low<<2) + base2int(str[i]);
				}
			}
			else{
				for(int i=len-32; i<=len-1; i++){
					this.low = (this.low<<2) + base2int(str[i]);
				}
				
				for(int i=0; i<=len-33; i++){
					this.high = (this.high<<2) + base2int(str[i]);
				}
			}
		}
		else{
			if(len <= 32){
				for(int i=len-1; i>=0; i--){
					this.low = (this.low<<2) + 3^base2int(str[i]);
				}
			}
			else{
				for(int i=31; i>=0; i--){
					this.low = (this.low<<2) + 3^base2int(str[i]);
				}
				
				for(int i=len-1; i>=32; i--){
					this.high = (this.high<<2) + 3^base2int(str[i]);
				}
			}
		}
		
	}
	
	public Kmer64(char[] str, int start, int end, boolean rev){
		
		this.high = this.low = 0;
		
		int len = end - start;
		
		if(!rev){
			if(len <= 32){
				for(int i=start; i<=end-1; i++){
					this.low = (this.low<<2) + base2int(str[i]);
				}
			}
			else{
				for(int i=end-32; i<=end-1; i++){
					this.low = (this.low<<2) + base2int(str[i]);
				}
				
				for(int i=start; i<=end-33; i++){
					this.high = (this.high<<2) + base2int(str[i]);
				}
			}
		}
		else{
			if(len <= 32){
				for(int i=end-1; i>=start; i--){
					this.low = (this.low<<2) + 3^base2int(str[i]);
				}
			}
			else{
				for(int i=start+31; i>=start; i--){
					this.low = (this.low<<2) + 3^base2int(str[i]);
				}
				
				for(int i=end-1; i>=start+32; i--){
					this.high = (this.high<<2) + 3^base2int(str[i]);
				}
			}
		}
		
	}
	
	public Kmer64(long low, long high){
		this.low = low;
		this.high = high;
	}
	
	@Override
	public boolean equals(Object another){
		Kmer64 k = (Kmer64)another;
		if(this.high == k.high && this.low == k.low)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return (int)((low^(low>>>32))^(high^(high>>>32)));
	}	
	
	public static boolean KmerSmaller(Kmer64 kmer1, Kmer64 kmer2)
	{
		if ( kmer1.high < kmer2.high )
			return true; 
		else if ( kmer1.high == kmer2.high )
		{
			if ( kmer1.low < kmer2.low )
				return true;
			else
				return false; 
		}
		else
			return false; 
	}
	
	public Kmer64 getTwinKmer(int k){
		
		return new Kmer64(this.toDNA(k),true);

	}
	
	public String toString(){
		return this.high+","+this.low;
	}
	
	public String toHexString(){
		return Long.toHexString(this.high)+" "+Long.toHexString(this.low);
	}
	
	public String toBinString(){
		return Long.toBinaryString(this.high)+" "+Long.toBinaryString(this.low);
	}
	
	public char[] toDNA(int len){
		int i, bit1, bit2;
		char ch;
		char kmerSeq[] = new char[len];
		
		bit2 = len > 32 ? 32 : len;
		bit1 = len > 32 ? len - 32 : 0;
		
		long tempLow = this.low;
		long tempHigh = this.high;

		for ( i = bit1 - 1; i >= 0; i-- )
		{
			ch = int2base((int)(tempHigh & 0x3));
			tempHigh >>= 2;
			kmerSeq[i] = ch;
		}

		for ( i = bit2 - 1; i >= 0; i-- )
		{
			ch = int2base((int)(tempLow & 0x3));
			tempLow >>= 2;
			kmerSeq[i + bit1] = ch;
		}
		
		return kmerSeq;
	}
	
	public String toDNAStr(int len){
		int i, bit1, bit2;
		char ch;
		char kmerSeq[] = new char[len];
		
		bit2 = len > 32 ? 32 : len;
		bit1 = len > 32 ? len - 32 : 0;
		
		long tempLow = this.low;
		long tempHigh = this.high;

		for ( i = bit1 - 1; i >= 0; i-- )
		{
			ch = int2base((int)(tempHigh & 0x3));
			tempHigh >>= 2;
			kmerSeq[i] = ch;
		}

		for ( i = bit2 - 1; i >= 0; i-- )
		{
			ch = int2base((int)(tempLow & 0x3));
			tempLow >>= 2;
			kmerSeq[i + bit1] = ch;
		}
		
		return new String(kmerSeq);
	}
	
	private static long HexToLong(String str){
		char[] reverse = {'f','e','d','c','b','a','9','8','7','6',0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,'5','4','3','2','1','0'};
		
		if(str.length()==16 && str.charAt(0)>'7'){
			char[] temp = new char[16];
			
			char[] input = str.toCharArray();
			
			for(int i = 0 ; i < 16; i ++)
				temp[i] = reverse[input[i]-'0'];
			
			return -(Long.parseLong(new String(temp), 16) + 1);
		}
		else
			return Long.parseLong(str,16);
	}
	
	public static void main(String[] args){
		
		Kmer64 k = new Kmer64("AAAAGCCGATAAAATCGGTTAGGAAACAATTATTAAAAAAGAAAAGAATGTTAAGTAG".toCharArray(), 2, 26, false);
		System.out.println(k.high+"\t"+k.low);
		System.out.println(k.toHexString());
		System.out.println(k.toDNA(24));
		
		//System.out.println(k.getTwinKmer(54));
		
		Kmer64 k_rev = new Kmer64("AAAAGCCGATAAAATCGGTTAGGAAACAATTATTAAAAAAGAAAAGAATGTTAAGTAG".toCharArray(), 2, 56,  true);
		System.out.println(k_rev.high+"\t"+k_rev.low);
		System.out.println("Rev: " + Long.toBinaryString(k_rev.low)+","+Long.toBinaryString(k_rev.high));
		System.out.println(k_rev.toDNA(54));
		
		Kmer64 k_small = new Kmer64("AAAAAAGAAAAAAAAAAAAAAAAAAAGAAAAAAAAAAACAAAAAACAGAGAAAAGTAGA".toCharArray(), false);
		System.out.println(k_small.high+"\t"+k_small.low);
		System.out.println(k_small.toBinString());
		System.out.println(k_small.toDNA(59));
		
		Kmer64 rev_k = new Kmer64("CTACTTAACATTCTTTTCTTTTTTAATAATTGTTTCCTAACCGATTTTATCGGCTTTTC".toCharArray(), false);
		System.out.println(rev_k.high+"\t"+rev_k.low);
		
		
		System.out.println(Long.toHexString(HexToLong("a5e430cfcc800000")));
			
	}

}