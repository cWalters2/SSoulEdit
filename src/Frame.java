class Frame{
	//doubly linked list to hold frames
	float time = 0.f;
	float value = 0.f;
	Frame next = null;
	Frame prev = null;
	public Frame(){}
	public Frame(float t){
		time = t;
	}
	public Frame(float t, float v){
		time=t;
		value=v;
	}
	public void Append(Frame f){
		if((next!=null)&&(next.time<f.time))
			next.Append(f);
		else if((next!=null)&&(next.time>f.time)&&(time<f.time)){//insert here
			f.prev=this;
			f.next = next;
			f.value = value;
			next = f;
		}
		else if((next==null)&&(time<f.time)){//insert at end
			f.prev=this;
			next = f;
			f.value = value;//copy priorities
		}
		else if(time>f.time){//insert at start
			f.prev=null;
			f.next=this;
			f.value = value;
			prev=f;		
		}
	}
	public void Append(float t, int i){				
		if(next!=null){
			if(next.time<t)
				next.Append(t, i);//advance
			else{//insert
				Frame f = new Frame(t);
				f.prev=this;
				f.next = next.next;
				next = f;
			}
		}else{
			if(time<t){//empty list
				Frame f = new Frame(t);
				f.prev=this;
				f.next = null;
				next = f;
			}
			else{
				Frame f = new Frame(t);
				f.next=this;
				f.prev=null;
				prev = f;
			}						
		}		
	}
	public void SetValue(float v){
	value=v;
	}
}
