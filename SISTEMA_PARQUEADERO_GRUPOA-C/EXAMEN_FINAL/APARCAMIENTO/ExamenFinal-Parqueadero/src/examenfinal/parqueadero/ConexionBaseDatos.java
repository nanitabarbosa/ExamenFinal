package examenfinal.parqueadero;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConexionBaseDatos {
    private final String db = "baseparqueadero";
    private final String url = "jdbc:mysql://127.0.0.1:3306/" + db;
    private final String user = "root";
    private final String pass = "";

    public Connection conectar() {
        Connection connection = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver cargado exitosamente.");
            connection = DriverManager.getConnection(url, user, pass);
            System.out.println("Conexión exitosa a la base de datos: " + db);
        } catch (Exception e) {
            System.out.println("Error durante la conexión a la base de datos: " + e.getMessage());
        }

        return connection;
    }
}
