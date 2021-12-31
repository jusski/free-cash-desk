
public class ExceptionUtils
{
	@SuppressWarnings("unchecked")
	public static <T extends Throwable> void rethrowUnchecked(Throwable t) throws T
	{
		throw (T) t;
	}
}
