package me.mag0ca.SignPorter;

public class config 
{
	static String DBType;
	static String DBURL;
	static String DBUsername;
	static String DBPassword;
	static String DBTable;
	
	static boolean RandomEnabled;
	static int RandomXmax;
	static int RandomZmax;
	static int RandomYmax;
	static int RandomXmin;
	static int RandomZmin;
	static int RandomYmin;
	
	static String ColumnsMySQL = 
		  "(id int NOT NULL AUTO_INCREMENT PRIMARY KEY," +	//0
		  "Name varchar(32) UNIQUE," +						//1
		  "Destination varchar(32)," +						//2
		  "coordX int NOT NULL," +							//3
		  "coordY int NOT NULL," +							//4
		  "coordZ int NOT NULL," +							//5
		  "world varchar(64) NOT NULL," +					//6
		  "Cost int," +										//7
		  "Creator varchar(64))"; 							//8
		  

	static String ColumnsSQLite = 
		
		  "(id INTEGER NOT NULL PRIMARY KEY," + 	//0
		  "Name varchar(32) UNIQUE," +				//1
		  "Destination varchar(32)," +				//2
		  "coordX INTEGER NOT NULL," +				//3
		  "coordY INTEGER NOT NULL," +				//4
		  "coordZ INTEGER NOT NULL," +				//5
		  "world varchar(64) NOT NULL," +			//6
		  "Cost INTEGER," +							//7
		  "Creator varchar(64))";					//8
}
