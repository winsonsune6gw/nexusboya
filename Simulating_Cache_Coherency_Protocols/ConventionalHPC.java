package cacheCoherence;
import java.io.*;
import java.util.*;

class CacheLine{
	public int tag ; 
	public int startOffset ;
	public boolean valid ;
	public boolean dirty ; 
	public boolean empty;
	public boolean exclusive_owned ; 
	public CacheLine() {
		tag=-1;
		dirty=false;
		empty=true;
		exclusive_owned=false;
	}
	
	public int stateReturner(){
		if(valid==false ){
			return 0 ; 
		}
		if(valid==true && dirty == false && exclusive_owned == false ){
			return 1 ; 
		}
		if(valid == true && dirty == true ){
			return 2;
		}
		if(valid==true && dirty == false && exclusive_owned == true){
			return 3 ; 
		}
		
		return -1;
	}
	
	public void statModifier(int state){
		//System.out.println("Entered the stateModifier with "+ state);
		if(state == 0 ){
			this.valid = false ;  
		}
		if(state==1 ){
			this.valid=true ;this.dirty = false;this.exclusive_owned=false;
		}
		if(state==2){
			this.valid=true ;this.dirty = true;
		}
		if(state==3){
			this.exclusive_owned=true;this.valid=true ;this.dirty = false;
		}
		
	}
}


class Processor {
	
	public int id ; 
	public CacheLine cache[][] = new CacheLine[128][4];
	
	public Processor(int x) {
		this.id=x;
		for(int i =0; i < 128 ;i++){
			for(int j=0 ; j< 4; j++){
				cache[i][j]=new CacheLine();
			}
		}	
	}
}

public class ConventionalHPC {
	
	

	public static void insert(CacheLine cache[][] , String tagS , String indexS , String OffsetS){
		int tag= Integer.parseInt(tagS,2);
		int index=Integer.parseInt(indexS,2);
		int offset =Integer.parseInt(OffsetS,2);
		int count=0;
		for(int i =0;i<4;i++){
			if(cache[index][i].empty){
				cache[index][i].tag=tag;
				cache[index][i].startOffset=offset;
				cache[index][i].empty=false;
				cache[index][i].valid=true;
				//System.out.println("Inserting at"+ tag+" "+index+" "+offset);
				break;
			}
			else{
				count++;
			}
		} //end For 
		if(count == 4){
			Random rand= new Random();
			int num = rand.nextInt(4);
			cache[index][num].tag=tag;
			cache[index][num].startOffset=offset;
		}
	}
	
	public static void mainInsert(CacheLine cache[][] , String tagS , String indexS , String OffsetS
								,int state){
		int tag= Integer.parseInt(tagS,2);
		int index=Integer.parseInt(indexS,2);
		int offset =Integer.parseInt(OffsetS,2);
		int count=0;
		for(int i =0;i<4;i++){
			if(cache[index][i].empty){
				cache[index][i].tag=tag;
				cache[index][i].startOffset=offset;
				//System.out.println("Modifying State Below");
				cache[index][i].statModifier(state);
				//System.out.println("Modified State is "+ cache[index][i].stateReturner());
				cache[index][i].empty=false;
				//System.out.println("Inserting at"+ tag+" "+index+" "+offset);
				break;
			}
			else{
				count++;
			}
		} //end For 
		
		// Need For Evecting from the Cache 
		if(count == 4){
			Random rand= new Random();
			int num = rand.nextInt(4);
			cache[index][num].tag=tag;
			cache[index][num].startOffset=offset;
		}
	}
	
	
	public static int search(CacheLine cache[][] , String tagS , String indexS , String OffsetS){
		int tag= Integer.parseInt(tagS,2);
		int index=Integer.parseInt(indexS,2);
		int offset =Integer.parseInt(OffsetS,2);
		//System.out.println("searching at"+ tag+" "+index+" "+offset);
		int count=0; int associative=-1;
		for(int i =0;i<4;i++){
			if(!cache[index][i].empty && cache[index][i].tag!=-1 && cache[index][i].tag==tag 
					){
				associative=i;
				break;
			}
			else{
				count++;
			}
		} //end For 
		
		if(count == 4){
			return -1 ; 
		}
		else{
			return associative;
		}
	}
	
	public static void statePrinter(CacheLine cache[][] ,int p_id, String tagS , String indexS , String OffsetS
			){
		if(search(cache,tagS,indexS,OffsetS)>=0){
		int tag= Integer.parseInt(tagS,2);
		int index=Integer.parseInt(indexS,2);
		int offset =Integer.parseInt(OffsetS,2);
		int assosciativity = search(cache, tagS, indexS, OffsetS);
		System.out.println("Processor ID = "+ p_id+" State Of Tag : "+tag+"Index : "+ index +
				" Assosociativity: "+ assosciativity+"State Number : "+
				cache[index][assosciativity].stateReturner());}
		else{
			System.out.println("Cache Line is not present On the processor : " + p_id );
		}
	}
	
	
	public static int total_reads=0; public static int total_writes =0; static int num_Of_invalidations=0; 
	static int total_read_miss=0;static int total_write_miss=0; static int read_miss_found_neigh=0; 
	static int write_miss_found_neigh=0;
	
