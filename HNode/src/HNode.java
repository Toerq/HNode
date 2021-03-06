import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;


public class HNode {
	static HNode head;
	
	FSet[] buckets;
	int size;
	HNode pred;
	
	
	public HNode(int tableSize) {
		this.buckets = new FSet[tableSize];
		for(int i = 0; i < tableSize; i++) {
			this.buckets[i] = new FSet();
		}
		this.size = tableSize;
		this.pred = null;
	}
	
	boolean insert(int k) {
		boolean resp = apply(FSetOp.OP.INS, k);
		return resp;
				
	}

	boolean contains(int k) {
		HNode t = HNode.head;
		FSet b = t.buckets[k % t.size];
		if (b == null) {
			HNode s = t.pred;
			if (s != null) {
				b = s.buckets[k % s.size];
			} else {
				b = t.buckets[k % t.size];
			}
		}
		return hasMember(b, k);
	}
	
	boolean hasMember(FSet b, int k) {
		return b.node.get().set.contains(k);
	}

	boolean apply(FSetOp.OP type, int k) {
		FSetOp op = new FSetOp(type, k, false);
		HNode t;
		FSet b;
		while (true) {
			t = HNode.head;
			b = t.buckets[k % t.size];
			if (b == null) {
				b = initBucket(t, k % t.size);
			}
			if (b.invoke(b, op)) {
				boolean q = b.node.get().set.contains(4);
				System.out.println("asd" + q);
				return op.resp;
			}
		}
	}

	private HashSet<Integer> newSet(HashSet<Integer> set, int hashIndex) {
		HashSet<Integer> newSet = new HashSet<Integer>();
		Iterator<Integer> iterator = set.iterator();
		int setSize = set.size();
		int currentElement;
		while(iterator.hasNext()) {
			currentElement = iterator.next();
			if (currentElement % setSize == hashIndex) {
				newSet.add(currentElement);
			}
		}
		return newSet;
	}
	
	private FSet initBucket(HNode t, int hashIndex) {
		FSet b = t.buckets[hashIndex];
		HNode s = t.pred;
		FSet m, n;
		HashSet<Integer> set;
		if (b == null && s != null) {
			System.out.println("inside");
			if (t.size == s.size * 2) { // Growing
				m = s.buckets[hashIndex % s.size];
				m.freeze();
				set = newSet(m.node.get().set, hashIndex);
			} else { // Shrinking
				m = s.buckets[hashIndex];
				n = s.buckets[hashIndex + t.size];
				m.freeze();
				n.freeze();
				set = new HashSet<Integer>(m.node.get().set);
				set.addAll(n.node.get().set);
			}
			FSetNode b_ = new FSetNode(set, true);
			t.buckets[hashIndex].node.compareAndSet(null, b_);
		}
		return t.buckets[hashIndex];
	}
	
}
class FSetOp {
	OP type;
	int key;
	boolean done;
	boolean resp;
	
	public enum OP { INS, REM; }
	public FSetOp(OP type, int k, boolean done) {
		this.type = type;
		this.key = k;
		this.done = done;
	}
	
}

class FSet {
	AtomicReference<FSetNode> node;
	
	public FSet() {
		FSetNode node = new FSetNode();
		this.node = new AtomicReference<FSetNode>(node);
	}
	
	HashSet<Integer> freeze() {
		FSetNode o = this.node.get();
		while (o.ok) {
			FSetNode n = new FSetNode(o.set, false);
			if (this.node.compareAndSet(o, n)) {
				break;
			}
			o = this.node.get();
		}
		return o.set;
	}
		
	boolean hasMember(FSetNode b, int key) {
		return b.set.contains(key);
	}
	
	boolean getResponse(FSetOp op) {
		return op.resp;
	}
	

    boolean invoke(FSet b, FSetOp op) {
		FSetNode o = b.node.get();
		boolean resp = false;
		HashSet<Integer> set = null;
		FSetNode n;
		while (o.ok) {
			if (op.type == FSetOp.OP.INS) {
				resp = !(hasMember(o, op.key));
				if (resp) {
					set = new HashSet<Integer>(o.set);
					set.add(op.key);
				}
			} else if (op.type == FSetOp.OP.REM) {
				resp = hasMember(o, op.key);//o.set.contains(op.key);
				if (resp) {
					set = new HashSet<Integer>(o.set);
					set.remove(op.key);
				}
			}
			n = new FSetNode(set, true);
			if (b.node.compareAndSet(o, n)) {
				System.out.println("aa" + b.node.get().set.contains(op.key));
				System.out.println("inside");
				op.resp = resp;
				return true;
				
			}
			o = b.node.get();
		}
		return false;
	}
	
}

class FSetNode {
	HashSet<Integer> set;
	boolean ok;
	
	public FSetNode() {
		this.set = new HashSet<Integer>();
		this.ok = true;
	}

	public FSetNode(HashSet<Integer> HashSet, boolean ok) {
		this.set = set;
		this.ok = ok;
	}
	
	static void printFunction(HNode h) {
		for(int i = 0; i < h.buckets.length; i++) {
			System.out.println("Bucket " + i);
			Iterator<Integer> k = h.buckets[i].node.get().set.iterator();
			while(k.hasNext()) {
				System.out.println(k.next());
			}
			
		}
	}
	/*
	public invoke(FSet b, FSetOp op) {
		
	}
	*/
	public static void main(String[] args) {
		FSetNode n = new FSetNode();
		HNode h = new HNode(64);
		FSetNode q = h.buckets[53].node.get();
		q.ok = false;
		HNode.head = h;
		h.apply(FSetOp.OP.INS, 3);
		printFunction(h);
	}
		
}