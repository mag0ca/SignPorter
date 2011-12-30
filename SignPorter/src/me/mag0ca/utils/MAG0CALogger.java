package me.mag0ca.utils;

/*import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
*/
public class MAG0CALogger 
{
	public static boolean enabled = true;
/*	private static File LOG = null;
	private static FileWriter stream = null;
	private static String Name = "database.log"; 
	private static BufferedWriter out = null;
	
	public static void initlogger (File Path, String Filename) throws IOException
	{
		if (enabled)
		{
			if (stream == null)
			{
				Name = Filename;
				LOG = new File(Path, Name);
				
				if (!LOG.exists())
				{
					LOG.createNewFile();
				}
				
			//	if (LOG.canWrite())
				//{
				stream = new FileWriter(LOG, true);
				out = new BufferedWriter(stream);
				System.out.println("[Logger] " + "MAG0CALogger enabled");
				//}
			}
		}
	}*/
	
	public static void logger(String log)
	{
		if (enabled)
		{
			//try	{out.append(DateUtil.now() + ":\t " + log);} catch (IOException e) {}	
			System.out.println("[Logger] " + DateUtil.now() + ":\t " + log);
		}
		
	}
}
