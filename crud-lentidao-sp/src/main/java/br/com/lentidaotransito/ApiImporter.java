package br.com.lentidaotransito;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

// Interface para reportar o progresso para a UI
interface ImportProgressListener {
    void onProgressUpdate(double progress);
    void onMessageUpdate(String message);
}

public class ApiImporter {

    // Extraído do seu JSON de exemplo
    private static final String RESOURCE_ID = "0791e764-d98c-4c22-8dcb-d8c6b050df9d";
    private static final String API_BASE_URL = "https://dados.prefeitura.sp.gov.br/api/3/action/datastore_search";

    // !!! COLOQUE SUA CHAVE DE ACESSO AQUI !!!
    // A API da prefeitura geralmente pede a chave no Header
    private static final String API_KEY = "pHHFNoUkv1Ng0jIIwmRrFx9ZVvYa";

    private HttpClient client = HttpClient.newHttpClient();
    private Gson gson = new Gson();
    private DatabaseManager dbManager = new DatabaseManager();

    // Aceita um 'listener' para atualizar a UI (ProgressBar)
    public void importarDados(ImportProgressListener listener) throws Exception {
        dbManager.criarTabela(); // Garante que a tabela exista

        int limite = 100; // Limite padrão da API
        int offset = 0;
        int total;

        do {
            // 1. Montar a URL de requisição
            // (Não usamos 'ano=2018' pois o resource_id já parece ser o dataset correto)
            String url = String.format("%s?resource_id=%s&limit=%d&offset=%d",
                                       API_BASE_URL, RESOURCE_ID, limite, offset);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", API_KEY) // A forma de autenticação pode variar!
                    .header("Accept", "application/json")
                    .build();

            // 2. Fazer a Requisição
            listener.onMessageUpdate("Buscando registros... Offset: " + offset);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Erro na API: " + response.body());
            }

            // 3. Parsear o JSON
            ApiResponse apiResponse;
            try {
                apiResponse = gson.fromJson(response.body(), ApiResponse.class);
            } catch (JsonSyntaxException e) {
                throw new RuntimeException("Erro ao ler o JSON da API: \"Excesso de dados ou formato inesperado?", e);
            }

            if (apiResponse == null || apiResponse.result == null) {
                 throw new RuntimeException("Resposta JSON inválida da API.");
            }

            ApiResult result = apiResponse.result;
            total = result.total; // Pega o total de registros (ex: 161250)

            // 4. Salvar no Banco
            List<LentidaoRegistro> registros = result.records;
            if (registros != null && !registros.isEmpty()) {
                dbManager.batchInsert(registros);
                
                offset += registros.size();
                
                // 5. Atualizar UI
                listener.onMessageUpdate("Importado " + offset + " de " + total + " registros.");
                listener.onProgressUpdate((double) offset / total);
                
            } else {
                break; // Sai do loop se não vier mais registros
            }

        } while (offset < total);

        listener.onMessageUpdate("Importação de 2018 concluída! Total: " + total + " registros.");
        listener.onProgressUpdate(1.0);
    }
}