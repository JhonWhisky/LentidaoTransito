package br.com.lentidaotransito;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class RegistroEditController {

    @FXML private Label idLabel;
    @FXML private TextField idField;
    @FXML private TextField dataField;
    @FXML private TextField corredorField;
    @FXML private TextField sentidoField;
    @FXML private TextField expressaField;
    @FXML private TextField tamanhoField;
    @FXML private TextField regiaoField;
    @FXML private TextField descricaoField;

    private Stage dialogStage;
    private LentidaoRegistro registro;
    private boolean salvo = false;

    /**
     * Define o Stage (janela) deste diálogo.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Define o registro a ser editado e preenche os campos.
     */
    public void setRegistro(LentidaoRegistro registro) {
        this.registro = registro;

        if (registro.get_id() != null) {
            // Editando um registro existente
            idField.setText(String.valueOf(registro.get_id()));
        } else {
            // Criando um novo registro
            // Esconde os campos de ID
            idField.setVisible(false);
            idLabel.setVisible(false);
            idField.setManaged(false); // Não ocupa espaço
            idLabel.setManaged(false); // Não ocupa espaço
        }

        // Preenche o resto (estará vazio se for um novo registro)
        dataField.setText(registro.getData());
        corredorField.setText(registro.getCorredor());
        sentidoField.setText(registro.getSentido());
        expressaField.setText(registro.getExpressa());
        tamanhoField.setText(String.valueOf(registro.getTamanho()));
        regiaoField.setText(registro.getNome_regiao());
        descricaoField.setText(registro.getDescricao());
    }

    /**
     * Retorna true se o usuário clicou em Salvar.
     */
    public boolean isSalvo() {
        return salvo;
    }

    /**
     * Chamado quando o usuário clica em Salvar.
     * Valida os dados e os atualiza no objeto registro.
     */
    @FXML
    private void handleSalvar() {
        if (isInputValid()) {
            // Atualiza o objeto registro com os novos dados
            registro.setData(dataField.getText());
            registro.setCorredor(corredorField.getText());
            registro.setSentido(sentidoField.getText());
            registro.setExpressa(expressaField.getText());
            registro.setTamanho(Integer.parseInt(tamanhoField.getText()));
            registro.setNome_regiao(regiaoField.getText());
            registro.setDescricao(descricaoField.getText());

            salvo = true;
            dialogStage.close();
        }
    }

    /**
     * Chamado quando o usuário clica em Cancelar.
     */
    @FXML
    private void handleCancelar() {
        dialogStage.close();
    }

    /**
     * Validação simples dos campos.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (corredorField.getText() == null || corredorField.getText().isEmpty()) {
            errorMessage += "Corredor inválido!\n";
        }
        try {
            Integer.parseInt(tamanhoField.getText());
        } catch (NumberFormatException e) {
            errorMessage += "Tamanho inválido (deve ser um número)!\n";
        }
        // ... (adicionar mais validações se necessário) ...

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            // Mostra o alerta de erro
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Campos Inválidos");
            alert.setHeaderText("Por favor, corrija os campos inválidos.");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}