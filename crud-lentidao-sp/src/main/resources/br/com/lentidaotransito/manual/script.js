// Aguarda o carregamento completo do HTML
document.addEventListener('DOMContentLoaded', () => {

    // Seleciona os botões de idioma
    const btnPt = document.getElementById('btn-pt');
    const btnEn = document.getElementById('btn-en');
    const btnEs = document.getElementById('btn-es');

    // Adiciona os 'escutadores' de clique
    btnPt.addEventListener('click', () => loadLanguage('pt'));
    btnEn.addEventListener('click', () => loadLanguage('en'));
    btnEs.addEventListener('click', () => loadLanguage('es'));

    /**
     * Função principal para carregar e aplicar as traduções
     * @param {string} lang - O código do idioma (ex: 'pt', 'en')
     */
    async function loadLanguage(lang) {
        try {
            // Busca o arquivo JSON correspondente
            const response = await fetch(`lang/${lang}.json`);
            if (!response.ok) {
                throw new Error(`Não foi possível carregar o arquivo ${lang}.json`);
            }
            const translations = await response.json();

            // Atualiza o atributo 'lang' da tag <html>
            document.documentElement.lang = lang;
            
            // Itera sobre todos os elementos que têm o atributo 'data-key'
            document.querySelectorAll('[data-key]').forEach(element => {
                const key = element.dataset.key;
                if (translations[key]) {
                    // Substitui o conteúdo do elemento pela tradução
                    element.innerHTML = translations[key];
                }
            });

            // Atualiza o título da página
            if (translations['manualTitle']) {
                document.title = translations['manualTitle'];
            }

        } catch (error) {
            console.error('Erro ao carregar tradução:', error);
        }
    }

    // Carrega o idioma português (pt) como padrão ao abrir a página
    loadLanguage('pt');
});