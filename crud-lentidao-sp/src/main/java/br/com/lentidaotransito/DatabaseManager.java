package br.com.lentidaotransito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseManager {

    // Define o nome do arquivo do banco. Ele será criado na raiz do projeto.
    private static final String DB_URL = "jdbc:sqlite:lentidao_2018.db";

    /**
     * Conecta ao banco de dados SQLite.
     * O banco será criado se não existir.
     */
    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Cria a tabela 'lentidao' no banco se ela ainda não existir.
     */
    public void criarTabela() {
        // SQL para criar a tabela, baseado nos campos do LentidaoRegistro
        String sql = "CREATE TABLE IF NOT EXISTS lentidao ("
                + " _id INTEGER PRIMARY KEY,"      // _id da API
                + " data TEXT NOT NULL,"
                + " corredor TEXT,"
                + " sentido TEXT,"
                + " expressa TEXT,"
                + " descricao TEXT,"
                + " tamanho INTEGER,"
                + " nome_regiao TEXT"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            // Executa o SQL
            stmt.execute(sql);
            System.out.println("Tabela 'lentidao' verificada/criada com sucesso.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void batchInsert(List<LentidaoRegistro> registros) {
        String sql = "INSERT OR IGNORE INTO lentidao(_id, data, corredor, sentido, expressa, descricao, tamanho, nome_regiao) "
                   + "VALUES(?,?,?,?,?,?,?,?)";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Desliga o auto-commit para performance máxima
            conn.setAutoCommit(false); 

            for (LentidaoRegistro reg : registros) {
                pstmt.setInt(1, reg.get_id());
                pstmt.setString(2, reg.getData());
                pstmt.setString(3, reg.getCorredor());
                pstmt.setString(4, reg.getSentido());
                pstmt.setString(5, reg.getExpressa());
                pstmt.setString(6, reg.getDescricao());
                pstmt.setInt(7, reg.getTamanho());
                pstmt.setString(8, reg.getNome_regiao()); // CUIDADO: Seu POJO tem 'nome_regiao'
                pstmt.addBatch();
            }
                
            pstmt.executeBatch(); // Executa todos os comandos de uma vez
            conn.commit(); // Salva as mudanças no banco

        } catch (SQLException e) {
            System.err.println("Erro durante o batch insert: " + e.getMessage());
        }
    }
    // --- AQUI ENTRARÃO OS OUTROS MÉTODOS ---
    // (batchInsert, buscarRegistros, update, delete)
    // Vamos adicionar o batchInsert na próxima fase.
}