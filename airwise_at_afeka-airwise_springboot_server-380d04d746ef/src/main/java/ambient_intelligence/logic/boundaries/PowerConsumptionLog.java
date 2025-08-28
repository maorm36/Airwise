package ambient_intelligence.logic.boundaries;

public class PowerConsumptionLog {
	
	// E = (100 Watts of device) * (5 hours) / 1000 = 0.5 kWh
	
	// cost = E * how much 1Kwh costs
	
	private String date;
	private double kwh;
	private double runtime;
	private double cost;
	
	public PowerConsumptionLog() {}
	
	public PowerConsumptionLog(String date, double kwh, double runtime, double cost) {
		this.date = date;
		this.kwh = kwh;
		this.runtime = runtime;
		this.cost = cost;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public double getKwh() {
		return kwh;
	}

	public void setKwh(double kwh) {
		this.kwh = kwh;
	}

	public double getRuntime() {
		return runtime;
	}

	public void setRuntime(double runtime) {
		this.runtime = runtime;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	@Override
	public String toString() {
		return "PowerConsumptionLog [date=" + date + ", kwh=" + kwh + ", runtime=" + runtime + ", cost=" + cost + "]";
	}
	
		
	
}
