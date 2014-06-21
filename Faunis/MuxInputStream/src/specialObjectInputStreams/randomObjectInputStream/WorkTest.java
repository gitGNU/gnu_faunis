package specialObjectInputStreams.randomObjectInputStream;


import org.junit.Test;

public class WorkTest {

	@Test
	public void test() {
		try {
			RandomObjectInputStream stream = new RandomObjectInputStream(new Object[] {"aa", "ä", "âââ", "ææ"});
			while(true) {
				Object x = stream.readObject();
				System.out.println((String) x);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
