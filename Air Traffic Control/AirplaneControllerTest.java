//import org.junit.Test;
//
//public class AirplaneControllerTest {
//
//	@Test
//	public void testOmegaGainSameAngle() {
//		DisplayClient dc = new DisplayClient("127.0.0.1");
//		Simulator s = new Simulator(dc);
//		double[] pose = {25, 25, 0};
//		Airplane p = new Airplane(pose, 5, 0, s, 100);
//		Airport a1 = new Airport(25, 25, 3, s);
//		Airport a2 = new Airport(25, 50, 3, s);
//		AirplaneController ac = new AirplaneController(s, p, a1, a2, 5);
//		
//		double desired = 0;
//		double actual = 0;
//		double expectedOmega = 0;
//		double gotOmega = ac.omegaGain(desired, actual);
//		
//		assert(expectedOmega == gotOmega);
//	}
//	
//	@Test
//	public void testOmegaGainSameAngle2() {
//		DisplayClient dc = new DisplayClient("127.0.0.1");
//		Simulator s = new Simulator(dc);
//		double[] pose = {25, 25, 0};
//		Airplane p = new Airplane(pose, 5, 0, s, 100);
//		Airport a1 = new Airport(25, 25, 3, s);
//		Airport a2 = new Airport(25, 50, 3, s);
//		AirplaneController ac = new AirplaneController(s, p, a1, a2, 5);
//		
//		double desired = 5;
//		double actual = 5;
//		double expectedOmega = 0;
//		double gotOmega = ac.omegaGain(desired, actual);
//		
//		assert(expectedOmega == gotOmega);
//	}
//
//	@Test
//	public void testOmegaGainSmallPositive() {
//		DisplayClient dc = new DisplayClient("127.0.0.1");
//		Simulator s = new Simulator(dc);
//		double[] pose = {25, 25, 0};
//		Airplane p = new Airplane(pose, 5, 0, s, 100);
//		Airport a1 = new Airport(25, 25, 3, s);
//		Airport a2 = new Airport(25, 50, 3, s);
//		AirplaneController ac = new AirplaneController(s, p, a1, a2, 5);
//		
//		double desired = Math.PI/4;
//		double actual = 0;
//		double expectedOmega = Math.PI/4;
//		double gotOmega = ac.omegaGain(desired, actual);
//		
//		assert(expectedOmega == gotOmega);
//	}
//	
//	@Test
//	public void testOmegaGainLargePositive() {
//		DisplayClient dc = new DisplayClient("127.0.0.1");
//		Simulator s = new Simulator(dc);
//		double[] pose = {25, 25, 0};
//		Airplane p = new Airplane(pose, 5, 0, s, 100);
//		Airport a1 = new Airport(25, 25, 3, s);
//		Airport a2 = new Airport(25, 50, 3, s);
//		AirplaneController ac = new AirplaneController(s, p, a1, a2, 5);
//		
//		double desired = 3*Math.PI/4 + .1;
//		double actual = 0;
//		double expectedOmega = 3*Math.PI/4 + .1;
//		double gotOmega = ac.omegaGain(desired, actual);
//		
//		assert(expectedOmega == gotOmega);
//	}
//	
//	@Test
//	public void testOmegaGainSmallNegative() {
//		DisplayClient dc = new DisplayClient("127.0.0.1");
//		Simulator s = new Simulator(dc);
//		double[] pose = {25, 25, 0};
//		Airplane p = new Airplane(pose, 5, 0, s, 100);
//		Airport a1 = new Airport(25, 25, 3, s);
//		Airport a2 = new Airport(25, 50, 3, s);
//		AirplaneController ac = new AirplaneController(s, p, a1, a2, 5);
//		
//		double desired = -Math.PI/4;
//		double actual = 0;
//		double expectedOmega = -Math.PI/4;
//		double gotOmega = ac.omegaGain(desired, actual);
//		
//		assert(expectedOmega == gotOmega);
//	}
//	
//	@Test
//	public void testOmegaGainLargeNegative() {
//		DisplayClient dc = new DisplayClient("127.0.0.1");
//		Simulator s = new Simulator(dc);
//		double[] pose = {25, 25, 0};
//		Airplane p = new Airplane(pose, 5, 0, s, 100);
//		Airport a1 = new Airport(25, 25, 3, s);
//		Airport a2 = new Airport(25, 50, 3, s);
//		AirplaneController ac = new AirplaneController(s, p, a1, a2, 5);
//		
//		double desired = -3*Math.PI/4 - .1;
//		double actual = 0;
//		double expectedOmega = -3*Math.PI/4 - .1;
//		double gotOmega = ac.omegaGain(desired, actual);
//		
//		assert(expectedOmega == gotOmega);
//	}
//
//	@Test
//	public void testOmegaGainSmallPositiveShift() {
//		DisplayClient dc = new DisplayClient("127.0.0.1");
//		Simulator s = new Simulator(dc);
//		double[] pose = {25, 25, 0};
//		Airplane p = new Airplane(pose, 5, 0, s, 100);
//		Airport a1 = new Airport(25, 25, 3, s);
//		Airport a2 = new Airport(25, 50, 3, s);
//		AirplaneController ac = new AirplaneController(s, p, a1, a2, 5);
//		
//		double desired = Math.PI/2;
//		double actual = Math.PI/4;
//		double expectedOmega = Math.PI/4;
//		double gotOmega = ac.omegaGain(desired, actual);
//		
//		assert(expectedOmega == gotOmega);
//	}
//	
//	@Test
//	public void testOmegaGainSmallPositiveShift2() {
//		DisplayClient dc = new DisplayClient("127.0.0.1");
//		Simulator s = new Simulator(dc);
//		double[] pose = {25, 25, 0};
//		Airplane p = new Airplane(pose, 5, 0, s, 100);
//		Airport a1 = new Airport(25, 25, 3, s);
//		Airport a2 = new Airport(25, 50, 3, s);
//		AirplaneController ac = new AirplaneController(s, p, a1, a2, 5);
//		
//		double desired = .1;
//		double actual = -Math.PI/4 + .1;
//		double expectedOmega = Math.PI/4;
//		double gotOmega = ac.omegaGain(desired, actual);
//		
//		assert(expectedOmega == gotOmega);
//	}
//	
//	@Test
//	public void testOmegaGainLargePositiveShift() {
//		DisplayClient dc = new DisplayClient("127.0.0.1");
//		Simulator s = new Simulator(dc);
//		double[] pose = {25, 25, 0};
//		Airplane p = new Airplane(pose, 5, 0, s, 100);
//		Airport a1 = new Airport(25, 25, 3, s);
//		Airport a2 = new Airport(25, 50, 3, s);
//		AirplaneController ac = new AirplaneController(s, p, a1, a2, 5);
//		
//		double desired = Math.PI + .1;
//		double actual = Math.PI/4 + .1;
//		double expectedOmega = 3*Math.PI/4;
//		double gotOmega = ac.omegaGain(desired, actual);
//		
//		assert(expectedOmega == gotOmega);
//	}
//	
//	@Test
//	public void testOmegaGainSmallNegativeShift() {
//		DisplayClient dc = new DisplayClient("127.0.0.1");
//		Simulator s = new Simulator(dc);
//		double[] pose = {25, 25, 0};
//		Airplane p = new Airplane(pose, 5, 0, s, 100);
//		Airport a1 = new Airport(25, 25, 3, s);
//		Airport a2 = new Airport(25, 50, 3, s);
//		AirplaneController ac = new AirplaneController(s, p, a1, a2, 5);
//		
//		double desired = -Math.PI/2;
//		double actual = -Math.PI/4;
//		double expectedOmega = -Math.PI/4;
//		double gotOmega = ac.omegaGain(desired, actual);
//		
//		assert(expectedOmega == gotOmega);
//	}
//	
//	@Test
//	public void testOmegaGainLargeNegativeShift() {
//		DisplayClient dc = new DisplayClient("127.0.0.1");
//		Simulator s = new Simulator(dc);
//		double[] pose = {25, 25, 0};
//		Airplane p = new Airplane(pose, 5, 0, s, 100);
//		Airport a1 = new Airport(25, 25, 3, s);
//		Airport a2 = new Airport(25, 50, 3, s);
//		AirplaneController ac = new AirplaneController(s, p, a1, a2, 5);
//		
//		double desired = 3*Math.PI/4 + .1;
//		double actual = -Math.PI/4;
//		double expectedOmega = Math.PI - .1;
//		double gotOmega = ac.omegaGain(desired, actual);
//		
//		assert(expectedOmega == gotOmega);
//	}
//}
