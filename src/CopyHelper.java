class CopyHelper{
	public static final int FRAME = 1;
	public static final int VERTEX = 2;
	public static final int HITBOX = 3;
	public static final int TRACK = 4;
	
	public static final String PFTEXT = "Paste Frame";
	public static final String VTEXT = "Paste Vertex";
	public static final String HBTEXT = "Paste Hitbox";
	public static final String TRTEXT = "Paste Track";
	
	private int type;
	private Vertex vertex;
	private Vertex[] hitbox;
	private Frame frame;
	private Track track;
	
	public CopyHelper(){
		vertex=null;
		hitbox=null;
		frame=null;
		track=null;
		type=0;
	}

	public Track GetTrack() {
		if(type==TRACK)
			return track;
		else
			return null;
	}
	public Frame GetFrame(){
		if(type==FRAME)
			return frame;
		else
			return null;
	}
	public int GetContentType() {
		return type;
	}
	public void Copy(Frame f){
		if(f!=null)
			type=FRAME;
		else
			type=0;
		frame = f;
		hitbox=null;
		vertex=null;
		track=null;
	}
	public void Copy(Vertex v){
		if(v!=null)
			type=VERTEX;
		else
			type=0;
		vertex=v;
		hitbox=null;
		frame=null;
		track=null;
	}
	public void Copy(Track t) {
		if(t!=null)
			type=TRACK;
		else
			type=0;
		t.next=null;
		t.last=null;
		hitbox=null;
		frame=null;
		vertex=null;
		track=t;
		
	}
	public void Copy(Vertex[] v, int a, int o){
		if(v!=null)
			type=HITBOX;
		else
			type=0;
		if(a>o)
			return;
		frame=null;
		track=null;
		vertex=null;
		hitbox = new Vertex[o-a+1];
		for(int i=a;i<=o;i++)
			hitbox[i] = new Vertex(v[i], null);//null keyframe
	}
	public Vertex GetVert(){
		
		if(type==VERTEX)
			return vertex;
		else
			return null;
		//no hitbox retrieval just yet
	}	
}
