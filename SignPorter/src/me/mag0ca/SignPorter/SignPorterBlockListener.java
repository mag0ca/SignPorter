package me.mag0ca.SignPorter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import me.mag0ca.utils.MAG0CALogger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

public class SignPorterBlockListener extends BlockListener 
{
	protected SignPorter plugin;
	protected int SourceX = 0;
	protected int DestinationX =0;
	protected int SourceY = 0;
	protected int DestinationY = 0;
	protected int SourceZ = 0;
	protected int DestinationZ = 0;
	protected int Cost = 0;
	//protected Location SourceLocation;
	//protected Location DestinationLocation;
	protected String SourceName;
	protected String DestinationName;
	protected Player player;
	protected Block sign;
	
	public SignPorterBlockListener( SignPorter instance ) 
	{
    	plugin = instance;
}
	
	//activated when a  sign object is changed
	public void onSignChange( SignChangeEvent event )
	{
		if (!event.isCancelled())
		{
			sign = event.getBlock();
			player = event.getPlayer();

			//is it a sign?
			//if (sign.getType() == Material.WALL_SIGN || sign.getType() == Material.SIGN_POST)
			//{
				//is it a SignPorter?
				if (event.getLine(0).compareToIgnoreCase("[SignPorter]") == 0)
				{
					//do you have the correct permissions?
					if (player.hasPermission("signporter.create") || player.hasPermission("signporter.*"))
					{	
						SourceX = sign.getLocation().getBlockX();
						SourceY = sign.getLocation().getBlockY();
						SourceZ = sign.getLocation().getBlockZ();
						SourceName = event.getLine(1);
						DestinationName = event.getLine(2);

						if (!event.getLine(3).isEmpty())
						{
							Cost = Integer.getInteger(event.getLine(3));
						}
						
						MAG0CALogger.logger("-Source Name: " + SourceName + " -Destination Name: " + DestinationName);

						//did you do it right?
						if (SourceName.isEmpty() && DestinationName.isEmpty())
						{
							player.sendMessage("[SignPorter] Error: you must have a sign name or destination");
							dropSign(event);
							return;
						}
						if (SourceName.equalsIgnoreCase("random"))
						{
							player.sendMessage("[SignPorter] the sign name \"Random\" is reserved and cannot be used except as destination");
							dropSign(event);
							return;
						}
						if (SourceName.equalsIgnoreCase("spawn"))
						{
							player.sendMessage("[SignPorter] the sign name \"Spawn\" is reserved and cannot be used except as destination");
							dropSign(event);
							return;
						}
						if (!SourceName.isEmpty())
						{
							if (plugin.checkSource(SourceName))
							{
								player.sendMessage("[SignPorter] Error: SignPorter " + SourceName + " already exists");
								dropSign(event);
								return;
							}
						}

						if (!DestinationName.isEmpty())
						{
							if (DestinationName.equalsIgnoreCase("spawn"))
							{
								DestinationName = "Spawn";
								player.sendMessage("[SignPorter] Congratulations you have created a Signporter to your spawn point.\n" +
										"This will send you to your bed or to the world spawn if you do not have a bed");
							}
							else if (DestinationName.equalsIgnoreCase("random"))
							{
								DestinationName = "Random";
								player.sendMessage("[SignPorter] Congratulations you have created a Signporter to a random point in this world.");
							}
							else if (!plugin.checkSource(DestinationName))
							{
								player.sendMessage("[SignPorter] Error: SignPorter " + DestinationName + " does not exist");
								dropSign(event);
								return;
							}
						}
						if (!SourceName.isEmpty())
						{
							String Columns = "(Name,Destination,coordX,coordY,coordZ,world,Cost,Creator)";
							String Values = "(\'" + SourceName + "\',\'" + DestinationName + "\',\'" + Integer.toString(SourceX) + "\',\'" + Integer.toString(SourceY) + "\',\'" + Integer.toString(SourceZ) + "\',\'" + player.getWorld().getName() +"\',\'" + Integer.toString(Cost) + "\',\'" + player.getName() + "\')";
							MAG0CALogger.logger("real values: X: " + SourceX + " Y: " + SourceY + " Z: " + SourceZ);
							Statement statement = null;
							Connection connection = null;

							try 
							{
								MAG0CALogger.logger("Initalizing Database connection");
								connection = DriverManager.getConnection("jdbc:" + plugin.DBType + ":" + config.DBURL, config.DBUsername, config.DBPassword);
								MAG0CALogger.logger("Readying statement processor");
								statement = connection.createStatement();
							} catch (SQLException e1) {MAG0CALogger.logger("statement processor failed " + e1.getMessage());}

							try 
							{
								MAG0CALogger.logger("inserting " );
								int ret = statement.executeUpdate("INSERT INTO " + config.DBTable + Columns + "VALUES " + Values );
								MAG0CALogger.logger("inserted " + ret + " rows" );
							} catch (SQLException e) 
							{
								MAG0CALogger.logger("An Error occured while executing SQL SELECT " + e.getMessage());
								player.sendMessage("[SignPorter] Unable to save sign location here is your sign back");
								dropSign(event);
								return;
							}						
							player.sendMessage("[SignPorter] Congratulations you have Created new SignPorter called " + SourceName);	
						}
						event.setLine(0, ChatColor.DARK_BLUE + "[SignPorter]");
						event.setLine(1, SourceName);
						event.setLine(2, DestinationName);
						event.setLine(3, Integer.toString(Cost));
					}
					else
					{
						player.sendMessage("[SignPorter] you do not have permissions to create a SignPorter");
						dropSign(event);
						return;
					}
				}
			//}
		}
	}

