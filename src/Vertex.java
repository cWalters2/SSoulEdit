class Vertex {
	private boolean brk;
	//to offset the mag arrow from the vertex circle
	public static float MOVEDIR_OFFSET = -2.0f;
	Vertex next = null;
	Frame keyframe = null;
	int vPtr;
	float xLoc;
	float yLoc;
	float mag;
	float dir;
	float wgt;
	float dmg;

	public Vertex() {
		vPtr=-1;
		keyframe = null;
		wgt = 1;
		brk = false;
	}
	
	public Vertex(float x, float y, Frame frameSel) {
		vPtr=-1;
		keyframe = frameSel;
		wgt = 1;
		brk = false;
		xLoc = x;
		yLoc = y;
	}
	public Vertex(Vertex o,Frame f) {
		this();		
		brk = o.getBreak();
		keyframe=f;
		xLoc = o.xLoc;
		yLoc = o.yLoc;
		mag = o.mag;
		dir = o.dir;
		wgt = o.wgt;
		dmg = o.dmg;
		vPtr=o.vPtr;
		if(o.next!=null)
		next = new Vertex(o.next, f.next);
	}
	
	public void MoveDirectional(float frameSet, float x, float y) {
		if ((frameSet > keyframe.time) && (next != null))// cycle to appropriate frame
			next.MoveDirectional(frameSet, x, y);
		else if (frameSet == keyframe.time) {
			float dX = x - xLoc;
			float dY = y - yLoc;
			mag = (float) Math.sqrt(dX * dX + dY * dY); // set mag to be equal to the distance
			mag += MOVEDIR_OFFSET;
			if(mag<0)
				mag=0;
			dir = (float) Math.atan2(dY, dX);
		}
	}
	
	public void MoveVertex(float frameSet, float x, float y) {
		if ((frameSet > keyframe.time) && (next != null))// cycle to appropriate frame
			next.MoveVertex(frameSet, x, y);
		else if (frameSet == keyframe.time) {
			xLoc = x;
			yLoc = y;
		}
	}
	
	public void SetBreak(boolean b) {
		brk = b;
		if (next != null)
			next.SetBreak(b);
	}
	
	public boolean getBreak() {
		return brk;
	}
	
	public Vertex InsertFrame(Frame frameSet, float fWeight, Vertex lVert) {
		//inserts a frame into the vertex. Object returned is the first item in the linked list.
		if(keyframe==null){
			keyframe=frameSet;
				if(lVert!=null){
				xLoc=lVert.xLoc;
				yLoc=lVert.yLoc;
				wgt=lVert.wgt;
				mag=lVert.mag;
				dmg=lVert.dmg;
				dir=lVert.dir;
			}
		}
		else if ((next != null)&&(frameSet.time > next.keyframe.time))// cycle to appropriate frame
			next.InsertFrame(frameSet, fWeight, this);
		else if ((frameSet.time == keyframe.time))
			;// do nothing
		else if ((frameSet.time > keyframe.time) && (next == null)) {//append to end
			next = new Vertex();//weight on default constructor is 1.1
			next.keyframe = frameSet;
			next.xLoc=xLoc;
			next.yLoc=yLoc;
			next.wgt=wgt;
			next.dir=dir;
			next.mag=mag;
			next.dmg=dmg;
		} 
		else if((next!=null)&&(frameSet.time < next.keyframe.time)){ //insert before
			Vertex vIns = new Vertex();//weight on default constructor is 1.1
			vIns.next=next;
			next=null;
			next=vIns;
			vIns.keyframe=frameSet;
			vIns.xLoc=fWeight * vIns.next.xLoc + (1 - fWeight) * xLoc;
			vIns.yLoc=fWeight * vIns.next.yLoc + (1 - fWeight) * yLoc;
			vIns.dir=fWeight * vIns.next.dir + (1 - fWeight) * dir;
			vIns.mag=fWeight * vIns.next.mag + (1 - fWeight) * mag;
			vIns.wgt=fWeight * vIns.next.wgt + (1 - fWeight) * wgt;
			vIns.dmg=fWeight * vIns.next.dmg + (1 - fWeight) * dmg;		
		}
		else if((frameSet.time < keyframe.time)){//insert at start
			Vertex vIns = new Vertex();
			vIns.keyframe = frameSet;
			vIns.xLoc=xLoc;
			vIns.yLoc=yLoc;
			vIns.wgt=wgt;
			vIns.dir=dir;
			vIns.mag=mag;
			vIns.dmg=dmg;
			vIns.next=this;
			return vIns;		
		}
		return this;
	}
	
	public void PointVertexTo(int p){
		vPtr=p;
		if(next!=null)
			next.PointVertexTo(p);
	}
	
	public void SetVertex(float frameSet, float x, float y) {
		//edits values at specified frame
		if ((frameSet > keyframe.time) && (next != null))// cycle to appropriate frame
			next.SetVertex(frameSet, x, y);
		else if (frameSet == keyframe.time) {
			xLoc = x;
			yLoc = y;
		} 
	}
	
	public void SetVertex(Frame frameSet, float x, float y) {
		if(keyframe==null){
			keyframe=frameSet;
			xLoc = x;
			yLoc = y;		
		}
		else if ((frameSet.time > keyframe.time) && (next != null))// cycle to appropriate frame
			next.SetVertex(frameSet, x, y);
		else if (frameSet == keyframe) {
			xLoc = x;
			yLoc = y;
		} else if ((frameSet.time > keyframe.time) && (next == null)) {
			next = new Vertex();
			next.SetVertex(frameSet, x, y);
		} else {
			keyframe = frameSet;
			xLoc = x;
			yLoc = y;
		}
	}
	
	public void SetMag(float frameSet, float iM) {

		if ((frameSet > keyframe.time) && (next != null))// cycle to appropriate frame
			next.SetMag(frameSet, iM);
		else if (frameSet == keyframe.time)
			mag = iM;
		else if ((frameSet > keyframe.time) && (next == null)) {
			next = new Vertex();
			next.SetMag(frameSet, iM);
		} else {
			mag = iM;
		}
	}
	
	public void SetDir(float frameSet, float iD) {
		while (iD > Math.PI)
			// sanitize input
			iD -= 2 * Math.PI;
		while (iD < -Math.PI)
			iD += 2 * Math.PI;
		if ((frameSet > keyframe.time) && (next != null))// cycle to appropriate frame
			next.SetDir(frameSet, iD);
		else if (frameSet == keyframe.time)
			dir = iD;
		else if ((frameSet > keyframe.time) && (next == null)) {
			next = new Vertex();
			next.SetDir(frameSet, iD);
		} else {
			dir = iD;
		}
	}
	
	public void SetWgt(float frameSet, float iW) {
		if ((frameSet > keyframe.time) && (next != null))// cycle to appropriate frame
			next.SetWgt(frameSet, iW);
		else if (frameSet == keyframe.time)
			wgt = iW;
		else if ((frameSet > keyframe.time) && (next == null)) {
			next = new Vertex();
			next.SetWgt(frameSet, iW);
		} else {
			wgt = iW;
		}
	}
	
	public void SetDmg(float frameSet, float iD) {
		if ((frameSet > keyframe.time) && (next != null))// cycle to appropriate frame
			next.SetDmg(frameSet, iD);
		else if (frameSet == keyframe.time)
			dmg = iD;
		else if ((frameSet > keyframe.time) && (next == null)) {
			next = new Vertex();
			next.SetDmg(frameSet, iD);
		} else {
			dmg = iD;
		}
	}
}
