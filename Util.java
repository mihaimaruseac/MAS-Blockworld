public class Util {
	private static Util instance;

	private Util() {
	}

	public static Util getUtil() {
		if (instance != null)
			return instance;
		instance = new Util();
		return instance;
	}

	public void sleep(double seconds) {
		try { Thread.sleep((int)(seconds * 1000)); } catch (Exception e) {}
	}

	public boolean isBlockId(char c) {
		return c >= 'A' && c <= 'Z';
	}
}