	public void onBlockPlace( BlockPlaceEvent event)
	{
		if (event.getBlockAgainst().getTypeId() == Material.WALL_SIGN.getId() || event.getBlockAgainst().getTypeId() == Material.SIGN_POST.getId())
		{
			Sign sign = (Sign) event.getBlockAgainst().getState();
			if (sign.getLine(0).equalsIgnoreCase(ChatColor.DARK_BLUE + "[SignPorter]"))
			{
				MAG0CALogger.logger("Block is a Signporter cancel event");
				event.setCancelled(true);
			}
		}
	}
	
	public void onBlockBreak( BlockBreakEvent event)
	{
		if (!event.isCancelled())
		{
			MAG0CALogger.logger("event is not cancelled");
			if (event.getBlock().getTypeId() == Material.SIGN_POST.getId() || event.getBlock().getTypeId() == Material.WALL_SIGN.getId())
			{
				Block signa = event.getBlock();
				player = event.getPlayer();
				Sign sign = (Sign) signa.getState();
				Connection connection = null;
				
				//is it a SignPorter?
				if (sign.getLine(0).equalsIgnoreCase(ChatColor.DARK_BLUE + "[SignPorter]"))
				{
					//do you have the correct permissions?
					if (player.hasPermission("SignPorter.create") || player.hasPermission("SignPorter.*"))
					{	
						if (plugin.checkSource(sign.getLine(1)))
						{
							try {
								MAG0CALogger.logger("Initalizing Database connection");
								connection = DriverManager.getConnection("jdbc:" + plugin.DBType + ":" + config.DBURL, config.DBUsername, config.DBPassword);
								Statement statement = connection.createStatement();
								MAG0CALogger.logger("running SQL command: DELETE FROM " + config.DBTable + "WHERE Name=\'" + sign.getLine(1) + "\'");
								int i = statement.executeUpdate("DELETE FROM " + config.DBTable + " WHERE Name=\'" + sign.getLine(1) + "\'");
								MAG0CALogger.logger("deleted " + i + " rows from the table " + config.DBTable);
								player.sendMessage("[SignPorter] you have successfully deleted the portal " + sign.getLine(1));
							} catch (SQLException e) { MAG0CALogger.logger("could not complete Database transaction " + e.getMessage()); event.setCancelled(true); }
						}
						else
						{
							MAG0CALogger.logger("Sign " + sign.getLine(1) + " is not in the database");
						}

					}
					else
					{
						player.sendMessage("[SignPorter] you do no have the correct permissions to break this portal");
						event.setCancelled(true);
					}
					return;
				}
				return;
			}
			return;
		}
	}

//	public boolean checkDestination() 
//	{
//		ResultSet results;
//		boolean TF = false;
//		String columns = "Destination";
//		String where = "Destination=\'" + DestinationName + "\'";
//		try 
//		{
//			MAG0CALogger.logger("Executing: "  + "SELECT " + columns + " FROM " + config.DBTable + " WHERE " + where);
//			TF = plugin.statement.execute("SELECT " + columns + " FROM " + config.DBTable + " WHERE " + where);
//		} catch (SQLException e) {MAG0CALogger.logger("An Error occured while executing SQL SELECT " + e.getMessage());}
//		
//		if (TF)
//		{
//			TF = false;
//			String Name = null;
//			try 
//			{
//				results = plugin.statement.getResultSet();
//				Name = results.getString("Destination");
//				MAG0CALogger.logger("Destination results " + Name);
//			} catch (SQLException e) {MAG0CALogger.logger("Error while getting results " + e.getMessage());	}
//			if (Name == DestinationName)
//			{
//				TF = true;
//			}
//		}
//		return TF;
//	}
//	
//	public boolean checkSource() 
//	{
//		ResultSet results;
//		boolean TF = false;
//		String columns = "Name";
//		String where = "Name=\'" + SourceName + "\'";
//		try 
//		{
//			MAG0CALogger.logger("Executing: "  + "SELECT " + columns + " FROM " + config.DBTable + " WHERE " + where);
//			TF = plugin.statement.execute("SELECT " + columns + " FROM " + config.DBTable + " WHERE " + where);
//		} catch (SQLException e) {MAG0CALogger.logger("An Error occured while executing SQL SELECT " + e.getMessage());}
//		
//		if (TF)
//		{
//			TF = false;
//			String Name = null;
//			try 
//			{
//				results = plugin.statement.getResultSet();
//				Name = results.getString("Name");
//				MAG0CALogger.logger("Source results " + Name);
//			} catch (SQLException e) {MAG0CALogger.logger("Error while getting results " + e.getMessage());	}
//			if (Name == SourceName)
//			{
//				TF = true;
//			}
//		}
//		return TF;
//	}
//	
	
// Thank you Acrobot. taken from Chestshop saved me from having to think lol 	
    private static void dropSign(SignChangeEvent event) 
    {
        event.setCancelled(true);
        Block block = event.getBlock();
        block.setType(Material.AIR);
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SIGN, 1));
    }
}
