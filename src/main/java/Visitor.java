import java.util.Collection;

public class Visitor implements Runnable
{
	private Collection<Cashier> cashiers;
	private Cashier cashier;
	
	private volatile boolean served = false;

	public boolean standInQueue()
	{
//		cashier = cashiers.stream().sorted(Comparator.comparingLong(Cashier::getQueueLegth)).findFirst().get();
//
//		return cashier.enterQueue(this);
		return true;
	}

	public void enterFastFoodRestaurant()
	{
		cashiers = FastFoodRestaurant.getCashiers();
	}

	public void offerFood()
	{
        served = true;
        System.out.println(Thread.currentThread().getId());
	}

	public void run()
	{
//		try
//		{
//			while (true)
//			{
//				if (standInQueue())
//				{
//                    while(false == served)
//                    {
//                    	Thread.sleep(10); // thread sleep?
//                    	
//                    	int position = cashier.getVisitorPositionInQueue(this);
//                    	Cashier cashierWithSmallestQueueLength = cashiers.stream()
//                    	.filter(e-> e.getQueueLegth() < position)
//                    	.findFirst()
//                    	.orElse(cashier);
//                    	
//                    	if(cashier != cashierWithSmallestQueueLength)
//                    	{
//                    		cashier.removeFromQueue(this);
//                    		cashierWithSmallestQueueLength.enterQueue(this);
//                    	}
//                    }
//				}
//				else
//				{
//					Thread.sleep(1000);
//				}
//
//			}
//		}
//		catch (InterruptedException e)
//		{
//			ExceptionUtils.rethrowUnchecked(e);
//		}

	}

	

}
