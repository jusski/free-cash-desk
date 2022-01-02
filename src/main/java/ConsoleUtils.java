
public class ConsoleUtils
{
    static public native void setCursorCoord(int x, int y);
    static public native void cls();
    static
    {
    	System.loadLibrary("main");
    }
}
