import java.util.Vector;

public class Arm implements IArm {
	private World goal;
	/* B: w in act
	 * D: goal
	 * I: create (goal for now)
	 */
	private Vector<BlockStack> I;
	private Plan p;
	private Vector<Plan> lastFailed;
	private String name;
	private boolean newActionRequired;
	private ArmSlave slave;
	private Plan pSlave;

	public Arm (String name, World goal) {
		this.goal = goal;
		this.name = name;

		p = new Plan();
		lastFailed = new Vector<Plan>();
		newActionRequired = true;

		slave = null;
		pSlave = new Plan();
	}

	public void setSlave(ArmSlave s) {
		slave = s;
	}

	public Plan getSlavePlan() {
		return pSlave;
	}

	public boolean done(World state) {
		return goal.isGoalIn(state);
	}

	public Action act(World w) {
		/* no replans if we know we will succeed */
		if (!newActionRequired)
			return p.peekStep(w);

		if (p.isEmpty())
			getInitialPlan(w);

		System.out.println(name + " tries plan: " + p);

		newActionRequired = false;
		return p.peekStep(w);
	}

	public void notifyDone() {
		System.out.println(name + " does plan: " + p);
		p.popStep();
		lastFailed.clear();
		newActionRequired = true; /* need another step */
	}

	public void notifyWillFail() {
		System.out.println(this + " with plan " + p + " will fail (last failed: " + lastFailed + ")");
		lastFailed.add(p.copyPlan());
		p.clear();
		newActionRequired = true; /* need another plan */
	}

	private void getInitialPlan(World B) {
		options(B);
		if (filter(B))
			return; /* shortcut */
		if (I.size() == 0)
			return; /* no intention -> no plan */
		plan(B);
	}

	private void options(World B) {
		/* do nothing: only the goal is in D */
	}

	private boolean filter(World B) {
		I = goal.getStacks();
		for (BlockStack b : B.getStacks())
			I.remove(b);

		if (I.size() == 0)
			return true; /* no intention */

		/* short-cut for one step if we have a slave */
		if (slave != null && canShortCut(B))
			return true;

		int size = I.get(0).height(); /* sure to have one element */
		for (BlockStack b : I)
			if (size > b.height())
				size = b.height();

		Vector<BlockStack> toBeRemoved = new Vector<BlockStack>();
		for (BlockStack b : I)
			if (size < b.height())
				toBeRemoved.add(b);
		I.removeAll(toBeRemoved);
		return false;
	}

	private boolean canShortCut(World B) {
		/* return true if there is an intention containing two blocks on
		 * the same stack. As a side effect create plans for slave and
		 * self.
		 */
		for (BlockStack b : I) {
			if (b.height() < 2)
				continue;
			if (B.sameStackAndApplicable(b)) {
				Plan mixed = new Plan();
				mixed.getComposedPlan(B, b);
				mixed.completeIntention2(B, b);
				mixed.interleave(p, pSlave);
				if (isGoodPlan(p) && isGoodPlan(pSlave))
					return true;
			}
		}
		return false;
	}

	private void plan(World B) {
		if (slave == null) {
			doPlanForMeOnly(B);
			return;
		}

		if (constructSimpleSubplans(B, true)) {
			if (constructSimpleSubplans(B, false))
				return;
			getComplexSubplans(B, 2);
			return;
		}

		if (constructInterleavePlan(B))
			return;

		System.err.println("Not touched");
		doPlanForMeOnly(B); /* failsafe */
	}

	private boolean constructSimpleSubplans(World B, boolean master) {
		for (BlockStack b : I) {
			BlockStack a = B.achievableVia(b);
			if (a != null) {
				if (master) {
					p.getSimplePlan(a, b);
					if (isGoodPlan(p))
						return true;
				} else {
					pSlave.getSimplePlan(a, b);
					if (pSlave.equals(p)) {
						pSlave.clear();
						continue;
					}
					if (isGoodPlan(pSlave))
						return true;
				}
			}
		}
		return false;
	}

	private boolean constructInterleavePlan(World B) {
		for (int i = 0; i < I.size(); i++) {
			Plan mixed = new Plan();
			mixed.getComposedPlan(B, I.get(i));
			mixed.completeIntention1(B, I.get(i));
			mixed.interleave(p, pSlave);
			if (isGoodPlan(p) && isGoodPlan(pSlave))
				return true;
		}
		return false;
	}

	private void getComplexSubplans(World B, int maxSteps) {
		for (int i = 0; i < I.size(); i++) {
			pSlave.getComposedPlan(B, maxSteps, I.get(i));
			if (isGoodPlan(pSlave) && !pSlave.passable())
				return;
		}
	}

	private void getComplexSubplans(World B) {
		for (int i = 0; i < I.size(); i++) {
			p.getComposedPlan(B, I.get(i));
			if (isGoodPlan(p))
				return;
		}
	}

	private void doPlanForMeOnly(World B) {
		if (constructSimpleSubplans(B, true))
			return;
		getComplexSubplans(B);
	}

	private boolean isGoodPlan(Plan p) {
		if (lastFailed.contains(p)) /* don't repeat failed plans */
			p.clear();
		return !p.isEmpty();
	}

	public String toString() {
		return name;
	}
}
