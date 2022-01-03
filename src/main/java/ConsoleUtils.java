import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConsoleUtils
{
    static public native void setCursorCoord(int x, int y);
    static public native void cls();
    static public native void printf(int x, int y,  String string);
    
    static
    {
    	System.load("C:\\GitHub\\free-cash-desk-master\\console.dll");
    }
    
    public static void main(String[] args) throws InterruptedException
	{
		cls();
		//setCursorCoord(1, 20);
		int counter = 0;
		while(true) {
			Thread.sleep(100);
			ConsoleUtils.printf(0, 20, "liaw liaw liaw%12d " + counter++);
			ConsoleUtils.printf(20, 24, "liaw " + counter++);
			cls();
			System.out.println("Hi");
		}
	}
}