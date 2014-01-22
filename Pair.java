class Pair<F, S> {
	private F o1;
	private S o2;

	public Pair(F o1, S o2) {
		this.o1 = o1;
		this.o2 = o2;
	}

	public F fst() {
		return o1;
	}

	public S snd() {
		return o2;
	}

	public String toString() {
		return "(" + o1.toString() + ", " + o2.toString() + ")";
	}
}
