import java.util.Vector;

public class BlockStack {
	private Vector<Character> stack;
	private boolean fromTable;

	public BlockStack(boolean onTable) {
		fromTable = onTable;
		stack = new Vector<Character>();
	}

	public void stackBlock(char a) {
		stack.addElement(new Character(a));
	}

	public void unStackBlock(char a) {
		stack.removeElementAt(stack.size() - 1);
	}

	public boolean top(char a) {
		return stack.elementAt(stack.size() - 1).equals(new Character(a));
	}

	public BlockStack getMaxSubIntention() {
		if (stack.size() == 0)
			return null;

		BlockStack b = new BlockStack(fromTable);
		for (Character c : stack)
			b.stack.add(c);
		b.stack.removeElementAt(b.stack.size() - 1);
		return b;
	}

	public boolean onTable(char a) {
		Character ca = new Character(a);
		for (int i = 0; i < stack.size(); i++)
			if (stack.elementAt(i).equals(ca))
				return i == 0;
		return false;
	}

	public char getUnder(char a) {
		Character ca = new Character(a);
		for (int i = 1; i < stack.size(); i++)
			if (stack.elementAt(i).equals(ca))
				return stack.elementAt(i - 1).charValue();
		return '?';
	}

	public int height() {
		return stack.size();
	}

	public char getLevel(int i) {
		return stack.elementAt(i - 1).charValue();
	}

	public char getTop() {
		return stack.elementAt(stack.size() - 1).charValue();
	}

	public char getSubTop() {
		return stack.elementAt(stack.size() - 2).charValue();
	}

	public boolean removeTop() {
		stack.remove(stack.size() - 1);
		return stack.size() == 0;
	}

	public String toString() {
		String s = fromTable ? "|" : " ";
		for (Character c : stack)
			s += c.charValue();
		return s;
	}

	public boolean equals(Object other) {
		String thiss = this.toString();
		String others = ((BlockStack)other).toString();
		return thiss.equals(others);
	}

	public Vector<BlockStack> getStacks() {
		Vector<BlockStack> v = new Vector<BlockStack>();
		Vector<Character> seen = new Vector<Character>();

		for (Character c : stack) {
			BlockStack bs = new BlockStack(true);
			seen.add(c.charValue());
			for (Character c1 : seen)
				bs.stackBlock(c1.charValue());
			v.add(bs);
		}

		return v;
	}

	public Vector<Character> getAllBlocks() {
		return stack;
	}

	public boolean hasBlock(char code) {
		return stack.contains(new Character(code));
	}

	public boolean hasBlock(Character code) {
		return stack.contains(code);
	}

	public Vector<Action> unstackUntil(char code) {
		Vector<Action> acts = new Vector<Action> ();

		for (int i = stack.size() - 1; i > 0; i--) {
			char c = stack.elementAt(i).charValue();
			if (c == code)
				return acts;
			else {
				acts.add(Action.buildUnstackAction(c, stack.elementAt(i-1).charValue()));
				acts.add(Action.buildPutdownAction(c));
			}
		}
		return acts;
	}
}