	private static int read_on_bus(int p_id, Processor[] twoProcess, String tagS, String indexS
												,String offsetS ) {
		int count = 0 ; 
		for(int i = 0 ; i < twoProcess.length ; i++){
			if(i != p_id ){
				if(search(twoProcess[i].cache, tagS, indexS, offsetS)>=0 ){
					//System.out.println("Entered Here");
					int ass_num = search(twoProcess[i].cache, tagS,indexS,offsetS);
					int set_index=Integer.parseInt(indexS,2);
					int state =twoProcess[i].cache[set_index][ass_num].stateReturner();
					if(state==2){
						//System.out.println("Entered Modified");
						twoProcess[i].cache[set_index][ass_num].statModifier(1);
						
						total_writes++;
					}
					if(state==0){
						//System.out.println("Enerrreed here");
						twoProcess[i].cache[set_index][ass_num].statModifier(1);
					}
					if(state==1){
						// Do Nothing 
					}
					if(state==3){
						twoProcess[i].cache[set_index][ass_num].statModifier(1);
						total_writes++;
					}
					count++;
				  
				} // End If 
			}
			
		}
		if(count >0){
			return 1 ; 
		}
		else{
			return 0 ; 
		}
		
	}
	
	private static void ivalidate_all_others(int p_id, Processor[] twoProcess, String tagS, String indexS,
			String offsetS) {
		for(int i = 0 ; i < twoProcess.length ; i++){
			if(i != p_id ){
				if(search(twoProcess[i].cache, tagS, indexS, offsetS)>=0 ){
					int ass_num = search(twoProcess[i].cache, tagS,indexS,offsetS);
					int set_index=Integer.parseInt(indexS,2);
					int state =twoProcess[i].cache[set_index][ass_num].stateReturner();
						twoProcess[i].cache[set_index][ass_num].statModifier(0);
						num_Of_invalidations++;
				} // End If 
			}
		}
		
	}
	
	private static int write_on_bus(int p_id, Processor[] twoProcess, String tagS, String indexS, String offsetS) {
		int count = 0 ; 
		for(int i = 0 ; i < twoProcess.length ; i++){
			
			if(i != p_id ){
				//System.out.println("i is not "+p_id+" i is "+ i);
				if(search(twoProcess[i].cache, tagS, indexS, offsetS)>=0 ){
					//System.out.println("Found on Processor " + i);
					int ass_num = search(twoProcess[i].cache, tagS,indexS,offsetS);
					int set_index=Integer.parseInt(indexS,2);
					int state =twoProcess[i].cache[set_index][ass_num].stateReturner();
					if(state==2){
						twoProcess[i].cache[set_index][ass_num].statModifier(0);
						total_writes++; // Since Writing Back to Memory
						num_Of_invalidations++;
					}
					if(state==1){
						twoProcess[i].cache[set_index][ass_num].statModifier(0);
						num_Of_invalidations++;
					}
					if(state==0){
						// Do Nothing 
					}
				   count++;
				} // End If 
			}
		}
		
		if(count > 0 ){
			return 1 ; 
		}
		else{
			return 0 ; 
		}
	}
	
