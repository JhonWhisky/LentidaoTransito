package br.com.lentidaotransito;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class PrimaryController {

    @FXML private Button syncButton;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;

    private ApiImporter importer = new ApiImporter();

    @FXML
    private void handleSincronizar() {
        // Desativa o botão para não clicar duas vezes
        syncButton.setDisable(true); 
        
        // Cria uma Task (tarefa em background)
        // Isso é ESSENCIAL para não travar a UI
        Task<Void> importTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Passa 'this' (a Task) como o listener
                importer.importarDados(new ImportProgressListener() {
                    @Override
                    public void onProgressUpdate(double progress) {
                        updateProgress(progress, 1.0);
                    }
                    
                    @Override
                    public void onMessageUpdate(String message) {
                        updateMessage(message);
                    }
                });
                return null;
            }
        };

        // Liga a barra de progresso e o label de status ao progresso da Task
        progressBar.progressProperty().bind(importTask.progressProperty());
        statusLabel.textProperty().bind(importTask.messageProperty());

        // O que fazer quando a Task terminar (com sucesso ou erro)
        importTask.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind(); // Desliga o bind
            statusLabel.setText("Sincronização concluída!");
            syncButton.setDisable(false);
        });

        importTask.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Erro na importação: " + importTask.getException().getMessage());
            syncButton.setDisable(false);
            importTask.getException().printStackTrace(); // Mostra o erro no console
        });

        // Inicia a Task em uma nova Thread
        new Thread(importTask).start();
    }
}