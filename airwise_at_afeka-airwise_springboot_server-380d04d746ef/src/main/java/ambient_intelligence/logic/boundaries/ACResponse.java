package ambient_intelligence.logic.boundaries;

public class ACResponse {
    private String message;
    private ACState acState;
    private int code;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ACState getAcState() {
        return acState;
    }

    public void setAcState(ACState acState) {
        this.acState = acState;
    }

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}