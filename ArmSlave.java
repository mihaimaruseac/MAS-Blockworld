public class ArmSlave implements IArm {
	private Arm master;
	private String name;
	private Plan p;

	public ArmSlave(String name, Arm master) {
		this.name = name;
		this.master = master;
		master.setSlave(this);
		p = new Plan();
	}

	public boolean done(World state) {
		return master.done(state);
	}

	public Action act(World w) {
		if (p.isEmpty())
			p = master.getSlavePlan();

		System.out.println(name + " tries plan: " + p);
		return p.peekStep(w);
	}

	public void notifyDone() {
		System.out.println(name + " does plan: " + p);
		p.popStep();
	}

	public void notifyWillFail() {
		/* do nothing */
	}

	public String toString() {
		return name;
	}
}
