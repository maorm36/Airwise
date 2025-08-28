package ambient_intelligence.logic.boundaries;

import ambient_intelligence.data.AcMode;
import ambient_intelligence.data.FanSpeed;

public class ACState {

	 private String serial;
	 private boolean power;
	 private Number temperature;
	 private AcMode mode;
	 private FanSpeed fanSpeed;
	 private String manufacturer;
	 private boolean motion;
	 
	
	 public String getSerial() {
	     return serial;
	 }
	
	 public void setSerial(String serial) {
	     this.serial = serial;
	 }
	
	 public boolean getPower() {
	     return power;
	 }
	
	 public void setPower(boolean power) {
	     this.power = power;
	 }
	
	 public Number getTemperature() {
	     return temperature;
	 }
	
	 public void setTemperature(Number temperature) {
	     this.temperature = temperature;
	 }
	
	 public AcMode getMode() {
	     return mode;
	 }
	
	 public void setMode(AcMode mode) {
	     this.mode = mode;
	 }
	
	 public FanSpeed getFanSpeed() {
	     return fanSpeed;
	 }
	
	 public void setFanSpeed(FanSpeed fanSpeed) {
	     this.fanSpeed = fanSpeed;
	 }

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public boolean isMotion() {
		return motion;
	}

	public void setMotion(boolean motion) {
		this.motion = motion;
	}
}

