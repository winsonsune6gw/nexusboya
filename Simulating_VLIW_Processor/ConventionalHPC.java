
import java.util.*;

import java.io.*;

 
class INSTRUCTION{
	public String opcode;
	public  int   source1;
	public int asr1;
	public int lsr1;
	public int lsl1;
	public int asr2;
	public int lsr2;
	public int lsl2;
	public int source2;
	public int imd1;
	public int imd2;
	public int destin;
	public int check ;
	public int position ; 
	public int stage ;
	public INSTRUCTION(String op,int dest,int so1 , int so2 , int immediate1,int immediate2,int ilsr1,int ilsr2,int iasr1,int iasr2,int ilsl1,int ilsl2,int pos) {
		opcode=op;
		destin=dest;
		source1=so1;
		source2=so2;
		imd1=immediate1;
		imd2=immediate2;
	    asr1=iasr1;
	    asr2=iasr2;
	    lsr1=ilsr1;
	    lsr2=ilsr2;
	    lsl1=ilsl1;
	    lsl2=ilsl2;
	    position=pos;
	}
	
	public INSTRUCTION(int checkSum,int pos ){
		opcode="DUMMY OPCODE";
		check=checkSum;
		position=pos;
		destin=-1;
		source1=-1;
		source2=-1;
	}
	
	public INSTRUCTION(int st)
	{	opcode="DUMMY OPCODE";
		stage = st ; 
		destin=-1;
		source1=-1;
		source2=-1;
	}
	public INSTRUCTION()
	{
		opcode="DUMMY OPCODE";
		destin=-1;
		source1=-1;
		source2=-1;
	}
}

public class ConventionalHPC {
	
	public static int valueReturner (String s ){
		if(s.equals("sp")){
			return 13;
		}
		else if (s.equals("fp"))
			{return 11;}
		else if (s.equals("pc")) {
			return 15;
		}
		else if (s.equals("lr")) {
			return 14;
		}
		else {
			return -1 ;
		}
	}
	