	public static String hexToBinary(String hexTok){
		int i = Integer.parseInt(hexTok,16);
		String binaryString = Integer.toBinaryString(i);
		while (binaryString .length() < 20) {
	        binaryString  = "0" + binaryString ;
	    }
	 return binaryString;
	}
	
	
	public static void main(String[] args) {
		CacheLine modelCache[][] = new CacheLine[128][4];
		for(int i =0; i < 128 ;i++){
			for(int j=0 ; j< 4; j++){
				modelCache[i][j]=new CacheLine();
			}
		}
		
		File text1 = new File("1.txt");
		File text2=new File("2.txt");
		File text3=new File("3.txt");
		File text4=new File("4.txt");
		
		Scanner sc = null;
		Scanner sc2 = null;
		Scanner sc3 = null;
		Scanner sc4 = null;
		try {
			 sc=new Scanner(text1);
			 sc2=new Scanner(text2);
			 sc3=new Scanner(text3);
			 sc4=new Scanner(text4);
			 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line;
		String tokens []=null;
		String numberTok[] = null;
		String tagS = null;
		String indexS = null;
		String offsetS = null;
		String inst = null;
		ArrayList<String> firstTrace = new ArrayList<String>();
		ArrayList<String> secondTrace = new ArrayList<String>();
		ArrayList<String> thirdTrace = new ArrayList<String>();
		ArrayList<String> fourthTrace = new ArrayList<String>();
		while(sc.hasNextLine()){
			line=sc.nextLine();
			
			 tokens =line.split(" ");
			 numberTok=tokens[1].split("0x");
			//System.out.println(numberTok[1]);
			numberTok[1]=numberTok[1].toUpperCase();
			//System.out.println(tokens[1]+ "Converts to " + hexToBinary(numberTok[1]).length() );
			numberTok[1]=hexToBinary(numberTok[1]);
			 tagS = numberTok[1].substring(0, 8);
			 indexS=numberTok[1].substring(8, 15);
			 offsetS = numberTok[1].substring(15);
			  inst=tokens[0]+" "+numberTok[1];
			  firstTrace.add(inst);
			 
		}
		
		while(sc2.hasNextLine()){
			line=sc2.nextLine();
			
			 tokens =line.split(" ");
			 numberTok=tokens[1].split("0x");
			//System.out.println(numberTok[1]);
			numberTok[1]=numberTok[1].toUpperCase();
			//System.out.println(tokens[1]+ "Converts to " + hexToBinary(numberTok[1]).length() );
			numberTok[1]=hexToBinary(numberTok[1]);
			 tagS = numberTok[1].substring(0, 8);
			 indexS=numberTok[1].substring(8, 15);
			 offsetS = numberTok[1].substring(15);
			  inst=tokens[0]+" "+numberTok[1];
			  secondTrace.add(inst);
			  //System.out.println("Instruction from file 2 is "+inst);
		}
		
		while(sc3.hasNextLine()){
			line=sc3.nextLine();
			
			 tokens =line.split(" ");
			 numberTok=tokens[1].split("0x");
			//System.out.println(numberTok[1]);
			numberTok[1]=numberTok[1].toUpperCase();
			//System.out.println(tokens[1]+ "Converts to " + hexToBinary(numberTok[1]).length() );
			numberTok[1]=hexToBinary(numberTok[1]);
			 tagS = numberTok[1].substring(0, 8);
			 indexS=numberTok[1].substring(8, 15);
			 offsetS = numberTok[1].substring(15);
			  inst=tokens[0]+" "+numberTok[1];
			  thirdTrace.add(inst);
		}
		
		while(sc4.hasNextLine()){
			line=sc4.nextLine();
			
			 tokens =line.split(" ");
			 numberTok=tokens[1].split("0x");
			//System.out.println(numberTok[1]);
			numberTok[1]=numberTok[1].toUpperCase();
			//System.out.println(tokens[1]+ "Converts to " + hexToBinary(numberTok[1]).length() );
			numberTok[1]=hexToBinary(numberTok[1]);
			 tagS = numberTok[1].substring(0, 8);
			 indexS=numberTok[1].substring(8, 15);
			 offsetS = numberTok[1].substring(15);
			  inst=tokens[0]+" "+numberTok[1];
			  fourthTrace.add(inst);
		}
		
		
		 System.out.println("----------------------------------------------For 1 Processor-------------------------------------------------------------------------");
		 total_reads=0;  total_writes =0;  num_Of_invalidations=0; 
		 total_read_miss=0;total_write_miss=0;  read_miss_found_neigh=0; 
		 write_miss_found_neigh=0;
		 Processor oneProcess[]=new Processor[1];
			oneProcess[0]=new Processor(0);
			
			
				
			for(int i = 0 ; i < 200000 ; i ++ )
			{	
				// Process For P1
				String tempTokens_p1[] = firstTrace.get(i).split(" ");
				char instruc_p1 = tempTokens_p1[0].charAt(0);
				
				//System.out.println(instruc_p1);
				
				String memAdd_p1 = tempTokens_p1[1];
				 tagS = memAdd_p1.substring(0, 8);
				 indexS=memAdd_p1.substring(8, 15);
				 offsetS = memAdd_p1.substring(15);
				 if(instruc_p1=='R'){
					if(search(oneProcess[0].cache, tagS,indexS,offsetS)>=0){
						int ass_num = search(oneProcess[0].cache, tagS,indexS,offsetS);
						int set_index=Integer.parseInt(indexS,2);
						int state =oneProcess[0].cache[set_index][ass_num].stateReturner();
						if(state==0){
							oneProcess[0].cache[set_index][ass_num].statModifier(1);
							read_on_bus(0, oneProcess, tagS, indexS, offsetS);
						}
						if(state == 1){
							oneProcess[0].cache[set_index][ass_num].statModifier(1);
						}
						if(state==2){
							// Do Nothing 
						}
						if(state==3){
							//Do Nothing
						}
						total_reads++;
					}
					else{  //Not Found in the processor Cache -> Check in the Neighbors cache -> Check in MainMem
						int val =0;
						val=read_on_bus(0,oneProcess,tagS,indexS,offsetS);
						if(val==1){
							mainInsert(oneProcess[0].cache, tagS, indexS, offsetS,1);
							read_miss_found_neigh++;
						}
						//Finally Insert Into the Processor Cache 
						else{
						mainInsert(oneProcess[0].cache, tagS, indexS, offsetS,3);
						}
						total_read_miss++;
						total_reads++;
					}
				 }
				 if(instruc_p1=='W'){
					 	
						if(search(oneProcess[0].cache, tagS,indexS,offsetS)>=0){
							int ass_num = search(oneProcess[0].cache, tagS,indexS,offsetS);
							int set_index=Integer.parseInt(indexS,2);
							int state =oneProcess[0].cache[set_index][ass_num].stateReturner();
							if(state==2){
								//Do Nothing Remain in the Same State 
							}
							if(state ==0){
								//Snoop the Data If Present from other caches write Back
								//write_req On Bus does 
								
								int val = 0 ; 
								val=write_on_bus(0,oneProcess,tagS,indexS,offsetS);
								if(val==1){
									write_miss_found_neigh++;
								}
								
								oneProcess[0].cache[set_index][ass_num].statModifier(2);
							}
							if(state == 1){
								ivalidate_all_others(0,oneProcess,tagS , indexS,offsetS);
								oneProcess[0].cache[set_index][ass_num].statModifier(2);
							}
							if(state==3){
								oneProcess[0].cache[set_index][ass_num].statModifier(2);
							}
							total_writes++;
							//insert(p1.cache, tagS, indexS, offsetS);
						}
						else{
							int val = 0 ; 
							val=write_on_bus(0,oneProcess,tagS,indexS,offsetS);
							if(val==1){
								write_miss_found_neigh++;
							}
							mainInsert(oneProcess[0].cache, tagS, indexS, offsetS,2);
							total_write_miss++;
							total_writes++;
						}
						
				 }
				 
				 /*statePrinter(oneProcess[0].cache, 0, tagS, indexS, offsetS);
				 statePrinter(oneProcess[1].cache,1, tagS, indexS, offsetS);*/
				 
				
					 
				 
			}

			System.out.println("Total Number Of Reads = " + total_reads);
			System.out.println("Total Number Of Writes = " + total_writes);
			System.out.println("Total Number Of Write Misses = "+ total_write_miss);
			System.out.println("Total Number Of Read Misses = "+ total_read_miss);
			System.out.println("Total Number Of Read Miss Found in Neighbourhood Cache = " + read_miss_found_neigh);
			System.out.println("Total Number Of Read Miss Found in Neighbourhood Cache = " + write_miss_found_neigh);
			
			System.out.println("----------------------------------------------End of 1 Processor-------------------------------------------------------------------------");
			System.out.println("\n\n\n");
		
		 total_reads=0;  total_writes =0;  num_Of_invalidations=0; 
		 total_read_miss=0;total_write_miss=0;  read_miss_found_neigh=0; 
		 write_miss_found_neigh=0;
		System.out.println("----------------------------------------------For 2 Processors-------------------------------------------------------------------------");
		
		Processor twoProcess[]=new Processor[2];
		twoProcess[0]=new Processor(0);
		twoProcess[1]=new Processor(1);
		
			
		for(int i = 0 ; i < 200000 ; i ++ )
		{	
			// Process For P1
			String tempTokens_p1[] = firstTrace.get(i).split(" ");
			char instruc_p1 = tempTokens_p1[0].charAt(0);
			
			//System.out.println(instruc_p1);
			
			String memAdd_p1 = tempTokens_p1[1];
			 tagS = memAdd_p1.substring(0, 8);
			 indexS=memAdd_p1.substring(8, 15);
			 offsetS = memAdd_p1.substring(15);
			 if(instruc_p1=='R'){
				if(search(twoProcess[0].cache, tagS,indexS,offsetS)>=0){
					int ass_num = search(twoProcess[0].cache, tagS,indexS,offsetS);
					int set_index=Integer.parseInt(indexS,2);
					int state =twoProcess[0].cache[set_index][ass_num].stateReturner();
					if(state==0){
						twoProcess[0].cache[set_index][ass_num].statModifier(1);
						read_on_bus(0, twoProcess, tagS, indexS, offsetS);
					}
					if(state == 1){
						twoProcess[0].cache[set_index][ass_num].statModifier(1);
					}
					if(state==2){
						// Do Nothing 
					}
					total_reads++;
				}
				else{  //Not Found in the processor Cache -> Check in the Neighbors cache -> Check in MainMem
					int val =0;
					val=read_on_bus(0,twoProcess,tagS,indexS,offsetS);
					if(val==1){
						mainInsert(twoProcess[0].cache, tagS, indexS, offsetS,1);
						read_miss_found_neigh++;
					}
					//Finally Insert Into the Processor Cache 
					else{
					mainInsert(twoProcess[0].cache, tagS, indexS, offsetS,3);
					}
					total_read_miss++;
					total_reads++;
				}
			 }
			 if(instruc_p1=='W'){
				 	
					if(search(twoProcess[0].cache, tagS,indexS,offsetS)>=0){
						int ass_num = search(twoProcess[0].cache, tagS,indexS,offsetS);
						int set_index=Integer.parseInt(indexS,2);
						int state =twoProcess[0].cache[set_index][ass_num].stateReturner();
						if(state==2){
							//Do Nothing Remain in the Same State 
						}
						if(state ==0){
							//Snoop the Data If Present from other caches write Back
							//write_req On Bus does 
							
							int val = 0 ; 
							val=write_on_bus(0,twoProcess,tagS,indexS,offsetS);
							if(val==1){
								write_miss_found_neigh++;
							}
							
							twoProcess[0].cache[set_index][ass_num].statModifier(2);
						}
						if(state == 1){
							ivalidate_all_others(0,twoProcess,tagS , indexS,offsetS);
							twoProcess[0].cache[set_index][ass_num].statModifier(2);
						}
						if(state==3){
							twoProcess[0].cache[set_index][ass_num].statModifier(2);
						}
						total_writes++;
						//insert(p1.cache, tagS, indexS, offsetS);
					}
					else{
						int val = 0 ; 
						val=write_on_bus(0,twoProcess,tagS,indexS,offsetS);
						if(val==1){
							write_miss_found_neigh++;
						}
						mainInsert(twoProcess[0].cache, tagS, indexS, offsetS,2);
						total_write_miss++;
						total_writes++;
					}
					
			 }
			 
			 /*statePrinter(twoProcess[0].cache, 0, tagS, indexS, offsetS);
			 statePrinter(twoProcess[1].cache,1, tagS, indexS, offsetS);*/
			 
			// End Of Processor 1 
			 
			 String tempTokens_p2[] = secondTrace.get(i).split(" ");
				char instruc_p2 = tempTokens_p2[0].charAt(0);
				//System.out.println(instruc_p2);
				String memAdd_p2 = tempTokens_p2[1];
				 tagS = memAdd_p2.substring(0, 8);
				 indexS=memAdd_p2.substring(8, 15);
				 offsetS = memAdd_p2.substring(15);
				// System.out.println(instruc_p2);
				 if(instruc_p2=='R'){
					if(search(twoProcess[1].cache, tagS,indexS,offsetS)>=0){
						int ass_num = search(twoProcess[1].cache, tagS,indexS,offsetS);
						int set_index=Integer.parseInt(indexS,2);
						int state =twoProcess[1].cache[set_index][ass_num].stateReturner();
						if(state==0){
							twoProcess[1].cache[set_index][ass_num].statModifier(1);
							read_on_bus(1, twoProcess, tagS, indexS, offsetS);
						}
						if(state == 1){
							twoProcess[1].cache[set_index][ass_num].statModifier(1);
						}
						if(state==2){
							// Do Nothing 
						}
						total_reads++;
					}
					else{  //Not Found in the processor Cache -> Check in the Neighbors cache -> Check in MainMem
						int val=0;
						 val= read_on_bus(1,twoProcess,tagS,indexS,offsetS);
						 if(val==1){
								mainInsert(twoProcess[1].cache, tagS, indexS, offsetS,1);
								read_miss_found_neigh++;
							}
							//Finally Insert Into the Processor Cache 
							else{
							mainInsert(twoProcess[1].cache, tagS, indexS, offsetS,3);
							}
						total_read_miss++;
						total_reads++;
					}
				 }
				 if(instruc_p2=='W'){
						if(search(twoProcess[1].cache, tagS,indexS,offsetS)>=0){
							int ass_num = search(twoProcess[1].cache, tagS,indexS,offsetS);
							
							int set_index=Integer.parseInt(indexS,2);
							int state =twoProcess[1].cache[set_index][ass_num].stateReturner();
							if(state==2){
								//Do Nothing Remain in the Same State 
							}
							if(state ==0){
								//Invalidate All Others 
								int val = 0 ; 
								val=write_on_bus(1,twoProcess,tagS,indexS,offsetS);
								if(val==1){
									write_miss_found_neigh++;
								}
								twoProcess[1].cache[set_index][ass_num].statModifier(2);
							}
							if(state == 1){
								ivalidate_all_others(1,twoProcess,tagS , indexS,offsetS);
								twoProcess[1].cache[set_index][ass_num].statModifier(2);
							}
							if(state==3){
								twoProcess[1].cache[set_index][ass_num].statModifier(2);
							}
							total_writes++;
							//insert(p1.cache, tagS, indexS, offsetS);
						}
						else{
							int val=0 ; 
							val=write_on_bus(1,twoProcess,tagS,indexS,offsetS);
							if(val==1){
								write_miss_found_neigh++;
							}
							mainInsert(twoProcess[1].cache, tagS, indexS, offsetS,2);
							//System.out.println("Checking at"+search(twoProcess[1].cache, tagS,indexS,offsetS));
							total_write_miss++;
							total_writes++;
						}
						
				 }
				 
				 /*statePrinter(twoProcess[0].cache,0, tagS, indexS, offsetS);
				 statePrinter(twoProcess[1].cache,1, tagS, indexS, offsetS);*/
				 
			 
		}// end for

		System.out.println("Total Number Of Reads = " + total_reads);
		System.out.println("Total Number Of Writes = " + total_writes);
		System.out.println("Total Number Of Write Misses = "+ total_write_miss);
		System.out.println("Total Number Of Read Misses = "+ total_read_miss);
		System.out.println("Total Number Of Read Miss Found in Neighbourhood Cache = " + read_miss_found_neigh);
		System.out.println("Total Number Of Read Miss Found in Neighbourhood Cache = " + write_miss_found_neigh);
		
		
		//System.out.println(total_read_miss+" "+total_write_miss+" "+num_Of_invalidations);
		System.out.println("----------------------------------------------End Of 2 Processors-------------------------------------------------------------------------");
		System.out.println();System.out.println();System.out.println();
		 total_reads=0;  total_writes =0;  num_Of_invalidations=0; 
		 total_read_miss=0;total_write_miss=0;  read_miss_found_neigh=0; 
		 write_miss_found_neigh=0;
		 
		 System.out.println("----------------------------------------------For 4 Processors-------------------------------------------------------------------------");
		// Start for Four Processors 
		
				Processor fourProcess[]=new Processor[4];
				fourProcess[0]=new Processor(0);
				fourProcess[1]=new Processor(1);
				fourProcess[2]=new Processor(2);
				fourProcess[3]=new Processor(3);
					
				for(int i = 0 ; i < 200000 ; i ++ )
				{	
					// Process For P1
					String tempTokens_p1[] = firstTrace.get(i).split(" ");
					char instruc_p1 = tempTokens_p1[0].charAt(0);
					
					//System.out.println(instruc_p1);
					
					String memAdd_p1 = tempTokens_p1[1];
					 tagS = memAdd_p1.substring(0, 8);
					 indexS=memAdd_p1.substring(8, 15);
					 offsetS = memAdd_p1.substring(15);
					 if(instruc_p1=='R'){
						if(search(fourProcess[0].cache, tagS,indexS,offsetS)>=0){
							int ass_num = search(fourProcess[0].cache, tagS,indexS,offsetS);
							int set_index=Integer.parseInt(indexS,2);
							int state =fourProcess[0].cache[set_index][ass_num].stateReturner();
							if(state==0){
								fourProcess[0].cache[set_index][ass_num].statModifier(1);
								read_on_bus(0, fourProcess, tagS, indexS, offsetS);
							}
							if(state == 1){
								fourProcess[0].cache[set_index][ass_num].statModifier(1);
							}
							if(state==2){
								// Do Nothing 
							}
							total_reads++;
						}
						else{  //Not Found in the processor Cache -> Check in the Neighbors cache -> Check in MainMem
							int val =0;
							val=read_on_bus(0,fourProcess,tagS,indexS,offsetS);
							if(val==1){
								mainInsert(fourProcess[0].cache, tagS, indexS, offsetS,1);
								read_miss_found_neigh++;
							}
							//Finally Insert Into the Processor Cache 
							else{
							mainInsert(fourProcess[0].cache, tagS, indexS, offsetS,3);
							}
							total_read_miss++;
							total_reads++;
						}
					 }
					 if(instruc_p1=='W'){
						 	
							if(search(fourProcess[0].cache, tagS,indexS,offsetS)>=0){
								int ass_num = search(fourProcess[0].cache, tagS,indexS,offsetS);
								int set_index=Integer.parseInt(indexS,2);
								int state =fourProcess[0].cache[set_index][ass_num].stateReturner();
								if(state==2){
									//Do Nothing Remain in the Same State 
								}
								if(state ==0){
									//Snoop the Data If Present from other caches write Back
									//write_req On Bus does 
									
									int val = 0 ; 
									val=write_on_bus(0,fourProcess,tagS,indexS,offsetS);
									if(val==1){
										write_miss_found_neigh++;
									}
									
									fourProcess[0].cache[set_index][ass_num].statModifier(2);
								}
								if(state == 1){
									ivalidate_all_others(0,fourProcess,tagS , indexS,offsetS);
									fourProcess[0].cache[set_index][ass_num].statModifier(2);
								}
								if(state==3){
									fourProcess[0].cache[set_index][ass_num].statModifier(2);
								}
								total_writes++;
								//insert(p1.cache, tagS, indexS, offsetS);
							}
							else{
								int val = 0 ; 
								val=write_on_bus(0,fourProcess,tagS,indexS,offsetS);
								if(val==1){
									write_miss_found_neigh++;
								}
								mainInsert(fourProcess[0].cache, tagS, indexS, offsetS,2);
								total_write_miss++;
								total_writes++;
							}
							
					 }
					 /*
					 statePrinter(fourProcess[0].cache, 0, tagS, indexS, offsetS);
					 statePrinter(fourProcess[1].cache,1, tagS, indexS, offsetS);
					 */
					// End Of Processor 1 
					 
					 String tempTokens_p2[] = secondTrace.get(i).split(" ");
						char instruc_p2 = tempTokens_p2[0].charAt(0);
						//System.out.println(instruc_p2);
						String memAdd_p2 = tempTokens_p2[1];
						 tagS = memAdd_p2.substring(0, 8);
						 indexS=memAdd_p2.substring(8, 15);
						 offsetS = memAdd_p2.substring(15);
						// System.out.println(instruc_p2);
						 if(instruc_p2=='R'){
							if(search(fourProcess[1].cache, tagS,indexS,offsetS)>=0){
								int ass_num = search(fourProcess[1].cache, tagS,indexS,offsetS);
								int set_index=Integer.parseInt(indexS,2);
								int state =fourProcess[1].cache[set_index][ass_num].stateReturner();
								if(state==0){
									fourProcess[1].cache[set_index][ass_num].statModifier(1);
									read_on_bus(1, fourProcess, tagS, indexS, offsetS);
								}
								if(state == 1){
									fourProcess[1].cache[set_index][ass_num].statModifier(1);
								}
								if(state==2){
									// Do Nothing 
								}
								
								total_reads++;
							}
							else{  //Not Found in the processor Cache -> Check in the Neighbors cache -> Check in MainMem
								int val=0;
								 val= read_on_bus(1,fourProcess,tagS,indexS,offsetS);
								 if(val==1){
										mainInsert(fourProcess[1].cache, tagS, indexS, offsetS,1);
										read_miss_found_neigh++;
									}
									//Finally Insert Into the Processor Cache 
									else{
									mainInsert(fourProcess[1].cache, tagS, indexS, offsetS,3);
									}
								total_read_miss++;
								total_reads++;
							}
						 }
						 if(instruc_p2=='W'){
								if(search(fourProcess[1].cache, tagS,indexS,offsetS)>=0){
									int ass_num = search(fourProcess[1].cache, tagS,indexS,offsetS);
									
									int set_index=Integer.parseInt(indexS,2);
									int state =fourProcess[1].cache[set_index][ass_num].stateReturner();
									if(state==2){
										//Do Nothing Remain in the Same State 
									}
									if(state ==0){
										//Invalidate All Others 
										int val = 0 ; 
										val=write_on_bus(1,fourProcess,tagS,indexS,offsetS);
										if(val==1){
											write_miss_found_neigh++;
										}
										fourProcess[1].cache[set_index][ass_num].statModifier(2);
									}
									if(state == 1){
										ivalidate_all_others(1,fourProcess,tagS , indexS,offsetS);
										fourProcess[1].cache[set_index][ass_num].statModifier(2);
									}
									if(state==3){
										fourProcess[1].cache[set_index][ass_num].statModifier(2);
									}
									total_writes++;
									//insert(p1.cache, tagS, indexS, offsetS);
								}
								else{
									int val=0 ; 
									val=write_on_bus(1,fourProcess,tagS,indexS,offsetS);
									if(val==1){
										write_miss_found_neigh++;
									}
									mainInsert(fourProcess[1].cache, tagS, indexS, offsetS,2);
									//System.out.println("Checking at"+search(fourProcess[1].cache, tagS,indexS,offsetS));
									total_write_miss++;
									total_writes++;
								}
								
						 }
						 /*
						 statePrinter(fourProcess[0].cache,0, tagS, indexS, offsetS);
						 statePrinter(fourProcess[1].cache,1, tagS, indexS, offsetS);
						 */
						 // Start Of processor 3
						  String tempTokens_p3[] = thirdTrace.get(i).split(" ");
						char instruc_p3 = tempTokens_p3[0].charAt(0);
						//System.out.println(instruc_p3);
						String memAdd_p3 = tempTokens_p3[1];
						 tagS = memAdd_p3.substring(0, 8);
						 indexS=memAdd_p3.substring(8, 15);
						 offsetS = memAdd_p3.substring(15);
						// System.out.println(instruc_p3);
						 if(instruc_p3=='R'){
							if(search(fourProcess[2].cache, tagS,indexS,offsetS)>=0){
								int ass_num = search(fourProcess[2].cache, tagS,indexS,offsetS);
								int set_index=Integer.parseInt(indexS,2);
								int state =fourProcess[2].cache[set_index][ass_num].stateReturner();
								if(state==0){
									fourProcess[2].cache[set_index][ass_num].statModifier(1);
									read_on_bus(2, fourProcess, tagS, indexS, offsetS);
								}
								if(state == 1){
									fourProcess[2].cache[set_index][ass_num].statModifier(1);
								}
								if(state==2){
									// Do Nothing 
								}
								total_reads++;
							}
							else{  //Not Found in the processor Cache -> Check in the Neighbors cache -> Check in MainMem
								int val=0;
								 val= read_on_bus(2,fourProcess,tagS,indexS,offsetS);
								 if(val==1){
										mainInsert(fourProcess[2].cache, tagS, indexS, offsetS,1);
										read_miss_found_neigh++;
									}
									//Finally Insert Into the Processor Cache 
									else{
									mainInsert(fourProcess[2].cache, tagS, indexS, offsetS,3);
									}
								total_read_miss++;
								total_reads++;
							}
						 }
						 if(instruc_p3=='W'){
								if(search(fourProcess[2].cache, tagS,indexS,offsetS)>=0){
									int ass_num = search(fourProcess[2].cache, tagS,indexS,offsetS);
									
									int set_index=Integer.parseInt(indexS,2);
									int state =fourProcess[2].cache[set_index][ass_num].stateReturner();
									if(state==2){
										//Do Nothing Remain in the Same State 
									}
									if(state ==0){
										//Invalidate All Others 
										int val = 0 ; 
										val=write_on_bus(2,fourProcess,tagS,indexS,offsetS);
										if(val==1){
											write_miss_found_neigh++;
										}
										fourProcess[2].cache[set_index][ass_num].statModifier(2);
									}
									if(state == 1){
										ivalidate_all_others(2,fourProcess,tagS , indexS,offsetS);
										fourProcess[2].cache[set_index][ass_num].statModifier(2);
									}
									if(state==3){
										fourProcess[2].cache[set_index][ass_num].statModifier(2);
									}
									total_writes++;
									//insert(p1.cache, tagS, indexS, offsetS);
								}
								else{
									int val=0 ; 
									val=write_on_bus(2,fourProcess,tagS,indexS,offsetS);
									if(val==1){
										write_miss_found_neigh++;
									}
									mainInsert(fourProcess[2].cache, tagS, indexS, offsetS,2);
									//System.out.println("Checking at"+search(fourProcess[2].cache, tagS,indexS,offsetS));
									total_write_miss++;
									total_writes++;
								}
								
						 }
						 // End Of Processor 3 
						 
						 // Start Of Processor 4
						  String tempTokens_p4[] = fourthTrace.get(i).split(" ");
						char instruc_p4 = tempTokens_p4[0].charAt(0);
						//System.out.println(instruc_p4);
						String memAdd_p4 = tempTokens_p4[1];
						 tagS = memAdd_p4.substring(0, 8);
						 indexS=memAdd_p4.substring(8, 15);
						 offsetS = memAdd_p4.substring(15);
						// System.out.println(instruc_p4);
						 if(instruc_p4=='R'){
							if(search(fourProcess[3].cache, tagS,indexS,offsetS)>=0){
								int ass_num = search(fourProcess[3].cache, tagS,indexS,offsetS);
								int set_index=Integer.parseInt(indexS,2);
								int state =fourProcess[3].cache[set_index][ass_num].stateReturner();
								if(state==0){
									fourProcess[3].cache[set_index][ass_num].statModifier(1);
									read_on_bus(3, fourProcess, tagS, indexS, offsetS);
								}
								if(state == 1){
									fourProcess[3].cache[set_index][ass_num].statModifier(1);
								}
								if(state==2){
									// Do Nothing 
								}
								total_reads++;
							}
							else{  //Not Found in the processor Cache -> Check in the Neighbors cache -> Check in MainMem
								int val=0;
								 val= read_on_bus(3,fourProcess,tagS,indexS,offsetS);
								 if(val==1){
										mainInsert(fourProcess[3].cache, tagS, indexS, offsetS,1);
										read_miss_found_neigh++;
									}
									//Finally Insert Into the Processor Cache 
									else{
									mainInsert(fourProcess[3].cache, tagS, indexS, offsetS,3);
									}
								total_read_miss++;
								total_reads++;
							}
						 }
						 if(instruc_p4=='W'){
								if(search(fourProcess[3].cache, tagS,indexS,offsetS)>=0){
									int ass_num = search(fourProcess[3].cache, tagS,indexS,offsetS);
									
									int set_index=Integer.parseInt(indexS,2);
									int state =fourProcess[3].cache[set_index][ass_num].stateReturner();
									if(state==2){
										//Do Nothing Remain in the Same State 
									}
									if(state ==0){
										//Invalidate All Others 
										int val = 0 ; 
										val=write_on_bus(3,fourProcess,tagS,indexS,offsetS);
										if(val==1){
											write_miss_found_neigh++;
										}
										fourProcess[3].cache[set_index][ass_num].statModifier(2);
									}
									if(state == 1){
										ivalidate_all_others(2,fourProcess,tagS , indexS,offsetS);
										fourProcess[3].cache[set_index][ass_num].statModifier(2);
									}
									if(state==3){
										fourProcess[3].cache[set_index][ass_num].statModifier(2);
									}
									total_writes++;
									//insert(p1.cache, tagS, indexS, offsetS);
								}
								else{
									int val=0 ; 
									val=write_on_bus(3,fourProcess,tagS,indexS,offsetS);
									if(val==1){
										write_miss_found_neigh++;
									}
									mainInsert(fourProcess[3].cache, tagS, indexS, offsetS,2);
									//System.out.println("Checking at"+search(fourProcess[3].cache, tagS,indexS,offsetS));
									total_write_miss++;
									total_writes++;
								}
								
						 }
					
					
					 
				}// end for 
				System.out.println("Total Number Of Reads = " + total_reads);
				System.out.println("Total Number Of Writes = " + total_writes);
				System.out.println("Total Number Of Write Misses = "+ total_write_miss);
				System.out.println("Total Number Of Read Misses = "+ total_read_miss);
				System.out.println("Total Number Of Read Miss Found in Neighbourhood Cache = " + read_miss_found_neigh);
				System.out.println("Total Number Of Read Miss Found in Neighbourhood Cache = " + write_miss_found_neigh);
				
				// END for Four Processors 
				System.out.println("----------------------------------------------End Of 4 Processors-------------------------------------------------------------------------");
				
		} // End Main

}
