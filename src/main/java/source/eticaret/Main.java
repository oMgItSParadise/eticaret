package source.eticaret;

import javafx.application.Application;
import javafx.stage.Stage;
import source.eticaret.controller.AuthController;
import source.eticaret.service.DatabaseService;
import source.eticaret.view.ViewManager;

import java.sql.SQLException;

public class Main extends Application {
    private static DatabaseService databaseService;

    @Override
    public void start(Stage primaryStage) {
        try {
            ViewManager.setPrimaryStage(primaryStage);
            initDatabase();
            ViewManager.showLoginView(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showErrorAlert("Başlatama hatası",
                    "Uygulama başlatılırken bir hata oluştu: " + e.getMessage());
        }
    }

    private void initDatabase() throws SQLException {
        System.out.println("Veritabanı bağlantısı kuruluyor...");
        databaseService = DatabaseService.getInstance();

        System.out.println("Veritabanı bağlantısı kontrol ediliyor...");
        databaseService.createDatabase();

        System.out.println("Veritabanı bağlantısı kuruldu.");
    }

    public static DatabaseService getDatabaseService() {
        return databaseService;
    }

    @Override
    public void stop() {
        try {
            if (databaseService != null) {
                databaseService.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}