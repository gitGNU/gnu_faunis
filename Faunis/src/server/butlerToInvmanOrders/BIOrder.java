package server.butlerToInvmanOrders;

import server.Butler;

public abstract class BIOrder {
	private Butler source;
	
	BIOrder(Butler source){
		this.source = source;
	}
	
	public Butler getSource(){
		return source;
	}
}
