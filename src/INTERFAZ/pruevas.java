package INTERFAZ;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class pruevas {
    public static void main(String[] args) {
        // Datos de conexión a la base de datos
        String url = "jdbc:mysql://localhost:3306/";  // URL de la base de datos (asegúrate de cambiarla si es diferente)
        String user = "root";  // Usuario de la base de datos
        String password = "";  // Contraseña de la base de datos

        try (Scanner scanner = new Scanner(System.in)) {
            // Solicitar el nombre de la base de datos
            System.out.print("Ingresa el nombre de la base de datos: ");
            String databaseName = scanner.nextLine();

            // Solicitar el nombre de la tabla
            System.out.print("Ingresa el nombre de la tabla: ");
            String tableName = scanner.nextLine();

            // Solicitar la cantidad de columnas
            System.out.print("Ingresa la cantidad de columnas: ");
            int numColumns = Integer.parseInt(scanner.nextLine());

            // Crear la conexión a la base de datos
            Connection connection = DriverManager.getConnection(url + databaseName, user, password);

            // Crear la sentencia SQL para crear la tabla
            System.out.println("Ingresa los nombres de las columnas y sus tipos de datos.");
            System.out.println("Formato: nombre_columna tipo_de_dato (por ejemplo, id INT).");

            StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

            for (int i = 0; i < numColumns; i++) {
                System.out.print("Columna " + (i + 1) + ": ");
                String columnDefinition = scanner.nextLine();
                createTableSQL.append(columnDefinition);
                if (i < numColumns - 1) {
                    createTableSQL.append(", ");
                }
            }

            createTableSQL.append(");");

            // Crear la tabla
            Statement statement = connection.createStatement();
            statement.executeUpdate(createTableSQL.toString());

            System.out.println("La tabla se ha creado con éxito.");

            // Cerrar la conexión
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al conectar a la base de datos o al crear la tabla: " + e.getMessage());
        }
    }
}
