package otocloud.gw.common;

public enum GatewayState {
	NOTINSTALL(-1),
	OFFLINE(0),
	ONLINE(1);	
	
	private final int _value;
	 
	private GatewayState(int value) {
	     this._value = value;
	}
	
	public int getValue() {
		return _value;
	}
}
