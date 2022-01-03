package cash;

import lombok.extern.log4j.Log4j2;

import java.net.URL;

@Log4j2
public class ConsoleUtils
{
    static public native void setCursorCoord(int x, int y);
    static public native void cls();
    static public native void printf(int x, int y,  String string);
    
    static {
		URL consoleDll = ConsoleUtils.class.getResource("/console.dll");
		assert consoleDll != null;
		System.load(consoleDll.getPath().substring(1));
    }
}