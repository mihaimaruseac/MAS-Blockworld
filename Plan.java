import java.util.Vector;

public class Plan {
	private Vector<Action> acts;

	public Plan() {
		acts = new Vector<Action>();
	}

	public boolean isEmpty() {
		return acts.isEmpty();
	}

	public String toString() {
		String s = "[";
		for (Action a : acts)
			s += a + "; ";
		s += "]";
		return s;
	}

	public boolean equals(Object o) {
		return equals((Plan) o);
	}

	private boolean equals(Plan o) {
		return toString().equals(o.toString());
	}
	
	public void getSimplePlan(BlockStack wFocus, BlockStack intention) {
		char b = wFocus.getTop();

		if (wFocus.height() == 1)
			acts.add(Action.buildPickupAction(b));
		else
			acts.add(Action.buildUnstackAction(b, wFocus.getSubTop()));

		if (intention.height() == 1)
			acts.add(Action.buildPutdownAction(b));
		else
			acts.add(Action.buildStackAction(b, intention.getSubTop()));
	}

	public void getComposedPlan(World w, BlockStack intention) {
		char top = intention.getTop();
		BlockStack bs = w.findStackWith(top);
		if (bs == null)
			return; /* empty plan again */

		acts.addAll(bs.unstackUntil(top));

		if (intention.height() > 1) {
			top = intention.getSubTop();
			bs = w.findStackWith(top);
			if (bs == null)
				return;
			acts.addAll(bs.unstackUntil(top));
		}
	}

	public void getComposedPlan(World w, int maxSteps, BlockStack intention) {
		getComposedPlan(w, intention);

		if (acts.size() > maxSteps)
			acts.setSize(maxSteps);

		for (int i = acts.size(); i < maxSteps; i++)
			acts.add(Action.NoAction);
	}

	public Action peekStep(World w) {
		while (acts.size() != 0) {
			Action a = acts.get(0);
			if (!a.isApplicable(w)) {
				acts.remove(0);
				acts.remove(0);
			} else
				return a;
		}
		return Action.NoAction;
	}
	
	public void popStep() {
		if (acts.size() != 0)
			acts.remove(0);
	}

	public void clear() {
		acts.clear();
	}

	public Plan copyPlan() {
		Plan p = new Plan();
		for (Action a : acts)
			p.acts.add(a);
		return p;
	}

	public boolean passable() {
		for (Action a : acts)
			if (!a.equals(Action.NoAction))
				return false;
		acts.clear();
		return true;
	}

	public void completeIntention1(World w, BlockStack intention) {
		char top = intention.getTop();
		BlockStack bs = w.findStackWith(top);
		if (bs.onTable(top))
			acts.add(Action.buildPickupAction(top));
		else
			acts.add(Action.buildUnstackAction(top, bs.getUnder(top)));

		if (intention.height() > 1) {
			char subTop = intention.getSubTop();
			bs = w.findStackWith(subTop);
			acts.add(Action.buildStackAction(top, subTop));
		} else
			acts.add(Action.buildPutdownAction(top));
	}

	public void completeIntention2(World w, BlockStack intention) {
		if (w.sameOrder(intention))
			completeIntention1(w, intention.getMaxSubIntention());
		char top = intention.getTop();
		char subTop = intention.getSubTop();
		acts.add(Action.buildPickupAction(top));
		acts.add(Action.buildStackAction(top, subTop));
	}

	public void interleave(Plan p1, Plan p2) {
		p1.clear();
		p2.clear();
		p2.acts.add(Action.NoAction);

		Plan crt = p1;
		boolean sw = false;
		for (Action a : acts) {
			crt.acts.add(a);
			sw = !sw;
			if (!sw) {
				if (crt == p1)
					crt = p2;
				else
					crt = p1;
				sw = false;
			}
		}

		if (p1.acts.size() == p2.acts.size() - 1)
			p1.acts.add(Action.NoAction);
		else if (p2.acts.size() +1 == p1.acts.size())
			p2.acts.add(Action.NoAction);
		else
			System.err.println("Strange interleaving");
	}
}
