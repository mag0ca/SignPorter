package me.mag0ca.SignPorter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.mag0ca.utils.MAG0CALogger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

public class SignPorterPlayerListener extends PlayerListener
{
	private SignPorter plugin;
	
	public SignPorterPlayerListener( SignPorter instance ) 
	{
        	plugin = instance;
	}

	//run when a player interacts with a sign object
	public void onPlayerInteract( PlayerInteractEvent event )
	{
		if (!event.isCancelled())
		{
			if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR)
			{
				return;
			}

			if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) && (event.getClickedBlock().getTypeId() == Material.WALL_SIGN.getId() || event.getClickedBlock().getTypeId() == Material.SIGN_POST.getId()))
			{
				Player player = event.getPlayer();

				//is the sign a SignPorter?
				Sign sign = (Sign) event.getClickedBlock().getState();
				if (sign.getLine(0).compareToIgnoreCase(ChatColor.DARK_BLUE + "[SignPorter]") == 0)
				{
					//does the player have the correct permissions?
					if (player.hasPermission("signporter.use") || player.hasPermission("signporter.*"))
					{
						//allow the sign to be broken
						if (player.isSneaking() || sign.getLine(2).isEmpty())
						{
							return;
						}
						String destinationName = sign.getLine(2);

						String[] L = (destinationName.split(","));

						//does the sign of direct coordinates on it?
						if (L.length == 3)
						{
							int[] S = new int[L.length];
							for (int i = 0; i > L.length; i++)
							{
								S[i] = Integer.parseInt(L[i]);
							}
							Location Locate = new Location(player.getWorld(),S[0], S[1], S[2]);

							//and your off
							player.teleport(Locate);
							player.sendMessage("you have just been teleported to " + Locate.getX() + ","  + Locate.getY() + "," + Locate.getZ() + " Hope you had a safe trip.");
						}

						//or if the sign set for a random location?
						else if (destinationName.equalsIgnoreCase("random"))
						{
							plugin.tpRandom(player);
						}
						else if (destinationName.equalsIgnoreCase("spawn"))
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
						}

						//or is it a specific destination?
						else
						{
							ResultSet results = null;
							boolean TF = false;
							Statement statement = null;
							String Name = null;
							String world = null;
							int X = 0;
							int Y = 0;
							int Z = 0;
							int cost = 0;
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
											player.sendMessage("[SignPorter] The destination for this portal has been removed");
											return;
										}
									}
									
								} catch (Exception e) {MAG0CALogger.logger("Error while getting results " + e.getMessage());	}
							}						
						}
					}
					else
					{
						player.sendMessage("you do  not have permissions to use a sign portal");
					}
				}
			}	
		}
	}
}