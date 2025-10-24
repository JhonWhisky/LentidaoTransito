package br.com.lentidaotransito;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PrimaryController {

    // --- Componentes da UI (FXML) ---
    @FXML private Button syncButton;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;

    // --- Filtros ---
    @FXML private DatePicker filtroData;
    @FXML private ComboBox<String> filtroExpressa;
    @FXML private TextField filtroCorredor;
    @FXML private TextField filtroSentido;
    @FXML private ComboBox<String> filtroTamanhoOperador;
    @FXML private TextField filtroTamanhoValor;

    // --- Tabela ---
    @FXML private TableView<LentidaoRegistro> tableView;
    @FXML private TableColumn<LentidaoRegistro, String> colData;
    @FXML private TableColumn<LentidaoRegistro, String> colCorredor;
    @FXML private TableColumn<LentidaoRegistro, String> colSentido;
    @FXML private TableColumn<LentidaoRegistro, String> colExpressa;
    @FXML private TableColumn<LentidaoRegistro, Integer> colTamanho;
    @FXML private TableColumn<LentidaoRegistro, String> colRegiao;

    // --- Variáveis de Classe ---
    private ApiImporter importer = new ApiImporter();
    private DatabaseManager dbManager = new DatabaseManager();
    private ObservableList<LentidaoRegistro> dadosTabela = FXCollections.observableArrayList();

    /**
     * Método chamado automaticamente quando o FXML é carregado.
     */
    @FXML
    public void initialize() {
        // 1. Configurar ComboBoxes de Filtro
        filtroExpressa.setItems(FXCollections.observableArrayList("Todos", "A", "E", "Vazio (Local)"));
        filtroExpressa.setValue("Todos");
        
        filtroTamanhoOperador.setItems(FXCollections.observableArrayList("=", ">", ">=", "<", "<="));
        filtroTamanhoOperador.setValue(">");

        // 2. Configurar Colunas da Tabela
        // O valor em PropertyValueFactory<> DEVE ser o nome exato da variável 
        // ou do método get/set no LentidaoRegistro.java
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colCorredor.setCellValueFactory(new PropertyValueFactory<>("corredor"));
        colSentido.setCellValueFactory(new PropertyValueFactory<>("sentido"));
        colExpressa.setCellValueFactory(new PropertyValueFactory<>("expressa"));
        colTamanho.setCellValueFactory(new PropertyValueFactory<>("tamanho"));
        colRegiao.setCellValueFactory(new PropertyValueFactory<>("nome_regiao"));

        // 3. Ligar a lista observável à tabela
        tableView.setItems(dadosTabela);
        
        // 4. Carregar os dados iniciais
        handleBuscar();
    }

    /**
     * Chamado pelo botão "Sincronizar".
     * Inicia a importação da API em uma thread separada.
     */
    @FXML
    private void handleSincronizar() {
        syncButton.setDisable(true);
        Task<Void> importTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
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

        progressBar.progressProperty().bind(importTask.progressProperty());
        statusLabel.textProperty().bind(importTask.messageProperty());

        importTask.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Sincronização concluída!");
            syncButton.setDisable(false);
            handleBuscar(); // Atualiza a tabela com os novos dados
        });

        importTask.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Erro na importação: " + importTask.getException().getMessage());
            syncButton.setDisable(false);
            importTask.getException().printStackTrace();
        });

        new Thread(importTask).start();
    }

    /**
     * Chamado pelo botão "Buscar".
     * Coleta os filtros, busca no banco e atualiza a tabela.
     */
    @FXML
    private void handleBuscar() {
        // 1. Coletar todos os filtros da UI
        Map<String, Object> filtros = new HashMap<>();
        
        if (filtroData.getValue() != null) {
            filtros.put("data", filtroData.getValue()); // tipo LocalDate
        }
        if (filtroCorredor.getText() != null && !filtroCorredor.getText().isEmpty()) {
            filtros.put("corredor", filtroCorredor.getText());
        }
        if (filtroSentido.getText() != null && !filtroSentido.getText().isEmpty()) {
            filtros.put("sentido", filtroSentido.getText());
        }
        
        String expressa = filtroExpressa.getValue();
        if (expressa != null && !expressa.equals("Todos")) {
            filtros.put("expressa", expressa.equals("Vazio (Local)") ? "" : expressa);
        }

        String tamanhoValor = filtroTamanhoValor.getText();
        if (tamanhoValor != null && !tamanhoValor.isEmpty()) {
            try {
                // Valida se é um número
                Integer.parseInt(tamanhoValor); 
                filtros.put("tamanhoValor", tamanhoValor);
                filtros.put("tamanhoOperador", filtroTamanhoOperador.getValue());
            } catch (NumberFormatException e) {
                // Ignora o filtro se não for um número válido
                statusLabel.setText("Filtro 'Tamanho' deve ser um número.");
            }
        }

        // 2. Chamar o DatabaseManager
        statusLabel.setText("Buscando dados no banco local...");
        List<LentidaoRegistro> resultados = dbManager.buscarRegistros(filtros);
        
        // 3. Atualizar a Tabela
        dadosTabela.setAll(resultados);
        statusLabel.setText(resultados.size() + " registros encontrados.");
    }

    /**
     * Chamado pelo botão "Excluir Selecionado".
     * Pede confirmação e exclui o item da tabela e do banco.
     */
    @FXML
    private void handleExcluirRegistro() {
        // 1. Pega o item selecionado
        LentidaoRegistro selecionado = tableView.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            // Nada selecionado
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Nenhuma Seleção");
            alert.setHeaderText("Nenhum registro selecionado");
            alert.setContentText("Por favor, selecione um registro na tabela para excluir.");
            alert.showAndWait();
            return;
        }

        // 2. Pede confirmação
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir registro _id: " + selecionado.get_id());
        alert.setContentText("Você tem certeza que deseja excluir este registro do banco de dados local?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 3. Usuário confirmou
            try {
                // Exclui do banco
                dbManager.deleteRegistro(selecionado.get_id());
                
                // Remove da tabela (da lista observável)
                dadosTabela.remove(selecionado);
                
                statusLabel.setText("Registro " + selecionado.get_id() + " excluído.");
            } catch (Exception e) {
                statusLabel.setText("Erro ao excluir: " + e.getMessage());
            }
        } else {
            // Usuário cancelou
            statusLabel.setText("Exclusão cancelada.");
        }
    }

    /**
     * Chamado pelo botão "Editar Selecionado".
     * Abre uma janela de diálogo para editar os detalhes do registro.
     */
    @FXML
    private void handleEditarRegistro() {
        LentidaoRegistro selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Nenhuma Seleção");
            alert.setHeaderText("Nenhum registro selecionado");
            alert.setContentText("Por favor, selecione um registro na tabela para editar.");
            alert.showAndWait();
            return;
        }

        boolean foiSalvo = showRegistroEditDialog(selecionado);
        if (foiSalvo) {
            // 1. Salva no banco de dados
            dbManager.updateRegistro(selecionado);
            
            // 2. Atualiza a tabela (força um refresh)
            tableView.refresh();
            statusLabel.setText("Registro " + selecionado.get_id() + " atualizado.");
        }
    }

    /**
     * Método auxiliar para carregar e exibir a janela de edição.
     * Retorna true se o usuário salvar.
     */
    private boolean showRegistroEditDialog(LentidaoRegistro registro) {
        try {
            // Carrega o arquivo fxml
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getResource("registro-edit-dialog.fxml"));
            GridPane page = (GridPane) loader.load();

            // Cria o Stage (janela) do diálogo
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Editar Registro");
            dialogStage.initModality(Modality.WINDOW_MODAL); // Bloqueia a janela principal
            dialogStage.initOwner(syncButton.getScene().getWindow()); // Define a janela "pai"
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Passa o registro para o controller
            RegistroEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setRegistro(registro);

            // Mostra a janela e espera até o usuário fechar
            dialogStage.showAndWait();

            return controller.isSalvo();

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Erro ao abrir janela de edição.");
            return false;
        }
    }
}