package me.mag0ca.SignPorter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.mag0ca.utils.MAG0CALogger;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SignPorterCommandExecutor implements CommandExecutor
{
	private SignPorter plugin;
	
	public SignPorterCommandExecutor(SignPorter instance) 
	{
		plugin = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		boolean TF = false;
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		
		if (command.getName().equalsIgnoreCase("sptp") || command.getName().equalsIgnoreCase("SignPorter")) 
		{
			if (player == null) 
			{
				sender.sendMessage("[SignPorter] This command must be run by an ingame Player.");
			} 
			else if (player.hasPermission("signporter.*") || player.hasPermission("signporter.tp"))
			{
				if ( args == null || args.length <= 0 )
				{
					return false;
				}
				
				String[] P = args[0].split(":");
				if (P[0].compareToIgnoreCase("p") == 0)
				{
					Player P2 = player.getServer().getPlayer(P[1]);
					if (P2 != null)
					{
						player.sendMessage("[SignPorter] Teleporting you to " + P2.getDisplayName());
						player.teleport(P2);
						P2.sendMessage("[SignPorter] Hey you have a friend " + player.getDisplayName() + " has come to play");
						TF = true;
					}
					else
					{
						player.sendMessage("[SignPorter] the player " + P[1] + " is not currently logged on");
						TF = true;

					}
				}
				//or if the sign set for a random location?
				else if (args[0].equalsIgnoreCase("random"))
				{
					plugin.tpRandom(player);
					TF = true;
				}
				else if (args[0].equalsIgnoreCase("spawn"))
				{
					Location playerBed = player.getBedSpawnLocation();
					if (playerBed == null)
					{
						player.teleport(player.getWorld().getSpawnLocation());
						player.sendMessage("[SignPorter] you have just been sent to the world spawnpoint. Hope you had a safe trip.");
					}
					else
					{
						player.teleport(playerBed);
						player.sendMessage("[SignPorter] you have just been sent to your bed. Hope you had a safe trip.");
					}
					TF = true;
				}
				else
				{
					ResultSet results = null;
					TF = false;
					Statement statement = null;
					String Name = null;
					String world = null;
					int X = 0;
					int Y = 0;
					int Z = 0;
					int cost = 0;
					String destinationName = args[0];
					Connection connection = null;

					try 
					{
						MAG0CALogger.logger("Initalizing Database connection");
						connection = DriverManager.getConnection("jdbc:" + plugin.DBType + ":" + config.DBURL, config.DBUsername, config.DBPassword);
						MAG0CALogger.logger("Readying statement processor");
						statement = connection.createStatement();
					} catch (SQLException e1) {MAG0CALogger.logger("statement processor failed " + e1.getMessage());}

					String columns = "*";
					String where = "Name=\'" + destinationName + "\'";
					try 
					{
						MAG0CALogger.logger("Executing: "  + "SELECT " + columns + " FROM " + config.DBTable + " WHERE " + where);
						TF = statement.execute("SELECT " + columns + " FROM " + config.DBTable + " WHERE " + where);
					} catch (SQLException e) {MAG0CALogger.logger("An Error occured while executing SQL SELECT " + e.getMessage());}

					if (TF)
					{
						TF = false;

						try 
						{
							MAG0CALogger.logger("Getting resultset");
							results = statement.getResultSet();
							MAG0CALogger.logger("got resultset");
							if (results != null)
							{
								MAG0CALogger.logger("resultset is not null");
								if (results.next())
								{
									MAG0CALogger.logger("into next result");
									Name = results.getString("Name");
									MAG0CALogger.logger("Got name" + Name);
									X = results.getInt("coordX");
									Y = results.getInt("coordY");
									Z = results.getInt("coordZ");
									world = results.getString("world");
									cost = results.getInt("Cost");
									MAG0CALogger.logger("Destination results " + Name + " X: " + X + " Y: " + Y + " Z: " + Z + " Cost:" + cost + " World: " + world);
									if (Name.equalsIgnoreCase(destinationName))
									{
										TF = true;
										Location Locate = null;
										Locate = new Location(player.getServer().getWorld(world), X,Y,Z);
										//and away we go
										player.teleport(Locate);
										player.sendMessage("you have just been teleported to " + Name + " Hope you had a safe trip");
									}
								}
								else
								{
									player.sendMessage("[SignPorter] The destination does not appear to exist");
									TF = true;
								}
							}
							
						} catch (Exception e) {MAG0CALogger.logger("Error while getting results " + e.getMessage());	}

					}	
					
//					ResultSet results = null;
//					boolean TF1 = false;
//					Statement statement = null;
//					try 
//					{
//						MAG0CALogger.logger("Readying statement processor");
//						statement = plugin.connection.createStatement();
//					} catch (SQLException e1) {MAG0CALogger.logger("statement processor failed " + e1.getMessage());}
//					
//					String columns = "Destination";
//					String where = "Destination=\'" + args[0] + "\'";
//					try 
//					{
//						MAG0CALogger.logger("Executing: "  + "SELECT " + columns + " FROM " + config.DBTable + " WHERE " + where);
//						TF1 = statement.execute("SELECT " + columns + " FROM " + config.DBTable + " WHERE " + where);
//					} catch (SQLException e) {MAG0CALogger.logger("An Error occured while executing SQL SELECT " + e.getMessage());}
//					
//					if (TF1)
//					{
//						TF1 = false;
//						String Name = null;
//						try 
//						{
//							results = statement.getResultSet();
//							Name = results.getString("Destination");
//							MAG0CALogger.logger("Destination results " + Name);
//							results.close();
//						} catch (SQLException e) {MAG0CALogger.logger("Error while getting results " + e.getMessage());	}
//						if (Name == args[0])
//						{
//							TF1 = true;
//						}
//					}
//					
//					//lets check out the database and see if it exists
//					if (TF1)
//					{
//						//does it exist?
//						try {
//							if (results.next())
//							{
//								Location Locate = null;
//								String Dest = null;
//								//get the destination info from the Database
//								Dest = results.getString(2);
//								Locate = new Location(plugin.getServer().getWorld(results.getString(6)), results.getInt(3),results.getInt(4),results.getInt(5));
//								TF = true;
//								//make sure everything worked out ok
//								if (!args[0].equalsIgnoreCase(Dest))
//								{
//									MAG0CALogger.logger("Something must have gone wrong with the Database query Destinations don't match. From DB: " + Dest + " from sign: " + args[0]);
//									return true;
//								}
//								
//								if (Locate == null || Dest == null)
//								{
//									MAG0CALogger.logger("Something must have gone wrong with the Database query the Location and/or Destination are null");
//									return true;
//								}
//								
//								//and away we go
//								player.teleport(Locate);
//								player.sendMessage("you have just been teleported to " + Dest + " Hope you had a safe trip");
//								TF = true;
//							}
//							results.close();
//						} catch (SQLException e) 
//						{
//							MAG0CALogger.logger("For some reason the results from the database threw and exception after being retrieved from the Database " + e.getMessage());
//							plugin.log.severe("A serious error has occured while reading the DataBase data");
//							return true;							}
//					}	
//					TF = true;
				}
//				{
//					String[] dest = {"*"};
//					//lets check out the database and see if it still exists
//					if (plugin.database.Select(config.DBTable, dest, "Destination = " + args[0]))
//					{
//						//does it exist?
//						if (plugin.database.results != null)
//						{
//							Location Locate = null;
//							String Dest = null;
//							//get the destination info from the Database
//							try 
//							{
//								Dest = plugin.database.results.getString(2);
//								Locate = new Location(plugin.getServer().getWorld(plugin.database.results.getString(6)), plugin.database.results.getInt(3),plugin.database.results.getInt(4),plugin.database.results.getInt(5));
//							} catch (SQLException e) 
//							{
//								MAG0CALogger.logger("For some reason the results fromt he database threw and exception after being retrieved from the Database " + e.getMessage());
//								plugin.log.severe("A serious error has occured while reading the DataBase data");
//								return true;
//							}
//
//							//make sure everything worked out ok
//							if (args[0].compareToIgnoreCase(Dest) != 0)
//							{
//								MAG0CALogger.logger("Something must have gone wrong with the Database query Destinations don't match. From DB: " + Dest + " from sign: " + args[0]);
//								return true;
//							}
//
//							if (Locate == null || Dest == null)
//							{
//								MAG0CALogger.logger("Something must have gone wrong with the Database query the Location and/or Destination are null");
//								return true;
//							}
//
//							//and away we go
//							player.teleport(Locate);
//							player.sendMessage("you have just been teleported to " + Dest + " Hope you had a safe trip");
//						}
//					}
//					TF = true;
//				}
			}
			else
			{
				player.sendMessage("[SignPorter] you do not have the correct permissions to use this command");
			}
		}		
		return TF;
	}
}