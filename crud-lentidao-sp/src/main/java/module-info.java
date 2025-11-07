module br.com.lentidaotransito {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    
    // Adicione estas duas linhas:
    requires java.sql; // Para o DatabaseManager (JDBC)
    requires com.google.gson; // Para o ApiImporter (JSON)
    requires java.net.http; // Para o ApiImporter (HTTP)

    requires java.desktop; // Para a classe Desktop

    opens br.com.lentidaotransito to javafx.fxml, com.google.gson;
    exports br.com.lentidaotransito;
}