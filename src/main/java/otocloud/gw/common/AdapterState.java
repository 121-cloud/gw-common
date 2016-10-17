package otocloud.gw.common;

public enum AdapterState {
	GATEWAYOFFLINE(-1),
	OFFLINE(0),
	ONLINE(1),
	CONFIG_COMPLETE(2),
	ERP_ONLINE(4),
	READY(7);	
	
	private final int _value;
	 
	private AdapterState(int value) {
	     this._value = value;
	}
	
	public int getValue() {
		return _value;
	}
}

