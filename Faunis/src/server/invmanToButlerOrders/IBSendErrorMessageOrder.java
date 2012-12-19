package server.invmanToButlerOrders;

public class IBSendErrorMessageOrder extends IBOrder{
	private String message;
	
	public IBSendErrorMessageOrder(String message){
		this.message = message;
	}
	public String getMessage(){
		return message;
	}
}
