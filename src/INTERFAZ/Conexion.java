package INTERFAZ;
import javax.swing.JFrame;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class Conexion {
	
	
	
	    private String host;
	    private int puerto;
	    private String usuario;
	    private String contrasena;

	  

	    // Resto del código de la clase...
	

	
    private Connection connection; // Asumiendo que tienes una conexión a la base de datos

	private Connection conexionMysQl;
	Statement statemnet = null;
	private JFrame frame;

	
	

	    public Connection getConnection() {
	        JOptionPane.showMessageDialog(null, "Se estableció la conexión a MySQL");
	        return conexionMysQl;
	    }

	    public void cerrar() throws SQLException {
	        if (conexionMysQl != null && !conexionMysQl.isClosed()) {
	            conexionMysQl.close();
	        }
	    }

	    public void crearBaseDeDatos(String nombreBD) throws SQLException {
	        try (Statement statement = conexionMysQl.createStatement()) {
	            String sql = "CREATE DATABASE " + nombreBD;
	            statement.executeUpdate(sql);
	            JOptionPane.showMessageDialog(null, "Se creó la base de datos '" + nombreBD + "' con éxito.");
	        } catch (SQLException e) {
	            JOptionPane.showMessageDialog(null, "Error al crear la base de datos: " + e.getMessage());
	        }
	    }

	    
	    
	  
	    
	      public void borrarBaseDeDatos(String nombreBD) throws SQLException {
	        try (Statement statement = conexionMysQl.createStatement()) {
	            String sql = "DROP DATABASE " + nombreBD;
	            statement.executeUpdate(sql);
	            JOptionPane.showMessageDialog(null, "Se borrol la base de datos '" + nombreBD + "' con éxito.");
	            
	        } catch (SQLException e) {
	            JOptionPane.showMessageDialog(null, "Error al borrar la base de datos: " + e.getMessage());
	        }
	    }
	      
	      
	      public List<String> obtenerNombresBasesDatos() throws SQLException {
	          List<String> nombresBasesDatos = new ArrayList<>();
	          Statement statement = conexionMysQl.createStatement();
	          ResultSet resultSet = statement.executeQuery("SHOW DATABASES");
	          while (resultSet.next()) {
	              String nombreBD = resultSet.getString(1);
	              nombresBasesDatos.add(nombreBD);
	          }
	          return nombresBasesDatos;
	      }
	      
	      
	     
/////////////////////////////////////////////////////////////////////77
	   

	      public void conexion(String host, String puerto, String usuario, String contrasena) throws SQLException {
	    	    String url = "jdbc:mysql://" + host + ":" + puerto + "/";
	    	    conexionMysQl = DriverManager.getConnection(url, usuario, contrasena);
	    	    JOptionPane.showMessageDialog(null, "Se estableció la conexión a MySQL");
	    	}

	    	public  Connection obtenerConexion() {
	    	    return conexionMysQl;
	    	}
	    
	    	

	    
	    	
	    	
	          public void cerrarConexion() {
	              try {
	                  if (conexionMysQl != null) {
	                      conexionMysQl.close();
	                      JOptionPane.showMessageDialog(null, "Se cerró la conexión a MySQL");
	                  }
	              } catch (SQLException e) {
	                  // Manejar cualquier error que ocurra al cerrar la conexión
	                  e.printStackTrace();
	              }
	          }
	      
	          
	          public void pruebaConexion(Connection connection) throws SQLException {
	        	    String sql = "SHOW DATABASES";
	        	    try (Statement statement = connection.createStatement()) {
	        	        ResultSet resultSet = statement.executeQuery(sql);

	        	        // Aquí puedes procesar el resultado si lo necesitas
	        	        while (resultSet.next()) {
	        	            String databaseName = resultSet.getString(1);
	        	            System.out.println("Database: " + databaseName);
	        	        }
	        	    }
	        	}

	          public List<String> obtenerBasesDeDatos() throws SQLException {
	        	    List<String> databases = new ArrayList<>();
	        	    DatabaseMetaData metaData = conexionMysQl.getMetaData();
	        	    try (ResultSet resultSet = metaData.getCatalogs()) {
	        	        while (resultSet.next()) {
	        	            String databaseName = resultSet.getString("TABLE_CAT");
	        	            databases.add(databaseName);
	        	        }
	        	    }
	        	    return databases;
	        	}


	          // Método para obtener la lista de tablas de una base de datos
	        	          public List<String> obtenerTablas(String databaseName) throws SQLException {
	        	              List<String> tables = new ArrayList<>();
	        	              Connection conn = obtenerConexion();
	        	              if (conn == null) {
	        	                  return tables;
	        	              }

	        	              Statement statement = null;
	        	              ResultSet resultSet = null;

	        	              try {
	        	                  statement = conn.createStatement();
	        	                  // Consulta SQL para obtener las tablas de una base de datos
	        	                  String sql = "SHOW TABLES FROM " + databaseName;
	        	                  resultSet = statement.executeQuery(sql);

	        	                  while (resultSet.next()) {
	        	                      String tableName = resultSet.getString(1);
	        	                      tables.add(tableName);
	        	                  }
	        	              } finally {
	        	                  if (resultSet != null) {
	        	                      resultSet.close();
	        	                  }
	        	                  if (statement != null) {
	        	                      statement.close();
	        	                  }
	        	                  conn.close();
	        	              }

	        	              return tables;
	        	          }
	                        

	          
	          public boolean createTable(String nombreTabla) {
	        	    // Configura la sentencia SQL para crear la tabla
					String sql = "CREATE TABLE " + nombreTabla + " (id INT PRIMARY KEY, nombre VARCHAR(255), edad INT)";
					
					// Crea la tabla en la base de datos
					try (Statement statement = connection.createStatement()) {
					    statement.executeUpdate(sql);
					    JOptionPane.showMessageDialog(null, "Se creó la tabla '" + nombreTabla + "' con éxito.");
					    return true;
					} catch (SQLException e) {
					    JOptionPane.showMessageDialog(null, "Error al crear la tabla: " + e.getMessage());
					    return false;
					}
	        	}

	     

	          public boolean agregarColumnaATabla(String nombreTabla, String nombreColumna, String tipoColumna, String longitudValores, boolean permitirNulo, String indice) {
	        	    // Configura la sentencia SQL para agregar una columna a la tabla
					String sql = "ALTER TABLE " + nombreTabla + " ADD COLUMN " + nombreColumna + " " + tipoColumna;
					
					// Agrega la longitud de valores si es aplicable
					if (longitudValores != null && !longitudValores.isEmpty()) {
					    sql += "(" + longitudValores + ")";
					}

					// Marca la columna como permitir nulos o no
					if (!permitirNulo) {
					    sql += " NOT NULL";
					}

					// Agrega un índice si es aplicable
					if (indice != null && !indice.isEmpty() && !indice.equals("Ninguno")) {
					    sql += ", ADD " + indice + " (" + nombreColumna + ")";
					}

					// Ejecuta la sentencia SQL
					try (Statement statement = connection.createStatement()) {
					    statement.executeUpdate(sql);
					    JOptionPane.showMessageDialog(null, "Se agregó la columna '" + nombreColumna + "' a la tabla '" + nombreTabla + "' con éxito.");
					    return true;
					} catch (SQLException e) {
					    JOptionPane.showMessageDialog(null, "Error al agregar la columna: " + e.getMessage());
					    return false;
					}
	        	}

		
	          
	      

}
