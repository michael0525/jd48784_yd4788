package de.syngenio.demo2;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class TestController {

	private Actor _actor;
	private Sensor _sensor;
	private Controller _controller;

	@Before
	public void setUp() throws Exception {
		_actor = mock(Actor.class);
		_sensor = mock(Sensor.class);
		_controller = new Controller(_sensor, _actor);
	}

	@Test
	public void assureThatMotorIsStoppedWhenBlocked() {
		when(_sensor.isMotorBlocked()).thenReturn(true);
		
		_controller.singleDecision();

		verify(_actor).stopMotor();
		verify(_actor,times(0)).moveMotor(anyInt());
	}
	
	@Test
	public void assureThatMotorIsMovedCorrectly() {
		when(_sensor.isMotorBlocked()).thenReturn(false);
		// 1. bright, too warm
		when(_sensor.getBrightness()).thenReturn(15);
		doReturn(30).when(_sensor).getTemperature();
		_controller.singleDecision();
		verify(_actor).moveMotor(-10);

		// 2. bright, too cold
		when(_sensor.getBrightness()).thenReturn(15);
		when(_sensor.getTemperature()).thenReturn(15);
		_controller.singleDecision();
		verify(_actor).moveMotor(5);
		
		// ...

	}

}
