package cash;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class ConsoleUtils
{
	static public native void setCursorCoord(int x, int y);

	static public native void cls();

	static public native void printf(int x, int y, String string);

	static int currentLine = 0;
	private static boolean initialized = false;

	static
	{
		URL consoleDll = ConsoleUtils.class.getResource("/console.dll");
		assert consoleDll != null;
		System.load(consoleDll.getPath().substring(1));
	}

	static public void printf(int x, int y, String format, Object... args)
	{
		printf(x, y, String.format(format, args));
	}

	static public void println(String format, Object... args)
	{
		printf(0, currentLine++, format, args);
	}
	
	static public void attachConsole()
	{
		if(!initialized)
		{
			cls();
			initialized = true;
		}
		
	}

	static public class Table
	{
		private HashMap<Integer, ColumnPorperties> columns = new HashMap<>();
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
			}
			else
			{
				table = new Table(tableOffset, columnNames);
				tables.put(hashCode, table);
				table.printHeader();
				currentLine += columnNames.length;
			}

			return table;
		}
		
		public static Table createTable(String... columnNames)
		{
			return createTable(currentLine, columnNames);
		}

		public void addColumn(int x, String name)
		{
			ColumnPorperties columnPorperties = new ColumnPorperties();
			columnPorperties.width = name.length();
			columnPorperties.offset = columnOffset;
			columnPorperties.name = name;

			columns.put(x, columnPorperties);

			columnOffset += name.length() + 2;
		}

		private void addColumn(String name)
		{
			addColumn(column++, name);
		}

		private void printHeader()
		{
			columns.entrySet().stream().sorted(Comparator.comparingInt(Entry::getKey))
					.forEach(e -> printf(e.getValue().offset, tableOffset, e.getValue().name));
		}

		private static class ColumnPorperties
		{
			int width;
			int offset;
			String name;
		}

		public void printColumn(int row, int column, String value)
		{
			if (columns.containsKey(column))
			{
				ColumnPorperties c = columns.get(column);
				printf(c.offset, row + tableOffset, "%" + c.width + "s", value);
			}
			else
			{
				throw new UnsupportedOperationException("no column was found: " + column);
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
			ConsoleUtils.currentLine = 3;
			println("some very interesting information");
			Table table = Table.createTable("Time", "Calculation", "Average waiting", "Locks");

			table.printColumn(1, 0, Math.random());
			table.printColumn(1, 2, Math.random());
			table.printColumn(1, 3, Math.random());

			table.printColumn(2, 1, Math.random());
			table.printColumn(2, 2, Math.random());
			table.printColumn(2, 3, Math.random());

			println("another valuable information");
			println("weather is today %d", 3);
			
			table = Table.createTable("Time21", "Calculation", "Average waiting", "Locks");

			table.printColumn(1, 0, Math.random());
			table.printColumn(1, 2, Math.random());
			table.printColumn(1, 3, Math.random());

			table.printColumn(2, 1, Math.random());
			table.printColumn(2, 2, Math.random());
			table.printColumn(2, 3, Math.random());
			
			Thread.sleep(100);
		}

	}
}
