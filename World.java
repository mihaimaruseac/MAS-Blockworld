import java.util.Vector;

public class World {
	private Vector<BlockStack> stacks;
	private Vector<Pair<IArm, Character>> holdings;

	public World() {
		this.stacks = new Vector<BlockStack>();
		holdings = new Vector<Pair<IArm, Character>>();
	}

	public boolean equals(World other) {
		return included(stacks, other.stacks) && included(other.stacks, stacks);
	}

	public boolean isGoalIn(World full) {
		return full.isGoal(stacks);
	}

	/* used for intentions */
	public boolean isGoal(Vector<BlockStack>targets) {
		return included(targets, stacks);
	}

	private boolean included(Vector<BlockStack> who, Vector<BlockStack> where) {
		boolean found = false;
		for (BlockStack bs : who) {
			found = false;
			for (BlockStack bs1 : where)
				if (bs.equals(bs1)) {
					found = true;
					break;
				}
			if (!found)
				return false;
		}
		return true;
	}

	public int getDifficulty(World initial) {
		Vector<Character> badBlocks = new Vector<Character>();

		/* check each block of ours */
		for (BlockStack bs : stacks) {
			Vector<Character> blocks = bs.getAllBlocks();
			for (Character c : blocks) {
				/* test initial blocks */
				for (BlockStack bsi : initial.stacks)
					if (bsi.hasBlock(c)) {
						Vector<Character> iblocks = bsi.getAllBlocks();
						/* start marking bad blocks */
						markBadBlocks(c, blocks, iblocks, badBlocks);
					}
			}
		}

		return badBlocks.size();
	}

	private void markBadBlocks(Character c, Vector<Character> gb, Vector<Character> ib, Vector<Character> bb) {
		/* scan below */
		for (Character ic : ib) {
			if (c.equals(ic))
				break;
			/* search below */
			boolean found = false;
			for (Character gc : gb) {
				if (c.equals(gc))
					break;
				if (ic.equals(gc)) {
					found = true;
					break;
				}
			}
			if (!found)
				if (!bb.contains(ic))
					bb.add(ic);
		}

		/* scan above */
		boolean found_start = false;
		for (Character ic : ib) {
			if (c.equals(ic)) {
				found_start = true;
				continue;
			} else if (!found_start) {
				continue;
			}
			/* search above */
			boolean found = false;
			boolean found_start_g = false;
			for (Character gc : gb) {
				if (c.equals(gc)) {
					found_start_g = true;
					continue;
				} else if (!found_start_g) {
					continue;
				}
				if (ic.equals(gc)) {
					found = true;
					break;
				}
			}
			if (!found)
				if (!bb.contains(ic))
					bb.add(ic);
		}
	}

	public void addDown(char code) {
		BlockStack bs = new BlockStack(true);
		bs.stackBlock(code);
		this.stacks.add(bs);
	}

	public void addDown(char code, IArm a) {
		addDown(code);
		clearHolding(a);
	}

	public void addOver(char newBlock, char oldBlock) {
		for (BlockStack bs : this.stacks)
			if (bs.top(oldBlock)) {
				bs.stackBlock(newBlock);
				return;
			}
		System.err.println("Adding " + newBlock + " on table instead.");
		addDown(newBlock);
	}

	public void addOver(char newBlock, char oldBlock, IArm a) {
		addOver(newBlock, oldBlock);
		clearHolding(a);
	}

	private void clearHolding(IArm a) {
		Vector<Pair<IArm, Character>> toBeRemoved = new Vector<Pair<IArm, Character>>();
		for (Pair<IArm, Character> pac : holdings)
			if (a.equals(pac.fst()))
				toBeRemoved.add(pac);
		holdings.removeAll(toBeRemoved);
	}

	public void pick(char code, IArm a) {
		Vector<BlockStack> toBeRemoved = new Vector<BlockStack>();
		for (BlockStack bs : stacks)
			if (bs.top(code)) {
				if (bs.removeTop())
					toBeRemoved.add(bs);
				break;
			}
		stacks.removeAll(toBeRemoved);
		holdings.add(new Pair<IArm, Character>(a, new Character(code)));
	}

	public BlockStack findStackWith(char code) {
		for (BlockStack bs : stacks)
			if (bs.hasBlock(code))
				return bs;
		return null;
	}

	public String toString() {
		String s = "";
		int height = 0;

		for (Pair<IArm, Character> pac : holdings)
			s += "[" + pac.snd() + " @ " + pac.fst() + "], ";
		s += "\n";

		for (BlockStack bs : stacks)
			if (bs.height() > height)
				height = bs.height();
		
		for (int i = height; i > 0; i--) {
			s += i + "\t";
			for (BlockStack bs : stacks)
				if (bs.height() >= i)
					s += bs.getLevel(i);
				else
					s += " ";
			s += "\n";
		}
		return s;
	}

	public Vector<BlockStack> getStacks() {
		Vector<BlockStack> v = new Vector<BlockStack>();

		for (BlockStack bs : stacks) {
			Vector<BlockStack> tmp = bs.getStacks();
			for (BlockStack bss : tmp)
				v.add(bss);
		}

		return v;
	}

	public BlockStack achievableVia1(char top) {
		for (BlockStack bs : stacks)
			if (bs.top(top))
				return bs;

		return null;
	}

	public BlockStack achievableVia2(char top, char subTop) {
		BlockStack bs = achievableVia1(top);
		if (bs == null)
			return null;

		if (achievableVia1(subTop) != null)
			return bs;

		return null;
	}

	public BlockStack achievableVia(BlockStack intention) {
		if (intention.height() == 1)
			return achievableVia1(intention.getTop());

		return achievableVia2(intention.getTop(), intention.getSubTop());
	}

	public boolean isFree(char code) {
		return achievableVia1(code) != null;
	}

	public boolean sameStackAndApplicable(BlockStack intention) {
		char top = intention.getTop();
		char subTop = intention.getSubTop();
		BlockStack bs = findStackWith(top);

		if (bs.getTop() != top && bs.getTop() != subTop)
			return false;

		if (bs != findStackWith(subTop))
			return false;

		if (intention.onTable(subTop))
			return true;

		return false;
	}

	public boolean sameOrder(BlockStack intention) {
		char top = intention.getTop();
		char subTop = intention.getSubTop();
		BlockStack bs = findStackWith(top);
		return bs.getTop() == top;
	}
}
