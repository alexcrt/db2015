import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.acl.LastOwnerException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
 
public class OracleJDBC {
	
	private static Connection connection;
	private static final int batchSize = 1000;
 
	public static void main(String[] argv) {
		File currentDirectory = new File(new File(".").getAbsolutePath());
		System.out.println(currentDirectory.getAbsolutePath());
		System.out.println("-------- Oracle JDBC Connection Testing ------");
		
		//OracleJDBC.runPerson();
		//OracleJDBC.runAltName();
		//OracleJDBC.runProduction();
		//OracleJDBC.runAltTitle();
		//OracleJDBC.runCharacter();
		//OracleJDBC.runCompany();
		//OracleJDBC.runProdCompany();
		OracleJDBC.runProdCast();
 
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
 
	public static void runPerson() {
		String sql = "INSERT INTO PERSON(id, name, gender, trivia, quotes, birthdate, deathdate, birthname, minibiography, spouse, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		long row_number = 0;
		BufferedReader br = null;
		int max_run = 10000000;
		int to_skip = 0;
		int i = 1;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your Oracle JDBC Driver?");
			e.printStackTrace();
			return;
		}
 
		System.out.println("Oracle JDBC Driver Registered!");
		connection = null;
 
		try {
			connection = DriverManager.getConnection(
					"jdbc:oracle:thin:db2015_g03/bdd2015_super_g03@diassrv2.epfl.ch:1521:orcldias");
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
		
		PreparedStatement ps = null;
		
		System.out.println("Starting PERSON.csv");
		try {
			ps = connection.prepareStatement(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
			br = new BufferedReader(new FileReader("PERSON.csv"));
			String line;
			
			while (((line = br.readLine()) != null) && (i <= (max_run + to_skip))) {
				if (i <= to_skip) {
					i++;
					continue;
				}
				//System.out.println("PERSON : Doing row " + (row_number+1));
		        // use comma as separator
				String[] row = line.split("\t");
				if (row.length < 11) {
					//System.out.println("Skipping row + " + (row_number + 1));
					continue;
				}
				ps.setInt(1, Integer.parseInt(row[0]));
				
				ps.setString(2, row[1]);
				
				ps.setString(3, row[2]);
				
				if (row[3].equals("\\N"))
					ps.setNull(4, java.sql.Types.NULL);
				else
					ps.setString(4, row[3]);
				
				if (row[4].equals("\\N"))
					ps.setNull(5, java.sql.Types.NULL);
				else
					ps.setString(5, row[4]);
				
				if (row[5].equals("\\N"))
					ps.setNull(6, java.sql.Types.NULL);
				else {
					Date date = new SimpleDateFormat("dd MMMM yyyy", Locale.US).parse(row[5]);
					ps.setDate(6, new java.sql.Date(date.getTime()));
				}
				
				if (row[6].equals("\\N"))
					ps.setNull(7, java.sql.Types.NULL);
				else {
					Date date = new SimpleDateFormat("dd MMMM yyyy", Locale.US).parse(row[6]);
					ps.setDate(7, new java.sql.Date(date.getTime()));
				}
				
				if (row[7].equals("\\N"))
					ps.setNull(8, java.sql.Types.NULL);
				else
					ps.setString(8, row[7]);
				
				if (row[8].equals("\\N"))
					ps.setNull(9, java.sql.Types.NULL);
				else
					ps.setString(9, row[8]);
				
				if (row[9].equals("\\N"))
					ps.setNull(10, java.sql.Types.NULL);
				else
					ps.setString(10, row[9]);
				
				if (row[10].equals("\\N"))
					ps.setNull(11, java.sql.Types.NULL);
				else {
					String height = row[10];
					if (height.contains("cm")) {
						String[] splitted = height.split("cm");
						Number number = format.parse(splitted[0]);
						ps.setFloat(11, number.floatValue());
					} else {
						int feet = 0;
						float inches = 0;
						String[] splittedFeet = height.split("'");
						feet = new Integer(splittedFeet[0].trim());
						if ((splittedFeet.length > 1) && splittedFeet[1].contains("\"")) {
							String[] splittedInches = splittedFeet[1].split("\"");
							if (splittedInches[0].contains("1/2")) {
								splittedInches = splittedInches[0].split("1/2");
								inches += 0.5;
							} else if (splittedInches[0].contains("1/4")) {
								splittedInches = splittedInches[0].split("1/4");
								inches += 0.25;
							} else if (splittedInches[0].contains("3/4")) {
								splittedInches = splittedInches[0].split("3/4");
								inches += 0.75;
							} else if (splittedInches[0].contains("5/8")) {
								splittedInches = splittedInches[0].split("5/8");
								inches += Integer.parseInt(splittedInches[0].trim()) / 8f;
							} else if (splittedInches[0].contains("7/8")) {
								splittedInches = splittedInches[0].split("7/8");
								inches += Integer.parseInt(splittedInches[0].trim()) / 8f;
							}
							if (!splittedInches[0].trim().equals(""))
								inches += new Integer(splittedInches[0].trim());
						}
						float cmSize = ((feet*12)+inches)*2.54f;
						ps.setFloat(11, cmSize);
					}
				}
				
				ps.addBatch();
				i++;
				
				if(++row_number % batchSize == 0) {
					System.out.println("At row : " + row_number);
					System.out.println("Pushing " + batchSize + " entries");
			        ps.executeBatch();
			    }
			}
			System.out.println("PERSON: Pushing final entries");
			ps.executeBatch();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
					ps.close();
					connection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void runAltName() {
		String sql = "INSERT INTO ALTERNATIVE_NAME(id, person_id, alternative_name) VALUES (?, ?, ?)";
		long row_number = 0;
		BufferedReader br = null;
		
		PreparedStatement ps = null;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your Oracle JDBC Driver?");
			e.printStackTrace();
			return;
		}
 
		System.out.println("Oracle JDBC Driver Registered!");
		connection = null;
 
		try {
			connection = DriverManager.getConnection(
					"jdbc:oracle:thin:db2015_g03/bdd2015_super_g03@diassrv2.epfl.ch:1521:orcldias");
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
		
		System.out.println("Starting ALTERNATIVE_NAME.csv");
		try {
			ps = connection.prepareStatement(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			br = new BufferedReader(new FileReader("ALTERNATIVE_NAME.csv"));
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.println("ALT_NAME: Doing row " + (row_number+1));

				String[] row = line.split("\t");
				if (row.length < 3) {
					System.out.println("ALT_NAME: Skipping row + " + (row_number + 1));
					continue;
				}
				ps.setInt(1, Integer.parseInt(row[0]));
				
				ps.setInt(2, Integer.parseInt(row[1]));
				
				ps.setString(3, row[2]);
				
				ps.addBatch();
				
				if(++row_number % batchSize == 0) {
					System.out.println("ALT_NAME: Pushing " + batchSize + " entries");
			        ps.executeBatch();
			    }
			}
			System.out.println("ALT_NAME: Pushing final entries");
			ps.executeBatch();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
					ps.close();
					connection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void runProduction() {
		String sql = "INSERT INTO PRODUCTION(id, title, production_year, series_id, season_number, episode_number, series_year, kind, genre) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		long row_number = 0;
		BufferedReader br = null;
		
		PreparedStatement ps = null;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your Oracle JDBC Driver?");
			e.printStackTrace();
			return;
		}
 
		System.out.println("Oracle JDBC Driver Registered!");
		connection = null;
 
		try {
			connection = DriverManager.getConnection(
					"jdbc:oracle:thin:db2015_g03/bdd2015_super_g03@diassrv2.epfl.ch:1521:orcldias");
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
		
		System.out.println("Starting PRODUCTION.csv");
		try {
			ps = connection.prepareStatement(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			br = new BufferedReader(new FileReader("PRODUCTION.csv"));
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.println("PRODUCTION: Doing row " + (row_number+1));

				String[] row = line.split("\t");
				if (row.length < 9) {
					System.out.println("PRODUCTION: Skipping row + " + (row_number + 1));
					continue;
				}
				
				ps.setInt(1, Integer.parseInt(row[0]));
				
				ps.setString(2, row[1]);
				
				if (row[2].equals("\\N"))
					ps.setNull(3, java.sql.Types.NULL);
				else
					ps.setInt(3, Integer.parseInt(row[2]));
				
				if (row[3].equals("\\N"))
					ps.setNull(4, java.sql.Types.NULL);
				else
					ps.setInt(4, Integer.parseInt(row[3]));
				
				if (row[4].equals("\\N"))
					ps.setNull(5, java.sql.Types.NULL);
				else
					ps.setInt(5, Integer.parseInt(row[4]));
				
				if (row[5].equals("\\N"))
					ps.setNull(6, java.sql.Types.NULL);
				else
					ps.setInt(6, Integer.parseInt(row[5]));
				
				if (row[6].equals("\\N"))
					ps.setNull(7, java.sql.Types.NULL);
				else
					ps.setString(7, row[6]);
				
				if (row[7].equals("\\N"))
					ps.setNull(8, java.sql.Types.NULL);
				else
					ps.setString(8, row[7]);
				
				if (row[8].equals("\\N"))
					ps.setNull(9, java.sql.Types.NULL);
				else
					ps.setString(9, row[8]);
				
				ps.addBatch();
				
				if(++row_number % batchSize == 0) {
					System.out.println("PRODUCTION: Pushing " + batchSize + " entries");
			        ps.executeBatch();
			    }
			}
			System.out.println("Pushing final entries");
			ps.executeBatch();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
					ps.close();
					connection.close();
					System.out.println("Connection closed !");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void runAltTitle() {
		String sql = "INSERT INTO ALTERNATIVE_TITLE(id, production_id, title) VALUES (?, ?, ?)";
		long row_number = 0;
		BufferedReader br = null;
		
		PreparedStatement ps = null;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your Oracle JDBC Driver?");
			e.printStackTrace();
			return;
		}
 
		System.out.println("Oracle JDBC Driver Registered!");
		connection = null;
 
		try {
			connection = DriverManager.getConnection(
					"jdbc:oracle:thin:db2015_g03/bdd2015_super_g03@diassrv2.epfl.ch:1521:orcldias");
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
		
		System.out.println("ALT_TITLE: Starting ALTERNATIVE_TITLE.csv");
		try {
			ps = connection.prepareStatement(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			br = new BufferedReader(new FileReader("ALTERNATIVE_TITLE.csv"));
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.println("ALT_TITLE: Doing row " + (row_number+1));
		        // use comma as separator
				String[] row = line.split("\t");
				if (row.length < 3) {
					System.out.println("ALT_TITLE: Skipping row + " + (row_number + 1));
					continue;
				}
				ps.setInt(1, Integer.parseInt(row[0]));
				
				ps.setInt(2, Integer.parseInt(row[1]));
				
				ps.setString(3, row[2]);
				
				ps.addBatch();
				
				if(++row_number % batchSize == 0) {
					System.out.println("ALT_TITLE: Pushing " + batchSize + " entries");
			        ps.executeBatch();
			    }
			}
			System.out.println("Pushing final entries");
			ps.executeBatch();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
					ps.close();
					connection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void runCharacter() {
		String sql = "INSERT INTO CHARACTER_TABLE(id, name) VALUES (?, ?)";
		long row_number = 0;
		BufferedReader br = null;
		
		PreparedStatement ps = null;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your Oracle JDBC Driver?");
			e.printStackTrace();
			return;
		}
 
		System.out.println("Oracle JDBC Driver Registered!");
		connection = null;
 
		try {
			connection = DriverManager.getConnection(
					"jdbc:oracle:thin:db2015_g03/bdd2015_super_g03@diassrv2.epfl.ch:1521:orcldias");
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
		
		System.out.println("Starting CHARACTER.csv");
		try {
			ps = connection.prepareStatement(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			br = new BufferedReader(new FileReader("CHARACTER.csv"));
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.println("CHARACTER: Doing row " + (row_number+1));
		        // use comma as separator
				String[] row = line.split("\t");
				if (row.length < 2) {
					System.out.println("CHARACTER: Skipping row + " + (row_number + 1));
					continue;
				}
				ps.setInt(1, Integer.parseInt(row[0]));
				
				ps.setString(2, row[1]);
				
				ps.addBatch();
				
				if(++row_number % batchSize == 0) {
					System.out.println("Pushing " + batchSize + " entries");
			        ps.executeBatch();
			    }
			}
			System.out.println("CHARACTER: Pushing final entries");
			ps.executeBatch();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
					ps.close();
					connection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void runCompany() {
		String sql = "INSERT INTO COMPANY(id, country_code, name) VALUES (?, ?, ?)";
		long row_number = 0;
		BufferedReader br = null;
		
		PreparedStatement ps = null;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your Oracle JDBC Driver?");
			e.printStackTrace();
			return;
		}
 
		System.out.println("Oracle JDBC Driver Registered!");
		connection = null;
 
		try {
			connection = DriverManager.getConnection(
					"jdbc:oracle:thin:db2015_g03/bdd2015_super_g03@diassrv2.epfl.ch:1521:orcldias");
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
		
		System.out.println("Starting COMPANY.csv");
		try {
			ps = connection.prepareStatement(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			br = new BufferedReader(new FileReader("COMPANY.csv"));
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.println("COMPANY: Doing row " + (row_number+1));
		        // use comma as separator
				String[] row = line.split("\t");
				if (row.length < 3) {
					System.out.println("COMPANY: Skipping row + " + (row_number + 1));
					continue;
				}
				ps.setInt(1, Integer.parseInt(row[0]));
				
				ps.setString(2, row[1]);
				
				ps.setString(3, row[2]);
				
				ps.addBatch();
				
				if(++row_number % batchSize == 0) {
					System.out.println("COMPANY: Pushing " + batchSize + " entries");
			        ps.executeBatch();
			    }
			}
			System.out.println("COMPANY: Pushing final entries");
			ps.executeBatch();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
					ps.close();
					connection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void runProdCompany() {
		String sql = "INSERT INTO PRODUCTION_COMPANY(id, company_id, production_id, company_type) VALUES (?, ?, ?, ?)";
		long row_number = 0;
		BufferedReader br = null;
		
		PreparedStatement ps = null;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your Oracle JDBC Driver?");
			e.printStackTrace();
			return;
		}
 
		System.out.println("Oracle JDBC Driver Registered!");
		connection = null;
 
		try {
			connection = DriverManager.getConnection(
					"jdbc:oracle:thin:db2015_g03/bdd2015_super_g03@diassrv2.epfl.ch:1521:orcldias");
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
		
		System.out.println("Starting PRODUCTION_COMPANY.csv");
		try {
			ps = connection.prepareStatement(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			br = new BufferedReader(new FileReader("PRODUCTION_COMPANY.csv"));
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.println("PROD_COMPANY: Doing row " + (row_number+1));
		        // use comma as separator
				String[] row = line.split("\t");
				if (row.length < 4) {
					System.out.println("PROD_COMPANY: Skipping row + " + (row_number + 1));
					continue;
				}
				ps.setInt(1, Integer.parseInt(row[0]));
				
				ps.setInt(2, Integer.parseInt(row[1]));
				
				ps.setInt(3, Integer.parseInt(row[2]));
				
				ps.setString(4, row[3]);
				
				ps.addBatch();
				
				if(++row_number % batchSize == 0) {
					System.out.println("PROD_COMPANY: Pushing " + batchSize + " entries");
			        ps.executeBatch();
			    }
			}
			System.out.println("PROD_COMPANY: Pushing final entries");
			ps.executeBatch();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
					ps.close();
					connection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/*public static void runProdCast() {
		String sql = "INSERT INTO PRODUCTION_CAST(id, production_id, person_id, character_id, role) VALUES (?, ?, ?, ?, ?)";
		long row_number = 0;
		long max_row_run = 1000000;
		int to_skip = 0; //500000
		int id = 1;
		BufferedReader br = null;
		
		PreparedStatement ps = null;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			//Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your Oracle JDBC Driver?");
			e.printStackTrace();
			return;
		}
 
		System.out.println("Oracle JDBC Driver Registered!");
		connection = null;
 
		try {
			connection = DriverManager.getConnection(
					"jdbc:oracle:thin:db2015_g03/bdd2015_super_g03@diassrv2.epfl.ch:1521:orcldias");
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
		
		System.out.println("Starting PRODUCTION_CAST.csv");
		try {
			ps = connection.prepareStatement(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			br = new BufferedReader(new FileReader("PRODUCTION_CAST.csv"));
			String line;
			
			long start = System.currentTimeMillis();
			while ((line = br.readLine()) != null) {
				System.out.println("PROD_CAST: Doing row " + (row_number+1));
		        // use comma as separator
				String[] row = line.split("\t");
				if (row.length < 4) {
					System.out.println("PROD_CAST: Skipping row " + (row_number + 1));
					continue;
				}
				
				if(to_skip > 0) {
					row_number++;
					to_skip--;
					System.out.println("Skipping row : " + row_number);
					continue;
				}
				
				ps.setInt(1, id);
				
				ps.setInt(2, Integer.parseInt(row[0]));
				
				ps.setInt(3, Integer.parseInt(row[1]));
				
				if (row[2].equals("\\N"))
					ps.setNull(4, java.sql.Types.NULL);
				else
					ps.setInt(4, Integer.parseInt(row[2]));
				
				ps.setString(5, row[3]);
				
				ps.addBatch();
				
				id++;
				
				if(++row_number % batchSize == 0) {
					System.out.println("At entry : " + row_number);
					System.out.println("PROD_CAST: Pushing " + batchSize + " entries");
					//System.out.println("Last mean-elapsed time : " + meanTime);
			        ps.executeBatch();
			        ps.clearBatch();
			        if (row_number == (max_row_run + to_skip)) {
			        	System.out.println("Last done row : " + row_number);
			        	break;
			        }
			    }
			}
			System.out.println("PROD_CAST: Pushing final entries");
			ps.executeBatch();
			float elapsed = (System.currentTimeMillis() - start)/1000f;
			System.out.println("Elapsed time in seconds : " + elapsed);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
					ps.close();
					connection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}*/
	
	public static void runProdCast() {
		String sql = "INSERT INTO PRODUCTION_CAST(id, production_id, person_id, character_id, role) VALUES (";
		int id = 1;
		int max_row_run = 14000000;
		int to_skip = 33000000;//1000000
		BufferedReader br = null;
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("PROD_CAST_PERSO_4.csv", "UTF-8");
			writer.println("id,production_id,person_id,character_id,role");
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			br = new BufferedReader(new FileReader("PRODUCTION_CAST.csv"));
			String line;
			
			while (((line = br.readLine()) != null) && (id <= (max_row_run+to_skip))) {
				if (id <= to_skip) {
					System.out.println("Skipping row " + id);
					id++;
					continue;
				}
				System.out.println("PROD_CAST: Doing row " + id);
		        // use comma as separator
				String[] row = line.split("\t");
				if (row[2].equals("\\N"))
					row[2] = "NULL";
				String toAdd = id + "," + row[0] + "," + row[1] + "," + row[2] + "," + "'" + row[3] + "'";
				writer.println(toAdd);
				id++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}