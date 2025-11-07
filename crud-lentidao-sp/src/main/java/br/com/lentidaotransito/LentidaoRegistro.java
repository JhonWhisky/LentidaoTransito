package br.com.lentidaotransito; // Mude se seu pacote for outro

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Esta classe representa um único registro de lentidão
public class LentidaoRegistro {

    // O Gson vai usar esses nomes para ler o JSON
    private Integer _id;
    private String data;
    private String corredor;
    private String sentido;
    private String expressa;
    private String descricao;
    private int tamanho;
    private String nome_regiao;

    // --- Getters e Setters ---
    // (Você pode gerar na sua IDE com Alt+Insert -> Getters and Setters)

    // Método auxiliar para converter a data
    public LocalDateTime getDataTimestamp() {
        if (this.data == null) return null;
        try {
            return LocalDateTime.parse(this.data, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            System.err.println("Erro ao parsear data: " + this.data);
            return null;
        }
    }

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    // Usamos .trim() para limpar os espaços em branco que vêm da API
    public String getCorredor() {
        return (corredor != null) ? corredor.trim() : null;
    }

    public void setCorredor(String corredor) {
        this.corredor = corredor;
    }

    public String getSentido() {
        return (sentido != null) ? sentido.trim() : null;
    }

    public void setSentido(String sentido) {
        this.sentido = sentido;
    }

    public String getExpressa() {
        return expressa;
    }

    public void setExpressa(String expressa) {
        this.expressa = expressa;
    }

    public String getDescricao() {
        return (descricao != null) ? descricao.trim() : null;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
    }

    public String getNome_regiao() {
        return nome_regiao;
    }

    public void setNome_regiao(String nome_regiao) {
        this.nome_regiao = nome_regiao;
    }
}