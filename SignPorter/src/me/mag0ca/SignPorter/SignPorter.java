/* 
 * TODO:
 * give the ability to use direct coordinates on signs and in the command
 * economy support
 * 
 * Known Bugs:
 * if you break the block that holds the sign the sign will drop but the reference will not be removed from the Database. this will prevent you from re-making a sign with that name but you will still be able to reference the old sign
 */


package me.mag0ca.SignPorter;

//import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.logging.Logger;

import me.mag0ca.utils.MAG0CALogger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class SignPorter extends JavaPlugin
{
	Logger log = Logger.getLogger("Minecraft");
	private SignPorterCommandExecutor myExecutor;
	String DBType;
	
	//run on plugin startup. starts the plugin's work
	public void onEnable()
	{

		checkConfig(); //load config file

		//try to make a connection with the database engine
		try {
			startDatabase();
		} catch (SQLException e) {
			log.severe("[SignPorter] Unable to connect to Database " + config.DBURL + " please check your config");
			MAG0CALogger.logger("Unable to connect to Database " + config.DBURL + " Exception: " + e.getMessage());
		}
		
		checkDB();
		
		//register events with Bukkit that will activate our plugin
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new SignPorterBlockListener(this) , this);
		pm.registerEvents(new SignPorterPlayerListener(this), this);
		
		//register console commands with Bukkit
		myExecutor = new SignPorterCommandExecutor(this);
//		if (pm.getPlugin("WorldEdit") != null)
//		{
//			getServer().getPluginCommand("sp").setExecutor(myExecutor);
//			log.info("[SignPorter] WorldEdit detected overriding sp command");
//		}
//		else
//		{
		getCommand("sptp").setExecutor(myExecutor);
//		}
		getCommand("signporter").setExecutor(myExecutor);

		log.info("[SignPorter] is enabled");
	}

	//run on plugin shutdown. clean up procedure
	public void onDisable()
	{
		log.info("[SignPorter] is disabled");
	}
	
	//load the config file and create it if it does not exist
    public void checkConfig() 
    {
   		getConfig().options().copyDefaults(true);
    	config.DBType = getConfig().getString("Database.Type", "sqlite");
    	config.DBURL = getConfig().getString("Database.URL", "SignPorter.db");
    	config.DBUsername = getConfig().getString("Database.Username", "");
    	config.DBPassword = getConfig().getString("Database.Password", "");
    	config.DBTable = getConfig().getString("Database.Table", "SignPorter");
    	
    	config.RandomEnabled = getConfig().getBoolean("Random.enabled", true);
    	config.RandomXmax = getConfig().getInt("Random.Xmax");
    	config.RandomZmax = getConfig().getInt("Random.Zmax");
    	config.RandomYmax = getConfig().getInt("Random.Ymax");
    	config.RandomXmin = getConfig().getInt("Random.Xmin");
    	config.RandomZmin = getConfig().getInt("Random.Zmin");
    	config.RandomYmin = getConfig().getInt("Random.Ymin");
    	
    	MAG0CALogger.enabled = getConfig().getBoolean("Logging.Enabled", true);
    	
    	
 /*   	if(config.getString("Database.Type") == null)
    	{
    		config.set("Database.Type", "sqlite");
    	}
    	if(config.getString("Database.URL") == null)
    	{
    		config.set("Database.URL", "sqlite:SignPorter.db");
    	}    	
    	if(config.getString("Database.Username") == null)
    	{
    		config.set("Database.Username", "");
    	}    	
    	if(config.getString("Database.Password") == null)
    	{
    		config.set("Database.Password", "");
    	}
    	if(config.getString("Database.Table") == null)
    	{
    		config.set("Database.Table", "SignPorter");
    	}*/
    	
    	
    	saveConfig();
    }
    
    //check the database for the table and create it is it doesn't
    private void checkDB()
    {
    	if (!CheckTable(config.DBTable))
    	{
    		if (config.DBType.compareToIgnoreCase("mysql") == 0)
    		{
    			CreateTable(config.DBTable, config.ColumnsMySQL);	
    		}
    		else if (config.DBType.compareToIgnoreCase("sqlite") == 0)
    		{
    			CreateTable(config.DBTable, config.ColumnsSQLite);	
    		}
    		else
    		{
    			MAG0CALogger.logger("error creating Table " + config.DBTable + " in " + config.DBType + " Database");
    		}
    	}
    }
    
	public boolean startDatabase() throws SQLException 
	{
		boolean TF = true;
		String DBDriver;
		
		//Statement statement = null;
		
		if (config.DBType.compareToIgnoreCase("sqlite") == 0 || config.DBType.compareToIgnoreCase("sqllite") == 0)
		{
			DBDriver = "org.sqlite.JDBC";
			DBType = "sqlite";
		}
		else if (config.DBType.compareToIgnoreCase("mysqldb") == 0 || config.DBType.compareToIgnoreCase("mysql") == 0)
		{
			DBDriver = "com.mysql.jdbc.Driver";
			DBType = "mysql";
		}
		else 
		{
			return false;
		}
		
		MAG0CALogger.logger("Initalizing Database driver " + DBDriver);
		
		try 
		{
			Class.forName(DBDriver);
		} 
		catch (ClassNotFoundException e)
		{
			MAG0CALogger.logger("an error occured while trying to load Database driver "+ DBDriver + " the exception is as follows " + e.getMessage());
		}
		
//		MAG0CALogger.logger("Initalizing Database connection");
//		connection = DriverManager.getConnection("jdbc:" + DBType + ":" + config.DBURL, config.DBUsername, config.DBPassword);
		 
//		if (connection != null)
//		{
//			MAG0CALogger.logger("Readying statement processor");
//			statement = connection.createStatement();
//			TF = true;
//		}
		
//		if (statement != null)
//		{
//			TF = true;
//		}
		return TF;			
	}
	
	public boolean CreateTable ( String Table, String Columns) 
	{
		MAG0CALogger.logger("Creating Table "+ Table + " in Database " + config.DBTable );
		boolean TF = false;
		Connection connection =null;
		Statement statement = null;
		try 
		{
			MAG0CALogger.logger("Initalizing Database connection");
			connection = DriverManager.getConnection("jdbc:" + DBType + ":" + config.DBURL, config.DBUsername, config.DBPassword);
			MAG0CALogger.logger("Readying statement processor");
			statement = connection.createStatement();
		} catch (SQLException e1) {MAG0CALogger.logger("statement processor failed " + e1.getMessage());}
		
		String column = "CREATE TABLE " + Table + " " + Columns;
//		for (int i = 0; i < Columns.length - 1; i++ )
//		{
//			column = column + Columns[i] + ",";
//		}
//		column = column + Columns[Columns.length - 1] + ")";
		
		try 
		{
			TF = statement.execute(column);
		}catch (SQLException e) {MAG0CALogger.logger("An Error occured while executing SQL CREATE " + e.getMessage());}
		return TF;
	}
	
	public boolean CheckTable(String Table)
	{
		boolean TF = false;
		Connection connection = null;

		MAG0CALogger.logger("Checking for existance of table: " + Table);
		try 
		{
			MAG0CALogger.logger("Initalizing Database connection");
			connection = DriverManager.getConnection("jdbc:" + DBType + ":" + config.DBURL, config.DBUsername, config.DBPassword);
			DatabaseMetaData dbm = connection.getMetaData();
			ResultSet tables = dbm.getTables(null, null, Table, null);
			
			if (tables.next())
			{
				TF = true;	
				MAG0CALogger.logger("Table " + Table + " Exists");
			}
			tables.close();
		} catch (SQLException e) {MAG0CALogger.logger("An Error occured while Searching for table " + Table + " " + e.getMessage());}
		return TF;	
	}