	public static int load_number =0; public static int num_of_bundles=0;
	public static int bundle_efficiency_25= 0 ; public static int bundle_efficiency_50=0; 
	public static int bundle_efficiency_75 =0 ; public static int bundle_efficiency_100=0;
	public static void main(String [] args){
		ArrayList <String> lolc = new ArrayList<String>();
		Scanner myScan = new Scanner(System.in);
		File text = new File("basic_math.txt");
		Scanner sc = null;
		try {
			 sc=new Scanner(text);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ArrayList<INSTRUCTION> inss=new ArrayList<INSTRUCTION>();
		String line = null ; 
		int line_number=0;
		try{
		
		while (sc.hasNextLine()) {
			line=sc.nextLine(); 
			line_number++;
			String [] tokens = line.split("[:]+");
			if(tokens.length>=3){
			tokens[3] = tokens[3].trim();
			String [] instruction= tokens[3].split("[\\s,]+");
			String opcode= instruction[0];
			int destination=0;
			int immediateAt1 =-1;
			int immediateAt2=-1;
			int source1=-1;
			int source2 = -1 ; 
			int lsr1=-1; int lsr2=-1;
			int asr1=-1; int asr2=-1;
			int lsl1=-1; int lsl2=-1;
			if(instruction.length>=2){
				if(instruction[1].charAt(0)=='r'){
					destination=Integer.parseInt(instruction[1].substring(1));
					//System.out.println(destination);
					}
				if(instruction[1].equals("sp")){
					destination=13;
					//System.out.println(destination);
				}
				if (instruction[1].equals("lr")) {
					destination=14;
					//System.out.println(destination);
				}
				if ( instruction[1].equals("pc") ) {
					destination=15;
					//System.out.println(destination);
				}
				if (instruction[1].equals("fp")) {
					destination=11;
					//System.out.println(destination);
				}	
			} // end first IF
			else if(instruction.length>=3){
				//System.out.println(""+instruction[2]);
				if (instruction.length==3 && instruction[2].charAt(0)=='#') {
					immediateAt1=Integer.parseInt(instruction[2].substring(1));
					//System.out.println(immediateAt1);
				}
				else if (instruction[2].charAt(0)=='[' && instruction[2].charAt(instruction[2].length()-1)==']') {
					source1=valueReturner((instruction[2].substring(1, instruction[2].length()-1) ));
					//System.out.println(source1);
				}
				else if(instruction[2].charAt(0)=='r'){
					source1=Integer.parseInt(instruction[2].substring(1));
					//System.out.println(source1);
					}
				
				else if(instruction[2].equals("sp")){
					source1=valueReturner("sp");
				//System.out.println(source1);
				}
				else if (instruction[2].equals("lr")) {
					source1=valueReturner(instruction[2]);
					//System.out.println(source1);
				}
				else if (instruction[2].equals("pc")) {
					source1=valueReturner(instruction[2]);
					//System.out.println(source1);
				}
				else if (instruction[2].equals("fp")) {
					source1=valueReturner(instruction[2]);
					//System.out.println(source1);
				}
				else if (instruction[2].charAt(0)=='[' && instruction[2].length()==3 && !(( instruction[2].charAt(instruction[2].length()-1))>=48 && 
						instruction[2].charAt(instruction[2].length()-1)<=57)) {
					source1= valueReturner(instruction[2].substring(1));
					
				}
				else if (instruction[2].charAt(0)=='['   &&( instruction[2].charAt(instruction[2].length()-1))>=48 && 
						instruction[2].charAt(instruction[2].length()-1)<=57 && instruction[2].charAt(1)=='r')
				{	source1=Integer.parseInt(instruction[2].substring(2));
				}
		 }
		 else if (instruction.length>=4) {
				//System.out.println("enteered");
				if (instruction.length==4 && instruction[3].charAt(0)=='#' &&( instruction[3].charAt(instruction[3].length()-1))>=48 && 
						instruction[3].charAt(instruction[3].length()-1)<=57){
					//System.out.println((int)instruction[3].charAt(instruction[3].length()-1));
					immediateAt2=Integer.parseInt(instruction[3].substring(1));
					//System.out.println(immediateAt2);
			     }
				
				else if (instruction.length==4 && instruction[3].charAt(0)=='#' && instruction[3].charAt(instruction[3].length()-1)=='!' 
						&& instruction[3].charAt(instruction[3].length()-2)==']' ) {
					immediateAt2=Integer.parseInt(instruction[3].substring(1,instruction[3].length()-2));
					//System.out.println(immediateAt2);
				}
				else if (instruction.length==4 && instruction[3].charAt(0)=='#' && 
						instruction[3].charAt(instruction[3].length()-1)==']') {
					immediateAt2=Integer.parseInt(instruction[3].substring(1,instruction[3].length()-1));
					//System.out.println(immediateAt2);
				}
				else if (instruction[3].charAt(0)=='[' && instruction[3].charAt(instruction[3].length()-1)==']') {
					source2=valueReturner((instruction[3].substring(1, instruction[3].length()-1) ));
					//System.out.println(source1);
				}
				if(instruction[3].equals("sp")){
					source2=valueReturner("sp");
				//System.out.println(source2);
				}
				else if (instruction[3].equals("lr")) {
					source2=valueReturner(instruction[3]);
					//System.out.println(source2);
				}
				else if (instruction[3].equals("pc")) {
					source2=valueReturner(instruction[3]);
					//System.out.println(source2);
				}
				else if (instruction[3].equals("fp")) {
					source2=valueReturner(instruction[3]);
					//System.out.println(source2);
				}
				else if(instruction[3].charAt(0)=='r' ){
						source2=Integer.parseInt(instruction[3].substring(1));
						//System.out.println(source2);//System.out.println("In tird");
						}
					else if(instruction[3].equals("sp")){
						source2=valueReturner("sp");
					//System.out.println(source2);
					}
				else if (instruction[3].equals("lr")) {
					source2=valueReturner(instruction[3]);
					//System.out.println(source2);
				}
				else if (instruction[3].equals("pc")) {
					source2=valueReturner(instruction[3]);
					//System.out.println(source2);
				}
				else if (instruction[3].equals("fp")) {
					source2=valueReturner(instruction[3]);
					//System.out.println(source2);
				}
				else if (instruction[3].charAt(0)=='[' && instruction[3].length()==3) {
					source2= valueReturner(instruction[3].substring(1));
					//System.out.println(source2);
			}
			else if (instruction[3].charAt(0)=='['   &&( instruction[3].charAt(instruction[3].length()-1))>=48 && 
					instruction[3].charAt(instruction[3].length()-1)<=57 && instruction[3].charAt(1)=='r')
				{source2=Integer.parseInt(instruction[3].substring(2));
				//System.out.println(source2);
				}
			} // End else if
		 	else if (instruction.length>=5) {
					//System.out.println("instruction.length");
					if (instruction.length==5 && instruction[3].equals("LSR") &&( ( instruction[4].charAt(instruction[4].length()-1))>=48 && 
							instruction[4].charAt(instruction[4].length()-1)<=57) ){
						//System.out.println(instruction[]);
						lsr1=Integer.parseInt(instruction[4].substring(1));
						//System.out.println("LSrrr"+lsr1);
					}
					else if (instruction.length==5 && instruction[3].equals("LSR") && !( ( instruction[4].charAt(instruction[4].length()-1))>=48 && 
							instruction[4].charAt(instruction[4].length()-1)<=57)) {
						lsr1=Integer.parseInt(instruction[4].substring(1,instruction[4].length()-1));
					}
					else if (instruction.length==5 && instruction[3].equals("LSL")&&( ( instruction[4].charAt(instruction[4].length()-1))>=48 && 
							instruction[4].charAt(instruction[4].length()-1)<=57) ){
						//System.out.println("enterred");
						lsl1=Integer.parseInt(instruction[4].substring(1));
						//System.out.println("LSll"+lsr1);
					}
					else if (instruction.length==5 && instruction[3].equals("LSL")&& !( ( instruction[4].charAt(instruction[4].length()-1))>=48 && 
							instruction[4].charAt(instruction[4].length()-1)<=57)) {
						lsl1=Integer.parseInt(instruction[4].substring(1,instruction[4].length()-1));
					}
					else if (instruction.length==5 && instruction[3].equals("ASR")&&( ( instruction[4].charAt(instruction[4].length()-1))>=48 && 
							instruction[4].charAt(instruction[4].length()-1)<=57) ){
						//System.out.println("enterred");
						asr1=Integer.parseInt(instruction[4].substring(1));
						//System.out.println("LSll"+asr1);
					}
					else if (instruction.length==5 && instruction[3].equals("ASR")&&!( ( instruction[4].charAt(instruction[4].length()-1))>=48 && 
							instruction[4].charAt(instruction[4].length()-1)<=57)) {
						asr1=Integer.parseInt(instruction[4].substring(1,instruction[4].length()-1));
					}
					else if (instruction.length==6 && instruction[4].equals("LSL") &&( ( instruction[5].charAt(instruction[5].length()-1))>=48 && 
							instruction[5].charAt(instruction[5].length()-1)<=57)) {
						//System.out.println("enn");
						lsl2=Integer.parseInt(instruction[5].substring(1));
						//System.out.println("LSL2 is "+lsl2);
					}
					else if (instruction.length==6 && instruction[4].equals("LSL") && ! ( ( instruction[5].charAt(instruction[5].length()-1))>=48 && 
							instruction[5].charAt(instruction[5].length()-1)<=57) ) {
						//System.out.println("enyerr");
						lsl2=Integer.parseInt(instruction[5].substring(1,instruction[5].length()-1));
						//System.out.println("LSL2 is "+lsl2);
					}
					else if (instruction.length==6 && instruction[4].equals("LSR") &&( ( instruction[5].charAt(instruction[5].length()-1))>=48 && 
							instruction[5].charAt(instruction[5].length()-1)<=57)) {
						lsr2=Integer.parseInt(instruction[5].substring(1));
						//System.out.println("LSL2"+lsr2);
					}
					else if (instruction.length==6 && instruction[4].equals("LSR") && ! ( ( instruction[5].charAt(instruction[5].length()-1))>=48 && 
							instruction[5].charAt(instruction[5].length()-1)<=57) ) {
						//System.out.println("enyerr");
						lsr2=Integer.parseInt(instruction[5].substring(1,instruction[5].length()-1));
						//System.out.println("LSL2 is "+lsr2);
					}
					else if (instruction.length==6 && instruction[4].equals("ASR") &&( ( instruction[5].charAt(instruction[5].length()-1))>=48 && 
							instruction[5].charAt(instruction[5].length()-1)<=57)) {
						asr2=Integer.parseInt(instruction[5].substring(1));
						//System.out.println("ASR2"+asr2);
					}
					else if (instruction.length==6 && instruction[4].equals("ASR") && ! ( ( instruction[5].charAt(instruction[5].length()-1))>=48 && 
							instruction[5].charAt(instruction[5].length()-1)<=57) ) {
						//System.out.println("enyerr");
						asr2=Integer.parseInt(instruction[5].substring(1,instruction[5].length()-1));
						//System.out.println("ASR2 is "+asr2);
					}
					else if (instruction.length==5 && instruction[4].charAt(0)=='#' &&( instruction[4].charAt(instruction[4].length()-1))>=48 && 
							instruction[4].charAt(instruction[4].length()-1)<=57){
						//System.out.println((int)instruction[4].charAt(instruction[4].length()-1));
						immediateAt2=Integer.parseInt(instruction[4].substring(1));
						//System.out.println(immediateAt2);
				     }
					
					else if (instruction.length==5 && instruction[4].charAt(0)=='#' && instruction[4].charAt(instruction[4].length()-1)=='!' 
							&& instruction[4].charAt(instruction[4].length()-2)==']' ) {
						immediateAt2=Integer.parseInt(instruction[4].substring(1,instruction[4].length()-2));
						//System.out.println(immediateAt2);
					}
					else if (instruction.length==5 && instruction[4].charAt(0)=='#' && 
							instruction[4].charAt(instruction[4].length()-1)==']') {
						immediateAt2=Integer.parseInt(instruction[4].substring(1,instruction[4].length()-1));
						//System.out.println(immediateAt2);
					}
			} // End last 
		 	else {
		 		//System.out.println(opcode);
		 		
		 	}
			if(isCISC(opcode) ){
				//num++;
				continue;
			}
			else{
				// num2++;
			}
		
			/*if(!lolc.contains(opcode)){
				lolc.add(opcode);
			}*/
			INSTRUCTION temp=new INSTRUCTION( opcode, destination, source1, source2, immediateAt1, immediateAt2, lsr1, lsr2, asr1, asr2, lsl1, lsl2,0);
			
			inss.add(temp);
			 
			}
			else{
			/*	String s = tokens[tokens.length-1];
				System.out.println(s);*/
			}
		
		} // End Outer While 
		}
		catch (Exception e) {
			//System.out.println("Line Number Is "+ line_number);
			e.printStackTrace();
		}
		
		Iterator<INSTRUCTION> itr = inss.iterator();
		VLIW_Instruc bundle = new VLIW_Instruc(); 
		INSTRUCTION inst = itr.next();
		
		while(itr.hasNext()){
			//System.out.println(inst.opcode);
			if(checkSpace(inst, bundle)>=0){
				int place = checkSpace(inst, bundle);
				bundle.instruc[place]=true;
			}
			else{
				//System.out.println("Came INTO Dispatch inside while");
				if(hasLoad(bundle)){load_number++;}
				int num = checkSpace(inst, bundle);;
				//print_bundle(bundle );
				//System.out.println(" jsjsjs "+  num);
				updated_efficiency_bundle(bundle);
				dispatch_bundle_clock(bundle);
				num_of_bundles++;
				int place = checkSpace(inst, bundle);
				bundle.instruc[place]=true;
				//dispatch
			}
			inst = itr.next();
		}
		
		if(checkSpace(inst, bundle)>=0 ){
			int place = checkSpace(inst, bundle);
			//System.out.println("Came into if out of while");
			bundle.instruc[place]=true;
		}
		else{	
			//System.out.println("Out Of While");
			//System.out.println(inst.opcode);
		if(hasLoad(bundle)){load_number++;}
		updated_efficiency_bundle(bundle);
		dispatch_bundle_clock(bundle);
		num_of_bundles++;
		int place = checkSpace(inst, bundle);
		bundle.instruc[place]=true;
		}
		if(hasLoad(bundle)){load_number++;}
		if(isNotEmpty(bundle)){
		updated_efficiency_bundle(bundle);
		dispatch_bundle_clock(bundle);
		num_of_bundles++;
		}


	System.out.println("Total Number Of V.L.I.W Instruction Bundles Created = "+ num_of_bundles);
	System.out.println("Percentage of bundles with 25% utilization  = " + (bundle_efficiency_25/(float)num_of_bundles ) *100) ;
	System.out.println("Percentage of bundles with 50% utilization  = " + (bundle_efficiency_50/(float)num_of_bundles ) *100) ;
	System.out.println("Percentage of bundles with 75% utilization  = " + (bundle_efficiency_75/(float)num_of_bundles ) *100) ;
	System.out.println("Percentage of bundles with 100% utilization  = " + (bundle_efficiency_100/(float)num_of_bundles ) *100) ;
	System.out.println("Total number Of cycles taken by the V.L.I.W Processor is = "+ number_Of_Cycles);
	
	}	// End Main
	static void updated_efficiency_bundle(VLIW_Instruc bundle){
		int count = 0 ; 
		for (int i = 0; i < bundle.instruc.length; i++) {
			if(bundle.instruc[i] != false){
				count++;
			}
		}
		if(count==1){
		   bundle_efficiency_25++;
		}
		if(count==2){
			bundle_efficiency_50++;
		}
		if(count==3){
			bundle_efficiency_75++;
		}
		if(count == 4){
			bundle_efficiency_100++;
		}
		
	}
	static void print_bundle(VLIW_Instruc bundle)
	{
		for (int i = 0; i < bundle.instruc.length; i++) {
			if(bundle.instruc[i]){
				System.out.print("1 ");
			}
			else{
				System.out.print("0 ");
			}
		}
	}
	
	static boolean isNotEmpty(VLIW_Instruc bundle){
		int count = 0 ; 
		for (int i = 0; i < bundle.instruc.length; i++) {
			if(bundle.instruc[i] != false){
				count++;
			}
		}
		if(count>0){
			return true;
		}
		else{
			return false;
		}
	}
	static boolean hasLoad(VLIW_Instruc bundle){
			if(bundle.instruc[3]==true){
				return true ; 
			}
			else{
				return false;
			}
		}
	
	static boolean hasMultiply(VLIW_Instruc bundle){
		if(bundle.instruc[2]==true){
			return true ; 
		}
		else{
			return false;
		}
	}
	static boolean hasALU(VLIW_Instruc bundle){
		if((bundle.instruc[0]==true) || (bundle.instruc[1]==true)){
			return true ; 
		}
		else{
			return false;
		}
	}
	static void dispatch_bundle_clock(VLIW_Instruc bundle){
		//System.out.println("Came into Dispactch Bundle");
		if(load_number != 0 && load_number%100==0 && hasLoad(bundle)){
			number_Of_Cycles+=45;
			}
		else if(load_number!=0 && load_number%10==0 && hasLoad(bundle) && load_number%100 != 0 ){
			number_Of_Cycles+=8;
		}
		else if(hasMultiply(bundle)){
			number_Of_Cycles+=3;
		}
		else if(hasLoad(bundle)){
			number_Of_Cycles+=2;
		}
		else if(hasALU(bundle)){
			
			//System.out.println("entered here");
			number_Of_Cycles+=1;
		}
		//System.out.println("Clearing All");
		for (int i = 0; i < bundle.instruc.length; i++) {
			bundle.instruc[i]=false;
		}
	}
	
	public static int number_Of_Cycles=0;
	static int function_unit_returner(String opcode){
		if(opcode.charAt(0)=='b' || opcode.startsWith("add") || opcode.startsWith("sub") || opcode.startsWith("cm") || 
				opcode.startsWith("mvn") || opcode.startsWith("rs")|| opcode.startsWith("sb")||opcode.charAt(0)=='t' 
				|| opcode.startsWith("and") || opcode.startsWith("mv") ||opcode.startsWith("mov") || opcode.startsWith("clz") 
				|| opcode.startsWith("orr") || opcode.startsWith("mrc") || opcode.startsWith("vm") || opcode.startsWith("eor")
				||opcode.startsWith("uop")||opcode.startsWith("svc")|| opcode.startsWith("adc") || opcode.startsWith("dmb") )
			{ // System.out.println("Returning 0");
				return  0 ; 
			}
		else if(opcode.startsWith("mul") || opcode.startsWith("mla") || opcode.startsWith("um")  ){
			return 1 ; 
		}
		else if(opcode.startsWith("str") || opcode.startsWith("ldr")  ){
			
			return 2 ;
		}
		else{
			//System.out.println("opcode =" + opcode );
			return -1 ;
		}
	}
	
		
	static int checkSpace(INSTRUCTION insy , VLIW_Instruc bundle){
		int fu = function_unit_returner(insy.opcode);
		int alu_free=-1; boolean alu_q=false; boolean mult_query=false; boolean store_query=false;
		if(fu==0){//System.out.println("Entered for 0 ");
			for(int i = 0 ; i < 2; i++){
				if(bundle.instruc[i] == false){
					alu_free=i;
					//System.out.println("i is "+ i);
					//System.out.println("Printing ALUFREE"+ alu_free);
					return alu_free;
				}
				
			    
			}
		}
		else if (fu==1){
			
			if(bundle.instruc[2] == false){
				
				//System.out.println("Has Space For MULTIPLY	with "+insy.opcode);
				return 2 ; 
			}
		}
		else if(fu==2){
			if(bundle.instruc[3]==false){
				return 3 ; 
			}
		}
		else{
	  // System.out.print("Doesnot Have Space for :");
	//	System.out.println(insy.opcode);
		return -1 ; 
		}
		return -1;
	}
	
	
	
	
	static boolean isCISC(String opcode){
		if(opcode.equals("ldmstm") || opcode.equals("ldr") || opcode.equals("str") || opcode.equals("ldrbne") || 
				opcode.equals("ldrbcs")||opcode.equals("strbcs")){
			return true ; 
		}
		else{
			return false ; 
		}
	}

}

class VLIW_Instruc{
	public boolean [] instruc = new boolean[4];
	public VLIW_Instruc() {
		for (int i = 0; i < instruc.length; i++) {
			instruc[i]=false;
		}
	}
	public void clear_all(){
		for (int i = 0; i < instruc.length; i++) {
			instruc[i]=false;
		}
	}
}
