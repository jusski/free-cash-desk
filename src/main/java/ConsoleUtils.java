import java.util.ArrayList;

public class ConsoleUtils
{
    static public native void setCursorCoord(int x, int y);
    static public native void cls();
    static public native void printf(int x, int y,  String string);
    
    static
    {
    	System.load("c:/LIB/Console/console.dll");
    }
    
    public static void main(String[] args) throws InterruptedException
	{
	
		
		ArrayList<String> list = new ArrayList<String>();
		
		
	}
}
