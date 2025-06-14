package source.eticaret.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseService {
    private static DatabaseService instance;
    private boolean isInitializing = false;

    private DatabaseService() {}

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "eticaretdb";
    private static final String URL = DB_URL + DB_NAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password";

    private Connection connection;
    private boolean isInitialized = false;

    public void connect() throws SQLException {
        getConnection();
    }

    public void createDatabase() throws SQLException {
        System.out.println("Veritabanı ve tablolar oluşturuluyor...");
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            stmt.execute("USE " + DB_NAME);
            if (!isInitialized) {
                initializeDatabase();
                isInitialized = true;
            }
        } catch (SQLException e) {
            System.err.println("Veritabanı oluşturma hatası: " + e.getMessage());
            throw e;
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (isInitializing) {
                throw new SQLException("Veritabanı başlatma devam ediyor");
            }
            
            System.out.println("Yeni veritabanı bağlantısı oluşturuluyor...");
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Veritabanı bağlantısı başarıyla kuruldu (otomatik onay: " + connection.getAutoCommit() + ")");
            } catch (SQLException e) {
                System.err.println("Veritabanı bağlantı hatası: " + e.getMessage());
                if (e.getMessage() != null && e.getMessage().contains("Unknown database")) {
                    if (isInitializing) {
                        throw new SQLException("Başlatılırken veritabanı oluşturulamadı", e);
                    }
                    System.out.println("Veritabanı bulunamadı, yeni veritabanı oluşturuluyor...");
                    createDatabase();
                    System.out.println("Yeni oluşturulan veritabanına bağlanılıyor...");
                    connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    System.out.println("Yeni oluşturulan veritabanına bağlantı başarılı (otomatik onay: " + connection.getAutoCommit() + ")");
                } else {
                    throw new SQLException("Veritabanı bağlantı hatası: " + e.getMessage(), e);
                }
            }
        }
        return connection;
    }

    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    private void dropDatabase() {
        String baseUrl = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(baseUrl, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP DATABASE IF EXISTS " + DB_NAME);
            System.out.println("Mevcut veritabanı silindi: " + DB_NAME);
        } catch (SQLException e) {
            System.err.println("Veritabanı silinirken hata oluştu: " + e.getMessage());
        }
    }

    public synchronized void initializeDatabase() throws SQLException {
        if (isInitialized) {
            System.out.println("Veritabanı zaten başlatıldı");
            return;
        }
        
        isInitializing = true;
        System.out.println("Veritabanı başlatılıyor...");
        
        try {
            String baseUrl = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            try (Connection conn = DriverManager.getConnection(baseUrl, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                System.out.println("Veritabanı kontrol edildi, gerekirse oluşturuldu");
                stmt.execute("USE " + DB_NAME);
                boolean tableExists = false;
                try (ResultSet rs = conn.getMetaData().getTables(null, null, "products", null)) {
                    tableExists = rs.next();
                }
                
                if (!tableExists) {
                    System.out.println("Tablo oluşturuluyor...");
                    executeSqlFile("/database.sql");
                    
                    if (shouldInsertInitialData()) {
                        System.out.println("Başlangıç verileri ekleniyor...");
                        insertInitialData();
                    }
                } else {
                    System.out.println("Mevcut veritabanı kullanılıyor, şema değişiklikleri uygulanıyor...");
                }
            }
            
            isInitialized = true;
            System.out.println("Veritabanı başlatma başarıyla tamamlandı");
            
        } catch (SQLException e) {
            System.err.println("Veritabanı başlatılamadı: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Veritabanı başlatılamadı: " + e.getMessage(), e);
        } finally {
            isInitializing = false;
        }
    }

    private void executeSqlFile(String filename) throws SQLException {
        try (InputStream input = getClass().getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            
            if (input == null) {
                throw new SQLException("SQL dosyası bulunamadı: " + filename);
            }
            

            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                fileContent.append(" ").append(line);
            }
            

            String[] statements = fileContent.toString().split(";\\s*");
            

            for (String sql : statements) {
                sql = sql.trim();
                if (sql.isEmpty()) continue;
                
                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                     Statement stmt = conn.createStatement()) {
                    
                    conn.setAutoCommit(false);
                    stmt.execute(sql);
                    conn.commit();
                    
                } catch (SQLException e) {
                    throw new SQLException("SQL çalıştırma hatası: " + e.getMessage() + "\nSQL: " + sql, e);
                }
            }
            
        } catch (IOException e) {
            throw new SQLException("SQL dosyası okunamadı: " + e.getMessage(), e);
        }
    }

    private boolean shouldInsertInitialData() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM products")) {
            if (rs.next()) {
                int count = rs.getInt("count");
                return count == 0;
            }
            return true;
        } catch (SQLException e) {
            return true;
        }
    }

    public void insertInitialData() throws SQLException {
        try {
            executeSqlFile("/veriekle.sql");
        } catch (Exception e) {
            System.err.println("Başlangıç verileri eklenirken hata: " + e.getMessage());
            if (e instanceof SQLException) throw (SQLException) e;
            throw new SQLException("Başlangıç verileri eklenirken hata: " + e.getMessage(), e);
        }
    }
}
