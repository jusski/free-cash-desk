import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FastFoodRestaurant
{
	private static final int NUMBER_OF_CASHIERS = 4;
	private static List<Cashier> cashiers;
	
	static
	{
		cashiers = new ArrayList<>();
		for (int i = 0; i < NUMBER_OF_CASHIERS; ++i)
		{
			Cashier cashier = new Cashier();
			new Thread(cashier).start();
			cashiers.add(cashier);
		}
	}
	
	public static Collection<Cashier> getCashiers()
	{
		return Collections.unmodifiableCollection(cashiers);
	}

}