//	public boolean checkSourceB(String SourceName)  
//	{
//		ResultSet results;
//		boolean TF = false;
//		Connection connection = null;
//		Statement statement = null;
//		PreparedStatement ps = null;
//		try 
//		{
//			MAG0CALogger.logger("Initalizing Database connection");
//			connection = DriverManager.getConnection("jdbc:" + DBType + ":" + config.DBURL, config.DBUsername, config.DBPassword);
//			MAG0CALogger.logger("Readying statement processor");
//			statement = connection.createStatement();
//			
//			String Select = "SELECT Name FROM " + config.DBTable + " WHERE Name=?";
//			ps = connection.prepareStatement(Select);
//			ps.
//			
//		} catch (SQLException e1) {MAG0CALogger.logger("statement processor failed " + e1.getMessage());}
//		
//		String columns = "Name";
//		String where = "Name = \'" + SourceName + "\'";
//		try 
//		{
//			//MAG0CALogger.logger("checking the statement " + statement.isClosed() + " as well as the connection " + connection.isClosed());
//			MAG0CALogger.logger("Executing: "  + "SELECT " + columns + " FROM " + config.DBTable + " WHERE " + where);
//			TF = statement.execute("SELECT " + columns + " FROM " + config.DBTable + " WHERE " + where);
//		} catch (SQLException e) {MAG0CALogger.logger("An Error occured while executing SQL SELECT " + e.getMessage());}
//		
//		if (TF)
//		{
//			TF = false;
//			String Name = null;
//			try 
//			{
//				results = statement.getResultSet();
//				if (results.next())
//				{
//					Name = results.getString("Name");
//					MAG0CALogger.logger("Source results " + Name);
//					if (Name.compareToIgnoreCase(SourceName) == 0 )
//					{
//						TF = true;
//					}
//				}
//				results.close();
//			} catch (SQLException e) {MAG0CALogger.logger("Error while getting results " + e.getMessage());	}
//
//		}
//		return TF;
//	}
	
	public boolean checkSource(String SourceName) 
	{
		ResultSet results;
		boolean TF = false;
		Connection connection = null;
		Statement statement = null;
		try 
		{
			MAG0CALogger.logger("Initalizing Database connection");
			connection = DriverManager.getConnection("jdbc:" + DBType + ":" + config.DBURL, config.DBUsername, config.DBPassword);
			MAG0CALogger.logger("Readying statement processor");
			statement = connection.createStatement();
		} catch (SQLException e1) {MAG0CALogger.logger("statement processor failed " + e1.getMessage());}
		
		String columns = "Name";
		String where = "Name = \'" + SourceName + "\'";
		try 
		{
			//MAG0CALogger.logger("checking the statement " + statement.isClosed() + " as well as the connection " + connection.isClosed());
			MAG0CALogger.logger("Executing: "  + "SELECT " + columns + " FROM " + config.DBTable + " WHERE " + where);
			TF = statement.execute("SELECT " + columns + " FROM " + config.DBTable + " WHERE " + where);
		} catch (SQLException e) {MAG0CALogger.logger("An Error occured while executing SQL SELECT " + e.getMessage());}
		
		if (TF)
		{
			TF = false;
			String Name = null;
			try 
			{
				results = statement.getResultSet();
				if (results.next())
				{
					Name = results.getString("Name");
					MAG0CALogger.logger("Source results " + Name);
					if (Name.compareToIgnoreCase(SourceName) == 0 )
					{
						TF = true;
					}
				}
				results.close();
			} catch (SQLException e) {MAG0CALogger.logger("Error while getting results " + e.getMessage());	}

		}
		return TF;
	}
	
	void tpRandom(Player player)
	{
		//boolean TF = true;
		//create a random number for each coord between the max and min set for that coord
		Random RandomGenerator = new Random(System.currentTimeMillis());
		player.sendMessage("[SignPorter] locking on to Coordonates");
		int Yi = 0;
		int X = 0;
		int Z = 0;
		double Yd = 0;
		int Y = 0;
		X = RandomGenerator.nextInt(config.RandomXmax - config.RandomXmin + 1) + config.RandomXmin;
		Z = RandomGenerator.nextInt(config.RandomZmax - config.RandomZmin + 1) + config.RandomZmin;
		Yi = RandomGenerator.nextInt(config.RandomYmax - config.RandomYmin + 1) + config.RandomYmin;
		Yd = RandomGenerator.nextGaussian() * (Yi/2) + 65;
		Y = (int) Math.round(Yd);
		
		Location Locate = null;
		MAG0CALogger.logger("Ymax:" + config.RandomYmax + " Ymin:" + config.RandomYmin);
		MAG0CALogger.logger("Created Random and got initial coords of: X:" + X + " Y:" + Y + " Z:" + Z);
		
		while (!(player.getWorld().getBlockAt(X,Y+1,Z).isEmpty() && player.getWorld().getBlockAt(X,Y,Z).isEmpty() && player.getWorld().getBlockAt(X,Y-1,Z).isEmpty() && Y >= config.RandomYmin && Y <= config.RandomYmax && !player.getWorld().getBlockAt(X,Y-2,Z).isEmpty() && !player.getWorld().getBlockAt(X,Y-2,Z).isLiquid())) 
				//&& !(!player.getWorld().getBlockAt(X,Y-2,Z).isEmpty() && player.getWorld().getBlockAt(X,Y-2,Z).getTypeId() != Material.LAVA.getId()) || !(!player.getWorld().getBlockAt(X,Y-3,Z).isEmpty() && player.getWorld().getBlockAt(X,Y-3,Z).getTypeId() != Material.LAVA.getId()))	
		{
			X = RandomGenerator.nextInt(config.RandomXmax - config.RandomXmin + 1) + config.RandomXmin;
			Z = RandomGenerator.nextInt(config.RandomZmax - config.RandomZmin + 1) + config.RandomZmin;
			Yi = RandomGenerator.nextInt(config.RandomYmax - config.RandomYmin + 1) + config.RandomYmin;
			Yd = RandomGenerator.nextGaussian() * (Yi/2) + 65;
			Y = (int) Math.round(Yd);
			MAG0CALogger.logger("in loop and got new coords of: X:" + X + " Y:" + Y + " Z:" + Z);
		}
		player.sendMessage("[SignPorter] Beam me up Scotty");
		Locate = new Location(player.getWorld(), X, Y, Z);
		player.teleport(Locate);
		player.sendMessage("you have just been teleported to " + Locate.getX() + ","  + Locate.getY() + "," + Locate.getZ() + " Hope you had a safe trip.");
	}
}
