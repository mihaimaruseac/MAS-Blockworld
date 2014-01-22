public interface IArm {
	public boolean done(World state);
	public Action act(World w);
	public void notifyDone();
	public void notifyWillFail();
}
