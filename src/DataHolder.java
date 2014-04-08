class DataHolder {
	public float HGT_DIV2 = 10.7f;
	public final int NUM_ATK = 23;
	public static final float WID_DIV2 = 3.55f;
	public static final int SCALE_EPS = 1000;
	Track[] attack=new Track[NUM_ATK];
	int vertSize = 0;
	DataHolder last = null;
	DataHolder next = null;
	boolean isUndo = false;
	boolean prepCommitFlag = false; //prepare a commit for draggable objects
	public DataHolder() {
		for(int i =0;i<NUM_ATK;i++){
			attack[i] = new Track();
			
		}
	}	
	public int Type(int a) {
		
		return attack[a].trType;
	}
	DataHolder(DataHolder o){
		this();//call default constructor
		for(int i = 0; i < NUM_ATK; i++){
			this.attack[i]=new Track(o.attack[i]);		
		}
	last = o.last;
			
	}
	public void MoveFrame(int aSel, int tSel, Frame f, float dF) {
		if(prepCommitFlag)
			Commit();
		
		if(f!=null){
		Track trk;
		if(attack[aSel].trType>0)
			trk=attack[aSel].GetTrack(tSel);
		else
			trk=attack[aSel];
		
		f.time = dF;
		
		}
		else
			System.out.println("DataHolder::MoveFrame - no frame selected");
		
	}
	
	public void Commit(){
		last = new DataHolder(this);
		last.next = this;
		prepCommitFlag = false;//commit confirmed
	}
	public void PrepareToCommit(){
		//Commit function for draggable objects.
		//once selected, they may or may not be moved
		//this function prepares and handles this 
		prepCommitFlag = true;
	}
	public void DeleteVertex(int atkSel, int iVert) {
		//function that removes the vertex at [atkSel][iVert] within the dataholder
		//returns an int which is used to identify the artifact vertex to flush
		attack[atkSel].DeleteVertex(iVert);	
	}
	
	public void InsertFrameAll(int aSel, Frame kf, float fWeight) {
		attack[aSel].InsertFrameAll(kf, fWeight);		
	}	
	
	public void PointToVertex(int moveSel, int vFrom, int vTo) {
		if (attack[moveSel].vertArr[vFrom] == null)
			attack[moveSel].vertArr[vFrom] = new Vertex();
		attack[moveSel].vertArr[vFrom].PointVertexTo(vTo); // negative frames denote vertex pointing xLoc contains vertex to point to
	}
	
	public StringBuffer SaveFile(String model) {
		int move = 0;
		int track = 0;
		int numTrack = 1;
		Vertex v;
		StringBuffer outSBuff = new StringBuffer("");
		outSBuff.append("<model=" + model + ">\r\n");
		int num_att = NUM_ATK;
		if(model.compareTo("serenity")!=0)
			num_att-=5;
		
		if(model.compareTo("Serenity")==0)//serenity special case
			num_att+=5;
		for(move = 0; move < num_att; move++)		
			if(attack[move].VertSize()>0){// <NA>			
				switch(move){
				case 0: outSBuff.append("<NA>\r\n");
					break;
				case 1: outSBuff.append("<SA>\r\n");
					break;
				case 2: outSBuff.append("<DA>\r\n");
					break;
				case 3: outSBuff.append("<UA>\r\n"); 
					break;
				case 4: outSBuff.append("<NB>\r\n");
					break;
				case 5: outSBuff.append("<SB>\r\n"); 
					break;
				case 6: outSBuff.append("<DB>\r\n"); 
					break;
				case 7: outSBuff.append("<UB>\r\n");
					break;
				case 8: outSBuff.append("<NC>\r\n"); 
					break;
				case 9: outSBuff.append("<SC>\r\n");
					break;
				case 10: outSBuff.append("<DC>\r\n");
					break;
				case 11: outSBuff.append("<UC>\r\n");
				    break;
				case 12: outSBuff.append("<DATK>\r\n");
					break;
				case 13: outSBuff.append("<NAIR>\r\n");
					break;
				case 14: outSBuff.append("<FAIR>\r\n");
					break;
				case 15: outSBuff.append("<BAIR>\r\n");
					break;
				case 16: outSBuff.append("<DAIR>\r\n");
					break;
				case 17: outSBuff.append("<UAIR>\r\n");
					break;
				case 18: outSBuff.append("<SNAIR>\r\n");
					break;
				case 19: outSBuff.append("<SFAIR>\r\n");
					break;
				case 20: outSBuff.append("<SBAIR>\r\n");
					break;
				case 21: outSBuff.append("<SDAIR>\r\n");
					break;
				case 22: outSBuff.append("<SUAIR>\r\n");
					break;
				}
				
				 if(attack[move].trType==1){
					 outSBuff.append("<multi>\r\n");  //<multi>
				 }
				 else if(attack[move].trType==2)
					 outSBuff.append("<paral>\r\n");  //<paral>
				 numTrack=0;
				 for(Track a = attack[move]; a!=null; a=a.next)
					 numTrack++;
				 outSBuff.append("<numtrack="+ numTrack +"/>\r\n");
				 track=0;
				 for(Track trk=attack[move]; trk!=null;trk=trk.next){
					 if(attack[move].trType>0)
						 outSBuff.append("<track " + track + ">\r\n"); // <track n>
					
					outSBuff.append("<vertex=" + trk.VertSize() + "/>\r\n");
					Frame p =trk.keyFrame;
					if((trk!=null)&&(trk.VertSize()>0))
						v = trk.vertArr[0];
					else
						v=null;
					Vertex printList[] = new Vertex[trk.VertSize()];
					for (int i = 0; i <trk.VertSize(); i++){	//fill up the list first
						printList[i] =trk.vertArr[i];
					}
					//make sure to account for vtx pointers!
					
					if((trk.centre==null)||(trk.centre.keyframe==null))//upgrade from legacy
							trk.SetCentreFrames();
					Vertex c = trk.centre;
					while (v != null) {
						outSBuff.append("<frame=" + v.keyframe.time + ">\r\n");
						outSBuff.append("<priority=" + v.keyframe.value + ">\r\n");
						if(c==null)
							v=new Vertex();
						outSBuff.append("<centre=" + c.xLoc +", " +c.yLoc+ ">\r\n");
						for (int i = 0; i < trk.VertSize(); i++){
							if(printList[i].vPtr >= 0)//vtx pointer
								outSBuff.append("<vtx" + i + "=" + "vtx" + (int)(printList[i].vPtr) + "/>\r\n");
							else
								outSBuff.append("<vtx" + i + "=" + printList[i].xLoc + ","
									+ printList[i].yLoc + "/>\r\n");
						}
						for (int i = 0; i < trk.VertSize(); i++)
							outSBuff.append("dmg" + i + " " + printList[i].dmg + "\r\n");
						for (int i = 0; i < trk.VertSize(); i++)
							outSBuff.append("mag" + i + " " + printList[i].mag + "\r\n");
						for (int i = 0; i < trk.VertSize(); i++)
							outSBuff.append("dir" + i + " " + printList[i].dir + "\r\n");
						for (int i = 0; i < trk.VertSize(); i++)
							outSBuff.append("wgt" + i + " " + printList[i].wgt + "\r\n");
						outSBuff.append("</frame>\r\n");
		
						for (int i = 0; i < trk.VertSize(); i++)  //cycle the list to the next frame
							if(trk.vertArr[i].vPtr<0)
								printList[i] = printList[i].next;
						v = v.next;
						c=c.next;
					}
					//add vertex breaks
					for (int i = 0; i < trk.VertSize(); i++){	
						if(trk.vertArr[i].getBreak()){
							outSBuff.append("<break=" + i + ">\r\n");
						}
					}
					if(attack[move].trType>0){
						outSBuff.append("</track>\r\n");
						track++;
					}
				 }
				switch(move){
				case 0: outSBuff.append("</NA>\r\n");
					break;
				case 1: outSBuff.append("</SA>\r\n"); 
					break;
				case 2: outSBuff.append("</DA>\r\n");
					break;
				case 3: outSBuff.append("</UA>\r\n"); 
					break;
				case 4: outSBuff.append("</NB>\r\n");
					break;
				case 5: outSBuff.append("</SB>\r\n"); 
					break;
				case 6: outSBuff.append("</DB>\r\n"); 
					break;
				case 7: outSBuff.append("</UB>\r\n");
					break;
				case 8: outSBuff.append("</NC>\r\n"); 
					break;
				case 9: outSBuff.append("</SC>\r\n");
					break;
				case 10: outSBuff.append("</DC>\r\n");
					break;
				case 11: outSBuff.append("</UC>\r\n");	
			        break;
				case 12: outSBuff.append("</DSH>\r\n");
					break;
				case 13: outSBuff.append("</NAIR>\r\n");
					break;
				case 14: outSBuff.append("</FAIR>\r\n");
					break;
				case 15: outSBuff.append("</BAIR>\r\n");
					break;
				case 16: outSBuff.append("</DAIR>\r\n");
					break;
				case 17: outSBuff.append("</UAIR>\r\n");
				}		
		}
			return outSBuff;
	}
	public void MoveVertex(Track trk, float frameSel, int vSel, float xCursor,
			float yCursor) {
		if(prepCommitFlag)
			Commit();
		trk.MoveVertex(frameSel, vSel, xCursor, yCursor);
		
	}
	public void MoveDirectional(Track trk, float frameSel, int vSel,
			float xCursor, float yCursor) {
		if(prepCommitFlag)
			Commit();
		trk.MoveDirectional(frameSel, vSel, xCursor, yCursor);
		
	}
	public void MoveMultipleVerts(Track trk, float fSel,
			int hbIndStart2, int hbIndEnd2, float f, float g) {
		if(prepCommitFlag)
			Commit();
		trk.MoveMultipleVerts(fSel, hbIndStart2, hbIndEnd2, f, g);
		
	}		
}