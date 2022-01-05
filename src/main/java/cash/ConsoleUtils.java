package cash;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ConsoleUtils
{
	static public native void setCursorCoord(int x, int y);

	static public native void clearScreen();
	static public native void attachConsole();

	static public native void printf(int x, int y, String string);

	static int currentLine = 0;

	static
	{
		System.load("c:/LIB/Console/console.dll");
	}

	static public void printf(int x, int y, String format, Object... args)
	{
		printf(x, y, String.format(format, args));
	}

	static public void println(String format, Object... args)
	{
		printf(0, currentLine++, format, args);
	}
	
	

	static public class Table
	{
		private ArrayList<ColumnProperties> columns = new ArrayList<>();
		private int column = 0;
		private int columnOffset = 0;
		private int tableOffset = 0;

		private static HashMap<Integer, Table> tables = new HashMap<>();

		private Table(int tableOffset, String... columnNames)
		{
			this.tableOffset = tableOffset;
			for (String name : columnNames)
			{
				addColumn(name);
			}

		}
		public static Table createTable(int tableOffset, String... columnNames)
		{
			Table table;
			int hashCode = Arrays.hashCode(columnNames);
			if (tables.containsKey(hashCode))
			{
				table = tables.get(hashCode);
				currentLine += columnNames.length;
			}
			else
			{
				table = new Table(tableOffset, columnNames);
				tables.put(hashCode, table);
				
				currentLine += columnNames.length;
			}
			table.printHeader();
			return table;
		}
		
		public static Table createTable(String... columnNames)
		{
			return createTable(currentLine, columnNames);
		}

		public void addColumn(int x, String name)
		{
			ColumnProperties columnPorperties = new ColumnProperties();
			columnPorperties.width = name.length();
			columnPorperties.offset = columnOffset;
			columnPorperties.name = name;

			columns.add(columnPorperties);

			columnOffset += name.length() + 2;
		}

		private void addColumn(String name)
		{
			addColumn(column++, name);
		}

		private void printHeader()
		{
			columns.stream().forEach(e -> printf(e.offset, tableOffset,	String.format("%" + e.width  + "s", e.name)));
		}

		private static class ColumnProperties
		{
			int width;
			int offset;
			String name;
		}

		public void printColumn(int row, int column, String value)
		{
			ColumnProperties c;
			if ((c = columns.get(column)) != null)
			{
				if (value.length() > (c.width))
				{
					int offset = value.length() - c.width;
					c.width = value.length();

					for (int i = column + 1; i < columns.size(); ++i)
					{
						c = columns.get(i);
						c.offset += offset;
					}
				}
				printf(c.offset, row + tableOffset, "%" + c.width + "s", value);
			}
		}

		public void printColumn(int row, int column, Integer value)
		{
			printColumn(row, column, value.toString());
		}

		public void printColumn(int row, int column, Double value)
		{
			BigDecimal bd = BigDecimal.valueOf(value);
			bd = bd.setScale(2, RoundingMode.HALF_UP);
			printColumn(row, column, String.valueOf(bd.doubleValue()));
		}

}

	public static void main(String[] args) throws InterruptedException
	{
		
		ConsoleUtils.attachConsole();
		while (true)
		{
			ConsoleUtils.currentLine = 1;
			println("some very interesting information");
			println("");
			Table table = Table.createTable("Time", "Calculation", "Average waiting", "Locks");

			table.printColumn(1, 0, Math.random());
			table.printColumn(1, 2, Math.random());
			table.printColumn(1, 3, Math.random());

			table.printColumn(2, 1, Math.random());
			table.printColumn(2, 2, Math.random());
			table.printColumn(2, 3, Math.random());

			println("another valuable information");
			println("weather is today %d", 3);
			
			table = Table.createTable("Time21", "C", "A", "L");

			table.printColumn(1, 0, Math.random());
			table.printColumn(1, 2, Math.random());
			table.printColumn(1, 3, Math.random());

			table.printColumn(2, 1, Math.random());
			table.printColumn(2, 2, Math.random());
			table.printColumn(2, 3, Math.random());
//			clearScreen();
			Thread.sleep(30);
		}

	}
}
