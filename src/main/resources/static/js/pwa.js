/*
 * Registra o service worker, que e o que faz o navegador oferecer "Instalar"
 * e o sistema abrir em janela propria, como um programa.
 *
 * O navegador so aceita service worker em HTTPS ou em localhost. Em producao
 * (Render) e em desenvolvimento local isso e atendido; num acesso por IP da
 * rede interna sem HTTPS, o registro falha e a aplicacao segue funcionando
 * normalmente como site — apenas sem a opcao de instalar.
 */
if ('serviceWorker' in navigator) {
    window.addEventListener('load', function () {
        navigator.serviceWorker.register('/sw.js').catch(function (erro) {
            console.info('Service worker nao registrado; o sistema funciona normalmente pelo navegador.', erro);
        });
    });
}
