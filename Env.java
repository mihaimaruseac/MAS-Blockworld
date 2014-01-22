import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;
import java.util.Vector;

public class Env {
	private float sleep;
	private Vector<IArm> arms;
	private World state;
	private IArm tokenAgent, nextTokenAgent;
	private Vector<Pair<IArm, Integer>> difficulty;
	private Vector<Pair<IArm, Float>> scores;

	public Env(float sleep, World w) {
		this.sleep = sleep;
		state = w;
		arms = new Vector<IArm>();
		difficulty = new Vector<Pair<IArm, Integer>>();
		scores = new Vector<Pair<IArm, Float>>();
		tokenAgent = null;
	}

	public void addArm(IArm a, World goal) {
		difficulty.add(new Pair<IArm, Integer>(a, new Integer(getDifficulty(goal))));
		if (tokenAgent == null)
			tokenAgent = a;
		arms.add(a);
	}

	private int getDifficulty(World goal) {
		return goal.getDifficulty(state);
	}

	private Vector<Pair<IArm, Action>> allRobotActions() {
		Vector<Pair<IArm, Action>> actpairs = new Vector<Pair<IArm, Action>>();
		/* until we have no conflicts */
		while (true) {
			/* let each arm act */
			actpairs.clear();
			for (IArm arm : arms)
				actpairs.add(new Pair<IArm, Action>(arm, arm.act(state)));

			boolean conflicts = false;
			for (int i = 0; i < actpairs.size(); i++) {
				Pair<IArm, Action> p = actpairs.get(i);
				Action a = p.snd(); IArm _a = p.fst();
				for (int j = i + 1; j < actpairs.size(); j++) {
					Pair<IArm, Action> p1 = actpairs.get(j);
					Action a1 = p1.snd(); IArm _a1 = p1.fst();
					if (a.conflicting(a1))
						conflicts = solveConflict(_a, _a1);
				}
			}

			if (!conflicts)
				break;
		}

		tokenAgent = nextTokenAgent;

		/* notify arm that it can carry out it's action */
		for (IArm arm : arms)
			arm.notifyDone();

		return actpairs;
	}

	private boolean solveConflict(IArm a1, IArm a2) {
		if (tokenAgent.equals(a1)) {
			nextTokenAgent = a2;
			a2.notifyWillFail();
		} else {
			nextTokenAgent = a1;
			a1.notifyWillFail();
		}
		return true;
	}

	private boolean filterDone(int time) {
		Vector<IArm> toBeRemoved = new Vector<IArm>();
		for (IArm arm : arms)
			if (arm.done(state)) {
				toBeRemoved.add(arm);
				Integer dif = new Integer(0);
				for (Pair<IArm, Integer> diffp : difficulty)
					if (arm.equals(diffp.fst())) {
						dif = diffp.snd();
						break;
					}
				float score = (float)(dif.intValue()) - (float)(time / 2.0);
				scores.add(new Pair<IArm, Float>(arm, new Float(score)));
				System.out.println("Agent " + arm + ": difficulty: "
						+ dif + " time: " + time +
						" score: " + score);
			}
		arms.removeAll(toBeRemoved);
		return arms.size() == 0;
	}

	private void mainLoop() {
		int time = 0;
		System.out.println("Initial state: ");
		System.out.println(this);
		while (true) {
			/* update world */
			time++;
			for (Pair<IArm, Action> aa : allRobotActions())
				aa.snd().changeWorld(state, aa.fst());
			/* update GUI */
			System.out.println("Time: " + time);
			System.out.println(this);
			/* remove agents which fullfilled their goals */
			if (filterDone(time))
				break;
			/* sleep */
			Util.getUtil().sleep(sleep);
		}
		System.out.println("Finished: ");
		for (Pair<IArm, Float> pai : scores)
			System.out.println("Agent " + pai.fst() + " has score " + pai.snd());
	}

	public String toString() {
		String s = "\nToken: " + tokenAgent + "\n\n";
		s += state + "\n";
		return s;
	}

	private static World readWorld (String fileName) {
		Vector<String> lines = new Vector<String> ();
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String s;
			while ((s = in.readLine()) != null)
				if (!s.equals(""))
					lines.add(0, s);
			in.close();
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
		World w = new World();
		String last = lines.remove(0);
		for (int i = 0; i < last.length(); i++)
			if (Util.getUtil().isBlockId(last.charAt(i)))
				w.addDown(last.charAt(i));
		for (String s : lines) {
			for (int i = 0; i < s.length(); i++) {
				if (Util.getUtil().isBlockId(s.charAt(i))) {
					if (!Util.getUtil().isBlockId(last.charAt(i))) {
						System.err.println("Invalid world");
						return null;
					}
					w.addOver(s.charAt(i), last.charAt(i));
				}
			}
			last = s;
		}
		return w;
	}

	public static void main(String args[]) {
		World wi = readWorld("si.txt");
		if (wi == null)
			return;
		Env e = new Env(Float.parseFloat(args[0]), wi);

		World wf1 = readWorld("sf1.txt");
		if (wf1 == null)
			return;
		Arm master1 = new Arm("*1", wf1);
		e.addArm(master1, wf1);
		e.addArm(new ArmSlave("*1s", master1), wf1);

		/* Disable for this assignment
		World wf2 = readWorld("sf2.txt");
		if (wf2 == null)
			return;
		e.addArm(new Arm("*2", wf2), wf2);
		*/

		e.mainLoop();
	}
}
