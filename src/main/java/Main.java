public class Main
{
	public static void main(String[] args)
	{

		Cashier cashier = new Cashier();
		new Thread(cashier).start();
		
		for(int i = 0; i < 2; ++i)
		{
            Visitor visitor = new Visitor();			
			cashier.put(visitor);
		}
		
	}

}
