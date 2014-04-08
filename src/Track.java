class Track {
	Vertex vertArr[];
	Vertex centre;
	Frame keyFrame;
	Track next;
	Track last;
	private int id;
	private int numVert;
	private int numFace;
	public int trType;
	private final float FRAME_MIN = 0.04f;

	public Track() {
		vertArr = null;
		keyFrame = null;
		next = null;
		last = null;
		numVert = 0;
		trType = 0;
		SetId(0);
		centre = new Vertex();

	}

	public int GetNumTracks() {
		int i = 0;
		for (Track t = this; t != null; t = t.next)
			i++;
		return i;
	}

	public void SortFrames() {
		Frame g = null;
		Frame h = null;
		float lastTime = 1.0f;
		Frame lastF = keyFrame;
		Frame lastG = keyFrame;
		for (Frame f = keyFrame; f != null; f = f.next) {
			lastG = f;
			if (f.time < 0)// sanity
				f.time = 0;
			else if (f.time > 1.0)
				f.time = 1;
			for (g = f.next; g != null; g = g.next) {
				if (g.time < 0)
					g.time = 0;
				else if (g.time > 1)
					g.time = 1;
				if (f.time > g.time) {// swap

					lastG.next = g.next;
					g.next = f;
					if (f == keyFrame) // keep keyframe pointed to top
						keyFrame = g;
					else
						lastF.next = g;
					lastTime = g.time;// record for comparison and adjustment
					for (h = f; h != null; h = h.next) {
						if (h.time < lastTime + FRAME_MIN)
							h.time = lastTime + FRAME_MIN;
						lastTime = h.time;
					}
					f = g;// reassign for consistency
				}
				lastG = g;
			}
			lastF = f;
		}
		// re-organize the vert list
		Vertex v;
		for (int i = 0; i < numVert; i++) {
			v = vertArr[i];
			if (v != null) {
				for (Frame w = keyFrame; w != null; w = w.next) {
					v.keyframe = w;
					v = v.next;
				}
			}
		}

	}

	public void AppendKeyFrame(Frame k) {
		if (keyFrame == null)
			keyFrame = k;
		else
			keyFrame.Append(k);
	}

	public Track GetTrack(int tSel) {
		// returns the selected track
		// returns null if invalid selection
		if (tSel < 0)
			return this;
		Track rtn = this;
		if ((next != null) && (rtn.GetId() != tSel))
			rtn = next.GetTrack(tSel);
		else if (rtn.GetId() == tSel)// special case for invalid selection
			return rtn;
		else if (next == null)
			return null;

		return rtn;
	}

	public Track addTrack() {
		Track rtn = null;
		if (next != null)
			rtn = next.addTrack();
		else {
			next = new Track();
			next.id = id + 1;
			next.last = this;
			return next;
		}
		return rtn;
	}

	public void CutTrack() {
		Track delTrack = this;
		if (delTrack.next != null) {
			while (delTrack.next.next != null)
				delTrack = delTrack.next;
			delTrack.next = null;
		}

	}

	public void SetVertNumber(int nV) {
		numVert = nV;
		vertArr = new Vertex[nV];
		numFace = 0;
	}

	public void MoveDirectional(float frameSel, int vertSel, float x, float y) {
		vertArr[vertSel].MoveDirectional(frameSel, x, y);
	}

	public void MoveVertex(float frameSel, int vertSel, float moveX, float moveY) {
		vertArr[vertSel].MoveVertex(frameSel, moveX, moveY);
	}

	public void SetBreak(int bNum, boolean b) {
		vertArr[bNum].SetBreak(b);
		numFace++;
	}

	public Frame GetKeyFrame(float fSel) {
		Frame f = keyFrame;
		if (f == null)
			return null;
		else {
			while ((f.time != fSel) && (f.next != null))
				f = f.next;
			if (f.time == fSel)
				return f;
		}
		return null;
	}

	public void SetType(String t) {
		if (GetId() == 0) {
			if (t.compareTo("multi") == 0)
				next = new Track(GetId());
			else if (t.compareTo("paralell") == 0)
				next = new Track(GetId());
			// leave the pointer up in case the user wishes to revert
			// additional tracks will not be saved as 'single' track however.
		}
		if (t.compareTo("multi") == 0)
			trType = 1;
		else if (t.compareTo("paralell") == 0)
			trType = 2;
		else
			trType = 0;
	}

	public void RemoveFrameAll(int frameInd) {
		Vertex v;
		Frame f = keyFrame;
		if (f == null)
			return;// nothing existed in the first place
		if (frameInd == 0)// special case:root
			if (keyFrame.next != null)
				keyFrame = keyFrame.next;
			else
				keyFrame = null;
		else if (frameInd > 0) {// sanity check
			for (int j = 0; j < frameInd - 1; j++)
				if (f.next != null)
					f = f.next;
			if ((f.next != null) && (f.next.next != null))
				f.next = f.next.next;
			else
				f.next = null;
		}
		Vertex c = centre;
		if ((c != null) && (frameInd == 0))
			if (c.next != null)
				centre = c.next;
			else {
				ClearCentre();
				return;
			}
		else {
			for (int j = 0; j < frameInd - 1; j++)
				if (c.next != null)
					c = c.next;
			if ((c.next != null) && (c.next.next != null))// sanity checks
				c.next = c.next.next;
			else
				c.next = null;
		}
		if (VertSize() == 0)
			return;// done already
		for (int i = 0; i < VertSize(); i++) {
			v = vertArr[i];
			if ((v != null) && (frameInd == 0))
				if (v.next != null)
					vertArr[i] = v.next;
				else {
					ClearVerts();
					return;
				}
			else {
				for (int j = 0; j < frameInd - 1; j++)
					if (v.next != null)
						v = v.next;
				if ((v.next != null) && (v.next.next != null))// sanity checks
					v.next = v.next.next;
				else
					v.next = null;
			}
		}
	}

	private void ClearCentre() {
		centre = new Vertex(0, 0, null);
		
	}

	public Vertex InsertVertex(int iVert, float x, float y) {
		// insert a vertex before the iVert index
		Vertex[] transferArr = vertArr;
		Vertex retV = null;// return value
		numVert++;
		vertArr = new Vertex[numVert]; // make a new, slightly bigger array
		if (numVert == 1) {// first vert
			vertArr[0] = new Vertex();
			Frame f = keyFrame;
			while (f != null) {
				vertArr[0].SetVertex(f, x, y);
				f = f.next;
			} // loop through to add every frame - cannot be an end point
		} else {
			for (int i = 0; i < numVert; i++) {
				if (i < iVert) {
					vertArr[i] = transferArr[i];
				} else if (i == iVert) {
					Vertex v = transferArr[i];
					if (v.vPtr >= 0)// frame ptr
						v = transferArr[v.vPtr];
					vertArr[i] = new Vertex();
					retV = vertArr[i];
					while (v != null) {
						vertArr[i].SetVertex(v.keyframe, x, y);
						v = v.next;
					} // loop through to add every frame - cannot be an end
						// point
				} else {
					vertArr[i] = transferArr[i - 1];
					if (vertArr[i].vPtr >= iVert) // check if pointed vertex
													// needs fix
						vertArr[i].vPtr++;
				}
			}
			vertArr[iVert].SetBreak(false); // make sure we fix a cloned end
											// vertex
		}
		return retV;
	}

	public void InsertFrameAll(Frame kf, float fWeight) {
		if (keyFrame == null)
			keyFrame = kf;
		else if (keyFrame.time > kf.time) {
			kf.value = keyFrame.value;
			keyFrame.Append(kf);

			keyFrame = kf;
		} else
			keyFrame.Append(kf);
		centre = centre.InsertFrame(kf, fWeight, null);
		for (int i = 0; i < numVert; i++)
			vertArr[i] = vertArr[i].InsertFrame(kf, fWeight, null);
		// reassign in case the head of the list has changed
	}

	public void NewHitBox(float x, float y) {
		Vertex[] transferArr = vertArr; // copy old array
		numVert++;
		numFace++;
		vertArr = new Vertex[numVert]; // make a new, slightly bigger array
		for (int i = 0; i < numVert - 1; i++)
			vertArr[i] = transferArr[i];
		Vertex v = new Vertex();
		if (numVert > 1) {
			v = transferArr[numVert - 2]; // last vert
			if ((v != null) && (v.vPtr >= 0))// frame ptr
				v = transferArr[v.vPtr];
			vertArr[numVert - 1] = new Vertex();
			while (v != null) {
				vertArr[numVert - 1].SetVertex(v.keyframe, x, y);
				v = v.next;
			} // loop through to add every frame
			vertArr[numVert - 2].SetBreak(true); // now needed to denote end of
													// last hitbox
			vertArr[numVert - 1].SetBreak(false); // make sure we fix the cloned
													// end vertex
		} else {// special case of no verts added
			vertArr[0] = new Vertex();
			vertArr[0].SetVertex(v.keyframe, x, y);
		}
	}

	public void SetPriority(float f, int p) {
		if (keyFrame != null)
			for (Frame pr = keyFrame; pr != null; pr = pr.next)
				if (pr.time == f)
					pr.value = p;
	}

	public void SetCentre(float f, float x, float y) {
		centre.SetVertex(f, x, y);

	}

	public void SetCentre(Frame f, float x, float y) {
		centre.SetVertex(f, x, y);

	}

	public void SetVertex(Frame frameSel, int vertSel, float x, float y) {
		if (vertArr[vertSel] == null)
			vertArr[vertSel] = new Vertex();
		vertArr[vertSel].SetVertex(frameSel, x, y);
	}

	public void SetVertexDir(float frameSel, int vertSel, float dir) {
		if (vertArr[vertSel] == null)
			vertArr[vertSel] = new Vertex();
		vertArr[vertSel].SetDir(frameSel, dir);
	}

	public void SetVertexDmg(float frameSel, int vertSel, float dmg) {
		if (vertArr[vertSel] == null)
			vertArr[vertSel] = new Vertex();
		vertArr[vertSel].SetDmg(frameSel, dmg);
	}

	public void SetVertexMag(float frameSel, int vertSel, float mag) {
		if (vertArr[vertSel] == null)
			vertArr[vertSel] = new Vertex();
		vertArr[vertSel].SetMag(frameSel, mag);
	}

	public void SetVertexWgt(float frameSel, int vertSel, float wgt) {
		if (vertArr[vertSel] == null)
			vertArr[vertSel] = new Vertex();
		vertArr[vertSel].SetWgt(frameSel, wgt);
	}

	public void DeleteVertex(int iVert) {
		Vertex[] transferArr = vertArr;
		Vertex v;
		if (numVert <= 0) // sanity check
			return;
		vertArr = new Vertex[numVert - 1]; // make a new, slightly smaller array
		for (int i = 0; i < numVert; i++) {
			if (i < iVert) {
				vertArr[i] = transferArr[i];
			} else if (i == iVert) {
				v = transferArr[i];
				if (v.vPtr >= 0)// vtx ptr
					v = transferArr[v.vPtr];
				if (v.getBreak())// special case for deleting endpoints
					if ((i == 0) || (transferArr[i - 1].getBreak()))// special
																	// case for
																	// deleting
																	// hitboxes
																	// (last
																	// vert in
																	// group)
						numFace--;
					else if (i != 0) // else mark the last vertex as the final
						vertArr[i - 1].SetBreak(true);
			} else {
				vertArr[i - 1] = transferArr[i];
				if (vertArr[i - 1].vPtr == iVert) {// points to deleted vertex
					v = transferArr[iVert];
					v.SetBreak(false);
					if (vertArr[i - 1].getBreak())
						v.SetBreak(true);
					vertArr[i - 1] = v;// make it the vertex we just deleted
				}
				if (vertArr[i - 1].vPtr > iVert) // check if pointed vertex
													// needs fix
					vertArr[i - 1].vPtr--;
			}
		}
		numVert--;
		// make sure the last vertex has no break.
		if (numVert > 0)
			vertArr[numVert - 1].SetBreak(false);
	}

	public void ClearVerts() {
		numVert = 0;
		vertArr = null;
	}

	public Vertex GetCentre(float frameSel) {
		Vertex c = centre;
		Vertex cRet = new Vertex();
		Frame retF = new Frame();
		// function vars
		float lastX = 0, lastY = 0;
		float fwdX = 0, fwdY = 0, lastFrame = 0, aWgt = 0, fwdFrame = 0, xLoc = 0, yLoc = 0;
		if((vertArr == null)||(c.keyframe==null)){
			cRet=c;
			cRet.keyframe = new Frame(frameSel, 0);
			return cRet;}
		else {
			// potential fix
			lastX = c.xLoc;
			lastY = c.yLoc;

			lastFrame = c.keyframe.time;
			while ((c.next != null) && (frameSel >= c.keyframe.time)) { // a
																		// values
																		// for
																		// interpolation
				lastX = c.xLoc;
				lastY = c.yLoc;
				lastFrame = c.keyframe.time;
				c = c.next;
			}
			fwdX = c.xLoc;
			fwdY = c.yLoc;
			fwdFrame = c.keyframe.time;
			if (lastFrame - c.keyframe.time == 0)
				aWgt = 0;
			else
				aWgt = (frameSel - lastFrame) / (fwdFrame - lastFrame); // alpha
																		// weight
																		// for
																		// interpolation
			if (aWgt > 1)
				aWgt = 1;
			xLoc = (1.f - aWgt) * lastX + aWgt * fwdX;
			yLoc = (1.f - aWgt) * lastY + aWgt * fwdY;
			// interpolation
			cRet.SetVertex(retF, xLoc, yLoc);
		}
		return cRet;
	}

	public Vertex GetVertAtFrame(int vSel, float frameSel) {
		// returns a vertex containing interpolated data
		Vertex v, vRet = new Vertex();
		Frame retF = new Frame();
		boolean isLast = false;
		// function vars
		float lastX = 0, lastY = 0, lastMag = 0, lastWgt = 0, lastDir = 0, lastDmg = 0, fwdMag = 0, fwdWgt = 0, fwdDir = 0;
		float fwdX = 0, fwdY = 0, fwdDmg = 0, lastFrame = 0, aWgt = 0, fwdFrame = 0, xLoc = 0, yLoc = 0;
		if (vertArr == null)
			return null;
		v = vertArr[vSel];
		isLast = v.getBreak();
		if (v.vPtr >= 0) {
			if (v.vPtr != vSel) {
				vRet.vPtr = v.vPtr;
				v = GetVertAtFrame(v.vPtr, frameSel);
			}
			xLoc = v.xLoc;
			yLoc = v.yLoc;
			vRet.SetVertex(retF, v.xLoc, v.yLoc);
		} else {
			// potential fix
			lastX = v.xLoc;
			lastY = v.yLoc;
			lastDir = v.dir;
			lastMag = v.mag;
			lastWgt = v.wgt;
			lastDmg = v.dmg;
			lastFrame = v.keyframe.time;
			while ((v.next != null) && (frameSel >= v.keyframe.time)) { // a
																		// values
																		// for
																		// interpolation
				lastX = v.xLoc;
				lastY = v.yLoc;
				lastDir = v.dir;
				lastMag = v.mag;
				lastWgt = v.wgt;
				lastDmg = v.dmg;
				lastFrame = v.keyframe.time;
				v = v.next;
			}
			fwdX = v.xLoc;
			fwdY = v.yLoc;
			fwdWgt = v.wgt;
			fwdMag = v.mag;
			fwdDir = v.dir;
			fwdDmg = v.dmg;
			fwdFrame = v.keyframe.time;
			if (lastFrame - v.keyframe.time == 0)
				aWgt = 0;
			else
				aWgt = (frameSel - lastFrame) / (fwdFrame - lastFrame); // alpha
																		// weight
																		// for
																		// interpolation
			if (aWgt > 1)
				aWgt = 1;
			xLoc = (1.f - aWgt) * lastX + aWgt * fwdX;
			yLoc = (1.f - aWgt) * lastY + aWgt * fwdY;
			if ((fwdDir - lastDir) > Math.PI)// these angles span the boundary
												// at rad = PI = -PI
				lastDir += 2 * Math.PI;
			else if (lastDir - fwdDir > Math.PI)
				fwdDir += 2 * Math.PI; // bump up for interpolation
			// interpolation
			vRet.SetVertex(retF, xLoc, yLoc);
			vRet.SetMag(0, (1.f - aWgt) * lastMag + aWgt * fwdMag);
			vRet.SetWgt(0, (1.f - aWgt) * lastWgt + aWgt * fwdWgt);
			vRet.SetDir(0, (1.f - aWgt) * lastDir + aWgt * fwdDir);
			vRet.SetDmg(0, (1.f - aWgt) * lastDmg + aWgt * fwdDmg);
		}
		vRet.SetBreak(isLast);
		return vRet;
	}

	public boolean CheckVtxBounds(float frameSel, int aSel, int vSel) {
		boolean retFlag = false;
		if ((aSel < 0) || (vSel < 0))
			return false;
		if ((VertSize() > 2) && (aSel >= 0) && (vSel >= 0)) {
			retFlag = true;
			Vertex lV, cV, nV; // last vert current vert next vert
			lV = null;
			nV = null;
			int first = 0;
			int lst = VertSize() - 1;
			for (int i = vSel; i < VertSize(); i++)
				if (GetVertAtFrame(i, frameSel).getBreak()) {
					lst = i;
					i = VertSize();// force exit
				}
			for (int i = vSel - 1; i >= 0; i--)
				if (GetVertAtFrame(i, frameSel).getBreak()) {
					first = i + 1;
					i = -1;// force exit
				}
			float lAng, nAng;
			for (int i = first; i <= lst; i++) {
				if (first == lst) {
					// do nothing
					return true;
				} else if (i == first) {
					lV = GetVertAtFrame(lst, frameSel);
					nV = GetVertAtFrame(i + 1, frameSel);
				} else if (i == lst) {
					lV = GetVertAtFrame(i - 1, frameSel);
					nV = GetVertAtFrame(first, frameSel);
				} else {
					lV = GetVertAtFrame(i - 1, frameSel);
					nV = GetVertAtFrame(i + 1, frameSel);
				}
				cV = GetVertAtFrame(i, frameSel);
				lAng = (float) Math.atan2(cV.yLoc - lV.yLoc, cV.xLoc - lV.xLoc);
				nAng = (float) Math.atan2(nV.yLoc - cV.yLoc, nV.xLoc - cV.xLoc);
				if (lAng > nAng) { // convex or ctr-clockwise property
									// potentially broken
					if (Math.abs(lAng - nAng) < Math.PI)// exclude condition for
														// angle wraparound
						retFlag = false;
				} else {// special wrapAround case
					if (nAng - lAng > Math.PI)
						retFlag = false;
				}
			}
		} else
			// only 2 or less;
			retFlag = true;
		return retFlag;
	}

	public void MoveMultipleVerts(float frameSel, int hbStart, int hbEnd,
			float moveX, float moveY) {
		if ((hbStart >= 0) && (hbEnd >= 0)) {
			Vertex v;
			float xDiff = 0.f, yDiff = 0.f;
			float startX = GetVertAtFrame(hbStart, frameSel).xLoc;
			float startY = GetVertAtFrame(hbStart, frameSel).yLoc;
			for (int i = hbStart; i <= hbEnd; i++) {
				xDiff = GetVertAtFrame(i, frameSel).xLoc - startX;
				yDiff = GetVertAtFrame(i, frameSel).yLoc - startY;
				v = vertArr[i];
				while (v.vPtr >= 0)
					v = vertArr[v.vPtr];// GetVertAtFrame(aSel, (int)v.xLoc,
										// frameSel);
				v.MoveVertex(frameSel, moveX + xDiff, moveY + yDiff);

			}
		}
	}

	public Track(int nid) {
		keyFrame = null;
		vertArr = null;
		numVert = 0;
		centre = new Vertex(0, 0, null);
		SetId(nid + 1);
	}

	public Track(Track o) {
		// a function to clone from another track
		numVert = o.numVert;
		numFace = o.numFace;
		vertArr = new Vertex[numVert];
		keyFrame = new Frame();
		Frame k = keyFrame;
		for (Frame f = o.keyFrame; f != null; f = f.next) {
			k.time = f.time;
			k.value = f.value;
			if (f.next != null) {
				k.next = new Frame();
				k = k.next;
			}
		}
		this.centre=new Vertex(o.centre, keyFrame);
		for (int j = 0; j < numVert; j++)
			this.vertArr[j] = new Vertex(o.vertArr[j], keyFrame);
		next = o.next;
		id = o.id;
		if (id == 0)
			trType = o.trType;
		else
			trType = 0;
		if(o.next!=null)//copy additional tracks if they exist as well
			next = new Track(o.next);
	}

	public int FaceSize() {
		return numFace;
	}

	public int VertSize() {
		return numVert;
	}

	public int GetId() {
		return id;
	}

	public void SetId(int id) {
		this.id = id;
	}

	public void SetCentreFrames() {
		// in case there are no centre frames (advancing a file from legacy)
		// this will set up everyting needed

		centre = new Vertex(0.f, 0.f, keyFrame);
		Vertex c = centre;
		if (keyFrame != null)
			for (Frame k = keyFrame.next; k != null; k = k.next) {
				c.next = new Vertex(0.f, 0.f, k);
				c = c.next;
			}

	}

}
