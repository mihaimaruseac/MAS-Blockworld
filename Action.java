public class Action {
	private ActionType type;
	private char b1, b2;
	public static Action NoAction = buildNoAction();

	private Action() {
	}

	private static Action buildNoAction() {
		Action a = new Action();
		a.type = ActionType.NoAction;
		a.b1 = a.b2 = '?';
		return a;
	}

	public static Action buildPickupAction(char _a) {
		Action a = new Action();
		a.type = ActionType.Pickup;
		a.b1 = _a;
		a.b2 = '?';
		return a;
	}

	public static Action buildPutdownAction(char _c) {
		Action a = new Action();
		a.type = ActionType.Putdown;
		a.b1 = _c;
		a.b2 = '?';
		return a;
	}

	public static Action buildUnstackAction(char _a, char _b) {
		Action a = new Action();
		a.type = ActionType.Unstack;
		a.b1 = _a;
		a.b2 = _b;
		return a;
	}

	public static Action buildStackAction(char _c, char _d) {
		Action a = new Action();
		a.type = ActionType.Stack;
		a.b1 = _c;
		a.b2 = _d;
		return a;
	}

	public String toString() {
		if (type == ActionType.NoAction)
			return "" + type;

		String s = "" + type + " " + b1;
		switch (type) {
		case Pickup: break;
		case Putdown: break;
		case Unstack: s += " " + b2; break;
		case Stack: s += " " + b2; break;
		case NoAction: break;
		}
		return s;
	}

	public void changeWorld(World w, IArm a) {
		switch (type) {
		case Pickup: w.pick(b1, a); break;
		case Unstack: w.pick(b1, a); break;
		case Putdown: w.addDown(b1, a); break;
		case Stack: w.addOver(b1, b2, a); break;
		case NoAction: break;
		}
	}

	public boolean isApplicable(World w) {
		switch (type) {
		case Pickup: return w.isFree(b1);
		case Unstack: return w.isFree(b1);
		case Putdown: return true;
		case Stack: return w.isFree(b2);
		case NoAction: return true;
		}
		return false;
	}

	public boolean conflicting(Action a) {
		return type != ActionType.NoAction && a.type != ActionType.NoAction && a.b1 == b1;
	}
}
